package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Repository for Scheme data access */
@Repository
class SchemeRepository {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Create a repository for accessing scheme records using the provided JdbcTemplate.
   *
   * @param jdbcTemplate the JdbcTemplate configured with the "dailyNavJdbcTemplate" qualifier used for database access
   */
  public SchemeRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private static final RowMapper<Scheme> SCHEME_ROW_MAPPER =
      (rs, rowNum) -> new Scheme(rs.getInt("scheme_code"), rs.getString("scheme_name"));

  /** Find scheme by scheme code */
  public Optional<Scheme> findBySchemeCode(Integer schemeCode) {
    String sql = "SELECT scheme_code, scheme_name FROM schemes WHERE scheme_code = ?";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, schemeCode).stream().findFirst();
  }

  /** Find all schemes */
  public List<Scheme> findAll() {
    String sql = "SELECT scheme_code, scheme_name FROM schemes ORDER BY scheme_name";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER);
  }

  /** Search schemes by name (case-insensitive) */
  public List<Scheme> findBySchemeNameContaining(String namePattern) {
    String sql =
        "SELECT scheme_code, scheme_name FROM schemes WHERE LOWER(scheme_name) LIKE LOWER(?) ORDER BY scheme_name";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, "%" + namePattern + "%");
  }
}