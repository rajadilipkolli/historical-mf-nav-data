package com.github.rajadilipkolli.dailynav.integration;

import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import com.github.rajadilipkolli.dailynav.infrastructure.ai.TextToSqlGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

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
public abstract class AbstractIntegrationTest {

  @Autowired protected TextToSqlGenerator textToSqlGenerator;

  @Autowired
  @Qualifier("dailyNavJdbcTemplate")
  protected JdbcTemplate jdbcTemplate;

  @Autowired protected MockMvcTester mockMvcTester;

  @Autowired protected MutualFundService mutualFundService;
}
