package com.github.rajadilipkolli.dailynav.application.port;

import com.github.rajadilipkolli.dailynav.domain.report.ReportContext;

public interface ReportAssemblyPort {
  ReportContext assembleContext(String isin, int days);
}
