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

import io.github.nahkd123.stonks.market.catalogue.Category;
import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;

public class MainContainerGui extends AbstractContainerGui {
	private int categoryIndex;

	public MainContainerGui(ContainerGui previous, MinecraftServer server, int categoryIndex) {
		super(previous, server, "main");
		this.categoryIndex = categoryIndex;
	}

	public int getCategoryIndex() { return categoryIndex; }

	public void setCategoryIndex(int categoryIndex) { this.categoryIndex = categoryIndex; }

	public LazyLoader<Category> getCategoryCache() {
		return getServer().getMarketCache().productsCatalogue
			.map(c -> categoryIndex < c.getCategories().size() ? c.getCategories().get(categoryIndex) : null);
	}

	@Override
	public TextComponent replacePlaceholder(TextFactory factory, String name) {
		// TODO
		return null;
	}
}
