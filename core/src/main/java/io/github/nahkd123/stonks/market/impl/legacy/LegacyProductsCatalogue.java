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
package io.github.nahkd123.stonks.market.impl.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nahkd123.stonks.market.catalogue.Category;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;

public class LegacyProductsCatalogue implements ProductsCatalogue {
	private List<Category> categories;
	private List<Product> products = new ArrayList<>();

	public LegacyProductsCatalogue(List<stonks.core.product.Category> legacyCategories) {
		categories = legacyCategories.stream()
			.map(l -> (Category) new LegacyCategoryWrapper(products::add, l))
			.toList();
	}

	@Override
	public List<Category> getCategories() { return categories; }

	@Override
	public List<Product> getProducts() { return Collections.unmodifiableList(products); }
}
