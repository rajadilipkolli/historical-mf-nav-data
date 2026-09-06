package com.github.rajadilipkolli.dailynav.infrastructure.persistence;

import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.application.service.TrendAnomalyService;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.report.ReportContext;
import com.github.rajadilipkolli.dailynav.domain.report.TrendAnomalyResult;
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
