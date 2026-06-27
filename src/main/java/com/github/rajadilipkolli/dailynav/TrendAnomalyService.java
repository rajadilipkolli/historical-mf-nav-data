package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

/** Service for detecting trends and anomalies in NAV series. */
public class TrendAnomalyService {

  private final NavByIsinRepository navByIsinRepository;
  private final ObjectProvider<ChatClient> chatClientProvider;

  public TrendAnomalyService(
      NavByIsinRepository navByIsinRepository, ObjectProvider<ChatClient> chatClientProvider) {
    this.navByIsinRepository = navByIsinRepository;
    this.chatClientProvider = chatClientProvider;
  }

  /**
   * Analyzes the trend and detects anomalies for the given ISIN using the last 200 trading days.
   *
   * @param isin The ISIN to analyze.
   * @return The structured TrendAnomalyResult.
   */
  public TrendAnomalyResult analyzeTrendAndAnomalies(String isin) {
    List<NavByIsin> records = navByIsinRepository.findLastNByIsin(isin, 200);

    if (records.isEmpty()) {
      throw new IllegalArgumentException("No NAV data found for ISIN: " + isin);
    }

    // Records come descending from repo, we need chronological for jump calculation
    Collections.reverse(records);

    double sum = 0.0;
    boolean hasAnomaly = false;

    for (int i = 0; i < records.size(); i++) {
      NavByIsin current = records.get(i);
      sum += current.getNav();

      if (i > 0) {
        double previousNav = records.get(i - 1).getNav();
        double pctChange = Math.abs(current.getNav() - previousNav) / previousNav;
        if (pctChange > 0.1) {
          hasAnomaly = true;
        }
      }
    }

    double dma200 = sum / records.size();
    NavByIsin latest = records.get(records.size() - 1);
    double latestNav = latest.getNav();
    double dmaDiffPct = ((latestNav - dma200) / dma200) * 100.0;

    String trendLabel = latestNav > dma200 ? "ABOVE" : (latestNav < dma200 ? "BELOW" : "NEUTRAL");

    boolean isStale =
        latest.getDate() != null && latest.getDate().isBefore(LocalDate.now().minusDays(200));

    String narrative = generateNarrative(isin, latestNav, dma200, trendLabel, hasAnomaly, isStale);

    return new TrendAnomalyResult(
        isin, latestNav, dma200, dmaDiffPct, trendLabel, hasAnomaly, isStale, narrative);
  }

  private String generateNarrative(
      String isin,
      double latestNav,
      double dma200,
      String trendLabel,
      boolean hasAnomaly,
      boolean isStale) {
    ChatClient chatClient = chatClientProvider.getIfAvailable();
    if (chatClient == null) {
      return null;
    }

    String prompt =
        String.format(
            "Analyze the trend for ISIN %s. The latest NAV is %.4f and its 200-day moving average is %.4f. "
                + "It is currently trading %s its 200-DMA. "
                + "Anomaly flag (>10%% jump in window): %b. Stale data flag: %b. "
                + "Provide a concise (1-2 sentences) financial narrative explaining this trend.",
            isin, latestNav, dma200, trendLabel, hasAnomaly, isStale);

    try {
      return chatClient.prompt().user(prompt).call().content();
    } catch (Exception e) {
      return "Unable to generate narrative due to AI service error.";
    }
  }
}
