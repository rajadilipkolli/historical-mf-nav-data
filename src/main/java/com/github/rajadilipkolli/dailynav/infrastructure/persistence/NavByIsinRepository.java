package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for ISIN-based NAV data access */
@Repository
public class NavByIsinRepository implements NavLookupPort {

  private static final Logger logger = LoggerFactory.getLogger(NavByIsinRepository.class);

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseInitializerPort databaseInitializerPort;
  private final MeterRegistry meterRegistry;

  /**
   * Creates an ISIN-based NAV repository with optional on-demand data loading.
   *
   * @param jdbcTemplate the template used to query the Daily NAV database
   * @param databaseInitializerPort the loader invoked before supported ISIN NAV queries, or {@code
   *     null} to query existing data only
   * @param meterRegistry optional meter registry for recording metrics
   */
  public NavByIsinRepository(
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

  private static final RowMapper<NavByIsin> NAV_BY_ISIN_ROW_MAPPER =
      (rs, rowNum) -> {
        NavByIsin nav = new NavByIsin();
        nav.setIsin(rs.getString("isin"));
        String dateStr = rs.getString("date");
        if (dateStr == null) {
          nav.setDate(null);
        } else {
          nav.setDate(LocalDate.parse(dateStr));
        }
        nav.setNav(rs.getDouble("nav"));
        return nav;
      };

  public Optional<NavByIsin> findLatestByIsin(String isin) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT 1";
    List<NavByIsin> results = jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin);
    if (!results.isEmpty()) {
      recordHit();
      return results.stream().findFirst();
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<NavByIsin> fallback = databaseInitializerPort.getFallbackNavForIsin(isin);
      databaseInitializerPort.seedNavForIsin(isin);
      return fallback.stream().findFirst();
    }
    return Optional.empty();
  }

  public Optional<NavByIsin> findByIsinAndDateOnOrBefore(String isin, LocalDate date) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    List<NavByIsin> results = jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, date);
    if (!results.isEmpty()) {
      recordHit();
      return results.stream().findFirst();
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<NavByIsin> fallback =
          databaseInitializerPort.getFallbackNavForIsin(isin).stream()
              .filter(n -> !n.getDate().isAfter(date))
              .collect(Collectors.toList());
      databaseInitializerPort.seedNavForIsin(isin);
      return fallback.stream().findFirst();
    }
    return Optional.empty();
  }

  @Override
  public List<NavByIsin> findLastNByIsin(String isin, int limit) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT ?";
    List<NavByIsin> results = jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, limit);
    if (!results.isEmpty()) {
      recordHit();
      return results;
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<NavByIsin> fallback =
          databaseInitializerPort.getFallbackNavForIsin(isin).stream()
              .limit(limit)
              .collect(Collectors.toList());
      databaseInitializerPort.seedNavForIsin(isin);
      return fallback;
    }
    return results;
  }

  public List<NavByIsin> findByIsinAndDateBetween(
      String isin, LocalDate startDate, LocalDate endDate) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    List<NavByIsin> results =
        jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, startDate, endDate);
    if (!results.isEmpty()) {
      recordHit();
      return results;
    }

    if (databaseInitializerPort != null) {
      recordMissAndFallback();
      List<NavByIsin> fallback =
          databaseInitializerPort.getFallbackNavForIsin(isin).stream()
              .filter(n -> !n.getDate().isBefore(startDate) && !n.getDate().isAfter(endDate))
              .collect(Collectors.toList());
      databaseInitializerPort.seedNavForIsin(isin);
      return fallback;
    }
    return results;
  }
}
