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
package io.github.nahkd123.stonks.minecraft.gui.provided.button;

import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.gui.LazyLoadedNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.gui.AbstractContainerGui;
import io.github.nahkd123.stonks.minecraft.gui.provided.gui.MainContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.minecraft.utils.TriStates;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;

public class ProductNativeButton implements LazyLoadedNativeButton<Product> {
	public static final String ID = "product";
	private AbstractContainerGui gui;
	private int ordinal;

	public ProductNativeButton(AbstractContainerGui gui, int ordinal) {
		this.gui = gui;
		this.ordinal = ordinal;
	}

	@Override
	public TextComponent replacePlaceholder(TextFactory factory, String name) {
		return switch (name) {
		case "product.id" -> TriStates.of(getLoader().map(p -> factory.literal(p.getId())), factory);
		case "product.name" -> TriStates.of(getLoader().map(p -> factory.literal(p.getDisplayName())), factory);
		default -> null;
		};
	}

	@Override
	public LazyLoader<Product> getLoader() {
		if (gui instanceof MainContainerGui main) {
			return main.getCategoryCache()
				.map(c -> ordinal < c.getProducts().size() ? c.getProducts().get(ordinal) : null);
		}

		// TODO add loader for product menu
		return LazyLoader.ofFailed(new RuntimeException("Unable to obtain product based on current GUI"));
	}

	@Override
	public void onClick(ContainerGui gui, Player player, ClickType type) {}
}
