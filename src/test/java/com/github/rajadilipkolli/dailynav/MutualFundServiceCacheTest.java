package com.github.rajadilipkolli.dailynav;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.rajadilipkolli.dailynav.model.NavByIsin;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = MutualFundServiceCacheTest.CacheTestConfig.class)
@TestPropertySource(
    properties = {"daily-nav.database-path=jdbc:sqlite::memory:", "daily-nav.auto-init=false"})
class MutualFundServiceCacheTest {

  @MockitoBean private NavByIsinRepository navByIsinRepository;

  // We mock other repositories so the context loads smoothly without needing actual DB connections
  // if not used
  @MockitoBean private SchemeRepository schemeRepository;

  @MockitoBean private SecurityRepository securityRepository;

  @MockitoBean private DatabaseInitializer databaseInitializer;

  @Autowired private MutualFundService mutualFundService;

  @Autowired private CacheManager cacheManager;

  @SpringBootConfiguration
  @EnableCaching
  @Import(DailyNavAutoConfiguration.class)
  static class CacheTestConfig {
    @Bean
    public CacheManager cacheManager() {
      return new CaffeineCacheManager("latestNav");
    }
  }

  @Test
  void testGetLatestNavByIsinIsCached() {
    String isin = "ISIN123";
    NavByIsin mockNav = new NavByIsin();
    mockNav.setIsin(isin);
    mockNav.setNav(100.0);

    when(navByIsinRepository.findLatestByIsin(isin)).thenReturn(Optional.of(mockNav));

    // First call, should hit the repository
    mutualFundService.getLatestNavByIsin(isin);

    // Second call, should hit the cache
    mutualFundService.getLatestNavByIsin(isin);

    // Verify repository was called only once
    verify(navByIsinRepository, times(1)).findLatestByIsin(isin);
  }
}
