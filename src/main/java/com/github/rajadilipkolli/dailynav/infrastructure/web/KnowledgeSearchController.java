package com.github.rajadilipkolli.dailynav.infrastructure.web;

import com.github.rajadilipkolli.dailynav.application.service.KnowledgeSearchService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/daily-nav/ai/ask")
public class KnowledgeSearchController {

  private final KnowledgeSearchService knowledgeSearchService;

  public KnowledgeSearchController(KnowledgeSearchService knowledgeSearchService) {
    this.knowledgeSearchService = knowledgeSearchService;
  }

  /**
   * Processes a knowledge search request and returns the answer with its sources.
   *
   * @param request the request containing the search query
   * @return a successful response with the search result, or a bad-request response when the request or query is missing or blank
   */
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AskResponse> ask(@RequestBody(required = false) AskRequest request) {
    if (request == null || request.query() == null || request.query().isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    var result = knowledgeSearchService.search(request.query());
    return ResponseEntity.ok(new AskResponse(result.answer(), result.sources()));
  }

  public record AskRequest(String query) {}

  public record AskResponse(String answer, List<String> sources) {}
}
