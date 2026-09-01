package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;

/** Service for mutual fund data operations */
public class MutualFundService {

  private final NavByIsinRepository navByIsinRepository;
  private final NavRepository navRepository;
  private final SchemeRepository schemeRepository;
  private final SecurityRepository securityRepository;
  private final DatabaseInitializer databaseInitializer;

  /**
   * Creates a service using the repositories and database initializer required for mutual-fund data
   * access.
   *
   * @param navByIsinRepository repository for NAV records indexed by ISIN
   * @param navRepository repository for NAV records indexed by scheme code
   * @param schemeRepository repository for mutual-fund schemes
   * @param securityRepository repository for mutual-fund securities
   * @param databaseInitializer component that tracks database initialization
   */
  public MutualFundService(
          NavByIsinRepository navByIsinRepository,
          NavRepository navRepository,
          SchemeRepository schemeRepository,
          SecurityRepository securityRepository,
          DatabaseInitializer databaseInitializer) {
    this.navByIsinRepository = navByIsinRepository;
    this.navRepository = navRepository;
    this.schemeRepository = schemeRepository;
    this.securityRepository = securityRepository;
    this.databaseInitializer = databaseInitializer;
  }

  @Autowired @Lazy private MutualFundService self;

  /**
   * Checks if the database is ready for queries.
   *
   * @return true if initialization is complete
   */
  public boolean isReady() {
    return databaseInitializer.isInitialized();
  }

  /**
   * Get latest NAV by ISIN, throwing an exception if not found.
   *
   * @param isin the ISIN to look up
   * @return the latest NAV record
   * @throws NoSuchElementException if no NAV data is found for the given ISIN
   */
  public NavByIsin getLatestNavByIsinOrThrow(String isin) {
    MutualFundService target = self != null ? self : this;
    return target
            .getLatestNavByIsin(isin)
            .orElseThrow(() -> new NoSuchElementException("No NAV data found for ISIN: " + isin));
  }

  /**
   * Find ISINs for a given scheme name pattern.
   *
   * @param namePattern the name pattern to search for
   * @return list of matching ISINs
   */
  public List<String> findIsinsBySchemeName(String namePattern) {
    List<Scheme> schemes = searchSchemes(namePattern);
    if (schemes.isEmpty()) {
      return List.of();
    }
    var schemeCodes = schemes.stream().map(Scheme::schemeCode).toList();
    return securityRepository.findBySchemeCodes(schemeCodes).stream()
        .map(Security::getIsin)
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Get latest NAV by ISIN
   *
   * @param isin the ISIN to look up
   * @return an Optional containing the latest NAV record, or empty if not found
   */
  @Cacheable(
      cacheNames = "latestNav",
      cacheManager = "dailyNavCacheManager",
      unless = "#result == null")
  public Optional<NavByIsin> getLatestNavByIsin(String isin) {
    return navByIsinRepository.findLatestByIsin(isin);
  }

  /**
   * Get NAV by ISIN for a specific date (or closest date before)
   *
   * @param isin the ISIN to look up
   * @param date the date to search for (or the closest date before)
   * @return an Optional containing the NAV record, or empty if not found
   */
  public Optional<NavByIsin> getNavByIsinAndDate(String isin, LocalDate date) {
    return navByIsinRepository.findByIsinAndDateOnOrBefore(isin, date);
  }

  /**
   * Get last N days NAV for an ISIN
   *
   * @param isin the ISIN to look up
   * @param days the number of days of NAV data to retrieve
   * @return a list of NAV records for the last N days
   */
  public List<NavByIsin> getLastNDaysNav(String isin, int days) {
    return navByIsinRepository.findLastNByIsin(isin, days);
  }

  /**
   * Get NAV history for ISIN within date range
   *
   * @param isin the ISIN to look up
   * @param startDate the start date of the range (inclusive)
   * @param endDate the end date of the range (inclusive)
   * @return a list of NAV records within the specified date range
   */
  public List<NavByIsin> getNavHistory(String isin, LocalDate startDate, LocalDate endDate) {
    return navByIsinRepository.findByIsinAndDateBetween(isin, startDate, endDate);
  }

  /** Get Navs information by scheme code */
  public List<Nav> getNavsBySchemeCode(Integer schemeCode) {
    return navRepository.findBySchemeCode(schemeCode);
  }

  /**
   * Retrieves scheme information by scheme code.
   *
   * @param schemeCode the scheme code to search for
   * @return the matching scheme, if available
   */
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

  /**
   * Helper class to combine security and scheme information
   *
   * @param security the security information
   * @param scheme the scheme information
   */
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
    @NonNull
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
