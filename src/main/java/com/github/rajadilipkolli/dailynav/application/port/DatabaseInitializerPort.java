package com.github.rajadilipkolli.dailynav.application.port;

public interface DatabaseInitializerPort {
  /**
   * Determines whether the database has been initialized.
   *
   * @return {@code true} if the database has been initialized, {@code false} otherwise
   */
  boolean isInitialized();

  /**
   * Requests on-demand loading of NAV data for a scheme when a backing source is available.
   *
   * @param schemeCode the scheme whose NAV data should be made available
   */
  void seedNavForScheme(int schemeCode);

  /**
   * Requests on-demand loading of NAV data for the scheme associated with an ISIN.
   *
   * @param isin the ISIN whose scheme NAV data should be made available
   */
  void seedNavForIsin(String isin);
}
