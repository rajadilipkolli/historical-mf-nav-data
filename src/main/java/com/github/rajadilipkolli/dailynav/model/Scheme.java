package com.github.rajadilipkolli.dailynav.model;

/**
 * Represents a mutual fund scheme (immutable)
 *
 * @param schemeCode the unique identifier code for the scheme
 * @param schemeName the name of the scheme
 */
public record Scheme(Integer schemeCode, String schemeName) {
  @Override
  public String toString() {
    return "Scheme{" + "schemeCode=" + schemeCode + ", schemeName='" + schemeName + '\'' + '}';
  }
}
