package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.util.List;

public interface DatabaseInitializerPort {
  /**
   * Determines whether the database has been initialized.
   *
   * @return {@code true} if the database has been initialized, {@code false} otherwise
   */
  boolean isInitialized();

  /** Checks whether the PostgreSQL database already has NAV data for the given scheme. */
  boolean hasNavForScheme(int schemeCode);

  /** Fallback read from the retained SQLite database for NAV rows by scheme code. */
  List<Nav> getFallbackNavForScheme(int schemeCode);

  /** Fallback read from the retained SQLite database for NAV rows by ISIN. */
  List<NavByIsin> getFallbackNavForIsin(String isin);

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
