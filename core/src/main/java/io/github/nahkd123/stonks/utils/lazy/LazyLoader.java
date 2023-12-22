/*
 * Copyright (c) 2023 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.nahkd123.stonks.utils.lazy;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import nahara.common.tasks.Task;

/**
 * <p>
 * Any objects that implements {@link LazyLoader} can be loaded lazily (that is,
 * it will be loaded when you request it to be loaded for display).
 * </p>
 */
public interface LazyLoader<T> {
	/**
	 * <p>
	 * Load the object if needed and returns the loaded state.
	 * </p>
	 * 
	 * @return The loaded state.
	 */
	public LoadState load();

	/**
	 * <p>
	 * Get the {@link Throwable} that makes this loader failed to load the object.
	 * </p>
	 * 
	 * @return The error/exception.
	 */
	public Throwable getFailure();

	/**
	 * <p>
	 * Get the object that has been loaded. May returns {@code null}.
	 * </p>
	 * 
	 * @return The object that has been lazily loaded.
	 * @throws IllegalStateException if {@link #load()} returns {@code false}
	 *                               because it hasn't been loaded, it is currently
	 *                               loading or an error occurred.
	 */
	public T get() throws IllegalStateException;

	/**
	 * <p>
	 * Get the object based on current loading state (loading, failed or success).
	 * </p>
	 * 
	 * @param onLoading The object supplier if the object is still being loaded.
	 * @param onFail    The object supplier if the loading process failed.
	 * @return The object.
	 */
	default T getTriState(Supplier<T> onLoading, Function<Throwable, T> onFail) {
		return switch (load()) {
		case LOADING -> onLoading.get();
		case FAILED -> onFail.apply(getFailure());
		case SUCCESS -> get();
		default -> onLoading.get();
		};
	}

	default <B> LazyMapper<T, B> map(Function<T, B> mapper) {
		return new LazyMapper<>(this, mapper);
	}

	public static <T> LazyLoader<T> wrap(CompletableFuture<T> future) {
		return new CompletableFutureLazyLoader<>(future);
	}

	public static <T> LazyLoader<T> wrap(Task<T> task) {
		return new LegacyTaskLazyLoader<>(task);
	}

	public static <T> LazyLoader<T> ofFinished(T result) {
		return new LoadedLazyLoader<>(result, null);
	}

	public static <T> LazyLoader<T> ofFailed(Throwable throwable) {
		return new LoadedLazyLoader<>(null, throwable);
	}
}
