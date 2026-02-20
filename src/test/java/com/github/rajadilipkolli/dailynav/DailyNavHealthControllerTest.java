package com.github.rajadilipkolli.dailynav;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DailyNavHealthControllerTest extends AbstractRepositoryTest {
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 7, 1);

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
  void healthEndpointWithNoData() throws Exception {
    // Remove all data
    connection.createStatement().execute("DELETE FROM nav");
    mockMvc
        .perform(get("/daily-nav/health").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.healthy").exists())
        .andExpect(jsonPath("$.databaseAccessible").exists());
  }

  @Test
  void infoEndpointReturnsOkAndExpectedFields() throws Exception {
    mockMvc
        .perform(get("/daily-nav/info").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.autoInit").exists())
        .andExpect(jsonPath("$.indexesEnabled").exists())
        .andExpect(jsonPath("$.databasePath").exists())
        .andExpect(jsonPath("$.debugMode").exists())
        .andExpect(jsonPath("$.dataStartDate").exists())
        .andExpect(jsonPath("$.dataEndDate").exists())
        .andExpect(jsonPath("$.dataSpanDays").exists())
        .andExpect(jsonPath("$.sampleSchemes").isArray());
  }

  @Test
  void infoEndpointWithNoDataReturnsNullDates() throws Exception {
    // Remove all data
    connection.createStatement().execute("DELETE FROM nav");
    mockMvc
        .perform(get("/daily-nav/info").accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dataStartDate").value((String) null))
        .andExpect(jsonPath("$.dataEndDate").value((String) null));
  }
}
