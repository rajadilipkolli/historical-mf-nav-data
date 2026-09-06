package com.github.rajadilipkolli.dailynav.application.service;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.application.port.NavPort;
import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.application.port.SecurityPort;
import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.model.Security;
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

  private final NavLookupPort navLookupPort;
  private final NavPort navPort;
  private final SchemePort schemePort;
  private final SecurityPort securityPort;
  private final DatabaseInitializerPort databaseInitializerPort;

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
      NavLookupPort navLookupPort,
      NavPort navPort,
      SchemePort schemePort,
      SecurityPort securityPort,
      DatabaseInitializerPort databaseInitializerPort) {
    this.navLookupPort = navLookupPort;
    this.navPort = navPort;
    this.schemePort = schemePort;
    this.securityPort = securityPort;
    this.databaseInitializerPort = databaseInitializerPort;
  }

  @Autowired @Lazy private MutualFundService self;

  /**
   * Checks whether the database initialization is complete.
   *
   * @return {@code true} if initialization is complete, {@code false} otherwise
   */
  public boolean isReady() {
    return databaseInitializerPort.isInitialized();
  }

  /**
   * Retrieves the latest NAV record for an ISIN.
   *
   * @param isin the ISIN to look up
   * @return the latest NAV record
   * @throws NoSuchElementException if no NAV data is found for the ISIN
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
    return securityPort.findIsinsBySchemeNamePattern(namePattern).stream()
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
    return navLookupPort.findLatestByIsin(isin);
  }

  /**
   * Retrieves the NAV for an ISIN on the specified date or the closest earlier date.
   *
   * @param isin the ISIN to look up
   * @param date the date for which to retrieve the NAV
   * @return the matching NAV record, or an empty {@code Optional} if none is found
   */
  public Optional<NavByIsin> getNavByIsinAndDate(String isin, LocalDate date) {
    return navLookupPort.findByIsinAndDateOnOrBefore(isin, date);
  }

  /**
   * Get last N days NAV for an ISIN
   *
   * @param isin the ISIN to look up
   * @param days the number of days of NAV data to retrieve
   * @return a list of NAV records for the last N days
   */
  public List<NavByIsin> getLastNDaysNav(String isin, int days) {
    return navLookupPort.findLastNByIsin(isin, days);
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
    return navLookupPort.findByIsinAndDateBetween(isin, startDate, endDate);
  }

  /**
   * Retrieves NAV records for a scheme code.
   *
   * @param schemeCode the scheme code used to find NAV records
   * @return the NAV records associated with the scheme code
   */
  public List<Nav> getNavsBySchemeCode(Integer schemeCode) {
    return navPort.findBySchemeCode(schemeCode);
  }

  /**
   * Retrieves scheme information by scheme code.
   *
   * @param schemeCode the scheme code to search for
   * @return the matching scheme, if available
   */
  public Optional<Scheme> getScheme(Integer schemeCode) {
    return schemePort.findBySchemeCode(schemeCode);
  }

  /**
   * Finds schemes whose names contain the specified pattern.
   *
   * @param namePattern the name pattern to search for
   * @return schemes whose names contain the pattern
   */
  public List<Scheme> searchSchemes(String namePattern) {
    return schemePort.findBySchemeNameContaining(namePattern);
  }

  /**
   * Retrieves all mutual-fund schemes.
   *
   * @return all available schemes
   */
  public List<Scheme> getAllSchemes() {
    return schemePort.findAll();
  }

  /** Get security information by ISIN */
  public Optional<Security> getSecurity(String isin) {
    return securityPort.findByIsin(isin);
  }

  /**
   * Retrieves complete fund information for an ISIN, including its security and scheme.
   *
   * @param isin the ISIN identifying the fund
   * @return the fund information when both the security and associated scheme are available;
   *     otherwise, an empty optional
   */
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

    /**
     * Retrieves the security's ISIN.
     *
     * @return the security's ISIN
     */
    public String getIsin() {
      return security.getIsin();
    }

    /**
     * Gets the associated scheme name.
     *
     * @return the scheme name
     */
    public String getSchemeName() {
      return scheme.schemeName();
    }

    /**
     * Retrieves the scheme code associated with this security.
     *
     * @return the security's scheme code
     */
    public Integer getSchemeCode() {
      return security.getSchemeCode();
    }

    /**
     * Gets the security type associated with this fund.
     *
     * @return the security type
     */
    public Integer getType() {
      return security.getType();
    }

    /**
     * Describes the security type associated with the fund.
     *
     * @return "Unknown" when the security or its type is null, "Growth/Dividend Payout" for type 0,
     *     or "Dividend Reinvestment" for other type values
     */
    public String getTypeDescription() {
      if (security == null || security.getType() == null) {
        return "Unknown";
      }
      return security.getType() == 0 ? "Growth/Dividend Payout" : "Dividend Reinvestment";
    }

    /**
     * Formats the fund information as a string containing its ISIN, scheme name, and type
     * description.
     *
     * @return the formatted fund information
     */
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
