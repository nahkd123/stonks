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
