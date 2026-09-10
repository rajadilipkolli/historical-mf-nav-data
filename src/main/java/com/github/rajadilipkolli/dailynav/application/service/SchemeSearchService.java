package com.github.rajadilipkolli.dailynav.application.service;

import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.search.PagedResult;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;

/** Service for scheme search and discovery operations. */
public class SchemeSearchService {

  private final SchemePort schemePort;

  public SchemeSearchService(SchemePort schemePort) {
    this.schemePort = schemePort;
  }

  /**
   * Searches for schemes matching the given criteria with pagination.
   *
   * @param criteria the search criteria
   * @return a paged result of schemes
   */
  public PagedResult<Scheme> search(SchemeSearchCriteria criteria) {
    long totalElements = schemePort.count(criteria);
    List<Scheme> data = schemePort.search(criteria);
    return new PagedResult<>(data, totalElements, criteria.page(), criteria.pageSize());
  }

  /**
   * Finds a scheme by its exact name.
   *
   * @param name the exact scheme name
   * @return the matching scheme, if found
   */
  public Optional<Scheme> findBySchemeName(String name) {
    return schemePort.findBySchemeName(name);
  }

  /**
   * Retrieves a list of distinct AMC (Asset Management Company) names.
   *
   * @return a sorted list of AMC names
   */
  @Cacheable(cacheNames = "dailyNavAmcs", cacheManager = "dailyNavCacheManager")
  public List<String> listAmcs() {
    return schemePort.findDistinctAmcs();
  }

  /**
   * Retrieves a list of distinct category names.
   *
   * @return a sorted list of category names
   */
  @Cacheable(cacheNames = "dailyNavCategories", cacheManager = "dailyNavCacheManager")
  public List<String> listCategories() {
    return schemePort.findDistinctCategories();
  }
}
