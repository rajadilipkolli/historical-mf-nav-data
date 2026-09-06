package com.github.rajadilipkolli.dailynav.domain.report;

import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.model.Security;

/** Represents core fund metadata within a report context. */
public record FundInfo(Security security, Scheme scheme) {

  /**
   * Retrieves the security's ISIN.
   *
   * @return the security's ISIN
   */
  public String getIsin() {
    return security.getIsin();
  }

  /**
   * Gets the associated scheme name.
   *
   * @return the scheme name
   */
  public String getSchemeName() {
    return scheme.schemeName();
  }
}
