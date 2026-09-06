package com.github.rajadilipkolli.dailynav.application.port;

public interface DatabaseInitializerPort {
  /**
   * Determines whether the database has been initialized.
   *
   * @return {@code true} if the database has been initialized, {@code false} otherwise
   */
  boolean isInitialized();
}
