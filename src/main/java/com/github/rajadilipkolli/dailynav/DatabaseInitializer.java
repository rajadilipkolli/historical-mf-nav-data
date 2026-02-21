package com.github.rajadilipkolli.dailynav;

import com.github.luben.zstd.ZstdInputStream;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Component responsible for initializing the database with fund data */
@Component
public class DatabaseInitializer {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;

  /**
   * Create a DatabaseInitializer with the provided JdbcTemplate and configuration.
   *
   * @param jdbcTemplate the JdbcTemplate configured for the Daily NAV database
   * @param properties   configuration properties that control initialization behavior (e.g., auto-init, index creation, debug)
   */
  public DatabaseInitializer(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
  }

  /**
   * Initialize the application's database according to the configured initialization settings.
   */
  @Async
  public void initializeDatabaseAsync() {
    initializeDatabase();
  }

  /**
   * Initializes the Daily NAV database according to configured properties.
   *
   * <p>If auto-initialization is disabled or the required tables already exist this method returns
   * without making changes. Otherwise it attempts to populate the database (first by restoring a
   * compressed database if available, falling back to executing the embedded SQL script) and,
   * optionally, creates indexes and logs database statistics when complete.
   *
   * @throws RuntimeException if an error occurs while restoring, loading, or initializing the database
   */
  public void initializeDatabase() {
    if (!properties.isAutoInit()) {
      logger.info("Auto-initialization disabled, skipping database setup");
      return;
    }

    try {
      logger.info("Initializing Daily NAV database...");

      // Check if tables already exist
      if (tablesExist()) {
        logger.info("Database tables already exist, skipping initialization");
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

    } catch (Exception e) {
      logger.error("Failed to initialize Daily NAV database", e);
      throw new RuntimeException("Database initialization failed", e);
    }
  }

  /**
   * Restore the SQLite database from the classpath resource "funds.db.zst" and place the decompressed DB file
   * into the configured database location when applicable.
   *
   * @return `true` if the resource was found and the database was successfully restored (and copied to the configured
   *         path when applicable), `false` otherwise.
   */
  boolean restoreDatabaseFromZst() {
    try {
      ClassPathResource resource = new ClassPathResource("funds.db.zst");
      if (!resource.exists()) {
        logger.info("funds.db.zst not found in classpath, falling back to SQL script");
        return false;
      }
      File tempDb = File.createTempFile("funds", ".db");
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
      try {
        String dbPath = properties.getDatabasePath();
        if (dbPath != null && !dbPath.isBlank() && !dbPath.contains(":memory:")) {
          File dest = new File(dbPath.replace("jdbc:sqlite:", ""));
          Files.copy(tempDb.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
          Files.deleteIfExists(tempDb.toPath());
          logger.info("Copied restored DB to configured path: {}", dest.getAbsolutePath());
        } else {
          // If using in-memory, you may need to adjust datasource config to use this file
          logger.warn(
              "Database is configured as in-memory. To use restored DB, set daily-nav.database-file property.");
          Files.deleteIfExists(tempDb.toPath());
        }
      } finally {
        // Clean up temp file
        Files.deleteIfExists(tempDb.toPath());
      }
      return true;
    } catch (Exception e) {
      logger.warn("Failed to restore database from funds.db.zst: {}", e.getMessage());
      return false;
    }
  }

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

      // Get date range of data
      try {
        String minDate = jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class);
        String maxDate = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
        logger.info("NAV data available from {} to {}", minDate, maxDate);
      } catch (Exception e) {
        logger.debug("Could not determine date range: {}", e.getMessage());
      }

    } catch (Exception e) {
      logger.warn("Could not retrieve database statistics: {}", e.getMessage());
    }
  }

  boolean tablesExist() {
    try {
      jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

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
   * Create the application's database indexes required for NAV and securities queries.
   *
   * Attempts to create indexes for nav(date, scheme_code), nav(scheme_code),
   * securities(scheme_code) and securities(isin). Non-fatal failures while creating
   * individual indexes are logged and do not stop the remaining index creation.
   */
  void createIndexes() {
    logger.info("Creating database indexes...");

    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"nav-main\" ON \"nav\" (\"date\",\"scheme_code\")",
        "nav-main");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"nav-scheme\" ON \"nav\" (\"scheme_code\")", "nav-scheme");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"securities-scheme\" ON \"securities\" (\"scheme_code\")",
        "securities-scheme");
    executeSilently(
        "CREATE INDEX IF NOT EXISTS \"securities-isin\" ON \"securities\" (\"isin\")",
        "securities-isin");

    logger.debug("Database index creation attempt finished");
  }

  /**
   * Executes the provided SQL statement (typically an index creation) and logs success or a non-fatal failure.
   *
   * @param sql         the SQL statement to execute (for example, a CREATE INDEX statement)
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