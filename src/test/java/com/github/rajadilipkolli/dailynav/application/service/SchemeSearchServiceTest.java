package com.github.rajadilipkolli.dailynav.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.rajadilipkolli.dailynav.AbstractRepositoryTest;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.search.PagedResult;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.SchemeRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemeSearchServiceTest extends AbstractRepositoryTest {

  private SchemeSearchService schemeSearchService;

  /** Creates the search service under test with the shared repository fixture. */
  @BeforeEach
  void setUpService() {
    SchemeRepository schemeRepository = new SchemeRepository(jdbcTemplate);
    schemeSearchService = new SchemeSearchService(schemeRepository);
  }

  /** Creates the scheme table used by the search service tests. */
  @Override
  protected void createSchema() throws SQLException {
    try (var stmt = connection.createStatement()) {
      stmt.execute(
          "CREATE TABLE schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT, amc TEXT, category TEXT, plan TEXT, option TEXT)");
    }
  }

  /** Inserts representative scheme metadata for search and lookup assertions. */
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
    }
  }

  /** Verifies that filtered searches return the expected page metadata and entries. */
  @Test
  void testSearchDelegatesToRepositoryAndReturnsPagedResult() {
    SchemeSearchCriteria criteria =
        new SchemeSearchCriteria("Scheme", null, null, null, null, "scheme_code", "ASC", 0, 1);
    PagedResult<Scheme> result = schemeSearchService.search(criteria);

    assertEquals(2, result.totalElements());
    assertEquals(1, result.data().size());
    assertEquals(1, result.data().getFirst().schemeCode());
    assertEquals(0, result.page());
    assertEquals(1, result.pageSize());
  }

  /** Verifies exact-name lookup through the search service. */
  @Test
  void testFindBySchemeName() {
    assertEquals(1, schemeSearchService.findBySchemeName("Test Scheme A").get().schemeCode());
  }
}
