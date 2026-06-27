package com.github.rajadilipkolli.dailynav;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

public class KnowledgeSearchService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;
  private final DailyNavAiProperties properties;

  public KnowledgeSearchService(
      ChatClient chatClient, VectorStore vectorStore, DailyNavAiProperties properties) {
    this.chatClient = chatClient;
    this.vectorStore = vectorStore;
    this.properties = properties;
  }

  public KnowledgeSearchResponse search(String query) {
    List<Document> documents =
        vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(properties.getTopK()).build());

    if (documents.isEmpty()) {
      return new KnowledgeSearchResponse(
          "No documents available in the knowledge base to answer this question.", List.of());
    }

    String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

    String prompt =
        "You are an expert mutual fund assistant. Answer the user's question using ONLY the provided context.\n\n"
            + "Context:\n"
            + context
            + "\n\n"
            + "Question:\n"
            + query;

    String answer = chatClient.prompt(prompt).call().content();

    List<String> sources =
        documents.stream()
            .map(doc -> doc.getMetadata().getOrDefault("filename", "unknown").toString())
            .distinct()
            .collect(Collectors.toList());

    return new KnowledgeSearchResponse(answer, sources);
  }

  public record KnowledgeSearchResponse(String answer, List<String> sources) {}
}
