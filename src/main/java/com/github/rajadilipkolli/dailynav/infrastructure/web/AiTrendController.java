package com.github.rajadilipkolli.dailynav.infrastructure.web;

import com.github.rajadilipkolli.dailynav.application.service.TrendAnomalyService;
import com.github.rajadilipkolli.dailynav.domain.report.TrendAnomalyResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST Controller for AI-powered trend and anomaly detection. */
@RestController
@RequestMapping("/api/v1/daily-nav/ai/trends")
public class AiTrendController {

  private final TrendAnomalyService trendAnomalyService;

  public AiTrendController(TrendAnomalyService trendAnomalyService) {
    this.trendAnomalyService = trendAnomalyService;
  }

  /**
   * Retrieves trend and anomaly analysis for a given ISIN.
   *
   * @param isin The ISIN to analyze.
   * @return TrendAnomalyResult containing stats and optional AI narrative.
   */
  @GetMapping(value = "/{isin}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<TrendAnomalyResult> getTrendAndAnomalies(
      @PathVariable("isin") String isin) {
    try {
      TrendAnomalyResult result = trendAnomalyService.analyzeTrendAndAnomalies(isin);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
