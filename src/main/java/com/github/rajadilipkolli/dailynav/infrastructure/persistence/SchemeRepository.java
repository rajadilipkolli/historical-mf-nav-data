package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** Repository for Scheme data access */
@Repository
public class SchemeRepository implements SchemePort {

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  /**
   * Create a repository for accessing scheme records using the provided JdbcTemplate.
   *
   * @param jdbcTemplate the JdbcTemplate configured with the "dailyNavJdbcTemplate" qualifier used
   *     for database access
   */
  public SchemeRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
  }

  private static final RowMapper<Scheme> SCHEME_ROW_MAPPER =
      (rs, rowNum) ->
          new Scheme(
              rs.getInt("scheme_code"),
              rs.getString("scheme_name"),
              rs.getString("amc"),
              rs.getString("category"),
              rs.getString("plan"),
              rs.getString("option"));

  /** Find scheme by scheme code */
  @Override
  public Optional<Scheme> findBySchemeCode(Integer schemeCode) {
    String sql =
        "SELECT scheme_code, scheme_name, amc, category, plan, option FROM schemes WHERE scheme_code = ?";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, schemeCode).stream().findFirst();
  }

  /** Find all schemes */
  @Override
  public List<Scheme> findAll() {
    String sql =
        "SELECT scheme_code, scheme_name, amc, category, plan, option FROM schemes ORDER BY scheme_name";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER);
  }

  /** Search schemes by name (case-insensitive) */
  @Override
  public List<Scheme> findBySchemeNameContaining(String namePattern) {
    String sql =
        "SELECT scheme_code, scheme_name, amc, category, plan, option FROM schemes WHERE LOWER(scheme_name) LIKE LOWER(?) ORDER BY scheme_name";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, "%" + namePattern + "%");
  }

  @Override
  public Optional<Scheme> findBySchemeName(String name) {
    String sql =
        "SELECT scheme_code, scheme_name, amc, category, plan, option FROM schemes WHERE scheme_name = ?";
    return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, name).stream().findFirst();
  }

  @Override
  public List<Scheme> search(SchemeSearchCriteria criteria) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT scheme_code, scheme_name, amc, category, plan, option FROM schemes ");
    MapSqlParameterSource params = new MapSqlParameterSource();

    appendWhereClause(sql, params, criteria);
    appendOrderAndLimit(sql, criteria);

    return namedParameterJdbcTemplate.query(sql.toString(), params, SCHEME_ROW_MAPPER);
  }

  @Override
  public long count(SchemeSearchCriteria criteria) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM schemes ");
    MapSqlParameterSource params = new MapSqlParameterSource();

    appendWhereClause(sql, params, criteria);

    Long result = namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    return result != null ? result : 0L;
  }

  @Override
  public List<String> findDistinctAmcs() {
    String sql = "SELECT DISTINCT amc FROM schemes WHERE amc IS NOT NULL ORDER BY amc";
    return jdbcTemplate.queryForList(sql, String.class);
  }

  @Override
  public List<String> findDistinctCategories() {
    String sql =
        "SELECT DISTINCT category FROM schemes WHERE category IS NOT NULL ORDER BY category";
    return jdbcTemplate.queryForList(sql, String.class);
  }

  private void appendWhereClause(
      StringBuilder sql, MapSqlParameterSource params, SchemeSearchCriteria criteria) {
    List<String> conditions = new ArrayList<>();

    if (criteria.namePattern() != null && !criteria.namePattern().isBlank()) {
      conditions.add("LOWER(scheme_name) LIKE LOWER(:namePattern)");
      params.addValue("namePattern", "%" + criteria.namePattern() + "%");
    }
    if (criteria.amc() != null && !criteria.amc().isBlank()) {
      conditions.add("amc = :amc");
      params.addValue("amc", criteria.amc());
    }
    if (criteria.category() != null && !criteria.category().isBlank()) {
      conditions.add("category = :category");
      params.addValue("category", criteria.category());
    }
    if (criteria.plan() != null && !criteria.plan().isBlank()) {
      conditions.add("plan = :plan");
      params.addValue("plan", criteria.plan());
    }
    if (criteria.option() != null && !criteria.option().isBlank()) {
      conditions.add("option = :option");
      params.addValue("option", criteria.option());
    }

    if (!conditions.isEmpty()) {
      sql.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
    }
  }

  private void appendOrderAndLimit(StringBuilder sql, SchemeSearchCriteria criteria) {
    if (criteria.sortField() != null && !criteria.sortField().isBlank()) {
      String sortDir = "DESC".equalsIgnoreCase(criteria.sortDirection()) ? "DESC" : "ASC";
      // Prevent SQL injection by allowing only valid sort fields
      String sortField =
          switch (criteria.sortField()) {
            case "scheme_code" -> "scheme_code";
            case "amc" -> "amc";
            case "category" -> "category";
            case "plan" -> "plan";
            case "option" -> "option";
            default -> "scheme_name";
          };
      sql.append("ORDER BY ").append(sortField).append(" ").append(sortDir).append(" ");
    } else {
      sql.append("ORDER BY scheme_name ASC ");
    }

    int limit = criteria.pageSize();
    int offset = criteria.page() * criteria.pageSize();
    sql.append("LIMIT ").append(limit).append(" OFFSET ").append(offset);
  }
}
