package com.github.rajadilipkolli.dailynav;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for checking the health status of Daily NAV library Can be used programmatically in
 * applications without web dependencies
 */
@Service
public class DailyNavHealthService {

  private static final Logger logger = LoggerFactory.getLogger(DailyNavHealthService.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;
  private final Clock clock;

  /**
   * Creates a DailyNavHealthService configured with the system default clock.
   */
  @Autowired
  public DailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this(jdbcTemplate, properties, Clock.systemDefaultZone());
  }

  /**
   * Constructs a DailyNavHealthService with the provided JDBC template, properties, and clock.
   *
   * @param clock the Clock to use for time-based checks; if null, the system default clock will be used
   */
  DailyNavHealthService(JdbcTemplate jdbcTemplate, DailyNavProperties properties, Clock clock) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.clock = clock == null ? Clock.systemDefaultZone() : clock;
  }

  /**
   * Performs a health check of the Daily NAV library.
   *
   * The returned status includes database connectivity, table counts (schemes, NAV records, securities),
   * data date range (start and end), latest data date, staleness flag, detected issues, overall health,
   * and configuration flags (autoInit, indexes, databasePath).
   *
   * @return DailyNavHealthStatus populated with the health information described above.
   */
  public DailyNavHealthStatus checkHealth() {
    DailyNavHealthStatus status = new DailyNavHealthStatus();

    try {
      // Check database connectivity
      boolean isDatabaseAccessible = checkDatabaseConnectivity();
      status.setDatabaseAccessible(isDatabaseAccessible);

      if (!isDatabaseAccessible) {
        status.setHealthy(false);
        status.addIssue("Database is not accessible");
        return status;
      }

      // Get table counts
      Map<String, Integer> tableCounts = getTableCounts();
      status.setSchemeCount(tableCounts.get("schemes"));
      status.setNavRecordCount(tableCounts.get("navRecords"));
      status.setSecurityCount(tableCounts.get("securities"));

      // Get date range and derive latest date from it (avoid duplicate MAX(date) calls)
      Map<String, LocalDate> dateRange = getDataDateRange();
      status.setDataStartDate(dateRange.get("startDate"));
      status.setDataEndDate(dateRange.get("endDate"));

      LocalDate latestDate = dateRange.get("endDate");
      status.setLatestDataDate(latestDate);

      boolean isStale = isDataStale(latestDate);
      status.setDataStale(isStale);

      if (isStale) {
        status.addIssue("Data appears to be stale (older than 10 days)");
      }

      // Check for sufficient data
      if (status.getSchemeCount() != null
          && status.getSchemeCount() >= 0
          && status.getSchemeCount() < 100) {
        status.addIssue("Insufficient scheme data (less than 100 schemes)");
      }

      if (status.getNavRecordCount() != null
          && status.getNavRecordCount() >= 0
          && status.getNavRecordCount() < 1000) {
        status.addIssue("Insufficient NAV data (less than 1000 records)");
      }

      // Set overall health status based on connectivity, data presence and freshness
      boolean overallHealthy = isOverallHealthy(status);
      status.setHealthy(overallHealthy);

      // Add configuration info
      status.setAutoInitEnabled(properties.isAutoInit());
      status.setIndexesEnabled(properties.isCreateIndexes());
      status.setDatabasePath(properties.getDatabasePath());

    } catch (Exception e) {
      logger.error("Health check failed", e);
      status.setHealthy(false);
      status.addIssue("Health check failed: " + e.getMessage());
    }

    return status;
  }

  private static boolean isOverallHealthy(DailyNavHealthStatus status) {
    boolean overallHealthy = status.isDatabaseAccessible();
    Integer schemeCount = status.getSchemeCount();
    Integer navCount = status.getNavRecordCount();

    // require non-null counts meeting minimum thresholds
    if (schemeCount == null || schemeCount < 100) {
      overallHealthy = false;
    }
    if (navCount == null || navCount < 1000) {
      overallHealthy = false;
    }
    if (status.isDataStale()) {
      overallHealthy = false;
    }
    return overallHealthy;
  }

  /** Simple health check that returns true if the database is accessible and has data */
  boolean isHealthy() {
    try {
      return checkDatabaseConnectivity() && hasMinimalData();
    } catch (Exception e) {
      logger.debug("Simple health check failed", e);
      return false;
    }
  }

  /**
   * Collects basic table counts and data date-range statistics for the Daily NAV dataset.
   *
   * @return a map of statistic names to values:
   *         - "schemes": Integer count of schemes
   *         - "navRecords": Integer count of NAV records
   *         - "securities": Integer count of securities
   *         - "startDate": LocalDate start of available data or null
   *         - "endDate": LocalDate end of available data or null
   *         - "latestDataDate": LocalDate most recent data date (same as endDate) or null
   *         - "dataStale": Boolean indicating whether the latest data is considered stale
   *         - "dataSpanDays": Long number of days between startDate and endDate (present when both dates exist)
   *         - "error": String error message when statistics could not be retrieved
   */
  Map<String, Object> getStatistics() {
    Map<String, Object> stats = new LinkedHashMap<>();

    try {
      Map<String, Integer> tableCounts = getTableCounts();
      stats.putAll(tableCounts);

      Map<String, LocalDate> dateRange = getDataDateRange();
      stats.putAll(dateRange);

      LocalDate latestDate = dateRange.get("endDate");
      stats.put("latestDataDate", latestDate);
      stats.put("dataStale", isDataStale(latestDate));

      // Calculate data span
      if (dateRange.get("startDate") != null && dateRange.get("endDate") != null) {
        try {
          LocalDate start = dateRange.get("startDate");
          LocalDate end = dateRange.get("endDate");
          long daysBetween = ChronoUnit.DAYS.between(start, end);
          stats.put("dataSpanDays", daysBetween);
        } catch (Exception e) {
          logger.debug("Failed to calculate data span", e);
        }
      }

    } catch (Exception e) {
      logger.error("Failed to get statistics", e);
      stats.put("error", "Failed to retrieve statistics: " + e.getMessage());
    }

    return stats;
  }

  private boolean checkDatabaseConnectivity() {
    try {
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      return true;
    } catch (Exception e) {
      logger.debug("Database connectivity check failed", e);
      return false;
    }
  }

  private boolean hasMinimalData() {
    try {
      Integer schemes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      Integer navRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class);
      return schemes != null && schemes > 0 && navRecords != null && navRecords > 0;
    } catch (Exception e) {
      logger.debug("Failed to check minimal data", e);
      return false;
    }
  }

  /**
   * Retrieves row counts for the primary tables used by the service.
   *
   * Counts are provided for "schemes", "navRecords", and "securities". If a table count cannot be obtained,
   * the corresponding value is -1.
   *
   * @return a map with keys "schemes", "navRecords", and "securities" mapped to their row counts or -1 when the count could not be retrieved
   */
  private Map<String, Integer> getTableCounts() {
    Map<String, Integer> counts = new LinkedHashMap<>();

    try {
      Integer schemes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
      counts.put("schemes", schemes);
    } catch (Exception e) {
      counts.put("schemes", -1);
      logger.debug("Failed to count schemes", e);
    }

    try {
      Integer navRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class);
      counts.put("navRecords", navRecords);
    } catch (Exception e) {
      counts.put("navRecords", -1);
      logger.debug("Failed to count NAV records", e);
    }

    try {
      Integer securities =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class);
      counts.put("securities", securities);
    } catch (Exception e) {
      counts.put("securities", -1);
      logger.debug("Failed to count securities", e);
    }

    return counts;
  }

  /**
   * Retrieve the date range present in the `nav` table.
   *
   * The returned map contains two entries:
   * - "startDate": the earliest `date` from the `nav` table, or `null` if unavailable.
   * - "endDate": the latest `date` from the `nav` table, or `null` if unavailable.
   *
   * @return a Map with keys "startDate" and "endDate" whose values are the corresponding LocalDate or `null` when no date is found or on error
   */
  private Map<String, LocalDate> getDataDateRange() {
    Map<String, LocalDate> dateRange = new LinkedHashMap<>();

    try {
      Map<String, Object> row =
          jdbcTemplate.queryForMap("SELECT MIN(date) AS min_date, MAX(date) AS max_date FROM nav");
      LocalDate minDate =
          row.get("min_date") != null ? LocalDate.parse(row.get("min_date").toString()) : null;
      LocalDate maxDate =
          row.get("max_date") != null ? LocalDate.parse(row.get("max_date").toString()) : null;

      dateRange.put("startDate", minDate);
      dateRange.put("endDate", maxDate);

    } catch (Exception e) {
      dateRange.put("startDate", null);
      dateRange.put("endDate", null);
      logger.debug("Failed to get data date range", e);
    }

    return dateRange;
  }

  private boolean isDataStale(LocalDate latestDataDate) {
    return latestDataDate == null || is10DaysOldData(latestDataDate);
  }

  boolean is10DaysOldData(LocalDate latestDataDate) {
    try {
      // Consider data stale if it's more than 10 days old
      long daysSinceLastUpdate = ChronoUnit.DAYS.between(latestDataDate, LocalDate.now(clock));
      return daysSinceLastUpdate > 10;
    } catch (Exception e) {
      logger.debug("Failed to parse latest data date: {}", latestDataDate, e);
      return true;
    }
  }
}