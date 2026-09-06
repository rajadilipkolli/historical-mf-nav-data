package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Security;
import java.util.List;
import java.util.Optional;

public interface SecurityPort {
  Optional<Security> findByIsin(String isin);

  List<String> findIsinsBySchemeNamePattern(String namePattern);
}
