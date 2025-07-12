package com.github.rajadilipkolli.dailynav.repository;

import com.github.rajadilipkolli.dailynav.model.Nav;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for NAV data access */
@Repository
public class NavRepository {

  private final JdbcTemplate jdbcTemplate;

  public NavRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final RowMapper<Nav> NAV_ROW_MAPPER =
      new RowMapper<Nav>() {
        @Override
        public Nav mapRow(ResultSet rs, int rowNum) throws SQLException {
          Nav nav = new Nav();
          nav.setSchemeCode(rs.getInt("scheme_code"));
          nav.setDate(rs.getDate("date").toLocalDate());
          nav.setNav(rs.getDouble("nav"));
          return nav;
        }
      };

  /** Get all NAV records for a specific scheme code */
  public List<Nav> findBySchemeCode(Integer schemeCode) {
    String sql = "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode);
  }

  /** Get NAV records for a specific scheme code and date range */
  public List<Nav> findBySchemeCodeAndDateBetween(
      Integer schemeCode, LocalDate startDate, LocalDate endDate) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date BETWEEN ? AND ? ORDER BY date DESC";
    return jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, startDate, endDate);
  }

  /** Get latest NAV for a specific scheme code */
  public Nav findLatestBySchemeCode(Integer schemeCode) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? ORDER BY date DESC LIMIT 1";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode);
    return results.isEmpty() ? null : results.get(0);
  }

  /** Get NAV on or before a specific date for a scheme code */
  public Nav findBySchemeCodeAndDateOnOrBefore(Integer schemeCode, LocalDate date) {
    String sql =
        "SELECT scheme_code, date, nav FROM nav WHERE scheme_code = ? AND date <= ? ORDER BY date DESC LIMIT 1";
    List<Nav> results = jdbcTemplate.query(sql, NAV_ROW_MAPPER, schemeCode, date);
    return results.isEmpty() ? null : results.get(0);
  }
}
