package io.github.nahkd123.stonks.service.provider;

import io.github.nahkd123.stonks.Stonks;
import io.github.nahkd123.stonks.service.MarketService;

public interface MarketServiceHost {
	/**
	 * <p>
	 * Get the service interface. This will be exposed by platform through
	 * {@link Stonks#getMarketService()}.
	 * </p>
	 * 
	 * @return The market service interface.
	 */
	MarketService getService();

	/**
	 * <p>
	 * Start the service.
	 * </p>
	 */
	void startService();

	/**
	 * <p>
	 * Stop the service.
	 * </p>
	 */
	void stopService();
}
