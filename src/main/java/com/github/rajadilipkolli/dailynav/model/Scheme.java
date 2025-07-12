package com.github.rajadilipkolli.dailynav.model;

/** Represents a mutual fund scheme (immutable) */
public record Scheme(Integer schemeCode, String schemeName) {
  @Override
  public String toString() {
    return "Scheme{" + "schemeCode=" + schemeCode + ", schemeName='" + schemeName + '\'' + '}';
  }
}
