package com.github.rajadilipkolli.dailynav.health;

import com.github.rajadilipkolli.dailynav.config.DailyNavProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple health check endpoint for Daily NAV library when Spring Boot Actuator is not available
 * Provides basic health information about the embedded database
 */
@RestController
@RequestMapping("/daily-nav")
@ConditionalOnWebApplication
@ConditionalOnMissingClass("org.springframework.boot.actuator.health.HealthIndicator")
public class DailyNavHealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(DailyNavHealthController.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final DailyNavProperties properties;
    
    public DailyNavHealthController(JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }
    
    /**
     * Basic health check endpoint
     * Returns HTTP 200 if the database is accessible, HTTP 503 otherwise
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            
            // Check database connectivity
            boolean isDatabaseAccessible = checkDatabaseConnectivity();
            if (!isDatabaseAccessible) {
                response.put("status", "DOWN");
                response.put("database", "Not accessible");
                return ResponseEntity.status(503).body(response);
            }
            
            response.put("status", "UP");
            response.put("database", "Accessible");
            
            // Add basic data counts
            try {
                Integer schemes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
                Integer navRecords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nav", Integer.class);
                Integer securities = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM securities", Integer.class);
                
                response.put("schemes", schemes);
                response.put("navRecords", navRecords);
                response.put("securities", securities);
                
                // Check data freshness
                String latestDate = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
                response.put("latestDataDate", latestDate);
                
                // Check if data is stale
                if (isDataStale(latestDate)) {
                    response.put("warning", "Data may be stale");
                }
                
            } catch (Exception e) {
                logger.debug("Failed to get additional health details", e);
                response.put("warning", "Could not retrieve detailed statistics");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(503).body(errorResponse);
        }
    }
    
    /**
     * Detailed information about the Daily NAV library
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        
        try {
            // Configuration information
            info.put("autoInit", properties.isAutoInit());
            info.put("indexesEnabled", properties.isCreateIndexes());
            info.put("databasePath", properties.getDatabasePath());
            info.put("debugMode", properties.isDebug());
            
            // Data statistics
            try {
                String minDate = jdbcTemplate.queryForObject("SELECT MIN(date) FROM nav", String.class);
                String maxDate = jdbcTemplate.queryForObject("SELECT MAX(date) FROM nav", String.class);
                
                info.put("dataStartDate", minDate);
                info.put("dataEndDate", maxDate);
                
                if (minDate != null && maxDate != null) {
                    LocalDate start = LocalDate.parse(minDate);
                    LocalDate end = LocalDate.parse(maxDate);
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                    info.put("dataSpanDays", daysBetween);
                }
                
                // Sample scheme names
                var sampleSchemes = jdbcTemplate.queryForList(
                    "SELECT scheme_name FROM schemes LIMIT 5", String.class);
                info.put("sampleSchemes", sampleSchemes);
                
            } catch (Exception e) {
                logger.debug("Failed to get detailed info", e);
                info.put("error", "Could not retrieve detailed information");
            }
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("Info endpoint failed", e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private boolean checkDatabaseConnectivity() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.debug("Database connectivity check failed", e);
            return false;
        }
    }
    
    private boolean isDataStale(String latestDataDate) {
        if (latestDataDate == null || "Unknown".equals(latestDataDate)) {
            return true;
        }
        
        try {
            LocalDate latestDate = LocalDate.parse(latestDataDate);
            LocalDate now = LocalDate.now();
            
            // Consider data stale if it's more than 10 days old
            long daysSinceLastUpdate = java.time.temporal.ChronoUnit.DAYS.between(latestDate, now);
            return daysSinceLastUpdate > 10;
            
        } catch (Exception e) {
            logger.debug("Failed to parse latest data date: {}", latestDataDate, e);
            return true;
        }
    }
}
