package io.github.nahkd123.stonks.mc.fabric.gui.tasked;

import java.util.concurrent.CompletableFuture;

import eu.pb4.sgui.api.gui.GuiInterface;

public interface TaskedGui<V> extends GuiInterface {
	/**
	 * <p>
	 * Open this GUI and return async task that can be used like task chaining.
	 * Canceling task will indirectly close the GUI on next tick. Joining the task
	 * to current thread must be done on non-server thread, otherwise it will block
	 * the GUI from ticking and possibly hanging the server forever.
	 * </p>
	 * <p>
	 * Regular call to {@link #open()} instead of {@link #openTasked()} will throw
	 * {@link UnsupportedOperationException}.
	 * </p>
	 * 
	 * @return The task that will be resolved.
	 */
	CompletableFuture<V> openTasked();
}
