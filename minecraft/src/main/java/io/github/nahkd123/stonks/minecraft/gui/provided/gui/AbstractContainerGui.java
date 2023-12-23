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
package io.github.nahkd123.stonks.minecraft.gui.provided.gui;

import java.util.HashMap;
import java.util.Map;

import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.gui.NativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.button.BackNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.button.CategoryNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.button.CloseNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.button.ProductNativeButton;

public abstract class AbstractContainerGui implements ContainerGui {
	@FunctionalInterface
	public static interface NativeButtonFactory {
		NativeButton create(AbstractContainerGui gui, int ordinal);
	}

	private static final Map<String, NativeButtonFactory> GLOBAL_REGISTRY = new HashMap<>();

	static {
		GLOBAL_REGISTRY.put(CloseNativeButton.ID, CloseNativeButton::new);
		GLOBAL_REGISTRY.put(BackNativeButton.ID, BackNativeButton::new);
		GLOBAL_REGISTRY.put(CategoryNativeButton.ID, CategoryNativeButton::new);
		GLOBAL_REGISTRY.put(ProductNativeButton.ID, ProductNativeButton::new);
	}

	private ContainerGui previous;
	private MinecraftServer server;
	private String id;

	public AbstractContainerGui(ContainerGui previous, MinecraftServer server, String id) {
		this.previous = previous;
		this.server = server;
		this.id = id;
	}

	public ContainerGui getPrevious() { return previous; }

	@Override
	public String getId() { return id; }

	@Override
	public MinecraftServer getServer() { return server; }

	@Override
	public NativeButton getNative(String id, int ordinal) {
		if (id == null) return null;
		NativeButtonFactory ctor = GLOBAL_REGISTRY.get(id);
		return ctor != null ? ctor.create(this, ordinal) : null;
	}
}
