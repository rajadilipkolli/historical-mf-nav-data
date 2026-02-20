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

  @Autowired
  public DailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this(jdbcTemplate, properties, Clock.systemDefaultZone());
  }

  // Package-private constructor for tests to inject a fixed clock
  DailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DailyNavProperties properties,
      Clock clock) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.clock = clock == null ? Clock.systemDefaultZone() : clock;
  }

  /**
   * Performs a comprehensive health check of the Daily NAV library
   *
   * @return DailyNavHealthStatus containing all health information
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

  private static boolean isOverallHealthy(DailyNavHealthStatus status) {
    boolean overallHealthy = status.isDatabaseAccessible();
    Integer schemeCount = status.getSchemeCount();
    Integer navCount = status.getNavRecordCount();
    // require non-null and >0 counts
    if (schemeCount == null || schemeCount <= 0) {
      overallHealthy = false;
    }
    if (navCount == null || navCount <= 0) {
      overallHealthy = false;
    }

    // Mark unhealthy if counts are present but below expected thresholds
    if (schemeCount != null && schemeCount > 0 && schemeCount < 100) {
      overallHealthy = false;
    }
    if (navCount != null && navCount > 0 && navCount < 1000) {
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

  private LocalDate getLatestDataDate() {
    try {
      String dateStr = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
      return dateStr != null ? LocalDate.parse(dateStr) : null;
    } catch (Exception e) {
      logger.debug("Failed to get latest data date", e);
      return null;
    }
  }

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
