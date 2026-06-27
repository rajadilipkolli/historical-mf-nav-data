package com.github.rajadilipkolli.dailynav;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for AI capabilities in the Daily NAV library. */
@ConfigurationProperties(prefix = "daily-nav.ai")
public class DailyNavAiProperties {

  /** Master toggle to enable or disable AI capabilities. Disabled by default. */
  private boolean enabled = false;

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
