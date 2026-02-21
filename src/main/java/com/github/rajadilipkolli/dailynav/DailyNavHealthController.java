package com.github.rajadilipkolli.dailynav;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health check endpoint for Daily NAV library when Spring Boot Actuator is not available
 * Provides basic health information about the embedded database
 */
@RestController
@RequestMapping("/daily-nav")
@ConditionalOnWebApplication
public class DailyNavHealthController {

  private static final Logger logger = LoggerFactory.getLogger(DailyNavHealthController.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;
  private final DailyNavHealthService healthService;

  /**
   * Create a DailyNavHealthController with its required dependencies.
   *
   * @param jdbcTemplate JdbcTemplate configured for Daily NAV data access (injected with qualifier
   *     "dailyNavJdbcTemplate")
   * @param properties configuration properties for Daily NAV behavior
   * @param healthService service used to evaluate application health
   */
  public DailyNavHealthController(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DailyNavProperties properties,
      DailyNavHealthService healthService) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.healthService = healthService;
  }

  /**
   * Exposes the application's health status for the Daily NAV service.
   *
   * @return a ResponseEntity containing the current DailyNavHealthStatus; HTTP 200 if the service
   *     is healthy, HTTP 503 (Service Unavailable) if not healthy.
   */
  @GetMapping("/health")
  public ResponseEntity<DailyNavHealthStatus> health() {
    DailyNavHealthStatus status = healthService.checkHealth();
    // Return 200 when the database is accessible; return 503 when not accessible.
    return status.isHealthy()
        ? ResponseEntity.ok(status)
        : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
  }

  /**
   * Provide runtime configuration flags and basic dataset statistics for Daily NAV.
   *
   * <p>The returned map includes non-sensitive configuration indicators and sample data
   * information. Common keys: - "autoInit": whether automatic initialization is enabled -
   * "indexesEnabled": whether index creation is enabled - "databasePath": non-sensitive indicator
   * of database location ("unknown", "in-memory", "file", or "external") - "databaseType": same
   * value as "databasePath" - "debugMode": whether debug mode is enabled - "dataStartDate":
   * earliest NAV date present (if available) - "dataEndDate": latest NAV date present (if
   * available) - "dataSpanDays": number of days between start and end dates (if both present) -
   * "sampleSchemes": up to five scheme names sampled from the database - "error": present when
   * detailed data retrieval fails
   *
   * @return a map of information entries describing configuration and basic dataset statistics
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> info() {
    Map<String, Object> info = new LinkedHashMap<>();

    try {
      // Configuration information
      info.put("autoInit", properties.isAutoInit());
      info.put("indexesEnabled", properties.isCreateIndexes());
      // Do not expose absolute filesystem paths. Provide a non-sensitive indicator instead.
      String dbPath = properties.getDatabasePath();
      String dbType;
      if (dbPath == null) {
        dbType = "unknown";
      } else if (dbPath.contains(":memory:")) {
        dbType = "in-memory";
      } else if (properties.getDatabaseFile() != null
          && !properties.getDatabaseFile().trim().isEmpty()) {
        dbType = "file";
      } else if (dbPath.startsWith("jdbc:sqlite:")) {
        dbType = "file";
      } else {
        dbType = "external";
      }
      // Keep the databasePath key for backwards compatibility tests but do not return absolute
      // paths.
      info.put("databasePath", dbType);
      info.put("databaseType", dbType);
      info.put("debugMode", properties.isDebug());

      // Data statistics
      try {
        LocalDate minDate =
            jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", LocalDate.class);
        LocalDate maxDate =
            jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", LocalDate.class);

        info.put("dataStartDate", minDate);
        info.put("dataEndDate", maxDate);

        if (minDate != null && maxDate != null) {
          long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);
          info.put("dataSpanDays", daysBetween);
        }

        // Sample scheme names
        var sampleSchemes =
            jdbcTemplate.queryForList("SELECT scheme_name FROM schemes LIMIT 5", String.class);
        info.put("sampleSchemes", sampleSchemes);

      } catch (Exception e) {
        logger.debug("Failed to get detailed info", e);
        info.put("error", "Could not retrieve detailed information");
      }

      return ResponseEntity.ok(info);

    } catch (Exception e) {
      logger.error("Info endpoint failed", e);
      Map<String, Object> errorResponse = new LinkedHashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }
}
