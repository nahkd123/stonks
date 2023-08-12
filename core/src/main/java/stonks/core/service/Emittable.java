package stonks.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Emittable<T> {
	private List<Consumer<T>> consumers = new ArrayList<>();
	private boolean isEmitting = false;
	private List<T> pendingObjects;
	private List<Consumer<T>> pendingConsumers;

	public void listen(Consumer<T> consumer) {
		if (!isEmitting) {
			consumers.add(consumer);
			return;
		}

		if (pendingConsumers == null) pendingConsumers = new ArrayList<>();
		pendingConsumers.add(consumer);
	}

	public void emit(T obj) {
		if (!isEmitting) {
			isEmitting = true;

			do {
				for (var c : consumers) {
					try {
						c.accept(obj);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (pendingConsumers != null) {
					for (var c : pendingConsumers) {
						try {
							consumers.add(c);
							c.accept(obj);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					pendingConsumers = null;
				}

				if (pendingObjects != null && pendingObjects.size() > 0) {
					obj = pendingObjects.remove(0);
				} else {
					obj = null;
					pendingObjects = null;
				}
			} while (obj != null);

			isEmitting = false;
			return;
		}

		if (pendingObjects == null) pendingObjects = new ArrayList<>();
		pendingObjects.add(obj);
	}
}
