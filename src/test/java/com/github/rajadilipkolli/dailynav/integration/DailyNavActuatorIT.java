package com.github.rajadilipkolli.dailynav.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.rajadilipkolli.dailynav.DailyNavAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = DailyNavAutoConfiguration.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "management.endpoints.web.exposure.include=health",
      "management.endpoint.health.show-details=always"
    })
@EnableAutoConfiguration
@AutoConfigureMockMvc
class DailyNavActuatorIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void actuatorHealthIncludesDailyNav() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.components.dailyNav").exists())
        .andExpect(jsonPath("$.components.dailyNav.status").exists())
        .andExpect(jsonPath("$.components.dailyNav.details.databaseAccessible").exists());
  }
}
