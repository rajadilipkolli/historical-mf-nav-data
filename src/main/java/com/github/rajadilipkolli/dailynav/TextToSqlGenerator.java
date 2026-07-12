package com.github.rajadilipkolli.dailynav;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

public class TextToSqlGenerator {

  private static final Logger logger = LoggerFactory.getLogger(TextToSqlGenerator.class);
  private static final int MAX_RETRIES = 3;

  private final ChatClient chatClient;
  private final JdbcTemplate jdbcTemplate;

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

  public TextToSqlGenerator(ChatClient chatClient, JdbcTemplate jdbcTemplate) {
    this.chatClient = chatClient.mutate().defaultSystem(SYSTEM_PROMPT).build();
    this.jdbcTemplate = jdbcTemplate;
  }

  public String execute(String userQuery) {
    String currentPrompt = userQuery;

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        String rawSql = chatClient.prompt(currentPrompt).call().content();
        if (rawSql == null) {
          throw new IllegalStateException("LLM returned an empty response.");
        }
        String sql = rawSql.strip();

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

        // Strip trailing semicolon if present
        if (sql.endsWith(";")) {
          sql = sql.substring(0, sql.length() - 1).strip();
        }

        // Reject generated SQL containing semicolons outside string literals
        String sqlWithoutStrings = sql.replaceAll("'[^']*'", "");
        if (sqlWithoutStrings.contains(";")) {
          throw new IllegalArgumentException("Multiple statements or semicolons are not allowed.");
        }

        String upperSql = sql.toUpperCase();
        if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("WITH")) {
          throw new IllegalArgumentException("Only SELECT or WITH queries are permitted.");
        }

        final String finalSql = sql;

        // Ensure the connection is configured read-only before executing either validation or
        // result queries
        List<Map<String, Object>> results =
            jdbcTemplate.execute(
                (Connection con) -> {
                  boolean wasReadOnly = con.isReadOnly();
                  try {
                    con.setReadOnly(true);

                    // Replace concatenated execute EXPLAIN dry-run with a safer queryForList-based
                    // approach
                    // We use prepareStatement to be safe, though EXPLAIN doesn't execute the query
                    try (PreparedStatement explainPs =
                            con.prepareStatement("EXPLAIN QUERY PLAN " + finalSql);
                        ResultSet explainRs = explainPs.executeQuery()) {
                      // dry-run successful
                    }

                    // Execute actual query
                    try (PreparedStatement ps = con.prepareStatement(finalSql)) {
                      ps.setMaxRows(100);
                      try (ResultSet rs = ps.executeQuery()) {
                        ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
                        List<Map<String, Object>> list = new ArrayList<>();
                        int rowNum = 0;
                        while (rs.next() && rowNum < 100) {
                          list.add(rowMapper.mapRow(rs, rowNum++));
                        }
                        return list;
                      }
                    }
                  } finally {
                    con.setReadOnly(wasReadOnly);
                  }
                });

        if (results == null || results.isEmpty()) {
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
      Map<String, Object> firstRow = results.getFirst();
      for (String key : firstRow.keySet()) {
        sb.append(key).append(" | ");
      }
      sb.append("\n");
      sb.repeat("--- | ", firstRow.size());
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
