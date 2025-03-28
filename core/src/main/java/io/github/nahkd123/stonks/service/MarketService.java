package io.github.nahkd123.stonks.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Represent an interface for interacting with market system.
 * </p>
 */
public interface MarketService {
	void addNotificationListener(ServiceNotificationListener listener);

	void removeNotificationListener(ServiceNotificationListener listener);

	/**
	 * <p>
	 * Query entire catalog from this service. Typically you may only use this while
	 * initializing instance; for watching catalog updates you may use
	 * {@link #addNotificationListener(ServiceNotificationListener)}.
	 * </p>
	 * 
	 * @return Async task that resolves to collection of products.
	 */
	CompletableFuture<Set<? extends Product>> queryCatalog();

	/**
	 * <p>
	 * Query all offers made by specific user with given user's UUID.
	 * </p>
	 * 
	 * @param uuid The UUID of user.
	 * @return Async task that resolves to collection of offers made by user.
	 */
	CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid);
}
