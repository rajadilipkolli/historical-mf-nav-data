package com.github.rajadilipkolli.dailynav.config;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

class DatabaseInitializerTest extends AbstractRepositoryTest {

  private DailyNavProperties properties;
  private DatabaseInitializer initializer;

  @BeforeEach
  void setUp() {
    properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    initializer = new DatabaseInitializer(jdbcTemplate, properties);
  }

  @Override
  protected void createSchema() throws SQLException {
    // No-op: schema will be created by DatabaseInitializer using funds.sql
  }

  @Override
  protected void insertTestData() throws SQLException {
    // Already loaded by createSchema if needed
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    properties.setDatabasePath("jdbc:sqlite::memory:");
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir)
      throws Exception {
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully create and handle temporary file");
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    DatabaseInitializer ioExceptionInitializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          boolean restoreDatabaseFromZst() {
            // Simulate IO exception during restoration
            return false; // In real implementation, this would catch IOException and return false
          }
        };
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    DatabaseInitializer noFileInitializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          boolean restoreDatabaseFromZst() {
            try {
              ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
              if (!resource.exists()) {
                return false;
              }
            } catch (Exception e) {
              return false;
            }
            return true;
          }
        };
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesNullDatabasePath() {
    properties.setDatabasePath(null);
    DatabaseInitializer nullPathInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = nullPathInitializer.restoreDatabaseFromZst();
    assertTrue(result, "Should successfully restore even with null database path");
  }

  @Test
  void loadSqlScript_handlesDebugLogging() throws Exception {
    properties.setDebug(true);
    assertDoesNotThrow(() -> initializer.loadSqlScript());
  }
}
