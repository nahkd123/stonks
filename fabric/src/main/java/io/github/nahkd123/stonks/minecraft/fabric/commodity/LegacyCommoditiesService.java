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

import java.util.HashMap;
import java.util.Map;

import io.github.nahkd123.stonks.minecraft.commodity.CommoditiesService;
import io.github.nahkd123.stonks.minecraft.commodity.Commodity;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import stonks.core.product.Product;
import stonks.fabric.adapter.StonksFabricAdapter;

public class LegacyCommoditiesService implements CommoditiesService {
	private StonksFabricAdapter adapter;
	private TextFactory textFactory;
	private Map<String, LegacyCommodityWrapper> wrappers = new HashMap<>();

	public LegacyCommoditiesService(TextFactory textFactory, StonksFabricAdapter adapter) {
		this.textFactory = textFactory;
		this.adapter = adapter;
	}

	public TextFactory getTextFactory() { return textFactory; }

	public StonksFabricAdapter getLegacyAdapter() { return adapter; }

	@Override
	public Commodity typeFromString(String str) {
		return wrappers.get(str);
	}

	public Commodity typeFromLegacy(Product product) {
		LegacyCommodityWrapper wrapper = wrappers.get(product.getProductConstructionData());
		if (wrapper == null)
			wrappers.put(product.getProductConstructionData(), wrapper = new LegacyCommodityWrapper(this, product));
		return wrapper;
	}
}
