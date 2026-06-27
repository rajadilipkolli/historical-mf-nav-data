package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.util.List;

public class ReportDataAssembler {

  private final MutualFundService mutualFundService;
  private final TrendAnomalyService trendAnomalyService;

  public ReportDataAssembler(
      MutualFundService mutualFundService, TrendAnomalyService trendAnomalyService) {
    this.mutualFundService = mutualFundService;
    this.trendAnomalyService = trendAnomalyService;
  }

  public ReportContext assembleContext(String isin, int days) {
    MutualFundService.FundInfo fundInfo =
        mutualFundService
            .getFundInfo(isin)
            .orElseThrow(() -> new IllegalArgumentException("Invalid ISIN: " + isin));

    List<NavByIsin> navHistory = mutualFundService.getLastNDaysNav(isin, days);

    TrendAnomalyResult trendAnomalyResult = trendAnomalyService.analyzeTrendAndAnomalies(isin);

    return new ReportContext(fundInfo, navHistory, trendAnomalyResult, days);
  }
}
