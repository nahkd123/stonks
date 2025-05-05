package io.github.nahkd123.stonks.mc.fabric;

import java.util.ServiceLoader;

import io.github.nahkd123.stonks.mc.fabric.econ.provider.EconomyServiceProvider;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ProductInfoProvider;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;

public interface StonksMcUniverse {
	/**
	 * <p>
	 * Register market service provider, in addition to providers loaded using
	 * {@link ServiceLoader}. You can register providers that are platform-specific
	 * by using this method.
	 * </p>
	 * 
	 * @param provider The provider.
	 */
	void registerProvider(MarketServiceProvider<?> provider);

	/**
	 * <p>
	 * Register economy service provider.
	 * </p>
	 * 
	 * @param provider The provider.
	 */
	void registerProvider(EconomyServiceProvider<?> provider);

	/**
	 * <p>
	 * Register product info provider.
	 * </p>
	 * 
	 * @param provider The provider.
	 */
	void registerProvider(ProductInfoProvider<?> provider);
}
