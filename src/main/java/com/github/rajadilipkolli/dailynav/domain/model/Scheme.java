package com.github.rajadilipkolli.dailynav.domain.model;

/**
 * Represents a mutual fund scheme (immutable)
 *
 * @param schemeCode the unique identifier code for the scheme
 * @param schemeName the name of the scheme
 * @param amc the asset management company (fund house)
 * @param category the scheme category
 * @param plan the plan type (e.g., Direct, Regular)
 * @param option the option type (e.g., Growth, Dividend)
 */
public record Scheme(
    Integer schemeCode,
    String schemeName,
    String amc,
    String category,
    String plan,
    String option) {
  @Override
  public String toString() {
    return "Scheme{"
        + "schemeCode="
        + schemeCode
        + ", schemeName='"
        + schemeName
        + '\''
        + ", amc='"
        + amc
        + '\''
        + ", category='"
        + category
        + '\''
        + ", plan='"
        + plan
        + '\''
        + ", option='"
        + option
        + '\''
        + '}';
  }
}
