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
