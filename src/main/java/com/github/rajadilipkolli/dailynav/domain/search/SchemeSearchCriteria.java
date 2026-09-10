package com.github.rajadilipkolli.dailynav.domain.search;

/**
 * Search criteria for filtering and paginating mutual fund schemes.
 *
 * @param namePattern the pattern to match in the scheme name
 * @param amc the AMC name to filter by
 * @param category the scheme category to filter by
 * @param plan the plan type to filter by
 * @param option the option type to filter by
 * @param sortField the field to sort by (e.g. scheme_name, scheme_code)
 * @param sortDirection the sort direction (e.g. ASC, DESC)
 * @param page the zero-based page number
 * @param pageSize the number of elements per page
 */
public record SchemeSearchCriteria(
    String namePattern,
    String amc,
    String category,
    String plan,
    String option,
    String sortField,
    String sortDirection,
    int page,
    int pageSize) {
  public SchemeSearchCriteria {
    if (page < 0) {
      page = 0;
    }
    if (pageSize <= 0) {
      pageSize = 20;
    }
  }
}
