package com.github.rajadilipkolli.dailynav;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;

public class TextToSqlGenerator {

  private static final Logger logger = LoggerFactory.getLogger(TextToSqlGenerator.class);
  private static final int MAX_RETRIES = 3;

  private final ChatClient chatClient;
  private final JdbcTemplate jdbcTemplate;
  private final TrendAnomalyService trendAnomalyService;

  private static final String SYSTEM_PROMPT =
      """
      You are an expert SQL assistant for a read-only SQLite database containing Indian Mutual Fund data.
      You MUST write a valid SQLite SELECT query to answer the user's request.
      Do NOT include markdown formatting, backticks, or explanations in your response. Return ONLY the raw SQL string.

      The database has the following schema:
      - `schemes` (scheme_code INTEGER PRIMARY KEY, scheme_name TEXT)
      - `securities` (isin TEXT PRIMARY KEY, type TEXT, scheme_code INTEGER)
      - `nav` (scheme_code INTEGER, date TEXT, nav REAL)
      - `nav_by_isin` (isin TEXT, date TEXT, nav REAL)

      Rules:
      1. ONLY generate SELECT queries.
      2. If you don't know how to answer or if the question is outside this schema, return 'UNKNOWN'.
      3. Use `nav_by_isin` when querying by ISIN, and `nav` when querying by scheme_code.
      """;

  public TextToSqlGenerator(
      ChatClient chatClient, JdbcTemplate jdbcTemplate, TrendAnomalyService trendAnomalyService) {
    this.chatClient = chatClient.mutate().defaultSystem(SYSTEM_PROMPT).build();
    this.jdbcTemplate = jdbcTemplate;
    this.trendAnomalyService = trendAnomalyService;
  }

  public String execute(String userQuery) {
    String currentPrompt = userQuery;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        String sql = chatClient.prompt(currentPrompt).call().content().strip();

        // Remove markdown backticks if LLM mistakenly includes them
        if (sql.startsWith("```sql")) {
          sql = sql.substring(6);
        }
        if (sql.startsWith("```")) {
          sql = sql.substring(3);
        }
        if (sql.endsWith("```")) {
          sql = sql.substring(0, sql.length() - 3);
        }
        sql = sql.strip();

        if (sql.equalsIgnoreCase("UNKNOWN")) {
          return "I cannot answer this query using the available mutual fund database.";
        }

        if (!sql.toUpperCase().startsWith("SELECT ")) {
          throw new IllegalArgumentException("Only SELECT queries are permitted.");
        }

        // Dry-run EXPLAIN QUERY PLAN
        jdbcTemplate.execute("EXPLAIN QUERY PLAN " + sql);

        // Execute query
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        if (results.isEmpty()) {
          return "The query executed successfully but returned no results.";
        }

        return formatResults(results);

      } catch (Exception e) {
        logger.warn("Text-to-SQL execution failed on attempt {}: {}", attempt, e.getMessage());
        if (attempt == MAX_RETRIES) {
          return "I'm sorry, I was unable to generate a valid SQL query to answer your question after several attempts.";
        }
        currentPrompt =
            userQuery
                + "\n\nYour previous SQL failed with error: "
                + e.getMessage()
                + ". Please provide a corrected SQL query.";
      }
    }

    return "Unable to process the request.";
  }

  private String formatResults(List<Map<String, Object>> results) {
    StringBuilder sb = new StringBuilder();
    if (!results.isEmpty()) {
      Map<String, Object> firstRow = results.get(0);
      for (String key : firstRow.keySet()) {
        sb.append(key).append(" | ");
      }
      sb.append("\n");
      for (int i = 0; i < firstRow.size(); i++) {
        sb.append("--- | ");
      }
      sb.append("\n");

      for (Map<String, Object> row : results) {
        for (Object value : row.values()) {
          sb.append(value != null ? value.toString() : "null").append(" | ");
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }
}
