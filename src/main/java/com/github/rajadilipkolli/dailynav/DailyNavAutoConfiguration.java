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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Auto-configuration for Daily NAV library */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(DailyNavProperties.class)
public class DailyNavAutoConfiguration {

  private final DailyNavProperties properties;

  /**
   * Create a DailyNavAutoConfiguration using the provided Daily NAV settings.
   *
   * @param properties configuration properties for Daily NAV, including database path and auto-init
   *     options
   */
  public DailyNavAutoConfiguration(DailyNavProperties properties) {
    this.properties = properties;
  }

  /**
   * Creates a DataSource configured for the Daily NAV SQLite database defined in {@link
   * DailyNavProperties}.
   *
   * <p>The returned datasource is tuned for SQLite usage and executes initialization SQL to enable
   * WAL journal mode and set synchronous mode to NORMAL.
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
   * Provides a NamedParameterJdbcTemplate using the Daily NAV JdbcTemplate.
   *
   * @param jdbcTemplate the Daily NAV JdbcTemplate
   * @return a NamedParameterJdbcTemplate backed by the Daily NAV JdbcTemplate
   */
  @Bean(name = "dailyNavNamedParameterJdbcTemplate")
  @ConditionalOnMissingBean(name = "dailyNavNamedParameterJdbcTemplate")
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  NamedParameterJdbcTemplate namedParameterJdbcTemplate(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new NamedParameterJdbcTemplate(jdbcTemplate);
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
  @ConditionalOnBean(name = {"dailyNavJdbcTemplate", "dailyNavNamedParameterJdbcTemplate"})
  SecurityRepository securityRepository(
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate,
      @Qualifier("dailyNavNamedParameterJdbcTemplate")
          NamedParameterJdbcTemplate dailyNavNamedParameterJdbcTemplate) {
    return new SecurityRepository(jdbcTemplate, dailyNavNamedParameterJdbcTemplate);
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
   * Create a MutualFundService configured with the library's repository dependencies.
   *
   * @param navByIsinRepository repository providing NAV lookup by ISIN
   * @param schemeRepository repository for mutual fund scheme metadata
   * @param securityRepository repository for security/instrument data
   * @param databaseInitializer initializer responsible for preparing or verifying DB state
   * @return a MutualFundService instance backed by the provided repositories
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(name = "dailyNavJdbcTemplate")
  MutualFundService mutualFundService(
      NavByIsinRepository navByIsinRepository,
      SchemeRepository schemeRepository,
      SecurityRepository securityRepository,
      DatabaseInitializer databaseInitializer) {
    return new MutualFundService(
        navByIsinRepository, schemeRepository, securityRepository, databaseInitializer);
  }

  /**
   * Creates the DailyNavHealthService used to perform health checks for the Daily NAV components.
   *
   * @param jdbcTemplate the JdbcTemplate backed by the daily NAV data source (qualified
   *     "dailyNavJdbcTemplate")
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
   * @param jdbcTemplate the JdbcTemplate bound to the Daily NAV datasource used for database
   *     operations
   * @param properties Daily NAV configuration properties that control database location and
   *     initialization behavior
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
   * Creates a DailyNavHealthIndicator wrapping DailyNavHealthService for management/health checks.
   */
  @Bean("dailyNavHealthIndicator")
  @ConditionalOnMissingBean
  @ConditionalOnBean(DailyNavHealthService.class)
  DailyNavHealthIndicator dailyNavHealthIndicator(DailyNavHealthService healthService) {
    return new DailyNavHealthIndicator(healthService);
  }

  /**
   * Creates the web controller that exposes health endpoints for the Daily NAV library.
   *
   * @param jdbcTemplate the dedicated JdbcTemplate for the Daily NAV database
   * @param properties configuration properties for the Daily NAV library
   * @param healthService service that evaluates the library's health
   * @return the configured DailyNavHealthController instance
   */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnWebApplication
  @ConditionalOnMissingClass("org.springframework.boot.health.contributor.HealthIndicator")
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
      prefix = "daily-nav",
      name = "auto-init",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnBean(DatabaseInitializer.class)
  ApplicationRunner initializerRunner(DatabaseInitializer initializer) {
    return args -> initializer.initializeDatabaseAsync();
  }

  /**
   * Provides a ThreadPoolTaskExecutor for Daily NAV asynchronous tasks.
   *
   * <p>The executor is configured with a core pool size of 2, maximum pool size of 5, a queue
   * capacity of 50, and thread name prefix "daily-nav-".
   *
   * @return the configured ThreadPoolTaskExecutor instance
   */
  @Bean(name = "dailyNavTaskExecutor")
  @ConditionalOnMissingBean(name = "dailyNavTaskExecutor")
  ThreadPoolTaskExecutor dailyNavTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("daily-nav-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    return executor;
  }

  @Configuration
  @EnableAsync
  @ConditionalOnProperty(prefix = "daily-nav", name = "enable-async", havingValue = "true")
  static class AsyncConfig {}
}
