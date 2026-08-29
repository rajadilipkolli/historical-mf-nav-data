package com.github.rajadilipkolli.dailynav;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/** Service for performing knowledge-based searches using vector store and AI. */
public class KnowledgeSearchService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;
  private final DailyNavAiProperties properties;

  /**
   * Constructs a new KnowledgeSearchService.
   *
   * @param chatClient the chat client for AI interactions
   * @param vectorStore the vector store for similarity search
   * @param properties the configuration properties for AI search
   */
  public KnowledgeSearchService(
      ChatClient chatClient, VectorStore vectorStore, DailyNavAiProperties properties) {
    this.chatClient = chatClient;
    this.vectorStore = vectorStore;
    this.properties = properties;
  }

  /**
   * Performs a knowledge-based search using the provided query.
   *
   * @param query the search query
   * @return the search response containing the answer and sources
   */
  public KnowledgeSearchResponse search(String query) {
    List<Document> documents =
        vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(properties.getTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .build());

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

  /**
   * Response payload for knowledge-based search
   *
   * @param answer the AI-generated answer based on retrieved documents
   * @param sources the list of source document filenames used to generate the answer
   */
  public record KnowledgeSearchResponse(String answer, List<String> sources) {}
}
