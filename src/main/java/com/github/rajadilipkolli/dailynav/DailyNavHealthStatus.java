package com.github.rajadilipkolli.dailynav;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Health status information for the Daily NAV library */
public class DailyNavHealthStatus {

  private boolean healthy = true;
  private boolean databaseAccessible = false;
  private Integer schemeCount;
  private Integer navRecordCount;
  private Integer securityCount;
  private LocalDate latestDataDate;
  private LocalDate dataStartDate;
  private LocalDate dataEndDate;
  private boolean dataStale = false;
  private boolean autoInitEnabled;
  private boolean indexesEnabled;
  private String databasePath;
  private List<String> issues = new ArrayList<>();

  // Constructors
  public DailyNavHealthStatus() {}

  // Getters and Setters
  public boolean isHealthy() {
    return healthy;
  }

  public void setHealthy(boolean healthy) {
    this.healthy = healthy;
  }

  public boolean isDatabaseAccessible() {
    return databaseAccessible;
  }

  public void setDatabaseAccessible(boolean databaseAccessible) {
    this.databaseAccessible = databaseAccessible;
  }

  public Integer getSchemeCount() {
    return schemeCount;
  }

  public void setSchemeCount(Integer schemeCount) {
    this.schemeCount = schemeCount;
  }

  public Integer getNavRecordCount() {
    return navRecordCount;
  }

  public void setNavRecordCount(Integer navRecordCount) {
    this.navRecordCount = navRecordCount;
  }

  public Integer getSecurityCount() {
    return securityCount;
  }

  public void setSecurityCount(Integer securityCount) {
    this.securityCount = securityCount;
  }

  public LocalDate getLatestDataDate() {
    return latestDataDate;
  }

  public void setLatestDataDate(LocalDate latestDataDate) {
    this.latestDataDate = latestDataDate;
  }

  public LocalDate getDataStartDate() {
    return dataStartDate;
  }

  public void setDataStartDate(LocalDate dataStartDate) {
    this.dataStartDate = dataStartDate;
  }

  public LocalDate getDataEndDate() {
    return dataEndDate;
  }

  public void setDataEndDate(LocalDate dataEndDate) {
    this.dataEndDate = dataEndDate;
  }

  public boolean isDataStale() {
    return dataStale;
  }

  public void setDataStale(boolean dataStale) {
    this.dataStale = dataStale;
  }

  public boolean isAutoInitEnabled() {
    return autoInitEnabled;
  }

  public void setAutoInitEnabled(boolean autoInitEnabled) {
    this.autoInitEnabled = autoInitEnabled;
  }

  public boolean isIndexesEnabled() {
    return indexesEnabled;
  }

  public void setIndexesEnabled(boolean indexesEnabled) {
    this.indexesEnabled = indexesEnabled;
  }

  public String getDatabasePath() {
    return databasePath;
  }

  public void setDatabasePath(String databasePath) {
    this.databasePath = databasePath;
  }

  public List<String> getIssues() {
    return issues;
  }

  public void setIssues(List<String> issues) {
    this.issues = issues != null ? issues : new ArrayList<>();
  }

  public void addIssue(String issue) {
    if (this.issues == null) {
      this.issues = new ArrayList<>();
    }
    this.issues.add(issue);
  }

  public boolean hasIssues() {
    return issues != null && !issues.isEmpty();
  }

  @Override
  public String toString() {
    return "DailyNavHealthStatus{"
        + "healthy="
        + healthy
        + ", databaseAccessible="
        + databaseAccessible
        + ", schemeCount="
        + schemeCount
        + ", navRecordCount="
        + navRecordCount
        + ", securityCount="
        + securityCount
        + ", latestDataDate='"
        + latestDataDate
        + '\''
        + ", dataStartDate='"
        + dataStartDate
        + '\''
        + ", dataEndDate='"
        + dataEndDate
        + '\''
        + ", dataStale="
        + dataStale
        + ", autoInitEnabled="
        + autoInitEnabled
        + ", indexesEnabled="
        + indexesEnabled
        + ", databasePath='"
        + databasePath
        + '\''
        + ", issues="
        + issues
        + '}';
  }
}
