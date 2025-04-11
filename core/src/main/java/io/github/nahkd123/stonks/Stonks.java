package io.github.nahkd123.stonks;

import io.github.nahkd123.stonks.service.MarketService;

/**
 * <p>
 * Represent an instance of Stonks. Stonks instance is usually bounds to an
 * instance of Minecraft server (either integrated or dedicated) or a process
 * (one process can spawn multiple instances of Stonks).
 * </p>
 * <p>
 * Stonks instances are implemented by platform implementation (they are not
 * supposed to be implemented by API consumers). Getting an instance of
 * {@link Stonks} rely on platform-specific API. The instance is usually
 * configured by user, and in some rare cases, by code.
 * </p>
 */
public interface Stonks {
	/**
	 * <p>
	 * Get the current market service that is running in this {@link Stonks}
	 * instance. The lifecycle of market service is managed by platform's
	 * implementation. It is not possible for {@link Stonks} to have no market
	 * service, which means the return value of this method will never be
	 * {@code null}.
	 * </p>
	 * 
	 * @return The current market service.
	 */
	MarketService getMarketService();
}
