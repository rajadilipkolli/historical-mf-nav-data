package com.github.rajadilipkolli.dailynav;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * Spring Boot Actuator HealthIndicator for Daily NAV library. Provides detailed health status of
 * the mutual fund database.
 */
public class DailyNavHealthIndicator implements HealthIndicator {

  private final DailyNavHealthService healthService;

  public DailyNavHealthIndicator(DailyNavHealthService healthService) {
    this.healthService = healthService;
  }

  @Override
  public Health health() {
    DailyNavHealthStatus status = healthService.checkHealth();
    Health.Builder builder = status.isHealthy() ? Health.up() : Health.down();

    builder
        .withDetail("databaseAccessible", status.isDatabaseAccessible())
        .withDetail("schemeCount", status.getSchemeCount())
        .withDetail("navRecordCount", status.getNavRecordCount())
        .withDetail("securityCount", status.getSecurityCount())
        .withDetail("latestDataDate", status.getLatestDataDate())
        .withDetail("dataStale", status.isDataStale());

    if (!status.getIssues().isEmpty()) {
      builder.withDetail("issues", status.getIssues());
    }

    return builder.build();
  }
}
