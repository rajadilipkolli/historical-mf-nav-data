package com.github.rajadilipkolli.dailynav;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;

/**
 * Wrapper that holds a `DailyNavHealthService` and the Hikari DataSource backing it. Callers must
 * close this to release the DataSource.
 */
public class CloseableDailyNavHealthService implements AutoCloseable {

  private final DailyNavHealthService delegate;
  private final HikariDataSource dataSource;

  /**
   * Create a CloseableDailyNavHealthService that wraps a DailyNavHealthService and its backing
   * DataSource.
   *
   * <p>The created instance delegates service calls to the provided `delegate` and will close the
   * provided `dataSource` when its `close()` method is invoked.
   *
   * @param delegate the underlying DailyNavHealthService to delegate to
   * @param dataSource the HikariDataSource backing the delegate; will be closed by this wrapper on
   *     close()
   */
  public CloseableDailyNavHealthService(
      DailyNavHealthService delegate, HikariDataSource dataSource) {
    this.delegate = delegate;
    this.dataSource = dataSource;
  }

  /**
   * Accesses the wrapped DailyNavHealthService.
   *
   * @return the wrapped DailyNavHealthService instance
   */
  public DailyNavHealthService service() {
    return delegate;
  }

  /**
   * Closes the underlying HikariDataSource backing the wrapped service.
   *
   * @throws IOException if closing the DataSource fails; the original exception is set as the
   *     cause.
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
