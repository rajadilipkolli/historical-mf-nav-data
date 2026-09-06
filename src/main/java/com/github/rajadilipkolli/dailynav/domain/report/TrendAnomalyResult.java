package com.github.rajadilipkolli.dailynav.domain.report;

public record TrendAnomalyResult(
    String isin,
    double latestNav,
    double dma200,
    double dmaDiffPct,
    String trendLabel,
    boolean hasAnomaly,
    boolean isStale,
    String narrative) {}
