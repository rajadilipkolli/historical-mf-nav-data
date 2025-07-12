package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavByIsinRepositoryTest extends AbstractRepositoryTest {
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 7, 1);
  private NavByIsinRepository navByIsinRepository;

  @BeforeEach
  void setUpNavByIsinRepo() {
    navByIsinRepository = new NavByIsinRepository(jdbcTemplate);
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE nav_by_isin (isin TEXT, date TEXT, nav REAL)");
  }

  @Override
  protected void insertTestData() throws SQLException {
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
  void testFindLatestByIsin() {
    Optional<NavByIsin> result = navByIsinRepository.findLatestByIsin("ISIN123");
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(REFERENCE_DATE, result.get().getDate());
    assertEquals(100.0, result.get().getNav());
  }

  @Test
  void testFindByIsinAndDateOnOrBefore() {
    Optional<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateOnOrBefore("ISIN123", REFERENCE_DATE.minusDays(1));
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(REFERENCE_DATE.minusDays(1), result.get().getDate());
    assertEquals(99.0, result.get().getNav());
  }

  @Test
  void testFindLastNByIsin() {
    List<NavByIsin> result = navByIsinRepository.findLastNByIsin("ISIN123", 2);
    assertEquals(2, result.size());
    assertEquals("ISIN123", result.get(0).getIsin());
    assertEquals("ISIN123", result.get(1).getIsin());
  }

  @Test
  void testFindByIsinAndDateBetween() {
    List<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateBetween(
            "ISIN123", REFERENCE_DATE.minusDays(1), REFERENCE_DATE);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 100.0));
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 99.0));
  }

  @Test
  void testFindLatestByIsinNotFound() {
    Optional<NavByIsin> result = navByIsinRepository.findLatestByIsin("NONEXISTENT");
    assertFalse(result.isPresent());
  }

  @Test
  void testFindByIsinAndDateOnOrBeforeNotFound() {
    Optional<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateOnOrBefore("ISIN123", REFERENCE_DATE.minusDays(10));
    assertFalse(result.isPresent());
  }

  @Test
  void testFindLastNByIsinNotFound() {
    List<NavByIsin> result = navByIsinRepository.findLastNByIsin("NONEXISTENT", 2);
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindByIsinAndDateBetweenNotFound() {
    List<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateBetween(
            "NONEXISTENT", REFERENCE_DATE.minusDays(1), REFERENCE_DATE);
    assertTrue(result.isEmpty());
  }
}
