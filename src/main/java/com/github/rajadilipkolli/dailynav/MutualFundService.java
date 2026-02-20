package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Service for mutual fund data operations */
@Service
public class MutualFundService {

  private final NavByIsinRepository navByIsinRepository;
  private final SchemeRepository schemeRepository;
  private final SecurityRepository securityRepository;

  public MutualFundService(
      NavByIsinRepository navByIsinRepository,
      SchemeRepository schemeRepository,
      SecurityRepository securityRepository) {
    this.navByIsinRepository = navByIsinRepository;
    this.schemeRepository = schemeRepository;
    this.securityRepository = securityRepository;
  }

  /** Get latest NAV by ISIN */
  public Optional<NavByIsin> getLatestNavByIsin(String isin) {
    return navByIsinRepository.findLatestByIsin(isin);
  }

  /** Get NAV by ISIN for a specific date (or closest date before) */
  public Optional<NavByIsin> getNavByIsinAndDate(String isin, LocalDate date) {
    return navByIsinRepository.findByIsinAndDateOnOrBefore(isin, date);
  }

  /** Get last N days NAV for an ISIN */
  public List<NavByIsin> getLastNDaysNav(String isin, int days) {
    return navByIsinRepository.findLastNByIsin(isin, days);
  }

  /** Get NAV history for ISIN within date range */
  public List<NavByIsin> getNavHistory(String isin, LocalDate startDate, LocalDate endDate) {
    return navByIsinRepository.findByIsinAndDateBetween(isin, startDate, endDate);
  }

  /** Get scheme information by scheme code */
  public Optional<Scheme> getScheme(Integer schemeCode) {
    return schemeRepository.findBySchemeCode(schemeCode);
  }

  /** Search schemes by name */
  public List<Scheme> searchSchemes(String namePattern) {
    return schemeRepository.findBySchemeNameContaining(namePattern);
  }

  /** Get all schemes */
  public List<Scheme> getAllSchemes() {
    return schemeRepository.findAll();
  }

  /** Get security information by ISIN */
  public Optional<Security> getSecurity(String isin) {
    return securityRepository.findByIsin(isin);
  }

  /** Get complete fund information (scheme + security) by ISIN */
  public Optional<FundInfo> getFundInfo(String isin) {
    Optional<Security> security = getSecurity(isin);
    if (security.isEmpty()) {
      return Optional.empty();
    }

    Security security1 = security.get();
    Optional<Scheme> scheme = getScheme(security1.getSchemeCode());
    return scheme.map(value -> new FundInfo(security1, value));
  }

  /** Helper class to combine security and scheme information */
  public record FundInfo(Security security, Scheme scheme) {

    public String getIsin() {
      return security.getIsin();
    }

    public String getSchemeName() {
      return scheme.schemeName();
    }

    public Integer getSchemeCode() {
      return security.getSchemeCode();
    }

    public Integer getType() {
      return security.getType();
    }

    public String getTypeDescription() {
      if (security == null || security.getType() == null) {
        return "Unknown";
      }
      return security.getType() == 0 ? "Growth/Dividend Payout" : "Dividend Reinvestment";
    }

    @Override
    public String toString() {
      return "FundInfo{"
          + "isin='"
          + getIsin()
          + '\''
          + ", schemeName='"
          + getSchemeName()
          + '\''
          + ", type='"
          + getTypeDescription()
          + '\''
          + '}';
    }
  }
}
