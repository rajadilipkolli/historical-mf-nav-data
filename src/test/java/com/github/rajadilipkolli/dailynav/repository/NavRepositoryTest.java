package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.Nav;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;

class NavRepositoryTest extends AbstractRepositoryTest {
  private NavRepository navRepository;

  @BeforeEach
  void setUpNavRepo() {
    navRepository = new NavRepository(jdbcTemplate);
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE nav (scheme_code INTEGER, date TEXT, nav REAL)");
  }

  @Override
  protected void insertTestData() throws SQLException {
    try (var ps1 =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)")) {
      ps1.setInt(1, 1);
      ps1.setString(2, LocalDate.now().toString());
      ps1.setDouble(3, 100.0);
      ps1.executeUpdate();
      ps1.setInt(1, 1);
      ps1.setString(2, LocalDate.now().minusDays(1).toString());
      ps1.setDouble(3, 99.0);
      ps1.executeUpdate();
      ps1.setInt(1, 2);
      ps1.setString(2, LocalDate.now().toString());
      ps1.setDouble(3, 200.0);
      ps1.executeUpdate();
    }
  }

  @Test
  void testFindBySchemeCode() {
    List<Nav> result = navRepository.findBySchemeCode(1);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 100.0));
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 99.0));
  }

  @Test
  void testFindBySchemeCodeAndDateBetween() {
    List<Nav> result =
        navRepository.findBySchemeCodeAndDateBetween(
            1, LocalDate.now().minusDays(1), LocalDate.now());
    assertEquals(2, result.size());
  }

  @Test
  void testFindLatestBySchemeCode() {
    Optional<Nav> result = navRepository.findLatestBySchemeCode(1);
    assertTrue(result.isPresent());
    assertEquals(100.0, result.get().getNav());
  }

  @Test
  void testFindBySchemeCodeAndDateOnOrBefore() {
    Optional<Nav> result =
        navRepository.findBySchemeCodeAndDateOnOrBefore(1, LocalDate.now().minusDays(1));
    assertTrue(result.isPresent());
    assertEquals(99.0, result.get().getNav());
  }

  @Test
  void testFindBySchemeCodeNotFound() {
    List<Nav> result = navRepository.findBySchemeCode(999);
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindLatestBySchemeCodeNotFound() {
    Optional<Nav> result = navRepository.findLatestBySchemeCode(999);
    assertFalse(result.isPresent());
  }

  @Test
  void testFindBySchemeCodeAndDateOnOrBeforeNotFound() {
    Optional<Nav> result =
        navRepository.findBySchemeCodeAndDateOnOrBefore(1, LocalDate.now().minusDays(10));
    assertFalse(result.isPresent());
  }
}
