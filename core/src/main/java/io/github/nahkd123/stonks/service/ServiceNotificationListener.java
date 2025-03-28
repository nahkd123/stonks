package io.github.nahkd123.stonks.service;

import java.util.Set;

/**
 * <p>
 * A listener that listens to notifications from {@link MarketService}.
 * </p>
 */
public interface ServiceNotificationListener {
	/**
	 * <p>
	 * Called when the product catalog of service changed (added, removed or
	 * updated). This will not be called when product's overview data changed.
	 * </p>
	 * 
	 * @param sender   The service that emit this notification.
	 * @param products A collection of products in new catalog.
	 */
	default void onCatalogUpdate(MarketService sender, Set<? extends Product> products) {}

	/**
	 * <p>
	 * Called when an offer is fully filled but not yet fully claimed. This will be
	 * used to push notification to user.
	 * </p>
	 * 
	 * @param sender The service that emit this notification.
	 * @param offer  The offer that has been fully filled.
	 */
	default void onOfferFilled(MarketService sender, Offer offer) {}
}
