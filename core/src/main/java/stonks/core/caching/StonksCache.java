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
