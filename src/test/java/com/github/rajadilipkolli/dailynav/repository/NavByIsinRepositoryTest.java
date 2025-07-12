package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

class NavByIsinRepositoryTest {

  private JdbcTemplate jdbcTemplate;
  private NavByIsinRepository navByIsinRepository;
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
    navByIsinRepository = new NavByIsinRepository(jdbcTemplate);

    // Create schema using the same connection
    connection
        .createStatement()
        .execute("CREATE TABLE nav_by_isin (isin TEXT, date TEXT, nav REAL)");

    // Insert test data using the same connection
    var ps =
        connection.prepareStatement("INSERT INTO nav_by_isin (isin, date, nav) VALUES (?, ?, ?)");
    ps.setString(1, "ISIN123");
    ps.setString(2, LocalDate.now().toString());
    ps.setDouble(3, 100.0);
    ps.executeUpdate();
    ps.setString(1, "ISIN123");
    ps.setString(2, LocalDate.now().minusDays(1).toString());
    ps.setDouble(3, 99.0);
    ps.executeUpdate();
    ps.setString(1, "ISIN456");
    ps.setString(2, LocalDate.now().toString());
    ps.setDouble(3, 200.0);
    ps.executeUpdate();
    ps.close();
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  @Test
  void testFindLatestByIsin() {
    Optional<NavByIsin> result = navByIsinRepository.findLatestByIsin("ISIN123");
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(LocalDate.now(), result.get().getDate());
    assertEquals(100.0, result.get().getNav());
  }

  @Test
  void testFindByIsinAndDateOnOrBefore() {
    Optional<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateOnOrBefore("ISIN123", LocalDate.now().minusDays(1));
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(LocalDate.now().minusDays(1), result.get().getDate());
    assertEquals(99.0, result.get().getNav());
  }

  @Test
  void testFindLastNByIsin() {
    List<NavByIsin> result = navByIsinRepository.findLastNByIsin("ISIN123", 2);
    assertEquals(2, result.size());
    assertEquals("ISIN123", result.get(0).getIsin());
    assertEquals("ISIN123", result.get(1).getIsin());
  }

  @Test
  void testFindByIsinAndDateBetween() {
    List<NavByIsin> result =
        navByIsinRepository.findByIsinAndDateBetween(
            "ISIN123", LocalDate.now().minusDays(1), LocalDate.now());
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 100.0));
    assertTrue(result.stream().anyMatch(n -> n.getNav() == 99.0));
  }
}
