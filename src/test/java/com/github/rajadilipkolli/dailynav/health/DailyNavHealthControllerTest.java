package com.github.rajadilipkolli.dailynav.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DailyNavHealthControllerTest extends AbstractRepositoryTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUpController() {
    DailyNavProperties properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    DailyNavHealthService healthService = new DailyNavHealthService(jdbcTemplate, properties);
    DailyNavHealthController controller =
        new DailyNavHealthController(jdbcTemplate, properties, healthService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE nav (scheme_code INTEGER, date TEXT, nav REAL)");
    connection
        .createStatement()
        .execute("CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
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
      for (int i = 1; i <= 5; i++) {
        ps.setInt(1, i);
        ps.setString(2, "Scheme " + i);
        ps.executeUpdate();
      }
    }
    // Insert nav
    try (var ps =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      for (int i = 1; i <= 5; i++) {
        for (int d = 0; d < 3; d++) {
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
      for (int i = 1; i <= 5; i++) {
        ps.setString(1, "ISIN" + i);
        ps.setInt(2, i % 2);
        ps.setInt(3, i);
        ps.executeUpdate();
      }
    }
  }

  @Test
  void healthEndpointReturnsOk() throws Exception {
    mockMvc
        .perform(get("/daily-nav/health").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.healthy").exists())
        .andExpect(jsonPath("$.databaseAccessible").exists());
  }

  @Test
  void healthEndpointReturnsExpectedFields() throws Exception {
    mockMvc
        .perform(get("/daily-nav/health").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.healthy").exists())
        .andExpect(jsonPath("$.databaseAccessible").exists());
  }

  @Test
  void healthEndpointWithNoData() throws Exception {
    // Remove all data
    connection.createStatement().execute("DELETE FROM nav");
    mockMvc
        .perform(get("/daily-nav/health").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.healthy").exists())
        .andExpect(jsonPath("$.databaseAccessible").exists());
  }
}
