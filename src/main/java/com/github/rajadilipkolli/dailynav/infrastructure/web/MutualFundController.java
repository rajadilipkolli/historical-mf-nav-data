package com.github.rajadilipkolli.dailynav.infrastructure.web;

import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.application.port.SecurityPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.model.Security;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MutualFundController {

  private final NavLookupPort navLookupPort;
  private final SchemePort schemePort;
  private final SecurityPort securityPort;

  public MutualFundController(
      NavLookupPort navLookupPort, SchemePort schemePort, SecurityPort securityPort) {
    this.navLookupPort = navLookupPort;
    this.schemePort = schemePort;
    this.securityPort = securityPort;
  }

  @GetMapping("/nav/latest/{isin}")
  public NavByIsin getLatestNav(@PathVariable("isin") String isin) {
    return navLookupPort.findLatestByIsin(isin).orElse(null);
  }

  @GetMapping("/nav/{isin}/{date}")
  public NavByIsin getNavByDate(
      @PathVariable("isin") String isin, @PathVariable("date") LocalDate date) {
    return navLookupPort.findByIsinAndDateOnOrBefore(isin, date).orElse(null);
  }

  @GetMapping("/nav/history/{isin}")
  public List<NavByIsin> getNavHistory(
      @PathVariable("isin") String isin,
      @RequestParam(value = "limit", defaultValue = "30") int limit) {
    return navLookupPort.findLastNByIsin(isin, limit);
  }

  @GetMapping("/nav/range/{isin}")
  public List<NavByIsin> getNavRange(
      @PathVariable("isin") String isin,
      @RequestParam("start") LocalDate start,
      @RequestParam("end") LocalDate end) {
    return navLookupPort.findByIsinAndDateBetween(isin, start, end);
  }

  @GetMapping("/security/{isin}")
  public Security getSecurity(@PathVariable("isin") String isin) {
    return securityPort.findByIsin(isin).orElse(null);
  }

  @GetMapping("/scheme/{code}")
  public Scheme getScheme(@PathVariable("code") Integer code) {
    return schemePort.findBySchemeCode(code).orElse(null);
  }

  @GetMapping("/schemes/search")
  public List<Scheme> searchSchemes(@RequestParam("name") String name) {
    return schemePort.findBySchemeNameContaining(name);
  }

  @GetMapping("/schemes/amcs")
  public List<String> getAmcs() {
    return schemePort.findDistinctAmcs();
  }

  @GetMapping("/schemes/categories")
  public List<String> getCategories() {
    return schemePort.findDistinctCategories();
  }
}
