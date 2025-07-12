package com.github.rajadilipkolli.dailynav.model;

/** Represents a security with ISIN information */
public class Security {
  private String isin;
  private Integer type; // 0=Growth/Dividend Payout, 1=Dividend Reinvestment
  private Integer schemeCode;

  public Security() {}

  public String getIsin() {
    return isin;
  }

  /**
   * Sets the ISIN (International Securities Identification Number) for this security
   *
   * @param isin the ISIN to set
   */
  public void setIsin(String isin) {
    this.isin = isin;
  }

  public Integer getType() {
    return type;
  }

  /**
   * Sets the type of security (0=Growth/Dividend Payout, 1=Dividend Reinvestment)
   *
   * @param type the type to set
   */
  public void setType(Integer type) {
    this.type = type;
  }

  public Integer getSchemeCode() {
    return schemeCode;
  }

  /**
   * Sets the scheme code for this security
   *
   * @param schemeCode the scheme code to set
   */
  public void setSchemeCode(Integer schemeCode) {
    this.schemeCode = schemeCode;
  }

  @Override
  public String toString() {
    return "Security{"
        + "isin='"
        + isin
        + '\''
        + ", type="
        + type
        + ", schemeCode="
        + schemeCode
        + '}';
  }
}
