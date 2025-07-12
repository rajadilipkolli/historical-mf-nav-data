package com.github.rajadilipkolli.dailynav.example;

import com.github.rajadilipkolli.dailynav.health.DailyNavHealthService;
import com.github.rajadilipkolli.dailynav.health.DailyNavHealthStatus;
import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.service.MutualFundService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Example application demonstrating usage of Daily NAV library */
@SpringBootApplication
public class DailyNavUsageExample implements CommandLineRunner {

  private final MutualFundService mutualFundService;
  private final DailyNavHealthService healthService;

  public DailyNavUsageExample(
      MutualFundService mutualFundService, DailyNavHealthService healthService) {
    this.mutualFundService = mutualFundService;
    this.healthService = healthService;
  }

  public static void main(String[] args) {
    SpringApplication.run(DailyNavUsageExample.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    System.out.println("=== Daily NAV Library Usage Example ===");

    // First, check the health of the library
    System.out.println("\n0. Health Check:");
    DailyNavHealthStatus healthStatus = healthService.checkHealth();
    if (healthStatus.isHealthy()) {
      System.out.println("   ✅ Library is healthy and ready to use");
      System.out.printf(
          "   📊 Data: %d schemes, %d NAV records, %d securities%n",
          healthStatus.getSchemeCount(),
          healthStatus.getNavRecordCount(),
          healthStatus.getSecurityCount());
      System.out.printf("   📅 Latest data: %s%n", healthStatus.getLatestDataDate());
    } else {
      System.out.println("   ⚠️  Library health issues detected:");
      for (String issue : healthStatus.getIssues()) {
        System.out.println("      - " + issue);
      }
    }

    // Example ISIN (this is a sample, replace with actual ISIN from your dataset)
    String sampleIsin = "INF277K01741";

    try {
      // Get latest NAV for an ISIN
      System.out.println("\n1. Getting latest NAV for ISIN: " + sampleIsin);
      NavByIsin latestNav = mutualFundService.getLatestNavByIsin(sampleIsin);
      if (latestNav != null) {
        System.out.printf("   Latest NAV: %.4f on %s%n", latestNav.getNav(), latestNav.getDate());
      } else {
        System.out.println("   No NAV data found for this ISIN");
      }

      // Get NAV for a specific date
      System.out.println("\n2. Getting NAV for specific date (2023-03-23):");
      LocalDate specificDate = LocalDate.of(2023, 3, 23);
      NavByIsin navOnDate = mutualFundService.getNavByIsinAndDate(sampleIsin, specificDate);
      if (navOnDate != null) {
        System.out.printf(
            "   NAV on or before %s: %.4f on %s%n",
            specificDate, navOnDate.getNav(), navOnDate.getDate());
      } else {
        System.out.println("   No NAV data found for this date");
      }

      // Get last 5 days NAV
      System.out.println("\n3. Getting last 5 NAV records:");
      List<NavByIsin> last5Days = mutualFundService.getLastNDaysNav(sampleIsin, 5);
      if (!last5Days.isEmpty()) {
        for (NavByIsin nav : last5Days) {
          System.out.printf("   %s: %.4f%n", nav.getDate(), nav.getNav());
        }
      } else {
        System.out.println("   No historical NAV data found");
      }

      // Get fund metadata
      System.out.println("\n4. Getting fund metadata:");
      var fundInfo = mutualFundService.getFundInfo(sampleIsin);
      if (fundInfo != null) {
        System.out.printf("   Scheme: %s%n", fundInfo.getSchemeName());
        System.out.printf("   Type: %s%n", fundInfo.getTypeDescription());
        System.out.printf("   Scheme Code: %d%n", fundInfo.getSchemeCode());
      } else {
        System.out.println("   No fund information found");
      }

    } catch (Exception e) {
      System.err.println("Error occurred: " + e.getMessage());
      e.printStackTrace();
    }

    System.out.println("\n=== Example completed ===");
  }
}
