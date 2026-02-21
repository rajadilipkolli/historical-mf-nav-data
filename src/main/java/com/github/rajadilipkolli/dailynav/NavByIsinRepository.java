package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for ISIN-based NAV data access */
@Repository
class NavByIsinRepository {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Constructs a NavByIsinRepository backed by the provided JdbcTemplate.
   *
   * @param jdbcTemplate the JdbcTemplate qualified as "dailyNavJdbcTemplate" used for database
   *     access
   */
  public NavByIsinRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin).stream().findFirst();
  }

  /** Get NAV for an ISIN on or before a specific date */
  public Optional<NavByIsin> findByIsinAndDateOnOrBefore(String isin, LocalDate date) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, date).stream().findFirst();
  }

  /** Get last N NAV records for an ISIN */
  public List<NavByIsin> findLastNByIsin(String isin, int limit) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? ORDER BY date DESC LIMIT ?";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, limit);
  }

  /** Get NAV records for an ISIN within a date range */
  public List<NavByIsin> findByIsinAndDateBetween(
      String isin, LocalDate startDate, LocalDate endDate) {
    String sql =
        "SELECT isin, date, nav FROM nav_by_isin WHERE isin = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_BY_ISIN_ROW_MAPPER, isin, startDate, endDate);
  }
}
