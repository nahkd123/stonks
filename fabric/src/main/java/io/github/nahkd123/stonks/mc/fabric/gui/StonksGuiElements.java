package io.github.nahkd123.stonks.mc.fabric.gui;

import static eu.pb4.sgui.api.elements.GuiElementBuilder.from;
import static net.minecraft.text.Text.literal;
import static net.minecraft.util.Formatting.DARK_GRAY;
import static net.minecraft.util.Formatting.GRAY;
import static net.minecraft.util.Formatting.ITALIC;
import static net.minecraft.util.Formatting.RED;
import static net.minecraft.util.Formatting.WHITE;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.AnimatedGuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class StonksGuiElements {
	public static final GuiElement FRAME = from(new ItemStack(Items.BLACK_STAINED_GLASS_PANE))
		.hideTooltip()
		.build();

	public static final GuiElement BACK = from(new ItemStack(Items.ARROW))
		.setItemName(Text.literal("<- ").formatted(GRAY).append(Text.literal("Go back").formatted(WHITE)))
		.build();

	public static final AnimatedGuiElement LOADING = new AnimatedGuiElementBuilder()
		.setInterval(5)
		.setItem(Items.CLOCK).setItemName(literal("Loading 0oo").formatted(GRAY)).saveItemStack()
		.setItem(Items.CLOCK).setItemName(literal("Loading o0o").formatted(GRAY)).saveItemStack()
		.setItem(Items.CLOCK).setItemName(literal("Loading oo0").formatted(GRAY)).saveItemStack()
		.setItem(Items.CLOCK).setItemName(literal("Loading o0o").formatted(GRAY)).saveItemStack()
		.build();

	public static final GuiElement CANCELED = from(new ItemStack(Items.BARRIER))
		.setItemName(literal("Operation is canceled").formatted(RED))
		.build();

	public static GuiElement internalError(Throwable t) {
		return from(new ItemStack(Items.BARRIER))
			.setItemName(literal("Internal error: ").formatted(RED)
				.append(literal(t.getLocalizedMessage()).formatted(WHITE)))
			.setLore(Stream.of(t.getStackTrace())
				.map(element -> {
					MutableText line = literal(element.getClassName() + "." + element.getMethodName()).formatted(GRAY);

					if (element.getModuleName() != null) line = literal(element.getModuleName()).formatted(GRAY)
						.append(literal(":").formatted(DARK_GRAY))
						.append(element.getModuleVersion() != null
							? literal(element.getModuleVersion())
							: literal("<Unknown module version>").formatted(DARK_GRAY, ITALIC))
						.append("//")
						.append(line);

					if (element.getFileName() != null) line = line
						.append(Text.literal(" (").formatted(DARK_GRAY))
						.append(Text.literal(element.getFileName()).formatted(WHITE))
						.append(Text.literal("::").formatted(DARK_GRAY))
						.append(element.getLineNumber() >= 0
							? Text.literal("" + (element.getLineNumber() + 1)).formatted(WHITE)
							: Text.literal("<Unknown line>").formatted(DARK_GRAY, ITALIC))
						.append(Text.literal(")").formatted(DARK_GRAY));

					return (Text) line;
				})
				.toList())
			.build();
	}

	public static <T> GuiElementInterface lazy(CompletableFuture<T> task, Function<T, GuiElementInterface> fn) {
		if (!task.isDone()) return LOADING;
		if (task.isCancelled()) return CANCELED;
		if (task.isCompletedExceptionally()) return internalError(task.exceptionNow());
		return fn.apply(task.getNow(null));
	}
}
