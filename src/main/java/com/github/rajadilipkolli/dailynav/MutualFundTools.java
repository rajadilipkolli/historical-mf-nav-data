package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.model.Scheme;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/** AI Tools exposing MutualFundService operations to the LLM. */
public class MutualFundTools {

  private final MutualFundService mutualFundService;

  public MutualFundTools(MutualFundService mutualFundService) {
    this.mutualFundService = mutualFundService;
  }

  @Tool(
      description =
          "Search for mutual fund schemes by name pattern. Returns a list of matching scheme records.")
  public List<Scheme> searchSchemes(
      @ToolParam(description = "The name pattern to search for (e.g. 'HDFC Growth')")
          String namePattern) {
    return mutualFundService.searchSchemes(namePattern);
  }

  @Tool(
      description =
          "Find ISINs (International Securities Identification Numbers) for a given scheme name pattern.")
  public List<String> findIsinsBySchemeName(
      @ToolParam(description = "The scheme name pattern to search for") String namePattern) {
    return mutualFundService.findIsinsBySchemeName(namePattern);
  }

  @Tool(description = "Get the latest NAV (Net Asset Value) record for a specific ISIN.")
  public Optional<NavByIsin> getLatestNavByIsin(
      @ToolParam(description = "The specific ISIN to look up") String isin) {
    return mutualFundService.getLatestNavByIsin(isin);
  }

  @Tool(description = "Get the historical NAV records for an ISIN within a specific date range.")
  public List<NavByIsin> getNavHistory(
      @ToolParam(description = "The ISIN to look up") String isin,
      @ToolParam(description = "The start date of the range (e.g. 2023-01-01)") LocalDate startDate,
      @ToolParam(description = "The end date of the range (e.g. 2023-12-31)") LocalDate endDate) {
    return mutualFundService.getNavHistory(isin, startDate, endDate);
  }

  @Tool(description = "Get the NAV records for an ISIN over the last N days.")
  public List<NavByIsin> getLastNDaysNav(
      @ToolParam(description = "The ISIN to look up") String isin,
      @ToolParam(description = "The number of days of history to fetch (e.g. 30)") int days) {
    return mutualFundService.getLastNDaysNav(isin, days);
  }

  @Tool(
      description =
          "Get complete fund information including scheme metadata and security context for an ISIN.")
  public Optional<MutualFundService.FundInfo> getFundInfo(
      @ToolParam(description = "The ISIN to look up") String isin) {
    return mutualFundService.getFundInfo(isin);
  }
}
