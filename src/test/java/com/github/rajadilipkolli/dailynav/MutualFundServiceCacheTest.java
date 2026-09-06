package com.github.rajadilipkolli.dailynav;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.DatabaseInitializer;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.NavByIsinRepository;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.SchemeRepository;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.SecurityRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = DailyNavAutoConfiguration.class)
@TestPropertySource(
    properties = {
      "daily-nav.database-path=jdbc:sqlite::memory:",
      "daily-nav.auto-init=false",
      "daily-nav.enable-caching=true"
    })
class MutualFundServiceCacheTest {

  @MockitoBean private NavByIsinRepository navByIsinRepository;

  // We mock other repositories so the context loads smoothly without needing
  // actual DB connections if not used
  @MockitoBean private SchemeRepository schemeRepository;

  @MockitoBean private SecurityRepository securityRepository;

  @MockitoBean private DatabaseInitializer databaseInitializer;

  @Autowired private MutualFundService mutualFundService;

  @Autowired private CacheManager cacheManager;

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
