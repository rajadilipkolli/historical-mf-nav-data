package com.github.rajadilipkolli.dailynav.repository;

import com.github.rajadilipkolli.dailynav.model.Scheme;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository for Scheme data access
 */
@Repository
public class SchemeRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public SchemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private static final RowMapper<Scheme> SCHEME_ROW_MAPPER = new RowMapper<Scheme>() {
        @Override
        public Scheme mapRow(ResultSet rs, int rowNum) throws SQLException {
            Scheme scheme = new Scheme();
            scheme.setSchemeCode(rs.getInt("scheme_code"));
            scheme.setSchemeName(rs.getString("scheme_name"));
            return scheme;
        }
    };
    
    /**
     * Find scheme by scheme code
     */
    public Scheme findBySchemeCode(Integer schemeCode) {
        String sql = "SELECT scheme_code, scheme_name FROM schemes WHERE scheme_code = ?";
        List<Scheme> results = jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, schemeCode);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Find all schemes
     */
    public List<Scheme> findAll() {
        String sql = "SELECT scheme_code, scheme_name FROM schemes ORDER BY scheme_name";
        return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER);
    }
    
    /**
     * Search schemes by name (case-insensitive)
     */
    public List<Scheme> findBySchemeNameContaining(String namePattern) {
        String sql = "SELECT scheme_code, scheme_name FROM schemes WHERE LOWER(scheme_name) LIKE LOWER(?) ORDER BY scheme_name";
        return jdbcTemplate.query(sql, SCHEME_ROW_MAPPER, "%" + namePattern + "%");
    }
}
