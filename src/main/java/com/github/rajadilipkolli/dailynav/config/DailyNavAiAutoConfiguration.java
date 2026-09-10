package com.github.rajadilipkolli.dailynav.config;

import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.application.port.ReportAssemblyPort;
import com.github.rajadilipkolli.dailynav.application.port.TextToSqlPort;
import com.github.rajadilipkolli.dailynav.application.service.KnowledgeSearchService;
import com.github.rajadilipkolli.dailynav.application.service.MutualFundService;
import com.github.rajadilipkolli.dailynav.application.service.MutualFundTools;
import com.github.rajadilipkolli.dailynav.application.service.NaturalLanguageSearchService;
import com.github.rajadilipkolli.dailynav.application.service.PerformanceReportService;
import com.github.rajadilipkolli.dailynav.application.service.TrendAnomalyService;
import com.github.rajadilipkolli.dailynav.application.service.assembler.ReportDataAssembler;
import com.github.rajadilipkolli.dailynav.configproperties.DailyNavAiProperties;
import com.github.rajadilipkolli.dailynav.infrastructure.ai.SchemeDocumentIngestionService;
import com.github.rajadilipkolli.dailynav.infrastructure.ai.TextToSqlGenerator;
import com.github.rajadilipkolli.dailynav.infrastructure.web.AiSearchController;
import com.github.rajadilipkolli.dailynav.infrastructure.web.AiTrendController;
import com.github.rajadilipkolli.dailynav.infrastructure.web.KnowledgeSearchController;
import com.github.rajadilipkolli.dailynav.infrastructure.web.PerformanceReportController;
import java.io.File;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/** Auto-configuration for AI features in Daily NAV. */
@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@ConditionalOnProperty(prefix = "daily-nav.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DailyNavAiProperties.class)
public class DailyNavAiAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(ChatClient.class)
  public ChatClient dailyNavChatClient(ChatClient.Builder builder) {

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

  /**
   * Creates the text-to-SQL service used to generate queries for daily NAV data.
   *
   * @param jdbcTemplate the JDBC template for the daily NAV database
   * @return the text-to-SQL service
   */
  @Bean
  @ConditionalOnMissingBean
  public TextToSqlPort textToSqlPort(
      ObjectProvider<ChatClient> chatClientProvider,
      @Qualifier("dailyNavJdbcTemplate") JdbcTemplate jdbcTemplate) {
    return new TextToSqlGenerator(chatClientProvider.getIfAvailable(), jdbcTemplate);
  }

  /**
   * Creates the service used to process natural-language mutual fund searches.
   *
   * @param dailyNavChatClient the chat client used to interpret search requests
   * @param mutualFundService the mutual fund service used to retrieve fund data
   * @param mutualFundTools the tools available for mutual fund operations
   * @param knowledgeSearchService the service used to search supporting knowledge
   * @param textToSqlPort the port used to generate SQL from natural-language requests
   * @return the configured natural-language search service
   */
  @Bean
  @ConditionalOnMissingBean
  public NaturalLanguageSearchService naturalLanguageSearchService(
      ChatClient dailyNavChatClient,
      MutualFundService mutualFundService,
      MutualFundTools mutualFundTools,
      KnowledgeSearchService knowledgeSearchService,
      TextToSqlPort textToSqlPort) {
    return new NaturalLanguageSearchService(
        dailyNavChatClient,
        mutualFundService,
        mutualFundTools,
        knowledgeSearchService,
        textToSqlPort);
  }

  @Bean
  @ConditionalOnWebApplication
  @ConditionalOnMissingBean
  public AiSearchController aiSearchController(NaturalLanguageSearchService searchService) {
    return new AiSearchController(searchService);
  }

  /**
   * Creates the service used to analyze NAV trends and detect anomalies.
   *
   * @param navLookupPort the port used to retrieve NAV data
   * @param chatClientProvider the provider for an optional chat client
   * @return the configured trend anomaly service
   */
  @Bean
  @ConditionalOnMissingBean
  public TrendAnomalyService trendAnomalyService(
      NavLookupPort navLookupPort, ObjectProvider<ChatClient> chatClientProvider) {
    return new TrendAnomalyService(navLookupPort, chatClientProvider);
  }

  /**
   * Creates the controller for exposing AI-powered NAV trend analysis in web applications.
   *
   * @param trendAnomalyService the service used to analyze NAV trends
   * @return the configured trend analysis controller
   */
  @Bean
  @ConditionalOnWebApplication
  @ConditionalOnMissingBean
  public AiTrendController aiTrendController(TrendAnomalyService trendAnomalyService) {
    return new AiTrendController(trendAnomalyService);
  }

  /**
   * Creates the report assembly service for combining mutual fund and trend analysis data.
   *
   * @param mutualFundService service for retrieving mutual fund data
   * @param trendAnomalyService service for retrieving trend and anomaly data
   * @return the report assembly port
   */
  @Bean
  @ConditionalOnMissingBean
  public ReportAssemblyPort reportAssemblyPort(
      MutualFundService mutualFundService, TrendAnomalyService trendAnomalyService) {
    return new ReportDataAssembler(mutualFundService, trendAnomalyService);
  }

  /**
   * Creates the service used to generate performance reports.
   *
   * @param reportAssemblyPort assembles the data required for performance reports
   * @return the configured performance report service
   */
  @Bean
  @ConditionalOnMissingBean
  public PerformanceReportService performanceReportService(
      ObjectProvider<ChatClient> chatClientProvider, ReportAssemblyPort reportAssemblyPort) {
    ChatClient chatClient = chatClientProvider.getIfAvailable();
    return new PerformanceReportService(chatClient, reportAssemblyPort);
  }

  /**
   * Creates the controller for exposing performance reports.
   *
   * @param performanceReportService the service used to generate performance reports
   * @return the performance report controller
   */
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
