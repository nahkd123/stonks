package stonks.core.caching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import nahara.common.tasks.Task;
import stonks.core.market.Offer;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;

public class StonksServiceCache {
	private StonksService service;
	private Task<List<Category>> categories;
	private Map<String, Category> categoryIdMap = null;

	// Expiring cache
	private Map<UUID, Cached<List<Offer>>> offers = new WeakHashMap<>();
	private Map<Product, Cached<ProductMarketOverview>> overviews = new WeakHashMap<>();

	public StonksServiceCache(StonksService service) {
		this.service = service;
	}

	public StonksService getService() { return service; }

	public Task<List<Category>> getAllCategories() {
		if (categories == null) return categories = service.queryAllCategories()
			.afterThatDo(categories -> {
				categoryIdMap = new HashMap<>();
				for (var category : categories) { categoryIdMap.put(category.getCategoryId(), category); }
				return categories;
			});
		return categories;
	}

	public Task<Category> getCategoryById(String id) {
		if (categoryIdMap != null) return Task.resolved(categoryIdMap.get(id));
		return getAllCategories().afterThatDo($ -> { return categoryIdMap.get(id); });
	}

	public Cached<List<Offer>> getOffers(UUID offerer) {
		var cachedList = offers.get(offerer);
		if (cachedList == null) offers.put(offerer, cachedList = new Cached<>(() -> getService().getOffers(offerer)));
		return cachedList;
	}

	public Cached<ProductMarketOverview> getOverview(Product product) {
		var cachedOverview = overviews.get(product);
		if (cachedOverview == null) overviews.put(product,
			cachedOverview = new Cached<>(() -> getService().queryProductMarketOverview(product)));
		return cachedOverview;
	}
}
