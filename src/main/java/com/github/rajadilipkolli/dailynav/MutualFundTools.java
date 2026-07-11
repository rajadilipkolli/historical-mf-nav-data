package com.github.rajadilipkolli.dailynav;

import java.time.LocalDate;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** AI Tools exposing MutualFundService operations to the LLM. */
public class MutualFundTools {

  private final MutualFundService mutualFundService;
  private static final String ISIN_REGEX = "^INF[A-Z0-9]{8}[0-9]$";

  public MutualFundTools(MutualFundService mutualFundService) {
    this.mutualFundService = mutualFundService;
  }

  @Tool(
      description =
          "Search for mutual fund schemes by name pattern. Returns a list of matching scheme records.")
  public Object searchSchemes(
      @ToolParam(description = "The name pattern to search for (e.g. 'HDFC Growth')")
          String namePattern) {
    if (namePattern == null || namePattern.trim().isEmpty()) {
      return "Error: Name pattern cannot be blank.";
    }
    return mutualFundService.searchSchemes(namePattern);
  }

  @Tool(
      description =
          "Find ISINs (International Securities Identification Numbers) for a given scheme name pattern.")
  public Object findIsinsBySchemeName(
      @ToolParam(description = "The scheme name pattern to search for") String namePattern) {
    if (namePattern == null || namePattern.trim().isEmpty()) {
      return "Error: Name pattern cannot be blank.";
    }
    return mutualFundService.findIsinsBySchemeName(namePattern);
  }

  @Tool(description = "Get the latest NAV (Net Asset Value) record for a specific ISIN.")
  public Object getLatestNavByIsin(
      @ToolParam(description = "The specific ISIN to look up") String isin) {
    if (!isValidIsin(isin)) {
      return "Error: Invalid ISIN format. Indian ISINs must match ^INF[A-Z0-9]{8}[0-9]$";
    }
    return mutualFundService.getLatestNavByIsin(isin).orElse(null);
  }

  @Tool(description = "Get the historical NAV records for an ISIN within a specific date range.")
  public Object getNavHistory(
      @ToolParam(description = "The ISIN to look up") String isin,
      @ToolParam(description = "The start date of the range (e.g. 2023-01-01)") LocalDate startDate,
      @ToolParam(description = "The end date of the range (e.g. 2023-12-31)") LocalDate endDate) {
    if (!isValidIsin(isin)) {
      return "Error: Invalid ISIN format. Indian ISINs must match ^INF[A-Z0-9]{8}[0-9]$";
    }
    if (startDate == null || endDate == null) {
      return "Error: Start date and end date must be provided.";
    }
    if (startDate.isAfter(endDate)) {
      return "Error: Start date must be before end date.";
    }
    return mutualFundService.getNavHistory(isin, startDate, endDate);
  }

  @Tool(description = "Get the NAV records for an ISIN over the last N days.")
  public Object getLastNDaysNav(
      @ToolParam(description = "The ISIN to look up") String isin,
      @ToolParam(description = "The number of days of history to fetch (e.g. 30)") Integer days) {
    if (!isValidIsin(isin)) {
      return "Error: Invalid ISIN format. Indian ISINs must match ^INF[A-Z0-9]{8}[0-9]$";
    }
    if (days == null || days <= 0) {
      return "Error: Days must be a positive integer.";
    }
    return mutualFundService.getLastNDaysNav(isin, days);
  }

  @Tool(
      description =
          "Get complete fund information including scheme metadata and security context for an ISIN.")
  public Object getFundInfo(@ToolParam(description = "The ISIN to look up") String isin) {
    if (!isValidIsin(isin)) {
      return "Error: Invalid ISIN format. Indian ISINs must match ^INF[A-Z0-9]{8}[0-9]$";
    }
    return mutualFundService.getFundInfo(isin).orElse(null);
  }

  private boolean isValidIsin(String isin) {
    return isin != null && isin.matches(ISIN_REGEX);
  }
}
