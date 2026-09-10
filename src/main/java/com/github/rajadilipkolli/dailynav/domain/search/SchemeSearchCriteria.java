package com.github.rajadilipkolli.dailynav.domain.search;

/**
 * Search criteria for filtering and paginating mutual fund schemes.
 *
 * <p>Negative page numbers are normalized to zero, and non-positive page sizes are normalized to
 * 20.
 *
 * @param namePattern a case-insensitive substring of the scheme name, or {@code null} or blank to
 *     disable this filter
 * @param amc the exact AMC name, or {@code null} or blank to disable this filter
 * @param category the exact scheme category, or {@code null} or blank to disable this filter
 * @param plan the exact plan type, or {@code null} or blank to disable this filter
 * @param option the exact option type, or {@code null} or blank to disable this filter
 * @param sortField one of {@code scheme_code}, {@code amc}, {@code category}, {@code plan}, or
 *     {@code option}; blank and unsupported values sort by scheme name
 * @param sortDirection {@code DESC} for descending order when {@code sortField} is non-blank; any
 *     other value uses ascending order
 * @param page the zero-based page number; negative values use zero
 * @param pageSize the number of elements per page; non-positive values use 20
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
