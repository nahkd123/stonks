package io.github.nahkd123.stonks.mc.fabric;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import io.github.nahkd123.stonks.mc.fabric.config.MainConfig;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.product.Category;
import io.github.nahkd123.stonks.service.ManageableMarketService;
import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;

class StonksMcInstanceImpl implements StonksMcInstance {
	private Logger logger;
	private MarketServiceHost marketHost;
	private EconomyService economy;
	private MainConfig config;

	public StonksMcInstanceImpl(Logger logger, MarketServiceHost marketHost, EconomyService economy, MainConfig config) {
		this.logger = logger;
		this.marketHost = marketHost;
		this.economy = economy;
		this.config = config;
	}

	@Override
	public MarketService getMarketService() { return marketHost.getService(); }

	@Override
	public EconomyService getEconomyService() { return economy; }

	@Override
	public List<Category> getCatalog() { return Collections.unmodifiableList(config.catalog()); }

	public void onServerStarting() {
		marketHost.startService();
		boolean createMissingProducts = config.services().market().createMissingProducts();
		boolean deleteExtraProducts = config.services().market().deleteExtraProducts();

		if (createMissingProducts || deleteExtraProducts) {
			Map<String, ? extends Product> serviceCatalog = marketHost.getService().queryCatalog().join()
				.stream()
				.collect(Collectors.toMap(p -> p.getId(), Function.identity()));

			Set<String> configIds = config.catalog().stream()
				.flatMap(p -> p.products().stream())
				.map(p -> p.productId())
				.collect(Collectors.toSet());

			Set<String> extraIds = new HashSet<>(serviceCatalog.keySet());
			extraIds.removeAll(configIds);

			Set<String> missingIds = new HashSet<>(configIds);
			missingIds.removeAll(serviceCatalog.keySet());

			if ((deleteExtraProducts && extraIds.size() > 0) || (createMissingProducts && missingIds.size() > 0)) {
				if (marketHost.getService() instanceof ManageableMarketService mgr) {
					if (createMissingProducts) {
						logger.info("createMissingProducts is specified");

						for (String id : missingIds) {
							logger.info("  Creating product with ID '{}'...", id);
							mgr.createProduct(id).join();
						}
					}

					if (deleteExtraProducts) {
						logger.info("deleteExtraProducts is specified");

						for (String id : extraIds) {
							Product serviceProduct = serviceCatalog.get(id);
							if (serviceProduct == null) continue;
							logger.info("  Deleting product with ID '{}'...", id);
							mgr.deleteProduct(serviceProduct);
						}
					}

					logger.info("Completed synchronizing service's catalog with configuration!");
				} else {
					logger.warn("Market service is not manageable");
					logger.warn("Service catalog will not be synchronized with configuration");
				}
			} else {
				logger.info("Service catalog appears to be synchronized with configuration");
			}
		}
	}

	public void onServerStopping() {
		marketHost.stopService();
	}

	@Override
	public double getTax() { return config.services().market().tax(); }
}
