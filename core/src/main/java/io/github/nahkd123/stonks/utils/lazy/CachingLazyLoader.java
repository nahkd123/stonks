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
import java.util.function.Supplier;

public class CachingLazyLoader<T> implements LazyLoader<T> {
	private Supplier<CompletableFuture<T>> loader;
	private long retentionTime;
	private long lastFetched = -1L;
	private LoadState lastState = null;
	private Throwable lastError = null;
	private T lastResult = null;

	public CachingLazyLoader(Supplier<CompletableFuture<T>> loader, long retentionTime) {
		this.loader = loader;
		this.retentionTime = retentionTime;
	}

	public void refresh() {
		lastFetched = System.currentTimeMillis();
		lastState = LoadState.LOADING;
		loader.get().handle((result, err) -> {
			if (err != null) {
				lastState = LoadState.FAILED;
				lastError = err;
				return null;
			}

			lastState = LoadState.SUCCESS;
			lastResult = result;
			return result;
		});
	}

	@Override
	public LoadState load() {
		if (lastState == null || System.currentTimeMillis() >= lastFetched + retentionTime) refresh();
		return lastState;
	}

	@Override
	public Throwable getFailure() { return lastError; }

	@Override
	public T get() throws IllegalStateException {
		if (lastState != LoadState.SUCCESS)
			throw new IllegalStateException("This lazy loader is not loaded successfully or is currently loading!");
		if (lastState == LoadState.FAILED) throw new IllegalStateException(lastError);
		return lastResult;
	}
}
