package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.sqlite.SQLiteDataSource;

class SchemeRepositoryTest {
  private JdbcTemplate jdbcTemplate;
  private SchemeRepository schemeRepository;
  private Connection connection;
  private SQLiteDataSource ds;
  private SingleConnectionDataSource singleConnectionDataSource;

  @BeforeEach
  void setUp() throws SQLException {
    // Use a shared in-memory SQLite DB
    String url = "jdbc:sqlite:file:memdb1?mode=memory&cache=shared";
    ds = new SQLiteDataSource();
    ds.setUrl(url);
    connection = ds.getConnection();
    singleConnectionDataSource = new SingleConnectionDataSource(connection, false);
    jdbcTemplate = new JdbcTemplate(singleConnectionDataSource);
    schemeRepository = new SchemeRepository(jdbcTemplate);

    // Create schema and insert data only if not exists
    try (var stmt = connection.createStatement()) {
      stmt.execute(
          "CREATE TABLE IF NOT EXISTS schemes (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)");
    }
    try (var ps =
        connection.prepareStatement(
            "INSERT INTO schemes (scheme_code, scheme_name) VALUES (?, ?)"); ) {
      ps.setInt(1, 1);
      ps.setString(2, "Test Scheme");
      ps.executeUpdate();
      ps.setInt(1, 2);
      ps.setString(2, "Another Scheme");
      ps.executeUpdate();
    }
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  @Test
  void testFindBySchemeCode() {
    Optional<Scheme> result = schemeRepository.findBySchemeCode(1);
    assertTrue(result.isPresent());
    assertEquals(new Scheme(1, "Test Scheme"), result.get());
    assertTrue(schemeRepository.findBySchemeCode(999).isEmpty());
  }

  @Test
  void testFindAll() {
    List<Scheme> result = schemeRepository.findAll();
    assertEquals(2, result.size());
    assertTrue(result.contains(new Scheme(1, "Test Scheme")));
    assertTrue(result.contains(new Scheme(2, "Another Scheme")));
  }

  @Test
  void testFindBySchemeNameContaining() {
    List<Scheme> result = schemeRepository.findBySchemeNameContaining("Test");
    assertEquals(1, result.size());
    assertEquals(new Scheme(1, "Test Scheme"), result.get(0));

    result = schemeRepository.findBySchemeNameContaining("Scheme");
    assertEquals(2, result.size());

    result = schemeRepository.findBySchemeNameContaining("Nonexistent");
    assertTrue(result.isEmpty());
  }
}
