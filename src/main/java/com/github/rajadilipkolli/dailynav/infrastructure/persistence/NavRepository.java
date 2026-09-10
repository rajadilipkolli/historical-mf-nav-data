package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavPort;
import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for NAV data access */
@Repository
public class NavRepository implements NavPort {

  private final JdbcTemplate jdbcTemplate;
  private final DatabaseInitializerPort databaseInitializerPort;

  /**
   * Creates a NAV repository with optional on-demand data loading.
   *
   * @param jdbcTemplate the template used to query the Daily NAV database
   * @param databaseInitializerPort the loader invoked before scheme NAV queries, or {@code null} to
   *     query existing data only
   */
  public NavRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DatabaseInitializerPort databaseInitializerPort) {
    this.jdbcTemplate = jdbcTemplate;
    this.databaseInitializerPort = databaseInitializerPort;
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

  /**
   * Retrieves all NAV records for a scheme, ordered from newest to oldest.
   *
   * <p>Requests on-demand loading for the scheme before querying.
   *
   * @param schemeCode the scheme code used to select NAV records
   * @return the matching NAV records
   */
  public List<Nav> findBySchemeCode(Integer schemeCode) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForScheme(schemeCode);
    String sql = "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode);
  }

  /**
   * Retrieves NAV records for a scheme within an inclusive date range, ordered from newest to
   * oldest.
   *
   * <p>Requests on-demand loading for the scheme before querying.
   *
   * @param schemeCode the scheme code
   * @param startDate the beginning of the date range
   * @param endDate the end of the date range
   * @return the matching NAV records
   */
  public List<Nav> findBySchemeCodeAndDateBetween(
      Integer schemeCode, LocalDate startDate, LocalDate endDate) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForScheme(schemeCode);
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, startDate, endDate);
  }

  /**
   * Finds the latest NAV record for a scheme.
   *
   * <p>Requests on-demand loading for the scheme before querying.
   *
   * @param schemeCode the scheme code to search for
   * @return the latest NAV record, or an empty optional if no record exists
   */
  public Optional<Nav> findLatestBySchemeCode(Integer schemeCode) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForScheme(schemeCode);
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode).stream().findFirst();
  }

  /**
   * Finds the latest NAV record for a scheme on or before the specified date.
   *
   * <p>Requests on-demand loading for the scheme before querying.
   *
   * @param schemeCode the scheme code
   * @param date the inclusive upper date bound
   * @return the latest matching NAV record, or an empty optional if none exists
   */
  public Optional<Nav> findBySchemeCodeAndDateOnOrBefore(Integer schemeCode, LocalDate date) {
    if (databaseInitializerPort != null) databaseInitializerPort.seedNavForScheme(schemeCode);
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, date).stream().findFirst();
  }
}
