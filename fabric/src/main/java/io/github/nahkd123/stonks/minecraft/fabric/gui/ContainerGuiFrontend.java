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
package io.github.nahkd123.stonks.minecraft.fabric.gui;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.ButtonTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.GuiTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.text.TextProxy;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class ContainerGuiFrontend extends SimpleGui {
	private ContainerGui backend;
	private GuiTemplate template;

	public ContainerGuiFrontend(ServerPlayerEntity player, boolean manipulatePlayerSlots, ContainerGui backend, GuiTemplate template) {
		super(template.type, player, manipulatePlayerSlots);
		this.backend = backend;
		this.template = template;
	}

	public ContainerGui getBackend() { return backend; }

	@Override
	public void beforeOpen() {
		refresh();
	}

	public void refresh() {
		if (template == null) return;

		setTitle(template.title.apply(this) instanceof TextProxy p ? p.getUnderlying() : null);
		for (ButtonTemplate buttonTemplate : template.buttons) {
			buttonTemplate.forEachInFillRects((x, y, ordinal) -> {
				int index = (y - 1) * getWidth() + (x - 1);
				GuiElementInterface guiElement = buttonTemplate.createGuiElement(this, ordinal);
				setSlot(index, guiElement);
			});
		}
	}

	public static ClickType toCommonClickType(eu.pb4.sgui.api.ClickType sgui) {
		return switch (sgui) {
		case MOUSE_LEFT -> ClickType.LEFT;
		case MOUSE_RIGHT -> ClickType.RIGHT;
		case MOUSE_LEFT_SHIFT -> ClickType.LEFT;
		case MOUSE_RIGHT_SHIFT -> ClickType.SHIFT_RIGHT;
		default -> ClickType.UNKNOWN;
		};
	}
}
