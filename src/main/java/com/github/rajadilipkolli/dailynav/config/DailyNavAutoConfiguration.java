package com.github.rajadilipkolli.dailynav.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Auto-configuration for Daily NAV library
 */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(DailyNavProperties.class)
@ComponentScan(basePackages = "com.github.rajadilipkolli.dailynav")
public class DailyNavAutoConfiguration {
    
    private final DailyNavProperties properties;
    
    public DailyNavAutoConfiguration(DailyNavProperties properties) {
        this.properties = properties;
    }
    
    @Bean(name = "dailyNavDataSource")
    @ConditionalOnMissingBean(name = "dailyNavDataSource")
    public DataSource dailyNavDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl(properties.getDatabasePath());
        return dataSource;
    }
    
    @Bean(name = "dailyNavJdbcTemplate")
    @ConditionalOnMissingBean(name = "dailyNavJdbcTemplate")
    public JdbcTemplate dailyNavJdbcTemplate() {
        return new JdbcTemplate(dailyNavDataSource());
    }
}
