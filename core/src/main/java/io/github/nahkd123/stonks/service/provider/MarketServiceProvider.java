package io.github.nahkd123.stonks.service.provider;

/**
 * <p>
 * A market service provider, following the SPI (Service Provider Interface)
 * pattern. Services must be declared in
 * {@code META-INF/services/io.github.nahkd123.stonks.service.provider.MarketServiceProvider},
 * with each line is a fully qualified class name of service provider. Below is
 * sample for file content.
 * </p>
 * {@snippet :
 * io.github.nahkd123.stonks.service.provider.database.DatabaseServiceProvider
 * io.github.nahkd123.stonks.service.provider.memory.MemoryServiceProvider
 * io.github.nahkd123.stonks.service.provider.remote.RemoteServiceProvider
 * }
 */
public interface MarketServiceProvider {
	/**
	 * <p>
	 * Get the provider name. This will be used by the platform to determine which
	 * provider to create host, using the provider name configured in user's
	 * configuration file.
	 * </p>
	 * 
	 * @return The provider name.
	 */
	String getProviderName();

	/**
	 * <p>
	 * Create a new market service host, which manages the lifecycle of market
	 * service. The host will be consumed by platform and will call lifetime methods
	 * accordingly.
	 * </p>
	 * 
	 * @param config
	 * @return
	 */
	MarketServiceHost createHost(Object config);
}
