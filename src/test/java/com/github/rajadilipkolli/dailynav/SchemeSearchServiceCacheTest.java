package com.github.rajadilipkolli.dailynav;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.rajadilipkolli.dailynav.application.service.SchemeSearchService;
import com.github.rajadilipkolli.dailynav.config.DailyNavAutoConfiguration;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.DatabaseInitializer;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.NavByIsinRepository;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.SchemeRepository;
import com.github.rajadilipkolli.dailynav.infrastructure.persistence.SecurityRepository;
import java.util.List;
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
class SchemeSearchServiceCacheTest {

  @MockitoBean private NavByIsinRepository navByIsinRepository;

  @MockitoBean private SchemeRepository schemeRepository;

  @MockitoBean private SecurityRepository securityRepository;

  @MockitoBean private DatabaseInitializer databaseInitializer;

  @Autowired private SchemeSearchService schemeSearchService;

  @Autowired private CacheManager cacheManager;

  @Test
  void testListAmcsIsCached() {
    List<String> amcs = List.of("AMC 1", "AMC 2");

    when(schemeRepository.findDistinctAmcs()).thenReturn(amcs);

    // First call, should hit the repository
    schemeSearchService.listAmcs();

    // Second call, should hit the cache
    schemeSearchService.listAmcs();

    // Verify repository was called only once
    verify(schemeRepository, times(1)).findDistinctAmcs();
  }

  @Test
  void testListCategoriesIsCached() {
    List<String> categories = List.of("Category 1", "Category 2");

    when(schemeRepository.findDistinctCategories()).thenReturn(categories);

    // First call, should hit the repository
    schemeSearchService.listCategories();

    // Second call, should hit the cache
    schemeSearchService.listCategories();

    // Verify repository was called only once
    verify(schemeRepository, times(1)).findDistinctCategories();
  }
}
