package com.github.rajadilipkolli.dailynav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

/** Test for DailyNavHealthService using real SQLite in-memory DB */
class DailyNavHealthServiceTest extends AbstractRepositoryTest {

  private static final int EXPECTED_SCHEME_COUNT = 120;
  private static final int EXPECTED_NAV_DAYS = 10;
  private static final int EXPECTED_SECURITY_COUNT = 120;
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 7, 1);

  private DailyNavHealthService healthService;

  @BeforeEach
  void setUpHealthService() {
    DailyNavProperties properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    // Inject fixed current date for deterministic tests
    Clock fixedClock =
        Clock.fixed(
            REFERENCE_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
    healthService = new DailyNavHealthService(jdbcTemplate, properties, fixedClock);
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
      for (int i = 1; i <= EXPECTED_SCHEME_COUNT; i++) {
        ps.setInt(1, i);
        ps.setString(2, "Scheme " + i);
        ps.executeUpdate();
      }
    }
    // Insert nav
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= EXPECTED_SCHEME_COUNT; i++) {
        for (int d = 0; d < EXPECTED_NAV_DAYS; d++) {
          ps.setInt(1, i);
          ps.setString(2, REFERENCE_DATE.minusDays(d).toString());
          ps.setDouble(3, 100.0 + i + d);
          ps.executeUpdate();
        }
      }
    }
    // Insert securities
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO securities (isin, type, scheme_code) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= EXPECTED_SECURITY_COUNT; i++) {
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
    assertEquals(EXPECTED_SCHEME_COUNT, status.getSchemeCount());
    assertEquals(EXPECTED_SCHEME_COUNT * EXPECTED_NAV_DAYS, status.getNavRecordCount());
    assertEquals(EXPECTED_SECURITY_COUNT, status.getSecurityCount());
    assertEquals(REFERENCE_DATE, status.getLatestDataDate());
    assertEquals(REFERENCE_DATE.minusDays(EXPECTED_NAV_DAYS - 1), status.getDataStartDate());
    assertFalse(status.isDataStale());
    assertTrue(status.getIssues().isEmpty());
  }

  @Test
  void checkHealth_shouldDetectStaleData() throws SQLException {
    // Set all nav dates to 20 days before REFERENCE_DATE
    connection.createStatement().execute("DELETE FROM nav");
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= EXPECTED_SCHEME_COUNT; i++) {
        ps.setInt(1, i);
        ps.setString(2, REFERENCE_DATE.minusDays(20).toString());
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
      ps.setString(2, REFERENCE_DATE.toString());
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
    assertEquals(EXPECTED_SCHEME_COUNT, stats.get("schemes"));
    assertEquals(EXPECTED_SCHEME_COUNT * EXPECTED_NAV_DAYS, stats.get("navRecords"));
    assertEquals(EXPECTED_SECURITY_COUNT, stats.get("securities"));
    assertEquals(REFERENCE_DATE, stats.get("latestDataDate"));
    assertEquals(REFERENCE_DATE.minusDays(EXPECTED_NAV_DAYS - 1), stats.get("startDate"));
    assertEquals(REFERENCE_DATE, stats.get("endDate"));
    assertTrue(stats.containsKey("dataSpanDays"));
    assertEquals(false, stats.get("dataStale"));
  }

  // --- Exception scenario tests for coverage ---

  @Test
  void checkHealth_shouldHandleDatabaseConnectivityException() {
    DailyNavProperties properties = new DailyNavProperties();
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    Mockito.when(mockJdbc.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("DB down"));
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    DailyNavHealthStatus status = service.checkHealth();
    assertFalse(status.isHealthy());
    assertFalse(status.isDatabaseAccessible());
    assertTrue(status.getIssues().stream().anyMatch(s -> s.contains("Database is not accessible")));
  }

  @Test
  void checkHealth_shouldHandleTableCountException() {
    DailyNavProperties properties = new DailyNavProperties();
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    // DB connectivity works, but table count fails
    Mockito.when(mockJdbc.queryForObject(Mockito.eq("SELECT 1"), Mockito.eq(Integer.class)))
        .thenReturn(1);
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM schemes"), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("fail"));
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM nav"), Mockito.eq(Integer.class)))
        .thenReturn(10);
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM securities"), Mockito.eq(Integer.class)))
        .thenReturn(5);
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT MAX(date) FROM nav"), Mockito.eq(String.class)))
        .thenReturn(REFERENCE_DATE.toString());
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT MIN(date) FROM nav"), Mockito.eq(String.class)))
        .thenReturn(REFERENCE_DATE.minusDays(1).toString());
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    DailyNavHealthStatus status = service.checkHealth();
    assertFalse(status.isHealthy());
    assertTrue(status.getSchemeCount() == -1);
  }

  @Test
  void checkHealth_shouldHandleExceptionInProperties() {
    // Simulate property getter throwing exception
    DailyNavProperties properties = Mockito.mock(DailyNavProperties.class);
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    Mockito.when(mockJdbc.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class)))
        .thenReturn(1);
    Mockito.when(mockJdbc.queryForObject(Mockito.anyString(), Mockito.eq(String.class)))
        .thenReturn(REFERENCE_DATE.toString());
    Mockito.when(properties.isAutoInit()).thenThrow(new RuntimeException("property fail"));
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    DailyNavHealthStatus status = service.checkHealth();
    assertFalse(status.isHealthy());
    assertTrue(status.getIssues().stream().anyMatch(s -> s.contains("Health check failed")));
  }

  @Test
  void isHealthy_shouldReturnFalse_onException() {
    DailyNavProperties properties = new DailyNavProperties();
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    Mockito.when(mockJdbc.queryForObject(Mockito.anyString(), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("fail"));
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    assertFalse(service.isHealthy());
  }

  @Test
  void getStatistics_shouldReturnError_onException() {
    DailyNavProperties properties = new DailyNavProperties();
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    // Mock all queries used in getTableCounts, getDataDateRange, getLatestDataDate
    // to throw
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM schemes"), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("fail"));
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM nav"), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("fail"));
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT COUNT(*) FROM securities"), Mockito.eq(Integer.class)))
        .thenThrow(new RuntimeException("fail"));
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT MIN(date) FROM nav"), Mockito.eq(String.class)))
        .thenThrow(new RuntimeException("fail"));
    Mockito.when(
            mockJdbc.queryForObject(
                Mockito.eq("SELECT MAX(date) FROM nav"), Mockito.eq(String.class)))
        .thenThrow(new RuntimeException("fail"));
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    Map<String, Object> stats = service.getStatistics();
    assertEquals(-1, stats.get("schemes"));
    assertEquals(-1, stats.get("navRecords"));
    assertEquals(-1, stats.get("securities"));
    assertNull(stats.get("startDate"));
    assertNull(stats.get("endDate"));
    assertNull(stats.get("latestDataDate"));
    // The 'error' key is only present if the outer try-catch is triggered, which is
    // not the case
    // here.
  }

  @Test
  void is10DaysOldData_shouldReturnTrue_onException() {
    DailyNavProperties properties = new DailyNavProperties();
    JdbcTemplate mockJdbc = Mockito.mock(JdbcTemplate.class);
    DailyNavHealthService service = new DailyNavHealthService(mockJdbc, properties);
    // Pass null to cause exception
    assertTrue(service.is10DaysOldData(null));
  }
}
