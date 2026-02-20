package com.github.rajadilipkolli.dailynav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rajadilipkolli.dailynav.model.Security;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecurityRepositoryTest extends AbstractRepositoryTest {
  private SecurityRepository securityRepository;

  @BeforeEach
  void setUpSecurityRepo() {
    securityRepository = new SecurityRepository(jdbcTemplate);
  }

  @Override
  protected void createSchema() throws SQLException {
    connection
        .createStatement()
        .execute("CREATE TABLE securities (isin TEXT, type INTEGER, scheme_code INTEGER)");
  }

  @Override
  protected void insertTestData() throws SQLException {
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO securities (isin, type, scheme_code) VALUES (?, ?, ?)")) {
      ps.setString(1, "ISIN123");
      ps.setInt(2, 1);
      ps.setInt(3, 1);
      ps.executeUpdate();
      ps.setString(1, "ISIN456");
      ps.setInt(2, 2);
      ps.setInt(3, 2);
      ps.executeUpdate();
    }
  }

  @Test
  void testFindByIsin() {
    Optional<Security> result = securityRepository.findByIsin("ISIN123");
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(1, result.get().getType());
    assertEquals(1, result.get().getSchemeCode());
  }

  @Test
  void testFindByIsinNotFound() {
    Optional<Security> result = securityRepository.findByIsin("NONEXISTENT");
    assertFalse(result.isPresent());
  }

  @Test
  void testFindBySchemeCode() {
    List<Security> result = securityRepository.findBySchemeCode(1);
    assertEquals(1, result.size());
    assertEquals("ISIN123", result.get(0).getIsin());
    assertEquals(1, result.get(0).getType());
    assertEquals(1, result.get(0).getSchemeCode());
  }

  @Test
  void testFindBySchemeCodeNotFound() {
    List<Security> result = securityRepository.findBySchemeCode(-1);
    assertTrue(result.isEmpty());
  }

  @Test
  void testFindAll() {
    List<Security> result = securityRepository.findAll();
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.getIsin().equals("ISIN123")));
    assertTrue(result.stream().anyMatch(s -> s.getIsin().equals("ISIN456")));
  }

  @Test
  void testFindAllEmpty() throws SQLException {
    // Remove all data
    connection.createStatement().execute("DELETE FROM securities");
    List<Security> result = securityRepository.findAll();
    assertTrue(result.isEmpty());
  }
}
