package com.github.rajadilipkolli.dailynav.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.model.Scheme;
import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import com.github.rajadilipkolli.dailynav.repository.NavByIsinRepository;
import com.github.rajadilipkolli.dailynav.repository.SchemeRepository;
import com.github.rajadilipkolli.dailynav.repository.SecurityRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MutualFundServiceTest extends AbstractRepositoryTest {
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 7, 1);

  private MutualFundService service;

  @BeforeEach
  void setUp() throws SQLException {
    // Create repositories with the test JdbcTemplate
    NavByIsinRepository navByIsinRepository = new NavByIsinRepository(jdbcTemplate);
    SchemeRepository schemeRepository = new SchemeRepository(jdbcTemplate);
    SecurityRepository securityRepository = new SecurityRepository(jdbcTemplate);
    service = new MutualFundService(navByIsinRepository, schemeRepository, securityRepository);
  }

  @Override
  protected void createSchema() throws SQLException {
    try (var stmt = connection.createStatement()) {
      stmt.execute("CREATE TABLE nav_by_isin (isin TEXT, date TEXT, nav REAL)");
      stmt.execute("CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
      stmt.execute("CREATE TABLE securities (isin TEXT, type INTEGER, scheme_code INTEGER)");
    }
  }

  @Override
  protected void insertTestData() throws SQLException {
    // Insert schemes
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO schemes (scheme_code, scheme_name) VALUES (?, ?)")) {
      ps.setInt(1, 1);
      ps.setString(2, "Test Scheme");
      ps.executeUpdate();
      ps.setInt(1, 2);
      ps.setString(2, "Another Scheme");
      ps.executeUpdate();
    }
    // Insert securities
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO securities (isin, type, scheme_code) VALUES (?, ?, ?)")) {
      ps.setString(1, "ISIN123");
      ps.setInt(2, 1);
      ps.setInt(3, 1);
      ps.executeUpdate();
      ps.setString(1, "ISIN456");
      ps.setInt(2, 0);
      ps.setInt(3, 2);
      ps.executeUpdate();
    }
    // Insert nav_by_isin
    try (var ps =
        connection.prepareStatement("INSERT INTO nav_by_isin (isin, date, nav) VALUES (?, ?, ?)")) {
      ps.setString(1, "ISIN123");
      ps.setString(2, REFERENCE_DATE.toString());
      ps.setDouble(3, 100.0);
      ps.executeUpdate();
      ps.setString(1, "ISIN123");
      ps.setString(2, REFERENCE_DATE.minusDays(1).toString());
      ps.setDouble(3, 99.0);
      ps.executeUpdate();
      ps.setString(1, "ISIN456");
      ps.setString(2, REFERENCE_DATE.toString());
      ps.setDouble(3, 200.0);
      ps.executeUpdate();
    }
  }

  @Test
  void getLatestNavByIsin_positive() {
    Optional<NavByIsin> nav = service.getLatestNavByIsin("ISIN123");
    assertTrue(nav.isPresent());
    assertEquals(100.0, nav.get().getNav());
  }

  @Test
  void getLatestNavByIsin_negative() {
    assertTrue(service.getLatestNavByIsin("NONEXISTENT").isEmpty());
  }

  @Test
  void getNavByIsinAndDate_positive() {
    Optional<NavByIsin> nav = service.getNavByIsinAndDate("ISIN123", REFERENCE_DATE);
    assertTrue(nav.isPresent());
    assertEquals(100.0, nav.get().getNav());
  }

  @Test
  void getNavByIsinAndDate_negative() {
    assertTrue(service.getNavByIsinAndDate("ISIN123", LocalDate.of(2000, 1, 1)).isEmpty());
  }

  @Test
  void getLastNDaysNav_positive() {
    List<NavByIsin> navs = service.getLastNDaysNav("ISIN123", 2);
    assertEquals(2, navs.size());
  }

  @Test
  void getLastNDaysNav_negative() {
    List<NavByIsin> navs = service.getLastNDaysNav("NONEXISTENT", 2);
    assertTrue(navs.isEmpty());
  }

  @Test
  void getScheme_positive() {
    Optional<Scheme> scheme = service.getScheme(1);
    assertTrue(scheme.isPresent());
    assertEquals("Test Scheme", scheme.get().schemeName());
  }

  @Test
  void getScheme_negative() {
    assertTrue(service.getScheme(999).isEmpty());
  }

  @Test
  void searchSchemes_positive() {
    List<Scheme> schemes = service.searchSchemes("Test");
    assertFalse(schemes.isEmpty());
  }

  @Test
  void searchSchemes_negative() {
    List<Scheme> schemes = service.searchSchemes("Nonexistent");
    assertTrue(schemes.isEmpty());
  }

  @Test
  void getAllSchemes_positive() {
    List<Scheme> schemes = service.getAllSchemes();
    assertEquals(2, schemes.size());
  }
}
