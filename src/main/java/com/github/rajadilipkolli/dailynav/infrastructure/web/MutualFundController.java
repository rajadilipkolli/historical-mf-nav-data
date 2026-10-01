package com.github.rajadilipkolli.dailynav.infrastructure.web;

import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.application.port.SecurityPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.model.Security;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MutualFundController {

  private static final int MAX_SEARCH_PAGE_SIZE = 100;

  private final NavLookupPort navLookupPort;
  private final SchemePort schemePort;
  private final SecurityPort securityPort;

  /**
   * Creates a controller backed by the NAV, scheme, and security lookup ports.
   *
   * @param navLookupPort port for NAV lookups by ISIN
   * @param schemePort port for scheme details and classifications
   * @param securityPort port for security lookups by ISIN
   */
  public MutualFundController(
      NavLookupPort navLookupPort, SchemePort schemePort, SecurityPort securityPort) {
    this.navLookupPort = navLookupPort;
    this.schemePort = schemePort;
    this.securityPort = securityPort;
  }

  /**
   * Retrieves the latest available NAV for an ISIN.
   *
   * @param isin the international securities identification number
   * @return HTTP 200 with the latest NAV record, or HTTP 404 if none is available
   */
  @GetMapping("/nav/latest/{isin}")
  public ResponseEntity<NavByIsin> getLatestNav(@PathVariable("isin") String isin) {
    return ResponseEntity.of(navLookupPort.findLatestByIsin(isin));
  }

  /**
   * Retrieves the most recent NAV for an ISIN on or before the requested date.
   *
   * @param isin the international securities identification number
   * @param date the latest eligible date
   * @return HTTP 200 with the matching NAV record, or HTTP 404 if none is available
   */
  @GetMapping("/nav/{isin}/{date}")
  public ResponseEntity<NavByIsin> getNavByDate(
      @PathVariable("isin") String isin, @PathVariable("date") LocalDate date) {
    return ResponseEntity.of(navLookupPort.findByIsinAndDateOnOrBefore(isin, date));
  }

  /**
   * Retrieves the most recent NAV records for an ISIN, up to the requested limit.
   *
   * @param isin the international securities identification number
   * @param limit the maximum number of records to return; defaults to 30 for HTTP requests
   * @return the matching NAV records, or an empty list if none are available
   */
  @GetMapping("/nav/history/{isin}")
  public List<NavByIsin> getNavHistory(
      @PathVariable("isin") String isin,
      @RequestParam(value = "limit", defaultValue = "30") int limit) {
    return navLookupPort.findLastNByIsin(isin, limit);
  }

  /**
   * Retrieves NAV records for an ISIN within an inclusive date range.
   *
   * @param isin the international securities identification number
   * @param start the first eligible date, inclusive
   * @param end the last eligible date, inclusive
   * @return the matching NAV records, or an empty list if none are available
   */
  @GetMapping("/nav/range/{isin}")
  public List<NavByIsin> getNavRange(
      @PathVariable("isin") String isin,
      @RequestParam("start") LocalDate start,
      @RequestParam("end") LocalDate end) {
    return navLookupPort.findByIsinAndDateBetween(isin, start, end);
  }

  /**
   * Retrieves security details for an ISIN.
   *
   * @param isin the international securities identification number
   * @return HTTP 200 with the matching security, or HTTP 404 if none is found
   */
  @GetMapping("/security/{isin}")
  public ResponseEntity<Security> getSecurity(@PathVariable("isin") String isin) {
    return ResponseEntity.of(securityPort.findByIsin(isin));
  }

  /**
   * Retrieves scheme details by scheme code.
   *
   * @param code the scheme code to look up
   * @return HTTP 200 with the matching scheme, or HTTP 404 if none is found
   */
  @GetMapping("/scheme/{code}")
  public ResponseEntity<Scheme> getScheme(@PathVariable("code") Integer code) {
    return ResponseEntity.of(schemePort.findBySchemeCode(code));
  }

  /**
   * Searches for schemes whose names contain the supplied pattern.
   *
   * @param name the pattern to search for within scheme names
   * @param page the zero-based page number; negative values use zero
   * @param pageSize the requested page size; defaults to 20 and is capped at 100
   * @return the matching page of schemes, or an empty list if none are found
   */
  @GetMapping("/schemes/search")
  public List<Scheme> searchSchemes(
      @RequestParam("name") String name,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
    SchemeSearchCriteria criteria =
        new SchemeSearchCriteria(
            name,
            null,
            null,
            null,
            null,
            null,
            null,
            page,
            Math.min(pageSize, MAX_SEARCH_PAGE_SIZE));
    return schemePort.search(criteria);
  }

  /**
   * Retrieves the distinct asset management company names available in the scheme data.
   *
   * @return the sorted AMC names, or an empty list if none are available
   */
  @GetMapping("/schemes/amcs")
  public List<String> getAmcs() {
    return schemePort.findDistinctAmcs();
  }

  /**
   * Retrieves the distinct fund categories available in the scheme data.
   *
   * @return the sorted category names, or an empty list if none are available
   */
  @GetMapping("/schemes/categories")
  public List<String> getCategories() {
    return schemePort.findDistinctCategories();
  }
}
