package com.lieuu.fetcher;

import com.lieuu.fetcher.exception.FetcherErrorCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class Fetchers {

  private final static Fetcher<ExecutorService> EXECUTOR_SERVICE_FETCHER = new ExecutorServiceCachingFetcher();

  @SafeVarargs
  public final static <T> Fetcher<T> getMultiConcurrentFetcher(final Fetcher<T>... fetchers) {

    final List<NonBlockingConcurrentFetcher<T>> fetchersWrapped = new ArrayList<>(fetchers.length);

    for (final Fetcher<T> fetcher : fetchers) {
      fetchersWrapped.add(
        new NonBlockingConcurrentFetcher<>(fetcher, Fetchers.getExecutorServiceFetcher()));
    }

    return new MultiFetcherValueWrapper<>(new BlockingMultiConcurrentFetcher<>(fetchersWrapped));

  }

  @SafeVarargs
  public final static <T> Fetcher<T> getMultiConcurrentFetcher(final int maxTimeMs,
    final Fetcher<T>... fetchers) {

    final List<NonBlockingConcurrentFetcher<T>> fetchersWrapped = new ArrayList<>(fetchers.length);

    for (final Fetcher<T> fetcher : fetchers) {
      fetchersWrapped.add(
        new NonBlockingConcurrentFetcher<>(fetcher, Fetchers.getExecutorServiceFetcher()));
    }

    return new MultiFetcherValueWrapper<>(
      new BlockingMultiConcurrentFetcher<>(maxTimeMs, fetchersWrapped));

  }

  @SafeVarargs
  public final static <T> Fetcher<T> getExpiringMultiConcurrentFetcher(
    final Fetcher<ExecutorService> executorServiceFetcher, final long maxTimeMs,
    final Fetcher<T>... fetchers) {

    final List<NonBlockingConcurrentFetcher<T>> fetchersWrapped = new ArrayList<>(fetchers.length);

    for (final Fetcher<T> fetcher : fetchers) {
      fetchersWrapped.add(new NonBlockingConcurrentFetcher<>(fetcher, executorServiceFetcher));
    }

    return new MultiFetcherValueWrapper<>(
      new ExpiringMultiConcurrentFetcher<>(maxTimeMs, fetchersWrapped));

  }

  @SafeVarargs
  public final static <T> Fetcher<T> getExpiringMultiConcurrentFetcher(final long maxTimeMs,
    final Fetcher<T>... fetchers) {
    return Fetchers.getExpiringMultiConcurrentFetcher(Fetchers.getExecutorServiceFetcher(),
      maxTimeMs, fetchers);
  }

  @SafeVarargs
  public final static <T> Fetcher<T> getWaterfallFetcher(final Fetcher<T>... fetchers) {

    final List<CachingFetcher<T>> fetchersWrapped = new ArrayList<>(fetchers.length);

    for (final Fetcher<T> fetcher : fetchers) {
      fetchersWrapped.add(new CachingFetcher<>(fetcher));
    }

    return new MultiFetcherValueWrapper<>(new WaterfallCachingFetcher<>(fetchersWrapped));

  }

  @SafeVarargs
  public final static <T> Fetcher<T> getWaterfallFetcher(final FetcherErrorCallback callback,
    final Fetcher<T>... fetchers) {

    final List<CachingFetcher<T>> fetchersWrapped = new ArrayList<>(fetchers.length);

    for (final Fetcher<T> fetcher : fetchers) {
      fetchersWrapped.add(new CachingFetcher<>(fetcher));
    }

    return new MultiFetcherValueWrapper<>(new WaterfallCachingFetcher<>(callback, fetchersWrapped));

  }

  public final static <T> Fetcher<T> getBlockingConcurrentFetcher(
    final Fetcher<ExecutorService> executorServiceFetcher, final Fetcher<T> fetcher) {
    return new BlockingConcurrentFetcher<>(fetcher, executorServiceFetcher);
  }

  public final static <T> Fetcher<T> getNonBlockingConcurrentFetcher(
    final Fetcher<ExecutorService> executorServiceFetcher, final Fetcher<T> fetcher) {
    return new NonBlockingConcurrentFetcher<>(fetcher, executorServiceFetcher);
  }

  public final static <T> Fetcher<T> getBlockingConcurrentFetcher(final Fetcher<T> fetcher) {
    return Fetchers.getBlockingConcurrentFetcher(Fetchers.getExecutorServiceFetcher(), fetcher);
  }

  public final static <T> Fetcher<T> getNonBlockingConcurrentFetcher(final Fetcher<T> fetcher) {
    return Fetchers.getNonBlockingConcurrentFetcher(Fetchers.getExecutorServiceFetcher(), fetcher);
  }

  public final static <T> Fetcher<T> getCachingFetcher(final Fetcher<T> fetcher) {
    return new CachingFetcher<>(fetcher);
  }

  public final static <T> Fetcher<T> getExpiringCachingFetcher(final Fetcher<T> fetcher,
    final int maxCacheTime) {
    return new ExpiringCachingFetcher<>(fetcher, maxCacheTime);
  }

  public final static Fetcher<ExecutorService> getExecutorServiceFetcher() {
    return Fetchers.EXECUTOR_SERVICE_FETCHER;
  }

  public final static class Implementations {

    public final static Fetcher<String> getCachingFileFetcher(
      final Fetcher<String> fileNameFetcher) {
      return new CachingFileFetcher(fileNameFetcher);
    }

  }

}
