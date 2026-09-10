package com.github.rajadilipkolli.dailynav.infrastructure.web;

import com.github.rajadilipkolli.dailynav.application.service.PerformanceReportService;
import com.github.rajadilipkolli.dailynav.domain.report.ReportRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/daily-nav/ai/report")
public class PerformanceReportController {

  private final PerformanceReportService performanceReportService;

  public PerformanceReportController(PerformanceReportService performanceReportService) {
    this.performanceReportService = performanceReportService;
  }

  /**
   * Generates a performance report for the requested security identifier.
   *
   * @param request the report request containing the ISIN and optional number of days
   * @return an HTTP 200 response containing the report, or an HTTP 400 response for an invalid
   *     request
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ReportResponse> generateReport(
      @RequestBody(required = false) ReportRequest request) {
    if (request == null || request.isin() == null || request.isin().isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    int days = request.days() != null ? request.days() : 30;
    try {
      String report = performanceReportService.generateReport(request.isin(), days);
      return ResponseEntity.ok(new ReportResponse(report));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  public record ReportResponse(String markdownReport) {}
}
