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

  /**
   * Constructs a CloseableMutualFundService that wraps the given MutualFundService and its HikariDataSource.
   *
   * @param delegate   the MutualFundService whose operations will be delegated
   * @param dataSource the HikariDataSource that will be closed when this wrapper is closed
   */
  public CloseableMutualFundService(MutualFundService delegate, HikariDataSource dataSource) {
    this.delegate = delegate;
    this.dataSource = dataSource;
  }

  /**
   * Provides access to the wrapped MutualFundService.
   *
   * @return the underlying MutualFundService instance held by this wrapper
   */
  public MutualFundService service() {
    return delegate;
  }

  /**
   * Closes the wrapped HikariDataSource if one is present.
   *
   * @throws IOException if an error occurs while closing the DataSource; the original exception
   *                     is set as the cause.
   */
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