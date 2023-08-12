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
package stonks.fabric.menu;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.fabric.StonksFabricPlatform;
import stonks.fabric.misc.TasksHandler;

public abstract class StackedMenu extends SimpleGui {
	private StackedMenu previous;
	private TasksHandler guiTasksHandler;

	public StackedMenu(StackedMenu previous, ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots) {
		super(type, player, manipulatePlayerSlots);
		this.previous = previous;

		placeBorder();
		placeButtons();
	}

	public StackedMenu getPrevious() { return previous; }

	protected void placeBorder() {
		for (int x = 0; x < getWidth(); x++) setSlot(x, MenuIcons.BORDER);
	}

	protected void placeButtons() {
		if (previous != null) { setSlot(1, MenuIcons.BACK); }
	}

	@Override
	public void onTick() {
		getGuiTasksHandler().tick();
	}

	/**
	 * <p>
	 * A local {@link TasksHandler} that's bound to this menu. Does not call
	 * callbacks if the menu is closed. If you want to handle tasks outside the
	 * bound of menus, consider using {@link StonksFabricPlatform#getTasksHandler()}.
	 * </p>
	 * 
	 * @return GUI tasks handler.
	 */
	public TasksHandler getGuiTasksHandler() {
		if (guiTasksHandler == null) guiTasksHandler = new TasksHandler();
		return guiTasksHandler;
	}
}
