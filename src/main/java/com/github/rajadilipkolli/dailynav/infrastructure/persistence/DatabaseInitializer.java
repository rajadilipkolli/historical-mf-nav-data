package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.luben.zstd.ZstdInputStream;
import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.configproperties.DailyNavProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;

public class DatabaseInitializer implements DatabaseInitializerPort {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;
  private volatile boolean initialized = false;

  /**
   * Create a DatabaseInitializer with the provided JdbcTemplate and configuration.
   *
   * @param jdbcTemplate the JdbcTemplate configured for the Daily NAV database
   * @param properties configuration properties that control initialization behavior (e.g.,
   *     auto-init, index creation, debug)
   */
  public DatabaseInitializer(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
  }

  /**
   * Returns whether the database has been initialized.
   *
   * @return true if initialization is complete
   */
  public boolean isInitialized() {
    return initialized;
  }

  /** Schedules database initialization to run on the configured "dailyNavTaskExecutor". */
  @Async("dailyNavTaskExecutor")
  public void initializeDatabaseAsync() {
    initializeDatabase();
  }

  /**
   * Initializes the Daily NAV database according to the configured properties.
   *
   * <p>When auto-initialization is enabled, restores the database or loads the embedded SQL script,
   * then optionally creates indexes and records database statistics. When disabled, updates the
   * initialization status based on whether the required tables exist.
   *
   * @throws RuntimeException if database initialization fails
   */
  public void initializeDatabase() {
    if (!properties.isAutoInit()) {
      logger.info("Auto-initialization disabled, skipping database setup");
      // If auto-init is disabled, mark initialized only if the expected tables exist.
      // This ensures callers (e.g. MutualFundService.isReady()) reflect actual DB readiness.
      this.initialized = tablesExist();
      return;
    }

    try {
      logger.info("Initializing Daily NAV database...");

      // Check if tables already exist
      if (tablesExist()) {
        logger.info("Database tables already exist, skipping initialization");
        this.initialized = true;
        return;
      }

      // Try to restore from compressed SQLite DB first
      if (!restoreDatabaseFromZst()) {
        // Fallback to SQL script if .db.zst not present
        loadSqlScript();
      }

      // Create indexes if enabled
      if (properties.isCreateIndexes()) {
        createIndexes();
      }

      logger.info("Daily NAV database initialized successfully");

      // Log database statistics
      logDatabaseStats();

      this.initialized = true;

    } catch (Exception e) {
      logger.error("Failed to initialize Daily NAV database", e);
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  /**
   * Restores the SQLite database from the compressed classpath resource {@code funds.db.zst}.
   *
   * @return {@code true} if restoration succeeds, {@code false} if the resource is unavailable or
   *     restoration fails
   */
  boolean restoreDatabaseFromZst() {
    boolean databaseRestored = false;
    File tempDb = null;
    try {
      ClassPathResource resource = new ClassPathResource("funds.db.zst");
      if (!resource.exists()) {
        logger.info("funds.db.zst not found in classpath, falling back to SQL script");
        return false;
      }
      tempDb = File.createTempFile("funds", ".db");
      try (InputStream zstdStream = new ZstdInputStream(resource.getInputStream());
          OutputStream out = new FileOutputStream(tempDb)) {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = zstdStream.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
      }
      logger.info("Restored database from funds.db.zst to {}", tempDb.getAbsolutePath());
      // If using a file-based DB, copy to the configured location
      String dbPath = properties.getDatabasePath();
      if (dbPath == null || dbPath.isBlank()) {
        logger.info("Database path is not configured, skipping restoration");
      } else if (!dbPath.contains(":memory:")) {
        File dest = new File(dbPath.replace("jdbc:sqlite:", ""));
        Files.copy(tempDb.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        logger.info("Copied restored DB to configured path: {}", dest.getAbsolutePath());
        databaseRestored = true;
      } else {
        // sqlite-jdbc extension: "restore from [filename]"
        // This copies the entire database from the file into the current connection
        File finalTempDb = tempDb;
        jdbcTemplate.execute(
            (ConnectionCallback<Void>)
                con -> {
                  try (var st = con.createStatement()) {
                    st.executeUpdate(
                        "restore from '" + finalTempDb.getAbsolutePath().replace("'", "''") + "'");
                  }
                  return null;
                });

        logger.info("Loaded restored database into in-memory database");
        databaseRestored = true;
      }
    } catch (Exception e) {
      logger.warn("Failed to restore database from funds.db.zst: {}", e.getMessage());
    } finally {
      if (tempDb != null && tempDb.exists()) {
        if (tempDb.delete()) {
          logger.debug("Deleted temporary database file: {}", tempDb.getAbsolutePath());
        } else {
          logger.warn("Could not delete temporary database file: {}", tempDb.getAbsolutePath());
        }
      }
    }
    return databaseRestored;
  }

  /**
   * Log basic statistics about the loaded database.
   *
   * <p>Queries and logs the number of schemes, NAV records, and securities. Then attempts to query
   * and log the minimum and maximum NAV dates. If the counts cannot be retrieved a warning is
   * logged; if the date range cannot be determined a debug message is logged.
   */
  void logDatabaseStats() {
    try {
      Integer schemeCount =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      Integer navCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class);
      Integer securityCount =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class);

      logger.info(
          "Database loaded with {} schemes, {} NAV records, and {} securities",
          schemeCount,
          navCount,
          securityCount);

      // Get date range of data (single consolidated query)
      try {
        var row =
            jdbcTemplate.queryForMap(
                "SELECT MIN(date) AS min_date, MAX(date) AS max_date FROM nav");
        String minDate = (row.get("min_date") != null) ? row.get("min_date").toString() : null;
        String maxDate = (row.get("max_date") != null) ? row.get("max_date").toString() : null;
        logger.info("NAV data available from {} to {}", minDate, maxDate);
      } catch (Exception e) {
        logger.debug("Could not determine date range: {}", e.getMessage());
      }

    } catch (Exception e) {
      logger.warn("Could not retrieve database statistics: {}", e.getMessage());
    }
  }

  /**
   * Determines whether the database tables have been initialized.
   *
   * @return {@code true} if the {@code schemes} table can be queried, {@code false} otherwise
   */
  boolean tablesExist() {
    try {
      jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      return true;
    } catch (BadSqlGrammarException e) {
      logger.info("Tables do not exist yet, initialization required.");
      return false;
    } catch (Exception e) {
      logger.error("Could not determine tables existence: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Loads and executes SQL statements from the embedded {@code funds.sql} script.
   *
   * @throws IOException if the SQL script is not available or cannot be read
   */
  void loadSqlScript() throws IOException {
    logger.info("Loading SQL data from embedded script (funds.sql)...");

    ClassPathResource resource = new ClassPathResource("funds.sql");
    if (!resource.exists()) {
      throw new IOException("SQL script 'funds.sql' not found in classpath");
    }

    try (InputStream sqlStream = resource.getInputStream();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(sqlStream, StandardCharsets.UTF_8))) {
      StringBuilder currentStatement = new StringBuilder();
      String line;
      int lineCount = 0;
      int executedStatements = 0;
      while ((line = reader.readLine()) != null) {
        lineCount++;
        // Skip comments and empty lines
        if (line.trim().startsWith("--") || line.trim().isEmpty()) {
          continue;
        }
        currentStatement.append(line).append("\n");
        // Check if this line ends a statement
        if (line.trim().endsWith(";")) {
          String statement = currentStatement.toString().trim();
          if (!statement.isEmpty()) {
            try {
              jdbcTemplate.execute(statement);
              executedStatements++;
              if (executedStatements % 1000 == 0) {
                logger.info("Executed {} statements...", executedStatements);
              }
            } catch (Exception e) {
              if (properties.isDebug()) {
                logger.warn(
                    "Failed to execute statement at line {}: {}", lineCount, e.getMessage());
              }
              // Continue with other statements
            }
          }
          currentStatement.setLength(0); // Reset for next statement
        }
      }
      // Handle any remaining statement without semicolon
      String finalStatement = currentStatement.toString().trim();
      if (!finalStatement.isEmpty()) {
        try {
          jdbcTemplate.execute(finalStatement);
          executedStatements++;
        } catch (Exception e) {
          if (properties.isDebug()) {
            logger.warn("Failed to execute final statement: {}", e.getMessage());
          }
        }
      }
      logger.info(
          "SQL data loaded successfully. Executed {} statements from {} lines",
          executedStatements,
          lineCount);
    }
  }

  /**
   * Creates indexes supporting NAV and securities queries.
   *
   * <p>Attempts to create indexes for NAV date and scheme lookups, securities scheme-code lookups,
   * and ISIN lookups. Individual failures are logged and do not prevent subsequent index creation
   * attempts.
   */
  void createIndexes() {
    logger.info("Creating database indexes...");

    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"nav-main\" ON \"nav\" (\"date\",\"scheme_code\")",
        "nav-main");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"nav-scheme-date\" ON \"nav\" (\"scheme_code\", \"date\" DESC)",
        "nav-scheme");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"securities-scheme\" ON \"securities\" (\"scheme_code\")",
        "securities-scheme");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"securities-isin\" ON \"securities\" (\"isin\")",
        "securities-isin");

    logger.debug("Database index creation attempt finished");
  }

  /**
   * Executes the provided SQL statement (typically an index creation) and logs success or a
   * non-fatal failure.
   *
   * @param sql the SQL statement to execute (for example, a CREATE INDEX statement)
   * @param description a short, human-readable description of the index used in log messages
   */
  private void executeSilently(String sql, String description) {
    try {
      jdbcTemplate.execute(sql);
      logger.info("Successfully created index: {}", description);
    } catch (Exception e) {
      logger.warn("Failed to create index: {} - {}. Continuing...", description, e.getMessage());
      logger.debug("Index creation failure detail", e);
    }
  }
}
