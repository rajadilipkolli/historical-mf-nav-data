package com.github.rajadilipkolli.dailynav.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.rajadilipkolli.dailynav.AiSearchController;
import com.github.rajadilipkolli.dailynav.DailyNavAutoConfiguration;
import com.github.rajadilipkolli.dailynav.KnowledgeSearchController;
import com.github.rajadilipkolli.dailynav.MutualFundService;
import com.github.rajadilipkolli.dailynav.PerformanceReportController;
import com.github.rajadilipkolli.dailynav.TrendAnomalyResult;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.JsonNode;

@SpringBootTest(
    classes = {DailyNavAutoConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "management.endpoints.web.exposure.include=health",
      "management.endpoint.health.show-details=always",
      "daily-nav.ai.enabled=true"
    })
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AiSearchControllerIT {

  @Autowired private MockMvcTester mockMvcTester;

  @Autowired private MutualFundService mutualFundService;

  @BeforeEach
  void setUp() {
    await().atMost(Duration.ofSeconds(30)).until(() -> mutualFundService.isReady());
  }

  @Test
  void testSearchEndpoint() {
    mockMvcTester
        .post()
        .uri("/api/v1/daily-nav/ai/search")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"query": "What is the NAV for HDFC Small Cap?"}
            """)
        .assertThat()
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson()
        .convertTo(AiSearchController.SearchResponse.class)
        .satisfies(searchResponse -> assertThat(searchResponse.answer()).isNotBlank());
  }

  @Test
  void testTrendEndpoint() {
    mockMvcTester
        .get()
        .uri("/api/v1/daily-nav/ai/trends/INF00XX01192")
        .assertThat()
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson()
        .convertTo(TrendAnomalyResult.class)
        .satisfies(
            result -> {
              assertThat(result.isin()).isEqualTo("INF00XX01192");
              assertThat(result.trendLabel()).isNotBlank();
              assertThat(result.dma200()).isNotNull();
            });
  }

  @Test
  void testReportEndpoint() {
    String requestBody =
        """
        { "isin": "INF00XX01192", "days": 30 }
        """;
    mockMvcTester
        .post()
        .uri("/api/v1/daily-nav/ai/report")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody)
        .assertThat()
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson()
        .convertTo(PerformanceReportController.ReportResponse.class)
        .satisfies(
            result -> {
              assertThat(result.markdownReport()).isNotBlank();
            });
  }

  @Test
  void actuatorHealthIncludesDailyNav() {
    mockMvcTester
        .get()
        .uri("/actuator/health")
        .assertThat()
        .hasStatus(503)
        .hasContentType("application/vnd.spring-boot.actuator.v3+json")
        .bodyJson()
        .convertTo(JsonNode.class)
        .satisfies(
            jsonNode -> {
              assertThat(jsonNode.path("components").path("dailyNav")).isNotNull();
              assertThat(jsonNode.path("components").path("dailyNav").path("status").asString())
                  .isNotBlank();
              assertThat(
                      jsonNode
                          .path("components")
                          .path("dailyNav")
                          .path("details")
                          .path("databaseAccessible")
                          .asBoolean())
                  .isTrue();
            });
  }

  @Test
  void testAskEndpoint() {
    String requestBody =
        """
        { "query": "What is the exit load for HDFC small cap fund?" }
        """;
    mockMvcTester
        .post()
        .uri("/api/v1/daily-nav/ai/ask")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody)
        .assertThat()
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson()
        .convertTo(KnowledgeSearchController.AskResponse.class)
        .satisfies(
            result -> {
              assertThat(result.answer()).isNotBlank();
              assertThat(result.sources()).isEmpty();
            });
  }
}
