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

  /**
   * Create a DailyNavAutoConfiguration using the provided Daily NAV settings.
   *
   * @param properties configuration properties for Daily NAV, including database path and auto-init options
   */
  public DailyNavAutoConfiguration(DailyNavProperties properties) {
    this.properties = properties;
  }

  /**
   * Creates a DataSource configured for the Daily NAV SQLite database defined in {@link DailyNavProperties}.
   *
   * The returned datasource is tuned for SQLite usage and executes initialization SQL to enable WAL journal mode
   * and set synchronous mode to NORMAL.
   *
   * @return the configured HikariDataSource for the Daily NAV database
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

  /**
   * Creates a JdbcTemplate configured to use the Daily NAV data source.
   *
   * @param dataSource the Daily NAV DataSource (qualified as "dailyNavDataSource")
   * @return a JdbcTemplate backed by the Daily NAV DataSource
   */
  @Bean(name = "dailyNavJdbcTemplate")
  @ConditionalOnMissingBean(name = "dailyNavJdbcTemplate")
  @ConditionalOnBean(name = "dailyNavDataSource")
  JdbcTemplate dailyNavJdbcTemplate(@Qualifier("dailyNavDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  /**
   * Create a NavRepository backed by the Daily NAV JdbcTemplate.
   *
   * @return a NavRepository that uses the Daily NAV JdbcTemplate
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  NavRepository navRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new NavRepository(jdbcTemplate);
  }

  /**
   * Configures a SchemeRepository backed by the Daily NAV JdbcTemplate.
   *
   * @return a SchemeRepository that uses the "dailyNavJdbcTemplate" JdbcTemplate
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  SchemeRepository schemeRepository(@Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new SchemeRepository(jdbcTemplate);
  }

  /**
   * Create a SecurityRepository that uses the Daily NAV JdbcTemplate.
   *
   * @param jdbcTemplate the Daily NAV {@code JdbcTemplate} to back the repository
   * @return a new {@link SecurityRepository} instance backed by the provided JdbcTemplate
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  SecurityRepository securityRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new SecurityRepository(jdbcTemplate);
  }

  /**
   * Creates a NavByIsinRepository backed by the Daily NAV JdbcTemplate.
   *
   * @return a NavByIsinRepository that uses the provided JdbcTemplate
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  NavByIsinRepository navByIsinRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new NavByIsinRepository(jdbcTemplate);
  }

  /**
   * Creates a MutualFundService composed of NAV-by-ISIN, scheme, and security repositories.
   *
   * @return a MutualFundService wired with the provided repositories
   */
  @Bean
  @ConditionalOnMissingBean
  MutualFundService mutualFundService(
      NavByIsinRepository navByIsinRepository,
      SchemeRepository schemeRepository,
      SecurityRepository securityRepository) {
    return new MutualFundService(navByIsinRepository, schemeRepository, securityRepository);
  }

  /**
   * Creates the DailyNavHealthService used to perform health checks for the Daily NAV components.
   *
   * @param jdbcTemplate the JdbcTemplate backed by the daily NAV data source (qualified "dailyNavJdbcTemplate")
   * @param properties configuration properties for Daily NAV
   * @return a DailyNavHealthService configured with the provided JdbcTemplate and properties
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  DailyNavHealthService dailyNavHealthService(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    return new DailyNavHealthService(jdbcTemplate, properties);
  }

  /**
   * Creates a DatabaseInitializer to prepare and initialize the Daily NAV database.
   *
   * @param jdbcTemplate the JdbcTemplate bound to the Daily NAV datasource used for database operations
   * @param properties   Daily NAV configuration properties that control database location and initialization behavior
   * @return a DatabaseInitializer configured to initialize and manage the Daily NAV database
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  DatabaseInitializer databaseInitializer(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    return new DatabaseInitializer(jdbcTemplate, properties);
  }

  /**
   * Creates a web controller that exposes health endpoints for the Daily NAV database.
   *
   * @param jdbcTemplate the JdbcTemplate wired to the Daily NAV datasource
   * @param properties configuration properties for Daily NAV (controls health behavior and paths)
   * @param healthService service that performs health checks against the Daily NAV database
   * @return a DailyNavHealthController configured to serve health-related endpoints
   */
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
   * Triggers database initialization at application startup when auto-init is enabled.
   *
   * @param initializer the DatabaseInitializer used to start initialization
   * @return an ApplicationRunner that invokes the initializer to perform initialization on startup
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