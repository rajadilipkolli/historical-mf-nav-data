package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Scheme;
import java.util.List;
import java.util.Optional;

public interface SchemePort {
  Optional<Scheme> findBySchemeCode(Integer schemeCode);
  List<Scheme> findBySchemeNameContaining(String namePattern);
  List<Scheme> findAll();
}
