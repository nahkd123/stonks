/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.core.caching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import stonks.core.market.Offer;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;

public class StonksCache {
	private StonksService service;
	private CompletableFuture<List<Category>> categories;
	private Map<String, Category> categoryIdMap = null;

	// Expiring cache
	private Map<UUID, FutureCache<List<Offer>>> offers = new WeakHashMap<>();
	private Map<Product, FutureCache<ProductMarketOverview>> overviews = new WeakHashMap<>();

	public StonksCache(StonksService service) {
		this.service = service;
	}

	public StonksService getService() { return service; }

	public CompletableFuture<List<Category>> getAllCategories() {
		if (categories == null) return categories = service.queryAllCategoriesAsync()
			.thenApply(categories -> {
				categoryIdMap = new HashMap<>();
				for (var category : categories) { categoryIdMap.put(category.getCategoryId(), category); }
				return categories;
			});
		return categories;
	}

	public CompletableFuture<Category> getCategoryById(String id) {
		if (categoryIdMap != null) return CompletableFuture.completedFuture(categoryIdMap.get(id));
		return getAllCategories().thenApply($ -> { return categoryIdMap.get(id); });
	}

	public FutureCache<List<Offer>> getOffers(UUID offerer) {
		var cachedList = offers.get(offerer);
		if (cachedList == null)
			offers.put(offerer, cachedList = new FutureCache<>(() -> getService().getOffersFromUserAsync(offerer)));
		return cachedList;
	}

	public FutureCache<ProductMarketOverview> getOverview(Product product) {
		var cachedOverview = overviews.get(product);
		if (cachedOverview == null) overviews.put(product,
			cachedOverview = new FutureCache<>(() -> getService().queryMarketOverviewAsync(product)));
		return cachedOverview;
	}
}
