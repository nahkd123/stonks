package io.github.nahkd123.stonks.utils;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EmitHandler<T> {
	private Set<T> listeners = new HashSet<>();
	private boolean emitting = false;
	private Queue<Consumer<T>> emitterQueue = new ConcurrentLinkedQueue<>();
	private Set<T> add = new HashSet<>();
	private Set<T> remove = new HashSet<>();

	public void addListener(T listener) {
		if (emitting) {
			if (!add.contains(listener)) add.add(listener);
		} else {
			if (remove.contains(listener)) remove.remove(listener);
			listeners.add(listener);
		}
	}

	public void removeListener(T listener) {
		if (!listeners.contains(listener)) return;

		if (emitting) {
			if (!remove.contains(listener)) remove.add(listener);
		} else {
			if (add.contains(listener)) add.remove(listener);
			listeners.remove(listener);
		}
	}

	public void beginEmit(Consumer<T> emitter) {
		emitterQueue.add(emitter);
		if (emitting) return;

		try {
			emitting = true;

			while (!emitterQueue.isEmpty()) {
				emitter = emitterQueue.poll();
				for (T listener : listeners) emitter.accept(listener);
				add.forEach(listeners::add);
				remove.forEach(listeners::remove);
				add.clear();
				remove.clear();
			}
		} finally {
			emitting = false;
		}
	}
}
