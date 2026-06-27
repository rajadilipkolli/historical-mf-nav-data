package com.github.rajadilipkolli.dailynav;

import org.springframework.ai.chat.client.ChatClient;

/** Service orchestrating natural language search against the Mutual Fund API. */
public class NaturalLanguageSearchService {

  private final ChatClient chatClient;
  private final MutualFundService mutualFundService;
  private final MutualFundTools mutualFundTools;

  public NaturalLanguageSearchService(
      ChatClient chatClient, MutualFundService mutualFundService, MutualFundTools mutualFundTools) {
    this.chatClient = chatClient;
    this.mutualFundService = mutualFundService;
    this.mutualFundTools = mutualFundTools;
  }

  /**
   * Processes a natural language query by leveraging the LLM to call appropriate tools.
   *
   * @param query The natural language user query.
   * @return The AI-generated answer.
   */
  public String search(String query) {
    if (!mutualFundService.isReady()) {
      return "The mutual fund database is currently initializing. Please try again in a few moments.";
    }

    return chatClient.prompt().user(query).tools(mutualFundTools).call().content();
  }
}
