package com.github.rajadilipkolli.dailynav.model;

/**
 * Represents a mutual fund scheme
 */
public class Scheme {
    private Integer schemeCode;
    private String schemeName;

    public Scheme() {}

    public Scheme(Integer schemeCode, String schemeName) {
        this.schemeCode = schemeCode;
        this.schemeName = schemeName;
    }

    public Integer getSchemeCode() {
        return schemeCode;
    }

    /**
     * Sets the scheme code for this mutual fund scheme
     * @param schemeCode the scheme code to set
     */
    public void setSchemeCode(Integer schemeCode) {
        this.schemeCode = schemeCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    /**
     * Sets the scheme name for this mutual fund scheme
     * @param schemeName the scheme name to set
     */
    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    @Override
    public String toString() {
        return "Scheme{" +
                "schemeCode=" + schemeCode +
                ", schemeName='" + schemeName + '\'' +
                '}';
    }
}
