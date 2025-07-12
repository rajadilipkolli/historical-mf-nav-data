package com.github.rajadilipkolli.dailynav.config;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Component responsible for initializing the database with fund data */
@Component
public class DatabaseInitializer {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;

  public DatabaseInitializer(JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
  }

  @PostConstruct
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

      // Load and execute SQL script
      loadSqlScript();

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

  private void logDatabaseStats() {
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

  private boolean tablesExist() {
    try {
      jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void loadSqlScript() throws IOException {
    logger.info("Loading SQL data from embedded script...");

    ClassPathResource resource = new ClassPathResource("funds.sql");
    if (!resource.exists()) {
      throw new RuntimeException(
          "funds.sql not found in classpath. Please ensure the data file is included in the JAR.");
    }

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

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

              // Log progress for large datasets
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

  private void createIndexes() {
    logger.info("Creating database indexes...");

    try {
      // Main Index to get NAV by date and scheme_code
      jdbcTemplate.execute(
          "CREATE INDEX IF NOT EXISTS \"nav-main\" ON \"nav\" (\"date\",\"scheme_code\")");

      // Index by scheme code separately to get NAV for all dates
      jdbcTemplate.execute(
          "CREATE INDEX IF NOT EXISTS \"nav-scheme\" ON \"nav\" (\"scheme_code\")");

      // Index all securities by scheme_code for joins with NAV table
      jdbcTemplate.execute(
          "CREATE INDEX IF NOT EXISTS \"securities-scheme\" ON \"securities\" (\"scheme_code\")");

      // Index all securities by isin for metadata information
      jdbcTemplate.execute(
          "CREATE INDEX IF NOT EXISTS \"securities-isin\" ON \"securities\" (\"isin\")");

      logger.info("Database indexes created successfully");

    } catch (Exception e) {
      logger.warn("Failed to create some indexes", e);
    }
  }
}
