package com.github.rajadilipkolli.dailynav;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for Daily NAV library */
@ConfigurationProperties(prefix = "daily-nav")
public class DailyNavProperties {

  /**
   * Whether to enable auto-initialization of the database. If true, data will be loaded from
   * funds.db.zst or funds.sql in the background.
   */
  private boolean autoInit = true;

  /** Database connection URL. Default is in-memory. */
  private String databasePath = "jdbc:sqlite::memory:";

  /** Whether to create indexes automatically after data loading. */
  private boolean createIndexes = true;

  /** Enable debug logging for database operations. */
  private boolean debug = false;

  /** Path to a persistent database file. If set, this overrides the default databasePath. */
  private String databaseFile = null;

  /** Whether to validate data integrity (counting records) after loading. */
  private boolean validateData = true;

  public boolean isAutoInit() {
    return autoInit;
  }

  public void setAutoInit(boolean autoInit) {
    this.autoInit = autoInit;
  }

  public String getDatabasePath() {
    // If a custom database file is specified, use it
    if (databaseFile != null && !databaseFile.trim().isEmpty()) {
      return "jdbc:sqlite:" + databaseFile;
    }
    return databasePath;
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
  }

  public String getDatabaseFile() {
    return databaseFile;
  }

  public void setDatabaseFile(String databaseFile) {
    this.databaseFile = databaseFile;
  }

  public boolean isCreateIndexes() {
    return createIndexes;
  }

  public void setCreateIndexes(boolean createIndexes) {
    this.createIndexes = createIndexes;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public boolean isValidateData() {
    return validateData;
  }

  public void setValidateData(boolean validateData) {
    this.validateData = validateData;
  }
}
