package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.Security;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for Security data access */
@Repository
class SecurityRepository {

  private final JdbcTemplate jdbcTemplate;

  public SecurityRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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

  /** Find all securities */
  public List<Security> findAll() {
    String sql = "SELECT isin, type, scheme_code FROM securities ORDER BY isin";
    return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER);
  }
}
