package com.github.rajadilipkolli.dailynav.config;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseInitializerTest extends AbstractRepositoryTest {

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
}
