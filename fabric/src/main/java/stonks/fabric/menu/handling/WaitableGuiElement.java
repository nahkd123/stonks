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
package stonks.fabric.menu.handling;

import java.util.stream.Stream;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import nahara.common.tasks.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import stonks.fabric.Translations.Icons;

public abstract class WaitableGuiElement<T> implements GuiElementInterface {
	protected static final ItemStack[] LOADING = Stream
		.of(":..", ".:.", "..:", ".:.")
		.map(v -> new GuiElementBuilder(Items.CLOCK)
			.setName(Icons.Loading(v))
			.asStack())
		.toArray(ItemStack[]::new);

	public static final AnimatedGuiElement ANIMATED_LOADING = new AnimatedGuiElement(LOADING, 5, false, EMPTY_CALLBACK);

	private Task<T> task;
	private int timeTicked;
	private ItemStack stack;

	public WaitableGuiElement(Task<T> task) {
		this.task = task;
	}

	@Override
	public ItemStack getItemStackForDisplay(GuiInterface gui) {
		var now = task.get();
		if (now.isEmpty()) { return LOADING[(timeTicked++ / 5) % LOADING.length].copy(); }

		if (stack == null) stack = createStackWhenLoaded(
			task.get().get().getSuccess(),
			task.get().get().getFailure());
		return stack.copy();
	}

	public abstract ItemStack createStackWhenLoaded(T success, Throwable error);

	public abstract void onSlotClick(int index, ClickType type, SlotActionType action, SlotGuiInterface gui, T success, Throwable error);

	@Override
	public ItemStack getItemStack() { return stack; }

	@Override
	public ClickCallback getGuiCallback() {
		var now = task.get();
		if (now.isEmpty()) return GuiElementInterface.super.getGuiCallback();
		return (index, type, action, gui) -> onSlotClick(index, type, action, gui, now.get().getSuccess(),
			now.get().getFailure());
	}
}
