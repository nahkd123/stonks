package io.github.nahkd123.stonks.service.provider.memory;

import com.google.auto.service.AutoService;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;

/**
 * <p>
 * A market service provider that provides {@link MemoryMarketService}. This
 * service provider does not require configuration.
 * </p>
 */
@AutoService(MarketServiceProvider.class)
public class MemoryServiceProvider implements MarketServiceProvider {
	@Override
	public String getProviderName() { return "memory"; }

	@Override
	public MarketServiceHost createHost(Object config) {
		return new Host(new MemoryMarketService());
	}

	class Host implements MarketServiceHost {
		private MemoryMarketService service;

		public Host(MemoryMarketService service) {
			this.service = service;
		}

		@Override
		public MarketService getService() { return service; }

		@Override
		public void startService() {}

		@Override
		public void stopService() {}
	}
}
