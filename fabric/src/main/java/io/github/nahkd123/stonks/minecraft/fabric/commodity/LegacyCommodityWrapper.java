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
package io.github.nahkd123.stonks.minecraft.fabric.commodity;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.commodity.Commodity;
import io.github.nahkd123.stonks.minecraft.fabric.PlayerWrapper;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import stonks.core.product.Product;

public class LegacyCommodityWrapper implements Commodity {
	private LegacyCommoditiesService service;
	private Product legacy;

	public LegacyCommodityWrapper(LegacyCommoditiesService service, Product legacy) {
		this.service = service;
		this.legacy = legacy;
	}

	public Product getLegacy() { return legacy; }

	@Override
	public TextComponent getDisplayName() { return service.getTextFactory().literal(legacy.getProductName()); }

	@Override
	public String getDisplayItemString() { return "minecraft:barrier"; }

	@Override
	public long getAmountAvailable(Player player) {
		return service.getLegacyAdapter().getUnits(((PlayerWrapper) player).getEntity(), legacy).or(0);
	}

	@Override
	public long takeAmount(Player player, long amount) {
		service.getLegacyAdapter().removeUnitsFrom(((PlayerWrapper) player).getEntity(), legacy, (int) amount);
		return 0;
	}

	@Override
	public long giveAmount(Player player, long amount) {
		service.getLegacyAdapter().addUnitsTo(((PlayerWrapper) player).getEntity(), legacy, (int) amount);
		return 0;
	}
}
