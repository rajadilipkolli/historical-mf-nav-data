package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

public class PerformanceReportService {

  private final ChatClient chatClient;
  private final ReportDataAssembler reportDataAssembler;

  private static final String REPORT_TEMPLATE =
      """
      Generate a professional, factual performance report for a mutual fund based on the provided context.
      Do not provide financial advice. The report must be formatted in Markdown.

      Structure the report with the following sections exactly:
      # Overview
      # Performance vs. 200-DMA
      # Notable Movements
      # Caveats

      Context Information:
      Fund Name: {fundName}
      ISIN: {isin}
      Period: Last {periodDays} days
      Trend Label: {trendLabel}
      200-DMA: {dma200}
      Anomalies Found: {hasAnomalies}

      NAV History Stats:
      {navStats}

      Use the context to populate the sections. The tone should be objective and analytical.
      """;

  public PerformanceReportService(ChatClient chatClient, ReportDataAssembler reportDataAssembler) {
    this.chatClient = chatClient;
    this.reportDataAssembler = reportDataAssembler;
  }

  public String generateReport(String isin, int days) {
    ReportContext context = reportDataAssembler.assembleContext(isin, days);

    // Compute basic NAV stats for the prompt
    double maxNav = context.navHistory().stream().mapToDouble(NavByIsin::getNav).max().orElse(0.0);
    double minNav = context.navHistory().stream().mapToDouble(NavByIsin::getNav).min().orElse(0.0);

    String navStats =
        String.format(
            "Min NAV: %.4f\nMax NAV: %.4f\nData Points: %d",
            minNav, maxNav, context.navHistory().size());

    PromptTemplate promptTemplate = new PromptTemplate(REPORT_TEMPLATE);
    Prompt prompt =
        promptTemplate.create(
            Map.of(
                "fundName", context.fundInfo().getSchemeName(),
                "isin", context.fundInfo().getIsin(),
                "periodDays", context.periodDays(),
                "trendLabel", context.trendAnomalyResult().trendLabel(),
                "dma200", context.trendAnomalyResult().dma200(),
                "hasAnomalies", context.trendAnomalyResult().hasAnomaly(),
                "navStats", navStats));

    return chatClient.prompt(prompt).call().content();
  }
}
