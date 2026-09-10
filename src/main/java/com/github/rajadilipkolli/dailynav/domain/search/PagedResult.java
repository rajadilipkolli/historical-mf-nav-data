package com.github.rajadilipkolli.dailynav.domain.search;

import java.util.List;

/**
 * Represents a page of results.
 *
 * @param <T> the type of the data elements
 * @param data the list of elements in the current page
 * @param totalElements the total number of elements available
 * @param page the current page index (zero-based)
 * @param pageSize the number of elements per page
 */
public record PagedResult<T>(List<T> data, long totalElements, int page, int pageSize) {}
