package com.github.rajadilipkolli.dailynav.domain.report;

import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.util.List;

public record ReportContext(
    MutualFundService.FundInfo fundInfo,
    List<NavByIsin> navHistory,
    TrendAnomalyResult trendAnomalyResult,
    Integer periodDays) {}
