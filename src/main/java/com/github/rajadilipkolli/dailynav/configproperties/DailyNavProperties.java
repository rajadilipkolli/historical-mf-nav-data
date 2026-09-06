package com.github.rajadilipkolli.dailynav.configproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for Daily NAV library */
@ConfigurationProperties(prefix = "daily-nav")
public class DailyNavProperties {

  /**
   * Whether to enable auto-initialization of the database. If true, data will be loaded from
   * funds.db.zst or funds.sql in the background.
   */
  private boolean autoInit = true;

  /** Database URL. Defaults to an in-memory SQLite database that is shared across connections. */
  private String databasePath = "jdbc:sqlite:file::memory:?cache=shared";

  /** Whether to create indexes automatically after data loading. */
  private boolean createIndexes = true;

  /** Enable debug logging for database operations. */
  private boolean debug = false;

  /** Path to a persistent database file. If set, this overrides the default databasePath. */
  private String databaseFile = null;

  /** Whether to validate data integrity (counting records) after loading. */
  private boolean validateData = true;

  /**
   * Whether to enable asynchronous execution for document ingestion and other background tasks. If
   * true, tasks will be run using the thread pool executor.
   */
  private boolean enableAsync = true;

  /**
   * Indicates whether database initialization is enabled.
   *
   * @return {@code true} if database initialization is enabled, {@code false} otherwise
   */
  public boolean isAutoInit() {
    return autoInit;
  }

  /**
   * Configures whether the database is initialized automatically.
   *
   * @param autoInit {@code true} to enable automatic database initialization; {@code false}
   *     otherwise
   */
  public void setAutoInit(boolean autoInit) {
    this.autoInit = autoInit;
  }

  /**
   * Resolves the database connection path, using the configured database file when provided.
   *
   * @return the SQLite JDBC URL for the configured database file, or the configured database path
   */
  public String getDatabasePath() {
    // If a custom database file is specified, use it
    if (databaseFile != null && !databaseFile.trim().isEmpty()) {
      return "jdbc:sqlite:" + databaseFile;
    }
    return databasePath;
  }

  /**
   * Sets the default database connection path.
   *
   * @param databasePath the database connection path to use when no database file is configured
   */
  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
  }

  public String getDatabaseFile() {
    return databaseFile;
  }

  /**
   * Sets the file used for persistent database storage.
   *
   * @param databaseFile the database file path
   */
  public void setDatabaseFile(String databaseFile) {
    this.databaseFile = databaseFile;
  }

  public boolean isCreateIndexes() {
    return createIndexes;
  }

  /**
   * Configures whether database indexes are created during initialization.
   *
   * @param createIndexes whether to create database indexes
   */
  public void setCreateIndexes(boolean createIndexes) {
    this.createIndexes = createIndexes;
  }

  /**
   * Indicates whether debug logging is enabled.
   *
   * @return {@code true} if debug logging is enabled, {@code false} otherwise
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Enables or disables debug logging.
   *
   * @param debug {@code true} to enable debug logging; {@code false} to disable it
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Indicates whether data validation is enabled after loading.
   *
   * @return {@code true} if data validation is enabled, {@code false} otherwise
   */
  public boolean isValidateData() {
    return validateData;
  }

  /**
   * Configures whether loaded data should be validated.
   *
   * @param validateData {@code true} to enable data validation; {@code false} to disable it
   */
  public void setValidateData(boolean validateData) {
    this.validateData = validateData;
  }

  /**
   * Indicates whether asynchronous execution is enabled.
   *
   * @return {@code true} if async execution is enabled, {@code false} otherwise
   */
  public boolean isEnableAsync() {
    return enableAsync;
  }

  /**
   * Configures whether asynchronous execution is enabled.
   *
   * @param enableAsync {@code true} to enable async execution; {@code false} to disable it
   */
  public void setEnableAsync(boolean enableAsync) {
    this.enableAsync = enableAsync;
  }
}
