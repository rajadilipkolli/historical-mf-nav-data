package com.github.rajadilipkolli.dailynav;

import com.github.rajadilipkolli.dailynav.application.service.DailyNavHealthService;
import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import com.github.rajadilipkolli.dailynav.domain.health.DailyNavHealthStatus;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Example application demonstrating usage of Daily NAV library */
@SpringBootApplication(scanBasePackageClasses = DailyNavAutoConfiguration.class)
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

  /** Demonstrates health checks and common Daily NAV library queries. */
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
      Optional<NavByIsin> latestNav = mutualFundService.getLatestNavByIsin(sampleIsin);
      if (latestNav.isPresent()) {
        System.out.printf(
            "   Latest NAV: %.4f on %s%n", latestNav.get().getNav(), latestNav.get().getDate());
      } else {
        System.out.println("   No NAV data found for this ISIN");
      }

      // Get NAV for a specific date
      System.out.println("\n2. Getting NAV for specific date (2023-03-23):");
      LocalDate specificDate = LocalDate.of(2023, 3, 23);
      Optional<NavByIsin> navOnDate =
          mutualFundService.getNavByIsinAndDate(sampleIsin, specificDate);
      if (navOnDate.isPresent()) {
        System.out.printf(
            "   NAV on or before %s: %.4f on %s%n",
            specificDate, navOnDate.get().getNav(), navOnDate.get().getDate());
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
      var optionalFundInfo = mutualFundService.getFundInfo(sampleIsin);
      if (optionalFundInfo.isPresent()) {
        MutualFundService.FundInfo fundInfo = optionalFundInfo.get();
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
