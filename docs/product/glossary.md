# Glossary

This document defines the core entities, domain terms, and identifiers used throughout the Daily NAV library.

## Core Entities

*   **Scheme**: Represents a mutual fund scheme. Identified by a unique `scheme_code` and carries a descriptive `scheme_name`. Maps to the `schemes` table.
*   **Security / ISIN**: Represents a specific security (or plan variant) under a scheme. Identified by an International Securities Identification Number (ISIN). It has a `type` property where `0` indicates a Growth/Dividend Payout plan, and `1` indicates a Dividend Reinvestment plan. Maps to the `securities` table.
*   **NAV**: The Net Asset Value record for a given day. Accessible via two dimensions:
    *   `nav`: Represents NAV indexed by `scheme_code` and date.
    *   `nav_by_isin`: Represents NAV indexed by ISIN and date.
*   **FundInfo**: A composite view combining both `Security` and `Scheme` information into a single entity, providing complete context (ISIN, Scheme Code, Scheme Name, and Type Description) in one object.

## Supporting Domain Terms

*   **NAV (Net Asset Value)**: The per-share/unit price of a mutual fund scheme on a specific date.
*   **AMFI**: Association of Mutual Funds in India. The authoritative source for historical and daily mutual fund NAV data in India.
*   **200-DMA**: 200-Day Moving Average. A technical indicator representing the average NAV over the last 200 trading days, used for market sentiment analysis and long-term trend tracking.
*   **Plan Variants**:
    *   *Direct vs. Regular*: Different expense structures for the same scheme.
    *   *Growth vs. IDCW*: Growth plans reinvest profits, whereas IDCW (Income Distribution cum Capital Withdrawal, formerly Dividend) plans distribute payouts.
*   **End-of-day Pricing**: The finalized NAV calculated and published at the end of the trading day. Intraday pricing is not applicable to mutual funds.
*   **Scheme Code vs. ISIN**:
    *   *Scheme Code*: An internal identifier used by AMFI for a scheme.
    *   *ISIN*: A globally recognized unique identifier for a specific security/plan variant. A single Scheme Code often maps to multiple ISINs representing different plans (e.g., Growth vs. Dividend).
