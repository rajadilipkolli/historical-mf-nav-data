package com.github.rajadilipkolli.dailynav;

import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Entry point for using the Daily NAV library in non-Spring applications. */
public final class DailyNav {

  /** Private constructor to prevent instantiation of this utility class. */
  private DailyNav() {
    // Static utility class
  }

  /**
   * Create and initialize a CloseableMutualFundService for the configured database.
   *
   * <p>If `dbFile` is non-null, the service will use a persistent SQLite database at that path;
   * otherwise an in-memory database is used.
   *
   * @param dbFile path to a SQLite database file, or `null` to use an in-memory database
   * @return a CloseableMutualFundService that wraps the MutualFundService and its DataSource; close
   *     it to release DataSource resources
   */
  public static CloseableMutualFundService create(String dbFile) {
    DailyNavProperties properties = new DailyNavProperties();
    if (dbFile != null) {
      properties.setDatabaseFile(dbFile);
    }

    HikariDataSource dataSource = createDataSource(properties);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    // Initialize database (restore from .db.zst if needed)
    MutualFundService service = getMutualFundService(jdbcTemplate, properties);
    return new CloseableMutualFundService(service, dataSource);
  }

  private static @NonNull MutualFundService getMutualFundService(
      JdbcTemplate jdbcTemplate, DailyNavProperties properties) {
    DatabaseInitializer initializer = new DatabaseInitializer(jdbcTemplate, properties);
    initializer.initializeDatabase();

    // Wire up repositories and service
    NavByIsinRepository navByIsinRepository = new NavByIsinRepository(jdbcTemplate);
    SchemeRepository schemeRepository = new SchemeRepository(jdbcTemplate);
    NamedParameterJdbcTemplate namedParameterJdbcTemplate =
        new NamedParameterJdbcTemplate(jdbcTemplate);
    SecurityRepository securityRepository =
        new SecurityRepository(jdbcTemplate, namedParameterJdbcTemplate);

    return new MutualFundService(
        navByIsinRepository, schemeRepository, securityRepository, initializer);
  }

  /**
   * Create a closeable DailyNavHealthService backed by a HikariDataSource.
   *
   * @param dbFile path to a persistent SQLite database file; if null an in-memory database is used
   * @return a CloseableDailyNavHealthService that delegates to the health service and closes the
   *     underlying DataSource when closed
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

  /**
   * Create and configure a HikariDataSource for the application's SQLite database.
   *
   * <p>Configures the data source to use the SQLite JDBC driver and sets the JDBC URL from the
   * provided properties.
   *
   * @param properties configuration containing the database path
   * @return a configured HikariDataSource ready for use
   */
  private static HikariDataSource createDataSource(DailyNavProperties properties) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setDriverClassName("org.sqlite.JDBC");
    dataSource.setJdbcUrl(properties.getDatabasePath());
    // Keep defaults; Hikari will manage connections and is AutoCloseable
    return dataSource;
  }
}
