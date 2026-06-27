package com.github.rajadilipkolli.dailynav;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for AI capabilities in the Daily NAV library. */
@ConfigurationProperties(prefix = "daily-nav.ai")
public class DailyNavAiProperties {

  /** Master toggle to enable or disable AI capabilities. Disabled by default. */
  private boolean enabled = false;

  /** Name of the chat model to use. */
  private String chatModel;

  /** Name of the embedding model to use. */
  private String embeddingModel;

  /** Base URL for the Ollama instance. */
  private String ollamaBaseUrl = "http://localhost:11434";

  /** Path to the document source corpus for RAG capabilities. */
  private String ragDocumentPath;

  /** Top-K retrieval size for similarity search in RAG. */
  private int topK = 5;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getChatModel() {
    return chatModel;
  }

  public void setChatModel(String chatModel) {
    this.chatModel = chatModel;
  }

  public String getEmbeddingModel() {
    return embeddingModel;
  }

  public void setEmbeddingModel(String embeddingModel) {
    this.embeddingModel = embeddingModel;
  }

  public String getOllamaBaseUrl() {
    return ollamaBaseUrl;
  }

  public void setOllamaBaseUrl(String ollamaBaseUrl) {
    this.ollamaBaseUrl = ollamaBaseUrl;
  }

  public String getRagDocumentPath() {
    return ragDocumentPath;
  }

  public void setRagDocumentPath(String ragDocumentPath) {
    this.ragDocumentPath = ragDocumentPath;
  }

  public int getTopK() {
    return topK;
  }

  public void setTopK(int topK) {
    this.topK = topK;
  }
}
