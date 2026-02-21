package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.Security;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Repository for Security data access */
@Repository
class SecurityRepository {

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  /**
   * Create a SecurityRepository backed by the provided JdbcTemplate.
   *
   * @param jdbcTemplate the JdbcTemplate configured for the daily NAV datasource (bean qualifier
   *     "dailyNavJdbcTemplate") used for repository database operations
   */
  public SecurityRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      @Qualifier("dailyNavNamedParameterJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  private static final RowMapper<Security> SECURITY_ROW_MAPPER =
      (rs, rowNum) -> {
        Security security = new Security();
        security.setIsin(rs.getString("isin"));
        security.setType(rs.getInt("type"));
        security.setSchemeCode(rs.getInt("scheme_code"));
        return security;
      };

  /** Find security by ISIN */
  public Optional<Security> findByIsin(String isin) {
    String sql = "SELECT isin, type, scheme_code FROM securities WHERE isin = ?";
    return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER, isin).stream().findFirst();
  }

  /** Find securities by scheme code */
  public List<Security> findBySchemeCode(Integer schemeCode) {
    String sql = "SELECT isin, type, scheme_code FROM securities WHERE scheme_code = ?";
    return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER, schemeCode);
  }

  /**
   * Find securities by a collection of scheme codes using a single IN-query. Returns an empty list
   * if the provided collection is null or empty.
   */
  public List<Security> findBySchemeCodes(Collection<Integer> schemeCodes) {
    if (schemeCodes == null || schemeCodes.isEmpty()) {
      return List.of();
    }
    String sql =
        "SELECT isin, type, scheme_code FROM securities WHERE scheme_code IN (:schemeCodes)";
    var params = new MapSqlParameterSource("schemeCodes", schemeCodes);
    return namedParameterJdbcTemplate.query(sql, params, SECURITY_ROW_MAPPER);
  }

  /** Find all securities */
  public List<Security> findAll() {
    String sql = "SELECT isin, type, scheme_code FROM securities ORDER BY isin";
    return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER);
  }
}
