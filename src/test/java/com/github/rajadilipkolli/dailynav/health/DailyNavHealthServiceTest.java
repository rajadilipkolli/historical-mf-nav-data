package com.github.rajadilipkolli.dailynav.health;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.*;

/** Test for DailyNavHealthService using real SQLite in-memory DB */
class DailyNavHealthServiceTest
    extends com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest {

  private DailyNavProperties properties;
  private DailyNavHealthService healthService;

  @BeforeEach
  void setUpHealthService() {
    properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    healthService = new DailyNavHealthService(jdbcTemplate, properties);
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
    connection
        .createStatement()
        .execute("CREATE TABLE nav (scheme_code INTEGER, date TEXT, nav REAL)");
    connection
        .createStatement()
        .execute("CREATE TABLE securities (isin TEXT, type INTEGER, scheme_code INTEGER)");
  }

  @Override
  protected void insertTestData() throws SQLException {
    // Insert schemes
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO schemes (scheme_code, scheme_name) VALUES (?, ?)")) {
      for (int i = 1; i <= 120; i++) {
        ps.setInt(1, i);
        ps.setString(2, "Scheme " + i);
        ps.executeUpdate();
      }
    }
    // Insert nav
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= 120; i++) {
        for (int d = 0; d < 10; d++) {
          ps.setInt(1, i);
          ps.setString(2, LocalDate.now().minusDays(d).toString());
          ps.setDouble(3, 100.0 + i + d);
          ps.executeUpdate();
        }
      }
    }
    // Insert securities
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO securities (isin, type, scheme_code) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= 120; i++) {
        ps.setString(1, "ISIN" + i);
        ps.setInt(2, i % 2);
        ps.setInt(3, i);
        ps.executeUpdate();
      }
    }
  }

  @Test
  void isHealthy_shouldReturnTrue_whenDatabaseIsAccessibleAndHasData() {
    assertTrue(healthService.isHealthy());
  }

  @Test
  void isHealthy_shouldReturnFalse_whenNoData() throws SQLException {
    connection.createStatement().execute("DELETE FROM nav");
    assertFalse(healthService.isHealthy());
  }

  @Test
  void checkHealth_shouldReturnHealthyStatus_whenAllChecksPass() {
    DailyNavHealthStatus status = healthService.checkHealth();
    assertTrue(status.isHealthy());
    assertTrue(status.isDatabaseAccessible());
    assertEquals(120, status.getSchemeCount());
    assertEquals(1200, status.getNavRecordCount());
    assertEquals(120, status.getSecurityCount());
    assertNotNull(status.getLatestDataDate());
    assertNotNull(status.getDataStartDate());
    assertFalse(status.isDataStale());
    assertTrue(status.getIssues().isEmpty());
  }

  @Test
  void checkHealth_shouldDetectStaleData() throws SQLException {
    // Set all nav dates to 20 days ago
    connection.createStatement().execute("DELETE FROM nav");
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= 120; i++) {
        ps.setInt(1, i);
        ps.setString(2, LocalDate.now().minusDays(20).toString());
        ps.setDouble(3, 100.0 + i);
        ps.executeUpdate();
      }
    }
    DailyNavHealthStatus status = healthService.checkHealth();
    assertTrue(status.isDataStale());
    assertTrue(status.getIssues().stream().anyMatch(s -> s.contains("stale")));
  }

  @Test
  void checkHealth_shouldDetectInsufficientData() throws SQLException {
    // Remove most schemes and navs
    connection.createStatement().execute("DELETE FROM schemes");
    connection.createStatement().execute("DELETE FROM nav");
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO schemes (scheme_code, scheme_name) VALUES (?, ?)")) {
      ps.setInt(1, 1);
      ps.setString(2, "Only Scheme");
      ps.executeUpdate();
    }
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      ps.setInt(1, 1);
      ps.setString(2, LocalDate.now().toString());
      ps.setDouble(3, 100.0);
      ps.executeUpdate();
    }
    DailyNavHealthStatus status = healthService.checkHealth();
    assertFalse(status.isHealthy());
    assertTrue(status.getIssues().stream().anyMatch(s -> s.contains("Insufficient")));
  }

  @Test
  void getStatistics_shouldReturnCompleteStatistics() {
    Map<String, Object> stats = healthService.getStatistics();
    assertEquals(120, stats.get("schemes"));
    assertEquals(1200, stats.get("navRecords"));
    assertEquals(120, stats.get("securities"));
    assertNotNull(stats.get("latestDataDate"));
    assertNotNull(stats.get("startDate"));
    assertNotNull(stats.get("endDate"));
    assertTrue(stats.containsKey("dataSpanDays"));
    assertEquals(false, stats.get("dataStale"));
  }
}
