package com.github.rajadilipkolli.dailynav.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/** Test for DailyNavHealthService */
@ExtendWith(MockitoExtension.class)
class DailyNavHealthServiceTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @Mock private DailyNavProperties properties;

  private DailyNavHealthService healthService;

  @BeforeEach
  void setUp() {
    healthService = new DailyNavHealthService(jdbcTemplate, properties);
  }

  @Test
  void isHealthy_shouldReturnTrue_whenDatabaseIsAccessibleAndHasData() {
    // Arrange
    when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class))
        .thenReturn(100);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class)).thenReturn(1000);

    // Act
    boolean healthy = healthService.isHealthy();

    // Assert
    assertThat(healthy).isTrue();
  }

  @Test
  void isHealthy_shouldReturnFalse_whenDatabaseIsNotAccessible() {
    // Arrange
    when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    boolean healthy = healthService.isHealthy();

    // Assert
    assertThat(healthy).isFalse();
  }

  @Test
  void checkHealth_shouldReturnHealthyStatus_whenAllChecksPass() {
    // Arrange
    when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class))
        .thenReturn(500);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class)).thenReturn(10000);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class))
        .thenReturn(800);
    when(jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class))
        .thenReturn("2025-07-12");
    when(jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class))
        .thenReturn("2020-01-01");

    when(properties.isAutoInit()).thenReturn(true);
    when(properties.isCreateIndexes()).thenReturn(true);
    when(properties.getDatabasePath()).thenReturn("jdbc:sqlite::memory:");

    // Act
    DailyNavHealthStatus status = healthService.checkHealth();

    // Assert
    assertThat(status.isHealthy()).isTrue();
    assertThat(status.isDatabaseAccessible()).isTrue();
    assertThat(status.getSchemeCount()).isEqualTo(500);
    assertThat(status.getNavRecordCount()).isEqualTo(10000);
    assertThat(status.getSecurityCount()).isEqualTo(800);
    assertThat(status.getLatestDataDate()).isEqualTo("2025-07-12");
    assertThat(status.getDataStartDate()).isEqualTo("2020-01-01");
    assertThat(status.isDataStale()).isFalse();
    assertThat(status.getIssues()).isEmpty();
  }

  @Test
  void checkHealth_shouldDetectStaleData() {
    // Arrange
    when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class))
        .thenReturn(500);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class)).thenReturn(10000);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class))
        .thenReturn(800);
    when(jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class))
        .thenReturn("2025-06-01"); // Old date
    when(jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class))
        .thenReturn("2020-01-01");

    when(properties.isAutoInit()).thenReturn(true);
    when(properties.isCreateIndexes()).thenReturn(true);
    when(properties.getDatabasePath()).thenReturn("jdbc:sqlite::memory:");

    // Act
    DailyNavHealthStatus status = healthService.checkHealth();

    // Assert
    assertThat(status.isDataStale()).isTrue();
    assertThat(status.getIssues()).contains("Data appears to be stale (older than 10 days)");
  }

  @Test
  void getStatistics_shouldReturnCompleteStatistics() {
    // Arrange
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class))
        .thenReturn(500);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class)).thenReturn(10000);
    when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class))
        .thenReturn(800);
    when(jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class))
        .thenReturn("2025-07-12");
    when(jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class))
        .thenReturn("2025-01-01");

    // Act
    Map<String, Object> stats = healthService.getStatistics();

    // Assert
    assertThat(stats).containsEntry("schemes", 500);
    assertThat(stats).containsEntry("navRecords", 10000);
    assertThat(stats).containsEntry("securities", 800);
    assertThat(stats).containsEntry("latestDataDate", "2025-07-12");
    assertThat(stats).containsEntry("startDate", "2025-01-01");
    assertThat(stats).containsEntry("endDate", "2025-07-12");
    assertThat(stats).containsKey("dataSpanDays");
    assertThat(stats).containsEntry("dataStale", false);
  }
}
