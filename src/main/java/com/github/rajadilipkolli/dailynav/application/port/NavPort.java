package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.Nav;
import java.util.List;

public interface NavPort {
  List<Nav> findBySchemeCode(Integer schemeCode);
}
