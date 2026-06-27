package com.github.rajadilipkolli.dailynav;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST Controller for exposing Natural Language search capabilities. */
@RestController
@RequestMapping("/api/v1/daily-nav/ai")
public class AiSearchController {

  private final NaturalLanguageSearchService searchService;

  public AiSearchController(NaturalLanguageSearchService searchService) {
    this.searchService = searchService;
  }

  @PostMapping("/search")
  public SearchResponse search(@RequestBody SearchRequest request) {
    String answer = searchService.search(request.query());
    return new SearchResponse(answer);
  }

  public record SearchRequest(String query) {}

  public record SearchResponse(String answer) {}
}
