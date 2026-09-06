package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NavLookupPort {
  List<NavByIsin> findLastNByIsin(String isin, int limit);
  Optional<NavByIsin> findLatestByIsin(String isin);
  Optional<NavByIsin> findByIsinAndDateOnOrBefore(String isin, LocalDate date);
  List<NavByIsin> findByIsinAndDateBetween(String isin, LocalDate startDate, LocalDate endDate);
}
