package com.github.rajadilipkolli.dailynav;

import java.util.List;
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

  @PostMapping
  public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) {
    if (request.query() == null || request.query().isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    var result = knowledgeSearchService.search(request.query());
    return ResponseEntity.ok(new AskResponse(result.answer(), result.sources()));
  }

  public record AskRequest(String query) {}

  public record AskResponse(String answer, List<String> sources) {}
}
