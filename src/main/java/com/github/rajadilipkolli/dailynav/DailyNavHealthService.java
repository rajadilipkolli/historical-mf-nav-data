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
   * Primary constructor used by Spring to create the service with the default system clock.
   *
   * @param jdbcTemplate the JdbcTemplate connected to the Daily NAV database (qualified as "dailyNavJdbcTemplate")
   * @param properties configuration properties for the Daily NAV service
   */
  @Autowired
  public DailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this(jdbcTemplate, properties, Clock.systemDefaultZone());
  }

  /**
   * Package-private constructor used for tests to allow injecting a fixed Clock; falls back to the system default clock if `clock` is null.
   *
   * @param clock the Clock to use for time-based calculations in the service; may be null to use the system default
   */
  DailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DailyNavProperties properties,
      Clock clock) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.clock = clock == null ? Clock.systemDefaultZone() : clock;
  }

  /**
   * Perform a comprehensive health check of the Daily NAV library.
   *
   * <p>Populates a status object with database connectivity, table counts, latest data date and range,
   * staleness, detected issues, overall health boolean, and relevant configuration flags/paths.
   *
   * @return a DailyNavHealthStatus containing connectivity, counts, date information, detected issues,
   *         overall health, and configuration details
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

      // Check data freshness
      LocalDate latestDate = getLatestDataDate();
      status.setLatestDataDate(latestDate);

      boolean isStale = isDataStale(latestDate);
      status.setDataStale(isStale);

      if (isStale) {
        status.addIssue("Data appears to be stale (older than 10 days)");
      }

      // Get date range
      Map<String, LocalDate> dateRange = getDataDateRange();
      status.setDataStartDate(dateRange.get("startDate"));
      status.setDataEndDate(dateRange.get("endDate"));

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

  /**
   * Evaluate whether the Daily NAV subsystem meets the minimum health criteria.
   *
   * @param status the aggregated health status containing connectivity, counts, and freshness indicators
   * @return `true` if the database is accessible, the scheme count is at least 100, the NAV record count is at least 1000, and the data is not stale; `false` otherwise
   */
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

  /**
   * Performs a quick health check of the Daily NAV data store.
   *
   * Minimal data means the database is reachable and contains at least one scheme and at least one NAV record.
   *
   * @return true if the database is reachable and contains minimal data (at least one scheme and one NAV record), false otherwise.
   */
  boolean isHealthy() {
    try {
      return checkDatabaseConnectivity() && hasMinimalData();
    } catch (Exception e) {
      logger.debug("Simple health check failed", e);
      return false;
    }
  }

  /** Gets basic statistics about the data */
  Map<String, Object> getStatistics() {
    Map<String, Object> stats = new LinkedHashMap<>();

    try {
      Map<String, Integer> tableCounts = getTableCounts();
      stats.putAll(tableCounts);

      Map<String, LocalDate> dateRange = getDataDateRange();
      stats.putAll(dateRange);

      LocalDate latestDate = getLatestDataDate();
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
   * Retrieve the latest date present in the `nav` table.
   *
   * @return the latest date from the `nav` table as a {@link java.time.LocalDate}, or `null` if no date is present or an error occurs while querying or parsing.
   */
  private LocalDate getLatestDataDate() {
    try {
      String dateStr = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
      return dateStr != null ? LocalDate.parse(dateStr) : null;
    } catch (Exception e) {
      logger.debug("Failed to get latest data date", e);
      return null;
    }
  }

  /**
   * Retrieve the earliest and latest NAV entry dates from the database.
   *
   * <p>On success the returned map contains two entries:
   * - "startDate": the minimum date from the nav table, or null if none.
   * - "endDate": the maximum date from the nav table, or null if none.
   *
   * @return a map with keys "startDate" and "endDate" mapped to their respective LocalDate values;
   *         if the dates cannot be determined or an error occurs both values will be null
   */
  private Map<String, LocalDate> getDataDateRange() {
    Map<String, LocalDate> dateRange = new LinkedHashMap<>();

    try {
      String minDateStr = jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class);
      String maxDateStr = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
      LocalDate minDate = minDateStr != null ? LocalDate.parse(minDateStr) : null;
      LocalDate maxDate = maxDateStr != null ? LocalDate.parse(maxDateStr) : null;

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

  /**
   * Determines whether the provided latest data date is more than ten days older than the current date
   * according to the service clock.
   *
   * @param latestDataDate the most recent data date to evaluate; may be null
   * @return `true` if `latestDataDate` is more than ten days before now or cannot be evaluated (including when null), `false` otherwise
   */
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