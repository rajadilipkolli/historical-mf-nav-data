# Assumptions and Gaps

This document captures the explicit assumptions, constraints, risks, and missing capabilities of the Daily NAV library.

## Explicit Assumptions

*   **Embedded Database**: The library embeds and relies on a compressed SQLite database (`funds.db.zst`) for historical data.
*   **Pricing Cadence**: Data represents end-of-day NAV pricing only. There is no intraday pricing available or supported for mutual funds.
*   **Geographic Scope**: Coverage is strictly limited to India and mutual funds tracked by AMFI.
*   **Update Cadence**: Database updates are executed daily via automated GitHub Actions pipelines.
*   **Runtime Environment**: The library targets Java/Spring Boot ecosystems exclusively.
*   **Versioning**: Datasets and library releases use a timestamp-based versioning scheme.

## Constraints and Risks

*   **AMFI Dependency**: The entire data ingestion pipeline relies entirely on the availability and structure of the public AMFI text endpoints. Any breaking changes to AMFI's format or prolonged downtime will impact data freshness.
*   **Data Staleness**: The library considers data >10 days old as "stale" and flags the system as unhealthy in Actuator health checks. Users must update their dependency or provide an updated database file.
*   **DMA Limitations**: 
    *   200-DMA calculations require at least 200 trading days of history. 
    *   The current pipeline does not filter out non-trading holidays beyond standard weekends.
    *   As a lagging indicator, the 200-DMA reflects past performance and may not immediately capture sudden market shifts.
*   **Observability Dependencies**: Health checks and observability require `spring-boot-starter-actuator` and optionally `spring-boot-starter-web` (WebMVC) to be provided by the consumer application.

## Missing Capabilities

The following capabilities are currently absent, ranked by priority and business impact:

1.  **Return / CAGR Calculations (High Priority)**
    *   *Impact*: Essential for portfolio performance evaluation and direct use in FinTech applications. Without this, consumers must build their own math logic.
2.  **Additional Moving Averages (High Priority)**
    *   *Impact*: Adding 50-day and 100-day moving averages provides a more nuanced view of short and medium-term market trends alongside the existing 200-DMA.
3.  **Portfolio Analytics (Medium Priority)**
    *   *Impact*: Aggregating NAV data to provide portfolio-level insights (e.g., standard deviation, alpha, beta) would elevate the library from a data source to an analytics engine.
4.  **Holiday Calendar Integration (Medium Priority)**
    *   *Impact*: Accounting for NSE/BSE public holidays (rather than just weekends) would significantly improve the accuracy of rolling window calculations like the 200-DMA.
5.  **Non-Java / Polyglot Support (Low Priority)**
    *   *Impact*: While the SQLite database can be accessed by any language, the rich service layer (`MutualFundService`) is currently Java/Spring only. A standalone microservice mode could unblock non-Java teams.
6.  **Real-Time / Intraday Data (Not Applicable)**
    *   *Impact*: Mutual funds are priced end-of-day. Real-time support is intentionally excluded as it contradicts the domain model.
