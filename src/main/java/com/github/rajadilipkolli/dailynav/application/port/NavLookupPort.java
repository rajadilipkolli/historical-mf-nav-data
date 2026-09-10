package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.model.NavByIsin;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NavLookupPort {
  /**
   * Retrieves the most recent NAV records for an ISIN, up to the specified limit.
   *
   * @param isin the international securities identification number
   * @param limit the maximum number of records to return
   * @return the matching NAV records
   */
  List<NavByIsin> findLastNByIsin(String isin, int limit);

  /**
   * Retrieves the latest NAV record for an ISIN.
   *
   * @param isin the ISIN to look up
   * @return the latest NAV record, if available
   */
  Optional<NavByIsin> findLatestByIsin(String isin);

  /**
   * Finds the NAV record for an ISIN dated on or before the specified date.
   *
   * @param isin the ISIN to search for
   * @param date the latest eligible date
   * @return the matching NAV record, if available
   */
  Optional<NavByIsin> findByIsinAndDateOnOrBefore(String isin, LocalDate date);

  /**
   * Finds NAV records for an ISIN within an inclusive date range.
   *
   * @param isin the ISIN to search for
   * @param startDate the beginning of the date range
   * @param endDate the end of the date range
   * @return the NAV records dated between the specified dates, inclusive
   */
  List<NavByIsin> findByIsinAndDateBetween(String isin, LocalDate startDate, LocalDate endDate);
}
