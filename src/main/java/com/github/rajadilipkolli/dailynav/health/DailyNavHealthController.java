package com.github.rajadilipkolli.dailynav.health;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
@ConditionalOnMissingClass("org.springframework.boot.actuator.health.HealthIndicator")
public class DailyNavHealthController {

  private static final Logger logger = LoggerFactory.getLogger(DailyNavHealthController.class);

  private final JdbcTemplate jdbcTemplate;
  private final DailyNavProperties properties;
  private final DailyNavHealthService healthService;

  public DailyNavHealthController(
      JdbcTemplate jdbcTemplate,
      DailyNavProperties properties,
      DailyNavHealthService healthService) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.healthService = healthService;
  }

  /**
   * Basic health check endpoint Returns HTTP 200 if the database is accessible, HTTP 503 otherwise
   */
  @GetMapping("/health")
  public DailyNavHealthStatus health() {
    return healthService.checkHealth();
  }

  /** Detailed information about the Daily NAV library */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> info() {
    Map<String, Object> info = new LinkedHashMap<>();

    try {
      // Configuration information
      info.put("autoInit", properties.isAutoInit());
      info.put("indexesEnabled", properties.isCreateIndexes());
      info.put("databasePath", properties.getDatabasePath());
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
