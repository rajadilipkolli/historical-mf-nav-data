package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Security;
import java.util.List;
import java.util.Optional;

public interface SecurityPort {
  /**
   * Looks up a security by its International Securities Identification Number.
   *
   * @param isin the security's ISIN
   * @return the matching security, if found
   */
  Optional<Security> findByIsin(String isin);

  /**
   * Finds the ISINs of securities whose scheme name matches the specified pattern.
   *
   * @param namePattern the pattern to match against scheme names
   * @return the matching security ISINs
   */
  List<String> findIsinsBySchemeNamePattern(String namePattern);
}
