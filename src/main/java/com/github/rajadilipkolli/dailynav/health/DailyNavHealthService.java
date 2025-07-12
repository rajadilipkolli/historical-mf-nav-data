package com.github.rajadilipkolli.dailynav.health;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public DailyNavHealthService(JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
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
      String latestDate = getLatestDataDate();
      status.setLatestDataDate(latestDate);

      boolean isStale = isDataStale(latestDate);
      status.setDataStale(isStale);

      if (isStale) {
        status.addIssue("Data appears to be stale (older than 10 days)");
      }

      // Get date range
      Map<String, String> dateRange = getDataDateRange();
      status.setDataStartDate(dateRange.get("startDate"));
      status.setDataEndDate(dateRange.get("endDate"));

      // Check for sufficient data
      if (status.getSchemeCount() != null && status.getSchemeCount() < 100) {
        status.addIssue("Insufficient scheme data (less than 100 schemes)");
      }

      if (status.getNavRecordCount() != null && status.getNavRecordCount() < 1000) {
        status.addIssue("Insufficient NAV data (less than 1000 records)");
      }

      // Set overall health status
      status.setHealthy(status.getIssues().isEmpty());

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

      Map<String, String> dateRange = getDataDateRange();
      stats.putAll(dateRange);

      String latestDate = getLatestDataDate();
      stats.put("latestDataDate", latestDate);
      stats.put("dataStale", isDataStale(latestDate));

      // Calculate data span
      if (dateRange.get("startDate") != null && dateRange.get("endDate") != null) {
        try {
          LocalDate start = LocalDate.parse(dateRange.get("startDate"));
          LocalDate end = LocalDate.parse(dateRange.get("endDate"));
          long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
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

  private String getLatestDataDate() {
    try {
      return jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
    } catch (Exception e) {
      logger.debug("Failed to get latest data date", e);
      return null;
    }
  }

  private Map<String, String> getDataDateRange() {
    Map<String, String> dateRange = new LinkedHashMap<>();

    try {
      String minDate = jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class);
      String maxDate = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);

      dateRange.put("startDate", minDate);
      dateRange.put("endDate", maxDate);

    } catch (Exception e) {
      dateRange.put("startDate", null);
      dateRange.put("endDate", null);
      logger.debug("Failed to get data date range", e);
    }

    return dateRange;
  }

  private boolean isDataStale(String latestDataDate) {
    if (latestDataDate == null || "Unknown".equals(latestDataDate)) {
      return true;
    }

    return is10DaysOldData(latestDataDate);
  }

  boolean is10DaysOldData(String latestDataDate) {
    try {
      LocalDate latestDate = LocalDate.parse(latestDataDate);
      LocalDate now = LocalDate.now();

      // Consider data stale if it's more than 10 days old
      long daysSinceLastUpdate = ChronoUnit.DAYS.between(latestDate, now);
      return daysSinceLastUpdate > 10;

    } catch (Exception e) {
      DailyNavHealthService.logger.debug("Failed to parse latest data date: {}", latestDataDate, e);
      return true;
    }
  }
}
