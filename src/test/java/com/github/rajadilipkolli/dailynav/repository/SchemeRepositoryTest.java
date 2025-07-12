package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;

class SchemeRepositoryTest extends AbstractRepositoryTest {
  private SchemeRepository schemeRepository;

  @BeforeEach
  void setUpSchemeRepo() {
    schemeRepository = new SchemeRepository(jdbcTemplate);
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
  }

  @Override
  protected void insertTestData() throws SQLException {
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
  }

  @Test
  void testFindBySchemeCode() {
    Optional<Scheme> result = schemeRepository.findBySchemeCode(1);
    assertTrue(result.isPresent());
    assertEquals(new Scheme(1, "Test Scheme"), result.get());
    assertTrue(schemeRepository.findBySchemeCode(999).isEmpty());
  }

  @Test
  void testFindAll() {
    List<Scheme> result = schemeRepository.findAll();
    assertEquals(2, result.size());
    assertTrue(result.contains(new Scheme(1, "Test Scheme")));
    assertTrue(result.contains(new Scheme(2, "Another Scheme")));
  }

  @Test
  void testFindBySchemeNameContaining() {
    List<Scheme> result = schemeRepository.findBySchemeNameContaining("Test");
    assertEquals(1, result.size());
    assertEquals(new Scheme(1, "Test Scheme"), result.get(0));

    result = schemeRepository.findBySchemeNameContaining("Scheme");
    assertEquals(2, result.size());

    result = schemeRepository.findBySchemeNameContaining("Nonexistent");
    assertTrue(result.isEmpty());
  }
}
