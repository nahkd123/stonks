package io.github.nahkd123.stonks.service.provided.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.ManagableMarketService;
import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceConfig;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.utils.EmitHandler;

/**
 * <p>
 * Reference implementation for {@link MarketService}. Every async methods are
 * actually synchronous. Attempting to call methods asynchronously may results
 * in broken state. If you want, consider making a wrapper that converts method
 * calls into transactions and execute each transaction one by one.
 * </p>
 */
public class MemoryMarketService implements ManagableMarketService {
	private Set<MemoryProduct> catalog = new HashSet<>();
	EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();
	Map<UUID, Map<UUID, MemoryOffer>> userOffers = new HashMap<>();
	ServiceConfig config = new ServiceConfig(false, 5);

	@Override
	public void addNotificationListener(ServiceNotificationListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeNotificationListener(ServiceNotificationListener listener) {
		listeners.removeListener(listener);
	}

	@Override
	public CompletableFuture<Set<? extends Product>> queryCatalog() {
		return CompletableFuture.completedFuture(Set.copyOf(catalog));
	}

	@Override
	public CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid) {
		Collection<MemoryOffer> offers = userOffers.computeIfAbsent(uuid, $ -> new HashMap<>()).values();
		return CompletableFuture.completedFuture(Set.copyOf(offers));
	}

	@Override
	public CompletableFuture<MemoryProduct> createProduct(String id) {
		MemoryProduct product = new MemoryProduct(this, id);
		catalog.add(product);
		listeners.beginEmit(listener -> listener.onCatalogUpdate(this, catalog));
		return CompletableFuture.completedFuture(product);
	}

	@Override
	public CompletableFuture<Void> deleteProduct(Product product) {
		if (catalog.remove(product)) {
			listeners.beginEmit(listener -> listener.onCatalogUpdate(this, catalog));
			return CompletableFuture.completedFuture(null);
		} else {
			return CompletableFuture.failedFuture(new ServiceException("Product %s does not exists in catalog"
				.formatted(product.getId())));
		}
	}

	@Override
	public CompletableFuture<ServiceConfig> queryConfig() {
		return CompletableFuture.completedFuture(config);
	}

	@Override
	public CompletableFuture<Void> useConfig(ServiceConfig config) {
		this.config = config;
		return CompletableFuture.completedFuture(null);
	}
}
