package stonks.core.caching;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class FutureCache<T> {
	private Supplier<CompletableFuture<T>> invoker;
	private long lastFetch = -1;
	private CompletableFuture<T> fetchingTask = null;
	private T result = null;

	// Options
	private long maxCacheTime = 5000L;

	public FutureCache(Supplier<CompletableFuture<T>> invoker, T defaultResult) {
		this.invoker = invoker;
		this.result = defaultResult;
	}

	public FutureCache(Supplier<CompletableFuture<T>> invoker) {
		this(invoker, null);
	}

	/**
	 * <p>
	 * Configure the cache time, which is the minimum amount of time the object is
	 * allowed to stay in this cache. Default cache time is 5000 milliseconds.
	 * </p>
	 * 
	 * @param maxCacheTime The maximum cache time.
	 * @return The cache.
	 */
	public FutureCache<T> withMaxCacheTime(long maxCacheTime) {
		this.maxCacheTime = maxCacheTime;
		return this;
	}

	/**
	 * <p>
	 * Return the state that determine if {@link #forceFetch()} should be called
	 * again.
	 * </p>
	 * 
	 * @return The state.
	 */
	public boolean shouldFetch() {
		return fetchingTask != null || lastFetch == -1L || (System.currentTimeMillis() - lastFetch > maxCacheTime);
	}

	/**
	 * <p>
	 * Force this cache to fetch the value. If there is already a task running, it
	 * will return that task.
	 * </p>
	 * 
	 * @return The fetch task.
	 */
	public CompletableFuture<T> forceFetch() {
		if (fetchingTask != null) return fetchingTask;
		return fetchingTask = invoker.get().handle((r, t) -> {
			lastFetch = System.currentTimeMillis();
			result = r;
			fetchingTask = null;

			if (t != null) throw new CompletionException(t);
			return r;
		});
	}

	/**
	 * <p>
	 * Get the object that is still under cache retention wrapped as immediately
	 * completed future, or future of the fetching task that is still running.
	 * </p>
	 * 
	 * @return The future.
	 */
	public CompletableFuture<T> get() {
		if (fetchingTask != null && fetchingTask.isDone()) {
			if (!fetchingTask.isCompletedExceptionally()) try {
				result = fetchingTask.get();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			
			fetchingTask = null;
		}

		if (!shouldFetch()) {
			if (fetchingTask != null) return fetchingTask;
			return CompletableFuture.completedFuture(result);
		}

		return forceFetch();
	}

	/**
	 * <p>
	 * Get the value immediately, regardless the cache state. If the value need to
	 * be fetched again, it will call the {@link #forceFetch()} method and return
	 * previous value.
	 * </p>
	 * 
	 * @return The cached value.
	 */
	public T getNow() {
		if (shouldFetch()) forceFetch();
		return result;
	}
}
