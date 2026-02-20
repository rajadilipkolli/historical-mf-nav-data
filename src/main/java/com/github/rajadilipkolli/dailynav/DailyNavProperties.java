package com.github.rajadilipkolli.dailynav;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for Daily NAV library */
@ConfigurationProperties(prefix = "daily-nav")
public class DailyNavProperties {

  /** Whether to enable auto-initialization of the database */
  private boolean autoInit = true;

  /** Database file location (default: in-memory) */
  private String databasePath = "jdbc:sqlite::memory:";

  /** Whether to create indexes automatically */
  private boolean createIndexes = true;

  /** Enable debug logging */
  private boolean debug = false;

  /** Custom database file location (for persistent storage) */
  private String databaseFile = null;

  /** Whether to validate data integrity after loading */
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
