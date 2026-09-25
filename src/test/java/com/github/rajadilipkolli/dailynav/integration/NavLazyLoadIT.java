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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestPropertySource(properties = "daily-nav.database-type=postgres")
class NavLazyLoadIT extends AbstractIntegrationTest {

  @Container
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
          .withInitScript("postgres-lazy-load.sql");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("daily-nav.url", postgres::getJdbcUrl);
    registry.add("daily-nav.username", postgres::getUsername);
    registry.add("daily-nav.password", postgres::getPassword);
  }

  @Autowired private NavPort navPort;
  @Autowired private DatabaseInitializerPort initializerPort;

  @BeforeEach
  void setUp() {
    await().atMost(Duration.ofSeconds(30)).until(() -> mutualFundService.isReady());
  }

  @Test
  void testLazyLoadFlow() {
    int schemeCode = 119551;
    assertThat(initializerPort.hasNavForScheme(schemeCode)).isFalse();

    List<Nav> firstRequest = navPort.findBySchemeCode(schemeCode);
    assertThat(firstRequest).isNotEmpty();

    await().atMost(Duration.ofSeconds(10)).until(() -> initializerPort.hasNavForScheme(schemeCode));

    List<Nav> secondRequest = navPort.findBySchemeCode(schemeCode);
    assertThat(secondRequest).isNotEmpty();
    assertThat(secondRequest.size()).isEqualTo(firstRequest.size());
  }
}
