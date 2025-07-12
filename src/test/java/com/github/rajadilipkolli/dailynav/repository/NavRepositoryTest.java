package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.Nav;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

class NavRepositoryTest {

  private JdbcTemplate jdbcTemplate;
  private NavRepository navRepository;
  private Connection connection;
  private SQLiteDataSource ds;
  private javax.sql.DataSource singleConnectionDataSource;

  @BeforeEach
  void setUp() throws SQLException {
    // Setup in-memory SQLite DB (shared connection)
    ds = new SQLiteDataSource();
    ds.setUrl("jdbc:sqlite::memory:");
    connection = ds.getConnection();
    singleConnectionDataSource =
        new javax.sql.DataSource() {
          @Override
          public Connection getConnection() {
            return connection;
          }

          @Override
          public Connection getConnection(String username, String password) {
            return connection;
          }

          @Override
          public <T> T unwrap(Class<T> iface) {
            throw new UnsupportedOperationException();
          }

          @Override
          public boolean isWrapperFor(Class<?> iface) {
            return false;
          }

          @Override
          public java.io.PrintWriter getLogWriter() {
            return null;
          }

          @Override
          public void setLogWriter(java.io.PrintWriter out) {}

          @Override
          public void setLoginTimeout(int seconds) {}

          @Override
          public int getLoginTimeout() {
            return 0;
          }

          @Override
          public java.util.logging.Logger getParentLogger() {
            return null;
          }
        };
    jdbcTemplate = new JdbcTemplate(singleConnectionDataSource);
    navRepository = new NavRepository(jdbcTemplate);

    // Create schema using the same connection
    connection
        .createStatement()
        .execute("CREATE TABLE nav (scheme_code INTEGER, date TEXT, nav REAL)");

    // Insert test data using the same connection
    var ps1 =
        connection.prepareStatement("INSERT INTO nav (scheme_code, date, nav) VALUES (?, ?, ?)");
    ps1.setInt(1, 1);
    ps1.setString(2, LocalDate.now().toString());
    ps1.setDouble(3, 100.0);
    ps1.executeUpdate();
    ps1.setInt(1, 1);
    ps1.setString(2, LocalDate.now().minusDays(1).toString());
    ps1.setDouble(3, 99.0);
    ps1.executeUpdate();
    ps1.setInt(1, 2);
    ps1.setString(2, LocalDate.now().toString());
    ps1.setDouble(3, 200.0);
    ps1.executeUpdate();
    ps1.close();
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  @Test
  void testFindBySchemeCode() {
    List<Nav> result = navRepository.findBySchemeCode(1);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 100.0));
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 99.0));
  }

  @Test
  void testFindBySchemeCodeAndDateBetween() {
    List<Nav> result =
        navRepository.findBySchemeCodeAndDateBetween(
            1, LocalDate.now().minusDays(1), LocalDate.now());
    assertEquals(2, result.size());
  }

  @Test
  void testFindLatestBySchemeCode() {
    Optional<Nav> result = navRepository.findLatestBySchemeCode(1);
    assertTrue(result.isPresent());
    assertEquals(100.0, result.get().getNav());
  }

  @Test
  void testFindBySchemeCodeAndDateOnOrBefore() {
    Optional<Nav> result =
        navRepository.findBySchemeCodeAndDateOnOrBefore(1, LocalDate.now().minusDays(1));
    assertTrue(result.isPresent());
    assertEquals(99.0, result.get().getNav());
  }
}
