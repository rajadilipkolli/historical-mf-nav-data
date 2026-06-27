package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.util.List;

public record ReportContext(
    MutualFundService.FundInfo fundInfo,
    List<NavByIsin> navHistory,
    TrendAnomalyResult trendAnomalyResult,
    Integer periodDays) {}
