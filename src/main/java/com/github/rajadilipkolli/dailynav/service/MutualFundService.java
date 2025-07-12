package com.github.rajadilipkolli.dailynav.service;

import com.github.rajadilipkolli.dailynav.model.*;
import com.github.rajadilipkolli.dailynav.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for mutual fund data operations
 */
@Service
public class MutualFundService {
    
    private final NavRepository navRepository;
    private final NavByIsinRepository navByIsinRepository;
    private final SchemeRepository schemeRepository;
    private final SecurityRepository securityRepository;
    
    public MutualFundService(NavRepository navRepository, 
                           NavByIsinRepository navByIsinRepository,
                           SchemeRepository schemeRepository, 
                           SecurityRepository securityRepository) {
        this.navRepository = navRepository;
        this.navByIsinRepository = navByIsinRepository;
        this.schemeRepository = schemeRepository;
        this.securityRepository = securityRepository;
    }
    
    /**
     * Get latest NAV by ISIN
     */
    public NavByIsin getLatestNavByIsin(String isin) {
        return navByIsinRepository.findLatestByIsin(isin);
    }
    
    /**
     * Get NAV by ISIN for a specific date (or closest date before)
     */
    public NavByIsin getNavByIsinAndDate(String isin, LocalDate date) {
        return navByIsinRepository.findByIsinAndDateOnOrBefore(isin, date);
    }
    
    /**
     * Get last N days NAV for an ISIN
     */
    public List<NavByIsin> getLastNDaysNav(String isin, int days) {
        return navByIsinRepository.findLastNByIsin(isin, days);
    }
    
    /**
     * Get NAV history for ISIN within date range
     */
    public List<NavByIsin> getNavHistory(String isin, LocalDate startDate, LocalDate endDate) {
        return navByIsinRepository.findByIsinAndDateBetween(isin, startDate, endDate);
    }
    
    /**
     * Get scheme information by scheme code
     */
    public Scheme getScheme(Integer schemeCode) {
        return schemeRepository.findBySchemeCode(schemeCode);
    }
    
    /**
     * Search schemes by name
     */
    public List<Scheme> searchSchemes(String namePattern) {
        return schemeRepository.findBySchemeNameContaining(namePattern);
    }
    
    /**
     * Get all schemes
     */
    public List<Scheme> getAllSchemes() {
        return schemeRepository.findAll();
    }
    
    /**
     * Get security information by ISIN
     */
    public Security getSecurity(String isin) {
        return securityRepository.findByIsin(isin);
    }
    
    /**
     * Get complete fund information (scheme + security) by ISIN
     */
    public FundInfo getFundInfo(String isin) {
        Security security = getSecurity(isin);
        if (security == null) {
            return null;
        }
        
        Scheme scheme = getScheme(security.getSchemeCode());
        return new FundInfo(security, scheme);
    }
    
    /**
     * Helper class to combine security and scheme information
     */
    public static class FundInfo {
        private final Security security;
        private final Scheme scheme;
        
        public FundInfo(Security security, Scheme scheme) {
            this.security = security;
            this.scheme = scheme;
        }
        
        public Security getSecurity() {
            return security;
        }
        
        public Scheme getScheme() {
            return scheme;
        }
        
        public String getIsin() {
            return security != null ? security.getIsin() : null;
        }
        
        public String getSchemeName() {
            return scheme != null ? scheme.getSchemeName() : null;
        }
        
        public Integer getSchemeCode() {
            return security != null ? security.getSchemeCode() : null;
        }
        
        public Integer getType() {
            return security != null ? security.getType() : null;
        }
        
        public String getTypeDescription() {
            if (security == null || security.getType() == null) {
                return "Unknown";
            }
            return security.getType() == 0 ? "Growth/Dividend Payout" : "Dividend Reinvestment";
        }
        
        @Override
        public String toString() {
            return "FundInfo{" +
                    "isin='" + getIsin() + '\'' +
                    ", schemeName='" + getSchemeName() + '\'' +
                    ", type='" + getTypeDescription() + '\'' +
                    '}';
        }
    }
}
