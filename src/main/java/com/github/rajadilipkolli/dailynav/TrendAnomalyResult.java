package com.github.rajadilipkolli.dailynav;

public record TrendAnomalyResult(
    String isin,
    double latestNav,
    double dma200,
    double dmaDiffPct,
    String trendLabel,
    boolean hasAnomaly,
    boolean isStale,
    String narrative) {}
