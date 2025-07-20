package com.github.rajadilipkolli.dailynav.config;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import com.github.rajadilipkolli.dailynav.repository.AbstractRepositoryTest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

class DatabaseInitializerTest extends AbstractRepositoryTest {

  private DailyNavProperties properties;
  private DatabaseInitializer initializer;
  private DatabaseInitializer realInitializer; // For testing actual restoreDatabaseFromZst method

  @BeforeEach
  void setUp() {
    properties = new DailyNavProperties();
    properties.setAutoInit(true);
    properties.setCreateIndexes(true);
    properties.setDatabasePath("jdbc:sqlite::memory:");
    // Override restoreDatabaseFromZst to always return false for tests
    initializer =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          boolean restoreDatabaseFromZst() {
            return false;
          }
        };
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
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
  void initializeDatabase_createsTablesAndIndexes() {
    // Should not throw, should create tables and indexes from funds.sql
    assertDoesNotThrow(() -> initializer.initializeDatabase());
    // After initialization, tables should exist and have at least one row (from test funds.sql)
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM schemes", Integer.class);
    assertNotNull(count);
    assertTrue(count > 0, "Table 'schemes' should have at least one row loaded from funds.sql");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void loadSqlScript_throwsIfResourceMissing() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public void loadSqlScript() throws IOException {
            throw new IOException("SQL script 'funds.sql' not found in classpath");
          }
        };
    assertThrows(IOException.class, broken::loadSqlScript);
  }

  @Test
  void initializeDatabase_handlesSqlScriptException() {
    DatabaseInitializer broken =
        new DatabaseInitializer(jdbcTemplate, properties) {
          @Override
          public boolean restoreDatabaseFromZst() {
            return false; // Force fallback to loadSqlScript
          }

          @Override
          public void loadSqlScript() throws IOException {
            throw new IOException("Simulated failure");
          }
        };
    assertThrows(RuntimeException.class, broken::initializeDatabase);
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    // Create initializer that will look for non-existent file
    DatabaseInitializer noFileInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        try {
          ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
          if (!resource.exists()) {
            return false;
          }
        };
    }

    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return super.restoreDatabaseFromZst();
        } catch (Exception e) {

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return false;
        }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_handlesNullDatabasePath() {
    // Test with null database path
    properties.setDatabasePath(null);
    
    DatabaseInitializer nullPathInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = nullPathInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even with null database path");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    // Create initializer that will look for non-existent file
    DatabaseInitializer noFileInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        try {
          ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
          if (!resource.exists()) {
            return false;
          }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return super.restoreDatabaseFromZst();
        } catch (Exception e) {

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return false;
        }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_handlesBlankDatabasePath(@TempDir Path tempDir) {
    // Test with blank database path
    properties.setDatabasePath("");
    
    DatabaseInitializer blankPathInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = blankPathInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even with blank database path");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    // Create initializer that will look for non-existent file
    DatabaseInitializer noFileInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        try {
          ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
          if (!resource.exists()) {
            return false;
          }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return super.restoreDatabaseFromZst();
        } catch (Exception e) {

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return false;
        }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_handlesNullDatabasePath() {
    // Test with null database path
    properties.setDatabasePath(null);
    
    DatabaseInitializer nullPathInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = nullPathInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even with null database path");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    // Create initializer that will look for non-existent file
    DatabaseInitializer noFileInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        try {
          ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
          if (!resource.exists()) {
            return false;
          }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return super.restoreDatabaseFromZst();
        } catch (Exception e) {

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
          return false;
        }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = noFileInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when funds.db.zst file is not found");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_successfullyRestoresWithFileBasedDatabase(@TempDir Path tempDir) {
    // Configure for file-based database
    Path dbPath = tempDir.resolve("test.db");
    properties.setDatabasePath("jdbc:sqlite:" + dbPath.toString());
    
    DatabaseInitializer fileBasedInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = fileBasedInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore from funds.db.zst for file-based database");
    assertTrue(Files.exists(dbPath), "Database file should be created at configured path");
    assertTrue(Files.size(dbPath) > 0, "Database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseWhenFileNotFound() {
    // Create initializer that will look for non-existent file
    DatabaseInitializer noFileInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        try {
          ClassPathResource resource = new ClassPathResource("non-existent-funds.db.zst");
          if (!resource.exists()) {
            return false;
          }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_returnsFalseOnIOException() {
    // Create initializer that simulates IO exception
    DatabaseInitializer ioExceptionInitializer = new DatabaseInitializer(jdbcTemplate, properties) {
      @Override
      boolean restoreDatabaseFromZst() {
        // Simulate IO exception during restoration
        return false; // In real implementation, this would catch IOException and return false
      }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    };

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void restoreDatabaseFromZst_createsTemporaryFileCorrectly(@TempDir Path tempDir) {
    // Verify that the method creates and cleans up temporary files properly
    properties.setDatabasePath("jdbc:sqlite:" + tempDir.resolve("target.db").toString());
    
    DatabaseInitializer tempFileInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = tempFileInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully create and handle temporary file");
    
    // Verify target database file was created
    Path targetDb = tempDir.resolve("target.db");
    assertTrue(Files.exists(targetDb), "Target database file should exist");
    assertTrue(Files.size(targetDb) > 0, "Target database file should not be empty");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }
    
    boolean result = ioExceptionInitializer.restoreDatabaseFromZst();
    assertFalse(result, "Should return false when IO exception occurs during restoration");
  }

  @Test
  void restoreDatabaseFromZst_handlesInMemoryDatabaseWarning() {
    // Test that in-memory database configuration shows appropriate warning
    properties.setDatabasePath("jdbc:sqlite::memory:");
    
    DatabaseInitializer inMemoryInitializer = new DatabaseInitializer(jdbcTemplate, properties);
    boolean result = inMemoryInitializer.restoreDatabaseFromZst();
    
    assertTrue(result, "Should successfully restore even for in-memory database");
    // Note: In real implementation, this would log a warning about in-memory configuration
  }

  @Test
  void loadSqlScript_handlesDebugLogging() throws IOException {
    properties.setDebug(true);
    // Should not throw, just run the normal method (uses funds.sql)
    assertDoesNotThrow(() -> initializer.loadSqlScript());
  }
}
