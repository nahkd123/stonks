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
