package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rajadilipkolli.dailynav.AbstractRepositoryTest;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemeRepositoryTest extends AbstractRepositoryTest {
  private SchemeRepository schemeRepository;

  @BeforeEach
  void setUpSchemeRepo() {
    schemeRepository = new SchemeRepository(jdbcTemplate);
  }

  @Override
  protected void createSchema() throws SQLException {
    try (var stmt = connection.createStatement()) {
      stmt.execute(
          "CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT, amc TEXT, category TEXT, plan TEXT, option TEXT)");
    }
  }

  @Override
  protected void insertTestData() throws SQLException {
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO schemes (scheme_code, scheme_name, amc, category, plan, option) VALUES (?, ?, ?, ?, ?, ?)")) {
      ps.setInt(1, 1);
      ps.setString(2, "Test Scheme A");
      ps.setString(3, "AMC 1");
      ps.setString(4, "Category 1");
      ps.setString(5, "Plan A");
      ps.setString(6, "Option X");
      ps.executeUpdate();

      ps.setInt(1, 2);
      ps.setString(2, "Another Scheme B");
      ps.setString(3, "AMC 2");
      ps.setString(4, "Category 1");
      ps.setString(5, "Plan B");
      ps.setString(6, "Option Y");
      ps.executeUpdate();

      ps.setInt(1, 3);
      ps.setString(2, "Test Scheme C");
      ps.setString(3, "AMC 1");
      ps.setString(4, "Category 2");
      ps.setString(5, "Plan A");
      ps.setString(6, "Option X");
      ps.executeUpdate();
    }
  }

  @Test
  void testFindBySchemeCode() {
    Optional<Scheme> result = schemeRepository.findBySchemeCode(1);
    assertTrue(result.isPresent());
    assertEquals(
        new Scheme(1, "Test Scheme A", "AMC 1", "Category 1", "Plan A", "Option X"), result.get());
    assertTrue(schemeRepository.findBySchemeCode(999).isEmpty());
  }

  @Test
  void testFindAll() {
    List<Scheme> result = schemeRepository.findAll();
    assertEquals(3, result.size());
  }

  @Test
  void testFindBySchemeName() {
    Optional<Scheme> result = schemeRepository.findBySchemeName("Test Scheme A");
    assertTrue(result.isPresent());
    assertEquals(1, result.get().schemeCode());

    assertFalse(schemeRepository.findBySchemeName("Test Scheme Z").isPresent());
  }

  @Test
  void testSearchFiltered() {
    // Exact match AMC
    SchemeSearchCriteria criteria =
        new SchemeSearchCriteria(null, "AMC 1", null, null, null, null, null, 0, 10);
    List<Scheme> result = schemeRepository.search(criteria);
    assertEquals(2, result.size());
    assertEquals(2, schemeRepository.count(criteria));

    // Multiple filters
    criteria = new SchemeSearchCriteria(null, "AMC 1", "Category 2", null, null, null, null, 0, 10);
    result = schemeRepository.search(criteria);
    assertEquals(1, result.size());
    assertEquals(3, result.get(0).schemeCode());
    assertEquals(1, schemeRepository.count(criteria));

    // Name Pattern
    criteria = new SchemeSearchCriteria("test", null, null, null, null, null, null, 0, 10);
    result = schemeRepository.search(criteria);
    assertEquals(2, result.size());
  }

  @Test
  void testSearchSorting() {
    SchemeSearchCriteria criteria =
        new SchemeSearchCriteria(null, null, null, null, null, "scheme_name", "DESC", 0, 10);
    List<Scheme> result = schemeRepository.search(criteria);
    assertEquals(3, result.size());
    assertEquals("Test Scheme C", result.get(0).schemeName());
    assertEquals("Test Scheme A", result.get(1).schemeName());
    assertEquals("Another Scheme B", result.get(2).schemeName());
  }

  @Test
  void testSearchPagination() {
    SchemeSearchCriteria criteria =
        new SchemeSearchCriteria(null, null, null, null, null, "scheme_code", "ASC", 0, 2);
    List<Scheme> result = schemeRepository.search(criteria);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).schemeCode());
    assertEquals(2, result.get(1).schemeCode());

    criteria = new SchemeSearchCriteria(null, null, null, null, null, "scheme_code", "ASC", 1, 2);
    result = schemeRepository.search(criteria);
    assertEquals(1, result.size());
    assertEquals(3, result.get(0).schemeCode());
  }

  @Test
  void testFindDistinctAmcs() {
    List<String> amcs = schemeRepository.findDistinctAmcs();
    assertEquals(2, amcs.size());
    assertEquals("AMC 1", amcs.get(0));
    assertEquals("AMC 2", amcs.get(1));
  }

  @Test
  void testFindDistinctCategories() {
    List<String> categories = schemeRepository.findDistinctCategories();
    assertEquals(2, categories.size());
    assertEquals("Category 1", categories.get(0));
    assertEquals("Category 2", categories.get(1));
  }
}
