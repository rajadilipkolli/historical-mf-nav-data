package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for ISIN-based NAV data access */
@Repository
public class NavByIsinRepository implements NavLookupPort {

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseInitializerPort databaseInitializerPort;

  /**
   * Creates an ISIN-based NAV repository with optional on-demand data loading.
   *
   * @param jdbcTemplate the template used to query the Daily NAV database
   * @param databaseInitializerPort the loader invoked before supported ISIN NAV queries, or {@code
   *     null} to query existing data only
   */
  public NavByIsinRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DatabaseInitializerPort databaseInitializerPort) {
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

  /**
   * Finds the latest NAV record for an ISIN after requesting on-demand loading.
   *
   * @param isin the ISIN to search for
   * @return the latest NAV record, or an empty optional if none exists
   */
  public Optional<NavByIsin> findLatestByIsin(String isin) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForIsin(isin);
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin).stream().findFirst();
  }

  /**
   * Finds the latest NAV record for an ISIN on or before the specified date.
   *
   * <p>Requests on-demand loading for the ISIN before querying.
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

  /**
   * Retrieves NAV records for an ISIN within an inclusive date range, ordered from newest to
   * oldest.
   *
   * <p>Requests on-demand loading for the ISIN before querying.
   *
   * @param isin the ISIN to search for
   * @param startDate the beginning of the date range
   * @param endDate the end of the date range
   * @return the matching NAV records
   */
  public List<NavByIsin> findByIsinAndDateBetween(
      String isin, LocalDate startDate, LocalDate endDate) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForIsin(isin);
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, startDate, endDate);
  }
}
