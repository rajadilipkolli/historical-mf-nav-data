package com.github.rajadilipkolli.dailynav;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;

/**
 * Wrapper that holds a `MutualFundService` and the Hikari DataSource backing it. Callers must close
 * this to release the DataSource.
 */
public class CloseableMutualFundService implements AutoCloseable {

  private final MutualFundService delegate;
  private final HikariDataSource dataSource;

  public CloseableMutualFundService(MutualFundService delegate, HikariDataSource dataSource) {
    this.delegate = delegate;
    this.dataSource = dataSource;
  }

  public MutualFundService service() {
    return delegate;
  }

  @Override
  public void close() throws IOException {
    try {
      if (dataSource != null) {
        dataSource.close();
      }
    } catch (Exception e) {
      throw new IOException("Failed to close DataSource", e);
    }
  }
}
