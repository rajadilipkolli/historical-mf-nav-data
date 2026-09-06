package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import java.util.List;
import java.util.Optional;

public interface SchemePort {
  /**
   * Finds a scheme by its code.
   *
   * @param schemeCode the code of the scheme to find
   * @return the matching scheme, if found
   */
  Optional<Scheme> findBySchemeCode(Integer schemeCode);

  /**
   * Finds schemes whose names contain the specified pattern.
   *
   * @param namePattern the pattern to search for within scheme names
   * @return the schemes with names containing the specified pattern
   */
  List<Scheme> findBySchemeNameContaining(String namePattern);

  /**
   * Retrieves all schemes.
   *
   * @return all available schemes
   */
  List<Scheme> findAll();
}
