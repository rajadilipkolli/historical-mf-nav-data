package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.util.List;
import java.util.Optional;

public interface SchemePort {
  /**
   * Finds a scheme by its code.
   *
   * @param schemeCode the code of the scheme to find
   * @return the matching scheme, if found
   */
  Optional<Scheme> findBySchemeCode(Integer schemeCode);

  /**
   * Finds schemes whose names contain the specified pattern.
   *
   * @param namePattern the pattern to search for within scheme names
   * @return the schemes with names containing the specified pattern
   */
  List<Scheme> findBySchemeNameContaining(String namePattern);

  /**
   * Retrieves all schemes.
   *
   * @return all available schemes
   */
  List<Scheme> findAll();

  /**
   * Finds a scheme by its exact name.
   *
   * @param name the exact scheme name
   * @return the matching scheme, if found
   */
  Optional<Scheme> findBySchemeName(String name);

  /**
   * Searches for schemes matching the given criteria.
   *
   * @param criteria the search criteria
   * @return the list of matched schemes for the requested page
   */
  List<Scheme> search(SchemeSearchCriteria criteria);

  /**
   * Counts the total number of schemes matching the given criteria.
   *
   * @param criteria the search criteria
   * @return the total count
   */
  long count(SchemeSearchCriteria criteria);

  /**
   * Retrieves a list of distinct AMC (Asset Management Company) names.
   *
   * @return a sorted list of AMC names
   */
  List<String> findDistinctAmcs();

  /**
   * Retrieves a list of distinct category names.
   *
   * @return a sorted list of category names
   */
  List<String> findDistinctCategories();
}
