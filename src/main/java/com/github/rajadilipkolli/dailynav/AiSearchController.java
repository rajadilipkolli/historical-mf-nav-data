package com.github.rajadilipkolli.dailynav;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
  public ResponseEntity<SearchResponse> search(
      @RequestBody(required = false) SearchRequest request) {
    if (request == null || request.query() == null || request.query().isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    String answer = searchService.search(request.query());
    return ResponseEntity.ok(new SearchResponse(answer));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
  }

  public record SearchRequest(String query) {}

  public record SearchResponse(String answer) {}
}
