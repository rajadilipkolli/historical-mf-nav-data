package com.github.rajadilipkolli.dailynav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

/** Basic tests for the Daily NAV library components */
class DailyNavBasicTest {

  @Test
  void propertiesCanBeCreated() {
    DailyNavProperties properties = new DailyNavProperties();

    // Test default values
    assertTrue(properties.isAutoInit());
    assertTrue(properties.isCreateIndexes());
    assertTrue(properties.isValidateData());
    assertFalse(properties.isDebug());
    assertEquals("jdbc:sqlite:file::memory:?cache=shared", properties.getDatabasePath());
  }

  @Test
  void propertiesCanBeConfigured() {
    DailyNavProperties properties = new DailyNavProperties();

    properties.setAutoInit(false);
    properties.setCreateIndexes(false);
    properties.setValidateData(false);
    properties.setDebug(true);
    properties.setDatabaseFile("/tmp/test.db");

    assertFalse(properties.isAutoInit());
    assertFalse(properties.isCreateIndexes());
    assertFalse(properties.isValidateData());
    assertTrue(properties.isDebug());
    assertEquals("jdbc:sqlite:/tmp/test.db", properties.getDatabasePath());
  }

  @Test
  void databasePathCanBeCustomized() {
    DailyNavProperties properties = new DailyNavProperties();

    // Test custom database file
    properties.setDatabaseFile("/path/to/custom.db");
    assertEquals("jdbc:sqlite:/path/to/custom.db", properties.getDatabasePath());

    // Test direct database path override
    properties.setDatabasePath("jdbc:sqlite:/direct/path.db");
    properties.setDatabaseFile(null);
    assertEquals("jdbc:sqlite:/direct/path.db", properties.getDatabasePath());
  }
  @Test
  void sharedMemoryConnectionPersistsData() throws Exception {
    DailyNavProperties properties = new DailyNavProperties();
    try (HikariDataSource ds = new HikariDataSource()) {
      ds.setJdbcUrl(properties.getDatabasePath());
      ds.setMaximumPoolSize(2);
      ds.setMinimumIdle(1); // Ensure at least one connection is kept alive

      try (Connection conn1 = ds.getConnection()) {
        try (Statement stmt1 = conn1.createStatement()) {
          stmt1.execute("CREATE TABLE test_shared (id INTEGER, val TEXT)");
          stmt1.execute("INSERT INTO test_shared VALUES (1, 'success')");
        }
      } // conn1 returned to pool

      try (Connection conn2 = ds.getConnection()) {
        try (Statement stmt2 = conn2.createStatement();
            ResultSet rs = stmt2.executeQuery("SELECT val FROM test_shared WHERE id = 1")) {
          assertTrue(rs.next());
          assertEquals("success", rs.getString("val"));
        }
      }
    }
  }
}
