package com.github.rajadilipkolli.dailynav.model;

import java.time.LocalDate;

/** Represents NAV data accessible by ISIN */
public class NavByIsin {
  private String isin;
  private LocalDate date;
  private Double nav;

  public NavByIsin() {}

  public String getIsin() {
    return isin;
  }

  /**
   * Sets the ISIN (International Securities Identification Number) for this NAV record
   *
   * @param isin the ISIN to set
   */
  public void setIsin(String isin) {
    this.isin = isin;
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
    return "NavByIsin{" + "isin='" + isin + '\'' + ", date=" + date + ", nav=" + nav + '}';
  }
}
