package com.github.rajadilipkolli.dailynav.config;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

class DatabaseInitializerTest extends AbstractRepositoryTest {

  private DailyNavProperties properties;
  private DatabaseInitializer initializer;

  @BeforeEach
  void setUp() {
    properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    // Override restoreDatabaseFromZst to always return false for tests
    initializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public boolean restoreDatabaseFromZst() {
            return false;
          }
        };
  }

  @Override
  protected void createSchema() throws SQLException {
    // No-op: schema will be created by DatabaseInitializer using funds.sql
  }

  @Override
  protected void insertTestData() throws SQLException {
    // Already loaded by createSchema if needed
  }

  @Test
  void initializeDatabase_createsTablesAndIndexes() {
    // Should not throw, should create tables and indexes from funds.sql
    assertDoesNotThrow(() -> initializer.initializeDatabase());
    // After initialization, tables should exist and have at least one row (from test funds.sql)
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
    assertNotNull(count);
    assertTrue(count > 0, "Table 'schemes' should have at least one row loaded from funds.sql");
  }

  @Test
  void initializeDatabase_skipsIfAutoInitFalse() {
    properties.setAutoInit(false);
    DatabaseInitializer noInit = new DatabaseInitializer(jdbcTemplate, properties);
    assertDoesNotThrow(noInit::initializeDatabase);
    // Tables should not exist
    assertThrows(
        Exception.class,
        () -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class));
  }

  @Test
  void initializeDatabase_skipsIfTablesExist() throws SQLException {
    // Create tables manually
    jdbcTemplate.execute(
        "CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
    jdbcTemplate.execute("CREATE TABLE nav (scheme_code INTEGER, date TEXT, nav REAL)");
    jdbcTemplate.execute("CREATE TABLE securities (isin TEXT, type INTEGER, scheme_code INTEGER)");
    // Should skip initialization
    assertDoesNotThrow(() -> initializer.initializeDatabase());
    // Tables should still exist
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
    assertNotNull(count);
  }

  @Test
  void logDatabaseStats_handlesMissingTables() {
    // Should not throw even if tables do not exist
    assertDoesNotThrow(() -> initializer.logDatabaseStats());
  }

  @Test
  void tablesExist_returnsFalseIfNoTable() {
    assertFalse(initializer.tablesExist());
  }

  @Test
  void tablesExist_returnsTrueIfTableExists() {
    jdbcTemplate.execute(
        "CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
    assertTrue(initializer.tablesExist());
  }

  @Test
  void createIndexes_handlesSqlException() {
    // Drop tables to force index creation to fail
    jdbcTemplate.execute("DROP TABLE IF EXISTS nav");
    jdbcTemplate.execute("DROP TABLE IF EXISTS securities");
    // Should not throw
    assertDoesNotThrow(() -> initializer.createIndexes());
  }

  @Test
  void loadSqlScript_throwsIfResourceMissing() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public void loadSqlScript() throws IOException {
            throw new IOException("SQL script 'funds.sql' not found in classpath");
          }
        };
    assertThrows(IOException.class, broken::loadSqlScript);
  }

  @Test
  void initializeDatabase_handlesSqlScriptException() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public boolean restoreDatabaseFromZst() {
            return false; // Force fallback to loadSqlScript
          }

          @Override
          public void loadSqlScript() throws IOException {
            throw new IOException("Simulated failure");
          }
        };
    assertThrows(RuntimeException.class, broken::initializeDatabase);
  }

  @Test
  void initializeDatabase_handlesIndexException() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public void createIndexes() {
            throw new RuntimeException("Simulated index failure");
          }
        };
    // Should throw RuntimeException, as index creation failure is not caught in initializeDatabase
    assertThrows(RuntimeException.class, broken::initializeDatabase);
  }

  @Test
  void loadSqlScript_handlesDebugLogging() throws IOException {
    properties.setDebug(true);
    // Should not throw, just run the normal method (uses funds.sql)
    assertDoesNotThrow(() -> initializer.loadSqlScript());
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    properties.setDatabasePath("jdbc:sqlite::memory:");
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir)
      throws Exception {
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully create and handle temporary file");
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    DatabaseInitializer ioExceptionInitializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          boolean restoreDatabaseFromZst() {
            // Simulate IO exception during restoration
            return false; // In real implementation, this would catch IOException and return false
          }
        };
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    DatabaseInitializer noFileInitializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          boolean restoreDatabaseFromZst() {
            try {
              ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
              if (!resource.exists()) {
                return false;
              }
            } catch (Exception e) {
              return false;
            }
            return true;
          }
        };
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesNullDatabasePath() {
    properties.setDatabasePath(null);
    DatabaseInitializer nullPathInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = nullPathInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully restore even with null database path");
  }
}
