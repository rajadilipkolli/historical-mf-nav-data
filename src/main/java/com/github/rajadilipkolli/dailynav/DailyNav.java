package com.github.rajadilipkolli.dailynav;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/** Entry point for using the Daily NAV library in non-Spring applications. */
public final class DailyNav {

  private DailyNav() {
    // Static utility class
  }

  /**
   * Creates and initializes a MutualFundService instance.
   *
   * @param dbFile Optional path to a persistent SQLite database file. If null, uses in-memory.
   * @return A fully initialized MutualFundService.
   */
  /**
   * Creates and initializes a closable MutualFundService instance. Caller must close the returned
   * CloseableMutualFundService to release the underlying DataSource resources.
   */
  public static CloseableMutualFundService create(String dbFile) {
    DailyNavProperties properties = new DailyNavProperties();
    if (dbFile != null) {
      properties.setDatabaseFile(dbFile);
    }

    HikariDataSource dataSource = createDataSource(properties);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    // Initialize database (restore from .db.zst if needed)
    DatabaseInitializer initializer = new DatabaseInitializer(jdbcTemplate, properties);
    initializer.initializeDatabase();

    // Wire up repositories and service
    NavByIsinRepository navByIsinRepository = new NavByIsinRepository(jdbcTemplate);
    SchemeRepository schemeRepository = new SchemeRepository(jdbcTemplate);
    SecurityRepository securityRepository = new SecurityRepository(jdbcTemplate);

    MutualFundService service =
        new MutualFundService(navByIsinRepository, schemeRepository, securityRepository);
    return new CloseableMutualFundService(service, dataSource);
  }

  /** Creates a health service for monitoring the library. */
  /**
   * Creates a closable DailyNavHealthService. Caller must close the returned
   * CloseableDailyNavHealthService to release the underlying DataSource resources.
   */
  public static CloseableDailyNavHealthService createHealthService(String dbFile) {
    DailyNavProperties properties = new DailyNavProperties();
    if (dbFile != null) {
      properties.setDatabaseFile(dbFile);
    }

    // We need a JdbcTemplate for the health service
    HikariDataSource dataSource = createDataSource(properties);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    DailyNavHealthService delegate = new DailyNavHealthService(jdbcTemplate, properties);
    return new CloseableDailyNavHealthService(delegate, dataSource);
  }

  private static HikariDataSource createDataSource(DailyNavProperties properties) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setJdbcUrl(properties.getDatabasePath());
    // Keep defaults; Hikari will manage connections and is AutoCloseable
    return dataSource;
  }
}
