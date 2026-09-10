package com.github.rajadilipkolli.dailynav.application.port;

public interface TextToSqlPort {
  /**
   * Converts a user query into a SQL statement.
   *
   * @param userQuery the query to convert
   * @return the generated SQL statement
   */
  String execute(String userQuery);
}
