package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavPort;
import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for NAV data access */
@Repository
public class NavRepository implements NavPort {

  private static final Logger logger = LoggerFactory.getLogger(NavRepository.class);

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseInitializerPort databaseInitializerPort;
  private final MeterRegistry meterRegistry;

  /**
   * Creates a NAV repository with optional on-demand data loading.
   *
   * @param jdbcTemplate the template used to query the Daily NAV database
   * @param databaseInitializerPort the loader invoked before scheme NAV queries, or {@code null} to
   *     query existing data only
   * @param meterRegistry optional meter registry for recording metrics
   */
  public NavRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DatabaseInitializerPort databaseInitializerPort,
      MeterRegistry meterRegistry) {
    this.jdbcTemplate = jdbcTemplate;
    this.databaseInitializerPort = databaseInitializerPort;
    this.meterRegistry = meterRegistry;
  }

  private void recordHit() {
    if (meterRegistry != null) meterRegistry.counter("daily_nav_cache_hit").increment();
    logger.info("PostgreSQL cache hit");
  }

  private void recordMissAndFallback() {
    if (meterRegistry != null) {
      meterRegistry.counter("daily_nav_cache_miss").increment();
      meterRegistry.counter("daily_nav_sqlite_fallback").increment();
    }
    logger.info("PostgreSQL cache miss");
    logger.info("SQLite fallback");
  }

  private static final RowMapper<Nav> NAV_ROW_MAPPER =
      (rs, rowNum) -> {
        Nav nav = new Nav();
        nav.setSchemeCode(rs.getInt("scheme_code"));
        String dateStr = rs.getString("date");
        if (dateStr == null) {
          nav.setDate(null);
        } else {
          nav.setDate(LocalDate.parse(dateStr));
        }
        nav.setNav(rs.getDouble("nav") / 10000.0);
        return nav;
      };

  public List<Nav> findBySchemeCode(Integer schemeCode) {
    String sql = "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode);
    if (!results.isEmpty()
        || (databaseInitializerPort != null
            && databaseInitializerPort.hasNavForScheme(schemeCode))) {
      recordHit();
      return results;
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<Nav> fallback = databaseInitializerPort.getFallbackNavForScheme(schemeCode);
      databaseInitializerPort.seedNavForScheme(schemeCode);
      return fallback;
    }
    return results;
  }

  public List<Nav> findBySchemeCodeAndDateBetween(
      Integer schemeCode, LocalDate startDate, LocalDate endDate) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, startDate, endDate);
    if (!results.isEmpty()
        || (databaseInitializerPort != null
            && databaseInitializerPort.hasNavForScheme(schemeCode))) {
      recordHit();
      return results;
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<Nav> fallback =
          databaseInitializerPort.getFallbackNavForScheme(schemeCode).stream()
              .filter(n -> !n.getDate().isBefore(startDate) && !n.getDate().isAfter(endDate))
              .toList();
      databaseInitializerPort.seedNavForScheme(schemeCode);
      return fallback;
    }
    return results;
  }

  public Optional<Nav> findLatestBySchemeCode(Integer schemeCode) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC LIMIT 1";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode);
    if (!results.isEmpty()
        || (databaseInitializerPort != null
            && databaseInitializerPort.hasNavForScheme(schemeCode))) {
      recordHit();
      return results.stream().findFirst();
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<Nav> fallback = databaseInitializerPort.getFallbackNavForScheme(schemeCode);
      databaseInitializerPort.seedNavForScheme(schemeCode);
      return fallback.stream().findFirst();
    }
    return Optional.empty();
  }

  public Optional<Nav> findBySchemeCodeAndDateOnOrBefore(Integer schemeCode, LocalDate date) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, date);
    if (!results.isEmpty()
        || (databaseInitializerPort != null
            && databaseInitializerPort.hasNavForScheme(schemeCode))) {
      recordHit();
      return results.stream().findFirst();
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<Nav> fallback =
          databaseInitializerPort.getFallbackNavForScheme(schemeCode).stream()
              .filter(n -> !n.getDate().isAfter(date))
              .toList();
      databaseInitializerPort.seedNavForScheme(schemeCode);
      return fallback.stream().findFirst();
    }
    return Optional.empty();
  }
}
