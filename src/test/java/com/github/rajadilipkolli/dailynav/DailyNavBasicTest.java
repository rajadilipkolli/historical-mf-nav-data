package com.github.rajadilipkolli.dailynav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Basic tests for the Daily NAV library components */
class DailyNavBasicTest {

  @Test
  void propertiesCanBeCreated() {
    DailyNavProperties properties = new DailyNavProperties();

    // Test default values
    assertTrue(properties.isAutoInit());
    assertTrue(properties.isCreateIndexes());
    assertTrue(properties.isValidateData());
    assertFalse(properties.isDebug());
    assertEquals("jdbc:sqlite::memory:", properties.getDatabasePath());
  }

  @Test
  void propertiesCanBeConfigured() {
    DailyNavProperties properties = new DailyNavProperties();

    properties.setAutoInit(false);
    properties.setCreateIndexes(false);
    properties.setValidateData(false);
    properties.setDebug(true);
    properties.setDatabaseFile("/tmp/test.db");

    assertFalse(properties.isAutoInit());
    assertFalse(properties.isCreateIndexes());
    assertFalse(properties.isValidateData());
    assertTrue(properties.isDebug());
    assertEquals("jdbc:sqlite:/tmp/test.db", properties.getDatabasePath());
  }

  @Test
  void databasePathCanBeCustomized() {
    DailyNavProperties properties = new DailyNavProperties();

    // Test custom database file
    properties.setDatabaseFile("/path/to/custom.db");
    assertEquals("jdbc:sqlite:/path/to/custom.db", properties.getDatabasePath());

    // Test direct database path override
    properties.setDatabasePath("jdbc:sqlite:/direct/path.db");
    properties.setDatabaseFile(null);
    assertEquals("jdbc:sqlite:/direct/path.db", properties.getDatabasePath());
  }
}
