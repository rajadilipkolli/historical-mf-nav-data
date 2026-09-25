package com.github.rajadilipkolli.dailynav.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.rajadilipkolli.dailynav.application.port.DatabaseInitializerPort;
import com.github.rajadilipkolli.dailynav.application.port.NavPort;
import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
    properties = {
      "daily-nav.database-type=postgres",
      "daily-nav.url=jdbc:tc:postgresql:18-alpine:///testdb",
      "daily-nav.username=test",
      "daily-nav.password=test"
    })
class NavLazyLoadIT extends AbstractIntegrationTest {

  @Autowired private NavPort navPort;
  @Autowired private DatabaseInitializerPort initializerPort;

  @BeforeEach
  void setUp() {
    await().atMost(Duration.ofSeconds(30)).until(() -> mutualFundService.isReady());
  }

  @Test
  void testLazyLoadFlow() {
    int schemeCode = 119551; // Just a sample scheme

    // Initially, postgres should not have NAV rows if not preloaded (since we have lazy load)
    // Actually, wait, does seedPostgresData populate NAV? No, it excludes NAV!
    // So postgres has 0 navs.

    // 1. First request -> returns from SQLite fallback
    List<Nav> firstRequest = navPort.findBySchemeCode(schemeCode);
    assertThat(firstRequest).isNotEmpty();

    // 2. The background thread should persist to PostgreSQL
    await().atMost(Duration.ofSeconds(10)).until(() -> initializerPort.hasNavForScheme(schemeCode));

    // 3. Subsequent request -> served from PostgreSQL cache hit
    List<Nav> secondRequest = navPort.findBySchemeCode(schemeCode);
    assertThat(secondRequest).isNotEmpty();
    assertThat(secondRequest.size()).isEqualTo(firstRequest.size());
  }
}
