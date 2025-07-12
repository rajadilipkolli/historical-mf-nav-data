package com.github.rajadilipkolli.dailynav.model;

import java.time.LocalDate;

/** Represents a mutual fund NAV (Net Asset Value) record */
public class Nav {
  private Integer schemeCode;
  private LocalDate date;
  private Double nav;

  public Nav() {}

  public Nav(Integer schemeCode, LocalDate date, Double nav) {
    this.schemeCode = schemeCode;
    this.date = date;
    this.nav = nav;
  }

  public Integer getSchemeCode() {
    return schemeCode;
  }

  /**
   * Sets the scheme code for this NAV record
   *
   * @param schemeCode the scheme code to set
   */
  public void setSchemeCode(Integer schemeCode) {
    this.schemeCode = schemeCode;
  }

  public LocalDate getDate() {
    return date;
  }

  /**
   * Sets the date for this NAV record
   *
   * @param date the date to set
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Double getNav() {
    return nav;
  }

  /**
   * Sets the NAV (Net Asset Value) for this record
   *
   * @param nav the NAV value to set
   */
  public void setNav(Double nav) {
    this.nav = nav;
  }

  @Override
  public String toString() {
    return "Nav{" + "schemeCode=" + schemeCode + ", date=" + date + ", nav=" + nav + '}';
  }
}
