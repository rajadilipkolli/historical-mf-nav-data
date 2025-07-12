package com.github.rajadilipkolli.dailynav.repository;

import com.github.rajadilipkolli.dailynav.model.Security;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Repository for Security data access
 */
@Repository
public class SecurityRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public SecurityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private static final RowMapper<Security> SECURITY_ROW_MAPPER = new RowMapper<Security>() {
        @Override
        public Security mapRow(ResultSet rs, int rowNum) throws SQLException {
            Security security = new Security();
            security.setIsin(rs.getString("isin"));
            security.setType(rs.getInt("type"));
            security.setSchemeCode(rs.getInt("scheme_code"));
            return security;
        }
    };
    
    /**
     * Find security by ISIN
     */
    public Security findByIsin(String isin) {
        String sql = "SELECT isin, type, scheme_code FROM securities WHERE isin = ?";
        List<Security> results = jdbcTemplate.query(sql, SECURITY_ROW_MAPPER, isin);
        return results.isEmpty() ? null : results.get(0);
    }
    
    /**
     * Find securities by scheme code
     */
    public List<Security> findBySchemeCode(Integer schemeCode) {
        String sql = "SELECT isin, type, scheme_code FROM securities WHERE scheme_code = ?";
        return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER, schemeCode);
    }
    
    /**
     * Find all securities
     */
    public List<Security> findAll() {
        String sql = "SELECT isin, type, scheme_code FROM securities ORDER BY isin";
        return jdbcTemplate.query(sql, SECURITY_ROW_MAPPER);
    }
}
