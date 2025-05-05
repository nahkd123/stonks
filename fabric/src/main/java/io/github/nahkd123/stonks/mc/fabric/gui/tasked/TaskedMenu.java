package io.github.nahkd123.stonks.mc.fabric.gui.tasked;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class TaskedMenu<V> extends SimpleGui implements TaskedGui<V> {
	private CompletableFuture<V> task = null;
	private boolean dirty = false;
	private boolean anotherGui = false;

	public TaskedMenu(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots) {
		super(type, player, manipulatePlayerSlots);
	}

	@Override
	public CompletableFuture<V> openTasked() {
		if (task != null) return task;
		task = new CompletableFuture<>();
		open();
		return task;
	}

	@Override
	public void onOpen() {
		if (task == null) throw new UnsupportedOperationException("Must be opened with openTasked()");
	}

	@Override
	public void onTick() {
		if (task == null || task.isCancelled()) {
			onCancel();
			close();
		}

		if (dirty) {
			dirty = false;
			onUpdate();
		}
	}

	@Override
	public void onClose() {
		if (task != null && !task.isDone() && !anotherGui)
			task.completeExceptionally(new PlayerAction("Player closed the menu"));
	}

	/**
	 * <p>
	 * Mark the GUI dirty upon task completion (either successfully or
	 * exceptionally). This also handle the exception logging upon completed
	 * exceptionally.
	 * </p>
	 * 
	 * @param <T>  Type of task return value.
	 * @param task The task.
	 * @return The new task.
	 */
	protected <T> CompletableFuture<T> markDirtyOnFinish(CompletableFuture<T> task) {
		return task
			.thenApply(v -> {
				dirty = true;
				return v;
			})
			.exceptionally(t -> {
				dirty = true;
				t.printStackTrace();
				throw new CompletionException(t);
			});
	}

	protected <T> CompletableFuture<T> openAnotherMenu(TaskedMenu<T> another) {
		anotherGui = true;
		return another
			.openTasked()
			.thenApply(v -> {
				anotherGui = false;
				open();
				return v;
			})
			.exceptionally(t -> {
				anotherGui = false;
				throw t instanceof CompletionException e ? e : new CompletionException(t);
			});
	}

	protected <T> T markDirty(T passthrough) {
		dirty = true;
		return passthrough;
	}

	/**
	 * <p>
	 * Called when GUI update is required.
	 * </p>
	 */
	protected abstract void onUpdate();

	/**
	 * <p>
	 * Called when the task is canceled.
	 * </p>
	 */
	protected void onCancel() {}

	protected void resolve(V result) {
		if (task != null) task.complete(result);
	}

	protected void reject(Throwable t) {
		if (task != null) task.completeExceptionally(t);
	}
}
