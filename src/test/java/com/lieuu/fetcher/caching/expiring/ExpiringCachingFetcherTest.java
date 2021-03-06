package com.lieuu.fetcher.caching.expiring;

import com.lieuu.fetcher.Fetcher;
import com.lieuu.fetcher.Fetchers;
import com.lieuu.fetcher.caching.concurrent.TestExecutorServiceCachingFetcher;
import com.lieuu.fetcher.exception.FetcherException;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpiringCachingFetcherTest {

  @Test
  public void testExpiringCachingFetcherWrapper() {

    final AtomicInteger count = new AtomicInteger(0);

    final Fetcher<Integer> cachingFetcher = Fetchers.getExpiringCachingFetcher(
      () -> count.incrementAndGet(), 50);

    try {

      Thread.sleep(200);

      Assertions.assertThat(cachingFetcher.fetch()).isEqualTo(1);

      Thread.sleep(200);

      Assertions.assertThat(cachingFetcher.fetch()).isEqualTo(2);

      Thread.sleep(200);

      Assertions.assertThat(cachingFetcher.fetch()).isEqualTo(3);

      Thread.sleep(200);

      Assertions.assertThat(cachingFetcher.fetch()).isEqualTo(4);
      Assertions.assertThat(cachingFetcher.fetch()).isEqualTo(4);

    }
    catch (FetcherException | InterruptedException e) {
      Assertions.fail(e.getMessage());
    }

  }

  @Test
  public void testMultiThreadedExpiringCachingFetcherWrapper() {

    final int maxTimeMs = 100;

    final AtomicInteger count = new AtomicInteger(0);

    final Fetcher<Integer> expire = Fetchers.getExpiringCachingFetcher(
      (Fetcher<Integer>) () -> count.incrementAndGet(), maxTimeMs);

    final TestExecutorServiceCachingFetcher exec = new TestExecutorServiceCachingFetcher();

    final List<Future<String>> futures = new ArrayList<>();

    for (int i = 0; i < 4; i++) { // run threads to check value of expiring
      // fetcher

      try {

        final Future<String> future = exec.fetch().submit(() -> {

          try {

            for (int countRuns = 1; countRuns <= 6; countRuns++) {

              Assertions.assertThat(expire.fetch()).isEqualTo(countRuns);
              Thread.sleep(maxTimeMs * 7);

            }

          }
          catch (FetcherException | InterruptedException e) {
            Assertions.fail(e.getMessage());
          }

          return "";

        });

        futures.add(future);

      }
      catch (final FetcherException e) {
        Assertions.fail(e.getMessage());
      }

    }

    for (final Future<String> future : futures) {

      try {
        future.get();
      }
      catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
        Assertions.fail(e.getMessage());
      }

    }

  }

}
