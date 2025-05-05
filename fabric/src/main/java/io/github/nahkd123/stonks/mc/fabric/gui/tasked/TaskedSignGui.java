package io.github.nahkd123.stonks.mc.fabric.gui.tasked;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TaskedSignGui extends SignGui implements TaskedGui<TaskedSignGui> {
	private CompletableFuture<TaskedSignGui> task = null;

	public TaskedSignGui(ServerPlayerEntity player) {
		super(player);
	}

	@Override
	public CompletableFuture<TaskedSignGui> openTasked() {
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
		if (task == null || task.isCancelled()) close();
	}

	@Override
	public void onClose() {
		if (task != null && !task.isDone()) task.complete(this);
	}

	public static CompletableFuture<TaskedSignGui> prompt(ServerPlayerEntity player, UnaryOperator<TaskedSignGui> transformer) {
		TaskedSignGui sign = transformer.apply(new TaskedSignGui(player));
		return sign.openTasked();
	}

	public static CompletableFuture<TaskedSignGui> prompt(ServerPlayerEntity player, Text... lines) {
		return prompt(player, s -> {
			for (int i = 0; i < Math.min(lines.length, 4); i++) s.setLine(i, lines[i]);
			return s;
		});
	}
}
