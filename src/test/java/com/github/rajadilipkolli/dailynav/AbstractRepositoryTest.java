package com.github.rajadilipkolli.dailynav;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.sqlite.SQLiteDataSource;

/**
 * Abstract base class for repository tests that sets up an in-memory SQLite DB, DataSource, and
 * JdbcTemplate. Subclasses must implement schema creation and test data insertion.
 */
public abstract class AbstractRepositoryTest {

  protected JdbcTemplate jdbcTemplate;
  protected Connection connection;
  protected SQLiteDataSource ds;
  protected SingleConnectionDataSource singleConnectionDataSource;

  @BeforeEach
  void setUpBase() throws SQLException {
    ds = new SQLiteDataSource();
    ds.setUrl("jdbc:sqlite::memory:");
    connection = ds.getConnection();
    singleConnectionDataSource = new SingleConnectionDataSource(connection, false);
    jdbcTemplate = new JdbcTemplate(singleConnectionDataSource);
    createSchema();
    insertTestData();
  }

  @AfterEach
  void tearDownBase() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  /** Subclasses must implement schema creation. */
  protected abstract void createSchema() throws SQLException;

  /** Subclasses must implement test data insertion. */
  protected abstract void insertTestData() throws SQLException;
}
