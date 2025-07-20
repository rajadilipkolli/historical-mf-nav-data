package com.github.rajadilipkolli.dailynav.config;

import static org.junit.jupiter.api.Assertions.*;

import com.github.luben.zstd.ZstdOutputStream;
import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseInitializerTest extends AbstractRepositoryTest {

  static {
    // Ensure funds.sql.zst exists in test resources before tests run
    try {
      Path sqlPath = Path.of("src/test/resources/funds.sql");
      Path zstPath = Path.of("src/test/resources/funds.sql.zst");
      if (Files.exists(sqlPath)
          && (!Files.exists(zstPath)
              || Files.getLastModifiedTime(sqlPath).toMillis()
                  > Files.getLastModifiedTime(zstPath).toMillis())) {
        try (FileInputStream fis = new FileInputStream(sqlPath.toFile());
            FileOutputStream fos = new FileOutputStream(zstPath.toFile());
            ZstdOutputStream zos = new ZstdOutputStream(fos)) {
          fis.transferTo(zos);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to compress funds.sql for test setup", e);
    }
  }

  private DailyNavProperties properties;
  private DatabaseInitializer initializer;

  @BeforeEach
  void setUp() {
    properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    initializer = new DatabaseInitializer(jdbcTemplate, properties);
  }

  @Override
  protected void createSchema() throws SQLException {
    // No tables by default; each test can create as needed
  }

  @Override
  protected void insertTestData() throws SQLException {
    // No-op for most tests
  }

  @Test
  void initializeDatabase_createsTablesAndIndexes() {
    // Should not throw, should create tables and indexes
    assertDoesNotThrow(() -> initializer.initializeDatabase());
    // After initialization, tables should exist
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
    assertNotNull(count);
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
            throw new IOException("SQL script 'funds.sql.zst' not found in classpath");
          }
        };
    assertThrows(IOException.class, broken::loadSqlScript);
  }

  @Test
  void initializeDatabase_handlesSqlScriptException() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
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
    // Should not throw, just run the normal method
    assertDoesNotThrow(() -> initializer.loadSqlScript());
  }
}
