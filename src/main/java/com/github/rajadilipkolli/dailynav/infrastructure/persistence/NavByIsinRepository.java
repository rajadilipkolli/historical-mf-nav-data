package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;

/** Repository for ISIN-based NAV data access */
@Repository
public class NavByIsinRepository implements NavLookupPort {

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseInitializerPort databaseInitializerPort;

  public NavByIsinRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DatabaseInitializerPort databaseInitializerPort) {
    this.jdbcTemplate = jdbcTemplate;
    this.databaseInitializerPort = databaseInitializerPort;
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

  /** Get latest NAV for an ISIN */
  public Optional<NavByIsin> findLatestByIsin(String isin) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForIsin(isin);
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin).stream().findFirst();
  }

  /**
   * Finds the latest NAV record for an ISIN on or before the specified date.
   *
   * @param isin the security's ISIN
   * @param date the latest date to include
   * @return the matching NAV record, or an empty optional if none exists
   */
  public Optional<NavByIsin> findByIsinAndDateOnOrBefore(String isin, LocalDate date) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForIsin(isin);
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, date).stream().findFirst();
  }

  /**
   * Retrieves the most recent NAV records for an ISIN.
   *
   * @param isin the ISIN to search for
   * @param limit the maximum number of records to return
   * @return the matching NAV records, ordered from most recent to oldest
   */
  @Override
  public List<NavByIsin> findLastNByIsin(String isin, int limit) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT ?";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, limit);
  }

  /** Get NAV records for an ISIN within a date range */
  public List<NavByIsin> findByIsinAndDateBetween(
      String isin, LocalDate startDate, LocalDate endDate) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForIsin(isin);
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, startDate, endDate);
  }
}
