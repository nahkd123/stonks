package io.github.nahkd123.stonks.service;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Basically {@link MarketService}, but with extra methods for remotely managing
 * the service. Single instance setup always have {@link ManagableMarketService}
 * in the one and only instance. Multi-instance setup requires extra permission
 * in order to obtain {@link ManagableMarketService}.
 * </p>
 */
public interface ManagableMarketService extends MarketService {
	/**
	 * <p>
	 * Create a new product with specific ID and add it to the catalog.
	 * </p>
	 * 
	 * @param id The ID of the new product.
	 * @return Async task that resolves to new product handle.
	 */
	CompletableFuture<? extends Product> createProduct(String id);

	/**
	 * <p>
	 * Delete an existing product in the catalog.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This will also cancel all ongoing offers targeting the
	 * product. Use at your own caution.
	 * </p>
	 * 
	 * @param product The product to delete.
	 * @return Async task.
	 */
	CompletableFuture<Void> deleteProduct(Product product);

	/**
	 * <p>
	 * Query the running configuration from this service.
	 * </p>
	 */
	CompletableFuture<ServiceConfig> queryConfig();

	/**
	 * <p>
	 * Ask service to use a new configuration.
	 * </p>
	 * 
	 * @param config The configuration to use.
	 */
	CompletableFuture<Void> useConfig(ServiceConfig config);
}
