package com.github.rajadilipkolli.dailynav;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/** Auto-configuration for Daily NAV library */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(DailyNavProperties.class)
public class DailyNavAutoConfiguration {

  private final DailyNavProperties properties;

  public DailyNavAutoConfiguration(DailyNavProperties properties) {
    this.properties = properties;
  }

  /**
   * Provides a dedicated {@link DataSource} for the Daily NAV library.
   *
   * <p>This bean is configured to use the SQLite database specified in {@link DailyNavProperties}.
   * It uses {@link HikariDataSource} for connection pooling to ensure optimal performance and avoid
   * file locking issues with SQLite in high-concurrency environments.
   *
   * @return the configured HikariDataSource
   */
  @Bean(name = "dailyNavDataSource")
  @ConditionalOnMissingBean(name = "dailyNavDataSource")
  DataSource dailyNavDataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setJdbcUrl(properties.getDatabasePath());
    dataSource.setPoolName("DailyNavPool");
    dataSource.setMaximumPoolSize(5); // SQLite handles small pools better
    dataSource.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL;");
    return dataSource;
  }

  @Bean(name = "dailyNavJdbcTemplate")
  @ConditionalOnMissingBean(name = "dailyNavJdbcTemplate")
  @ConditionalOnBean(name = "dailyNavDataSource")
  JdbcTemplate dailyNavJdbcTemplate(@Qualifier("dailyNavDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  NavRepository navRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new NavRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  SchemeRepository schemeRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new SchemeRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  SecurityRepository securityRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new SecurityRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  NavByIsinRepository navByIsinRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new NavByIsinRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  MutualFundService mutualFundService(
      NavByIsinRepository navByIsinRepository,
      SchemeRepository schemeRepository,
      SecurityRepository securityRepository) {
    return new MutualFundService(navByIsinRepository, schemeRepository, securityRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  DailyNavHealthService dailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    return new DailyNavHealthService(jdbcTemplate, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  DatabaseInitializer databaseInitializer(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    return new DatabaseInitializer(jdbcTemplate, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnWebApplication
  @ConditionalOnMissingClass("org.springframework.boot.actuator.health.HealthIndicator")
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  DailyNavHealthController dailyNavHealthController(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      DailyNavProperties properties,
      DailyNavHealthService healthService) {
    return new DailyNavHealthController(jdbcTemplate, properties, healthService);
  }

  /**
   * Runner that triggers the asynchronous database initialization.
   *
   * @param initializer the database initializer
   * @return the ApplicationRunner bean
   */
  @Bean
  @ConditionalOnProperty(
      prefix = "daily.nav",
      name = "auto-init",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnBean(DatabaseInitializer.class)
  ApplicationRunner initializerRunner(DatabaseInitializer initializer) {
    return args -> initializer.initializeDatabaseAsync();
  }

  @Configuration
  @EnableAsync
  @ConditionalOnMissingBean(AsyncConfigurer.class)
  static class AsyncConfig {}
}
