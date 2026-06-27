package com.github.rajadilipkolli.dailynav;

import java.io.File;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for AI features in Daily NAV. */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@ConditionalOnProperty(prefix = "daily-nav.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DailyNavAiProperties.class)
public class DailyNavAiAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "dailyNavChatClient")
  public ChatClient dailyNavChatClient(
      ChatClient.Builder builder, DailyNavAiProperties properties) {

    return builder
        .defaultSystem(
            "You are an expert financial assistant embedded in the Daily NAV library. "
                + "You help users understand Indian mutual funds, scheme metrics, and NAV (Net Asset Value) histories.")
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public MutualFundTools mutualFundTools(MutualFundService mutualFundService) {
    return new MutualFundTools(mutualFundService);
  }

  @Bean
  @ConditionalOnMissingBean
  public NaturalLanguageSearchService naturalLanguageSearchService(
      ChatClient dailyNavChatClient,
      MutualFundService mutualFundService,
      MutualFundTools mutualFundTools) {
    return new NaturalLanguageSearchService(dailyNavChatClient, mutualFundService, mutualFundTools);
  }

  @Bean
  @ConditionalOnWebApplication
  @ConditionalOnMissingBean
  public AiSearchController aiSearchController(NaturalLanguageSearchService searchService) {
    return new AiSearchController(searchService);
  }

  @Bean
  @ConditionalOnMissingBean
  public TrendAnomalyService trendAnomalyService(
      NavByIsinRepository navByIsinRepository, ObjectProvider<ChatClient> chatClientProvider) {
    return new TrendAnomalyService(navByIsinRepository, chatClientProvider);
  }

  @Bean
  @ConditionalOnWebApplication
  @ConditionalOnMissingBean
  public AiTrendController aiTrendController(TrendAnomalyService trendAnomalyService) {
    return new AiTrendController(trendAnomalyService);
  }

  @Bean
  @ConditionalOnMissingBean
  public ReportDataAssembler reportDataAssembler(
      MutualFundService mutualFundService, TrendAnomalyService trendAnomalyService) {
    return new ReportDataAssembler(mutualFundService, trendAnomalyService);
  }

  @Bean
  @ConditionalOnMissingBean
  public PerformanceReportService performanceReportService(
      ObjectProvider<ChatClient> chatClientProvider, ReportDataAssembler reportDataAssembler) {
    ChatClient chatClient = chatClientProvider.getIfAvailable();
    return new PerformanceReportService(chatClient, reportDataAssembler);
  }

  @Bean
  @ConditionalOnMissingBean
  public PerformanceReportController performanceReportController(
      PerformanceReportService performanceReportService) {
    return new PerformanceReportController(performanceReportService);
  }

  @Bean
  @ConditionalOnMissingBean(name = "dailyNavVectorStore")
  public VectorStore dailyNavVectorStore(
      EmbeddingModel embeddingModel, DailyNavAiProperties properties) {
    SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

    // Attempt to load from persistence if a path is configured and exists
    String path = properties.getRagDocumentPath();
    if (path != null && !path.trim().isEmpty()) {
      File file = new File(path, "vectorstore.json");
      if (file.exists()) {
        vectorStore.load(file);
      }
    }
    return vectorStore;
  }

  @Bean
  @ConditionalOnMissingBean
  public SchemeDocumentIngestionService schemeDocumentIngestionService(
      VectorStore dailyNavVectorStore, DailyNavAiProperties properties) {
    return new SchemeDocumentIngestionService(dailyNavVectorStore, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public KnowledgeSearchService knowledgeSearchService(
      ChatClient dailyNavChatClient,
      VectorStore dailyNavVectorStore,
      DailyNavAiProperties properties) {
    return new KnowledgeSearchService(dailyNavChatClient, dailyNavVectorStore, properties);
  }

  @Bean
  @ConditionalOnWebApplication
  @ConditionalOnMissingBean
  public KnowledgeSearchController knowledgeSearchController(
      KnowledgeSearchService knowledgeSearchService) {
    return new KnowledgeSearchController(knowledgeSearchService);
  }
}
