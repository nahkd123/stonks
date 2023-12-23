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

import io.github.nahkd123.stonks.market.catalogue.Category;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.gui.LazyLoadedNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.provided.gui.AbstractContainerGui;
import io.github.nahkd123.stonks.minecraft.gui.provided.gui.MainContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.minecraft.utils.LazyTexts;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;
import io.github.nahkd123.stonks.utils.lazy.LoadState;

public class CategoryNativeButton implements LazyLoadedNativeButton<Category> {
	public static final String ID = "category";
	private LazyLoader<ProductsCatalogue> cache;
	private int ordinal;

	public CategoryNativeButton(LazyLoader<ProductsCatalogue> cache, int ordinal) {
		this.cache = cache;
		this.ordinal = ordinal;
	}

	public CategoryNativeButton(AbstractContainerGui gui, int ordinal) {
		this(gui.getServer().getMarketCache().productsCatalogue, ordinal);
	}

	@Override
	public void onClick(ContainerGui gui, Player player, ClickType type) {
		if (cache.load() != LoadState.SUCCESS) return;
		if (getLoader().get() == null) return;

		MainContainerGui main = gui instanceof MainContainerGui m
			? m
			: new MainContainerGui(gui, gui.getServer(), ordinal);
		main.setCategoryIndex(ordinal);

		if (!(gui instanceof MainContainerGui)) player.openGui(main);
	}

	@Override
	public TextComponent replacePlaceholder(TextFactory factory, String name) {
		return switch (name) {
		case "category.id" -> LazyTexts.loading(getLoader().map(v -> factory.literal(v.getId())), factory);
		case "category.name" -> LazyTexts.loading(getLoader().map(v -> factory.literal(v.getDisplayName())), factory);
		case "category.productsCount" -> LazyTexts
			.loading(getLoader().map(v -> factory.literal(Integer.toString(v.getProducts().size()))), factory);
		default -> null;
		};
	}

	@Override
	public LazyLoader<Category> getLoader() {
		return cache
			.map(v -> v.getCategories())
			.map(v -> ordinal < v.size() ? v.get(ordinal) : null);
	}
}
