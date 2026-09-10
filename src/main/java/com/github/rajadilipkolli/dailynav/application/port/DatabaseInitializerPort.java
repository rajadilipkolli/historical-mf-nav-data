package com.github.rajadilipkolli.dailynav.application.port;

public interface DatabaseInitializerPort {
  /**
   * Determines whether the database has been initialized.
   *
   * @return {@code true} if the database has been initialized, {@code false} otherwise
   */
  boolean isInitialized();

  /**
   * Seeds the NAV data for a specific scheme code on demand.
   *
   * @param schemeCode the scheme code
   */
  void seedNavForScheme(int schemeCode);

  /**
   * Seeds the NAV data for a specific ISIN on demand.
   *
   * @param isin the ISIN
   */
  void seedNavForIsin(String isin);
}
