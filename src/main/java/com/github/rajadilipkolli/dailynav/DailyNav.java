package com.github.rajadilipkolli.dailynav;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
  public static MutualFundService create(String dbFile) {
    DailyNavProperties properties = new DailyNavProperties();
    if (dbFile != null) {
      properties.setDatabaseFile(dbFile);
    }

    DataSource dataSource = createDataSource(properties);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    // Initialize database (restore from .db.zst if needed)
    DatabaseInitializer initializer = new DatabaseInitializer(jdbcTemplate, properties);
    initializer.initializeDatabase();

    // Wire up repositories and service
    NavByIsinRepository navByIsinRepository = new NavByIsinRepository(jdbcTemplate);
    SchemeRepository schemeRepository = new SchemeRepository(jdbcTemplate);
    SecurityRepository securityRepository = new SecurityRepository(jdbcTemplate);

    return new MutualFundService(navByIsinRepository, schemeRepository, securityRepository);
  }

  /** Creates a health service for monitoring the library. */
  public static DailyNavHealthService createHealthService(
      MutualFundService service, String dbFile) {
    DailyNavProperties properties = new DailyNavProperties();
    if (dbFile != null) {
      properties.setDatabaseFile(dbFile);
    }

    // We need a JdbcTemplate for the health service
    DataSource dataSource = createDataSource(properties);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    return new DailyNavHealthService(jdbcTemplate, properties);
  }

  private static DataSource createDataSource(DailyNavProperties properties) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setUrl(properties.getDatabasePath());
    return dataSource;
  }
}
