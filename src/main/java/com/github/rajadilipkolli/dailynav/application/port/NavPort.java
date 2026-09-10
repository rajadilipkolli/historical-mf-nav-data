package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import java.util.List;

public interface NavPort {
  /**
   * Finds NAV records associated with a scheme code.
   *
   * @param schemeCode the scheme code used to identify the records
   * @return the NAV records associated with the scheme code
   */
  List<Nav> findBySchemeCode(Integer schemeCode);
}
