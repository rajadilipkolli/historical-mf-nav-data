package com.github.rajadilipkolli.dailynav.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.rajadilipkolli.dailynav.model.Security;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

class SecurityRepositoryTest {

  private JdbcTemplate jdbcTemplate;
  private SecurityRepository securityRepository;
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
    securityRepository = new SecurityRepository(jdbcTemplate);

    // Create schema using the same connection (table name must be 'securities')
    connection
        .createStatement()
        .execute("CREATE TABLE securities (isin TEXT, type INTEGER, scheme_code INTEGER)");

    // Insert test data
    var ps =
        connection.prepareStatement(
            "INSERT INTO securities (isin, type, scheme_code) VALUES (?, ?, ?)");
    ps.setString(1, "ISIN123");
    ps.setInt(2, 1);
    ps.setInt(3, 1);
    ps.executeUpdate();
    ps.setString(1, "ISIN456");
    ps.setInt(2, 2);
    ps.setInt(3, 2);
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
  void testFindByIsin() {
    Optional<Security> result = securityRepository.findByIsin("ISIN123");
    assertTrue(result.isPresent());
    assertEquals("ISIN123", result.get().getIsin());
    assertEquals(1, result.get().getType());
    assertEquals(1, result.get().getSchemeCode());
  }

  @Test
  void testFindBySchemeCode() {
    List<Security> result = securityRepository.findBySchemeCode(1);
    assertEquals(1, result.size());
    assertEquals("ISIN123", result.get(0).getIsin());
    assertEquals(1, result.get(0).getType());
    assertEquals(1, result.get(0).getSchemeCode());
  }

  @Test
  void testFindAll() {
    List<Security> result = securityRepository.findAll();
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(s -> s.getIsin().equals("ISIN123")));
    assertTrue(result.stream().anyMatch(s -> s.getIsin().equals("ISIN456")));
  }
}
