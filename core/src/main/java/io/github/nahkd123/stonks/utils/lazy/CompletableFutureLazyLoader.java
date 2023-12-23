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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CompletableFutureLazyLoader<T> implements LazyLoader<T> {
	private CompletableFuture<T> future;

	public CompletableFutureLazyLoader(CompletableFuture<T> future) {
		this.future = future;
	}

	public Future<T> getFuture() { return future; }

	@Override
	public LoadState load() {
		return future.isDone()
			? future.isCancelled() || future.isCompletedExceptionally()
				? LoadState.FAILED
				: LoadState.SUCCESS
			: LoadState.LOADING;
	}

	@Override
	public Throwable getFailure() {
		if (!future.isDone()) return null;
		if (future.isCompletedExceptionally()) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				return e;
			}
		}
		if (future.isCancelled()) return new CancellationException();
		return null;
	}

	@Override
	public T get() throws IllegalStateException {
		if (!future.isDone()) throw new IllegalStateException("The CompletableFuture is still loading!");
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
