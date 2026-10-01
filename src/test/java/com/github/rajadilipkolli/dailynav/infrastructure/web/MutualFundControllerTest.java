package com.github.rajadilipkolli.dailynav.infrastructure.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.rajadilipkolli.dailynav.application.port.NavLookupPort;
import com.github.rajadilipkolli.dailynav.application.port.SchemePort;
import com.github.rajadilipkolli.dailynav.application.port.SecurityPort;
import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import com.github.rajadilipkolli.dailynav.domain.model.Security;
import com.github.rajadilipkolli.dailynav.domain.search.SchemeSearchCriteria;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MutualFundControllerTest {
  private NavLookupPort navLookupPort;
  private SchemePort schemePort;
  private SecurityPort securityPort;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    navLookupPort = mock(NavLookupPort.class);
    schemePort = mock(SchemePort.class);
    securityPort = mock(SecurityPort.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new MutualFundController(navLookupPort, schemePort, securityPort))
            .build();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/nav/latest/MISSING",
        "/nav/MISSING/2025-07-01",
        "/security/MISSING",
        "/scheme/999"
      })
  void missingLookupReturnsNotFound(String path) throws Exception {
    mockMvc
        .perform(get("/api/v1" + path))
        .andExpect(status().isNotFound())
        .andExpect(content().string(""));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/nav/latest/ISIN1", "/nav/ISIN1/2025-07-01"})
  void presentNavReturnsOkWithBody(String path) throws Exception {
    NavByIsin nav = new NavByIsin();
    nav.setIsin("ISIN1");
    nav.setDate(LocalDate.of(2025, 6, 30));
    nav.setNav(123.45);
    when(navLookupPort.findLatestByIsin("ISIN1")).thenReturn(Optional.of(nav));
    when(navLookupPort.findByIsinAndDateOnOrBefore("ISIN1", LocalDate.of(2025, 7, 1)))
        .thenReturn(Optional.of(nav));

    mockMvc
        .perform(get("/api/v1" + path))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isin").value("ISIN1"))
        .andExpect(jsonPath("$.date").value("2025-06-30"))
        .andExpect(jsonPath("$.nav").value(123.45));
  }

  @Test
  void presentSecurityReturnsOkWithBody() throws Exception {
    Security security = new Security();
    security.setIsin("ISIN1");
    security.setSchemeCode(1);
    security.setType(0);
    when(securityPort.findByIsin("ISIN1")).thenReturn(Optional.of(security));

    mockMvc
        .perform(get("/api/v1/security/ISIN1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isin").value("ISIN1"))
        .andExpect(jsonPath("$.schemeCode").value(1))
        .andExpect(jsonPath("$.type").value(0));
  }

  @Test
  void presentSchemeReturnsOkWithBody() throws Exception {
    when(schemePort.findBySchemeCode(1)).thenReturn(Optional.of(scheme()));

    mockMvc
        .perform(get("/api/v1/scheme/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schemeCode").value(1))
        .andExpect(jsonPath("$.schemeName").value("Test Fund"));
  }

  @Test
  void searchDefaultsToFirstPageOfTwenty() throws Exception {
    SchemeSearchCriteria criteria = criteria(0, 20);
    when(schemePort.search(criteria)).thenReturn(List.of(scheme()));

    mockMvc
        .perform(get("/api/v1/schemes/search").param("name", "Fund"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].schemeName").value("Test Fund"));
    verify(schemePort).search(criteria);
    verifyNoMoreInteractions(schemePort);
  }

  @ParameterizedTest
  @CsvSource({"2,5,2,5", "0,101,0,100", "0,2147483647,0,100", "-1,0,0,20", "0,-1,0,20"})
  void searchBoundsCriteriaBeforeQuerying(int page, int size, int expectedPage, int expectedSize)
      throws Exception {
    SchemeSearchCriteria criteria = criteria(expectedPage, expectedSize);
    when(schemePort.search(criteria)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/v1/schemes/search")
                .param("name", "Fund")
                .param("page", Integer.toString(page))
                .param("pageSize", Integer.toString(size)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    verify(schemePort).search(criteria);
    verifyNoMoreInteractions(schemePort);
  }

  private SchemeSearchCriteria criteria(int page, int pageSize) {
    return new SchemeSearchCriteria("Fund", null, null, null, null, null, null, page, pageSize);
  }

  private Scheme scheme() {
    return new Scheme(1, "Test Fund", "AMC", "Equity", "Direct", "Growth");
  }
}
