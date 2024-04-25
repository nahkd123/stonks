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
package stonks.core.caching;

import java.util.function.Supplier;

import nahara.common.tasks.Task;

public class Cached<T> {
	private T cachedValue = null;
	private long lastUpdate;
	private long cacheMaxTime = 5000;
	private Supplier<Task<T>> updater;
	private Task<T> pendingTask;

	public Cached(Supplier<Task<T>> updater) {
		this.updater = updater;
	}

	public Cached<T> cacheMaxTime(long time) {
		cacheMaxTime = time;
		return this;
	}

	public Cached<T> invalidate() {
		lastUpdate = 0L;
		return this;
	}

	@Deprecated(forRemoval = true)
	public Task<T> get() {
		if (pendingTask != null) {
			if (pendingTask.get().isPresent()) {
				var t = pendingTask;
				pendingTask = null;
				if (!shouldUpdate()) return t;
			} else {
				return pendingTask;
			}
		}

		if (shouldUpdate()) {
			return pendingTask = updater
				.get()
				.afterThatDo(t -> {
					cachedValue = t;
					lastUpdate = System.currentTimeMillis();
					pendingTask = null;
					return t;
				});
		}

		return Task.resolved(cachedValue);
	}

	public boolean shouldUpdate() {
		var now = System.currentTimeMillis();
		return cachedValue == null || now > (lastUpdate + cacheMaxTime);
	}
}
