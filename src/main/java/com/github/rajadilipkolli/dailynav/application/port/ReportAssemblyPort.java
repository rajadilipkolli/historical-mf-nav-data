package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.report.ReportContext;

public interface ReportAssemblyPort {
  /**
 * Assembles a report context for an instrument and time period.
 *
 * @param isin  the instrument's ISIN identifier
 * @param days  the number of days in the reporting period
 * @return      the assembled report context
 */
ReportContext assembleContext(String isin, int days);
}
