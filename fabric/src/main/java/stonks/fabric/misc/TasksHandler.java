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
package stonks.fabric.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import nahara.common.tasks.Task;

/**
 * <p>
 * Handle tasks, with {@link Throwable} catching.
 * </p>
 */
public class TasksHandler {
	private static record Entry(Task<?> task, BiConsumer<?, Throwable> callback) {
	}

	private List<Entry> entries = new ArrayList<>();

	public <T> void handle(Task<T> task, BiConsumer<T, Throwable> callback) {
		entries.add(new Entry(task, callback));
	}

	@SuppressWarnings("unchecked")
	public void tick() {
		var iter = entries.iterator();

		while (iter.hasNext()) {
			var e = iter.next();
			var now = e.task().get();
			if (now.isEmpty()) continue;

			iter.remove();
			((BiConsumer<Object, Throwable>) e.callback()).accept(now.get().getSuccess(), now.get().getFailure());
		}
	}
}
