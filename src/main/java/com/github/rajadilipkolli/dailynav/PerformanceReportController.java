package com.github.rajadilipkolli.dailynav;

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

  @PostMapping
  public ResponseEntity<ReportResponse> generateReport(@RequestBody ReportRequest request) {
    try {
      String report = performanceReportService.generateReport(request.isin(), request.days());
      return ResponseEntity.ok(new ReportResponse(report));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  public record ReportResponse(String markdownReport) {}
}
