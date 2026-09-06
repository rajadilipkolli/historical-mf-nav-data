package com.github.rajadilipkolli.dailynav.application.service.assembler;

import com.github.rajadilipkolli.dailynav.application.port.ReportAssemblyPort;
import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.application.service.TrendAnomalyService;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.report.FundInfo;
import com.github.rajadilipkolli.dailynav.domain.report.ReportContext;
import com.github.rajadilipkolli.dailynav.domain.report.TrendAnomalyResult;
import java.util.List;

public class ReportDataAssembler implements ReportAssemblyPort {

  private final MutualFundService mutualFundService;
  private final TrendAnomalyService trendAnomalyService;

  public ReportDataAssembler(
      MutualFundService mutualFundService, TrendAnomalyService trendAnomalyService) {
    this.mutualFundService = mutualFundService;
    this.trendAnomalyService = trendAnomalyService;
  }

  /**
   * Assembles the data required to generate a report for a mutual fund.
   *
   * @param isin the fund's International Securities Identification Number
   * @param days the number of days of NAV history to include
   * @return the assembled report context
   * @throws IllegalArgumentException if no fund exists for the specified ISIN
   */
  @Override
  public ReportContext assembleContext(String isin, int days) {
    MutualFundService.FundInfo appFundInfo =
        mutualFundService
            .getFundInfo(isin)
            .orElseThrow(() -> new IllegalArgumentException("Invalid ISIN: " + isin));

    FundInfo domainFundInfo = new FundInfo(appFundInfo.security(), appFundInfo.scheme());

    List<NavByIsin> navHistory = mutualFundService.getLastNDaysNav(isin, days);

    TrendAnomalyResult trendAnomalyResult = trendAnomalyService.analyzeTrendAndAnomalies(isin);

    return new ReportContext(domainFundInfo, navHistory, trendAnomalyResult, days);
  }
}
