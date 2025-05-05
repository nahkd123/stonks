package io.github.nahkd123.stonks.mc.fabric;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.mc.fabric.config.ConfigPaths;
import io.github.nahkd123.stonks.mc.fabric.config.MainConfig;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.EconomyServiceProvider;
import io.github.nahkd123.stonks.mc.fabric.product.Category;
import io.github.nahkd123.stonks.mc.fabric.product.CategoryProduct;
import io.github.nahkd123.stonks.mc.fabric.product.ProductInfo;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ProductInfoProvider;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

class StonksMcUniverseImpl implements StonksMcUniverse {
	private Logger logger;
	private Map<String, MarketServiceProvider<?>> marketProviders = new HashMap<>();
	private Map<String, EconomyServiceProvider<?>> economyProviders = new HashMap<>();
	private Map<String, ProductInfoProvider<?>> productInfoProviders = new HashMap<>();
	private Map<Class<?>, String> reverseProviders = new HashMap<>();

	public StonksMcUniverseImpl(Logger logger) {
		this.logger = logger;
		for (MarketServiceProvider<?> market : ServiceLoader.load(MarketServiceProvider.class))
			registerProvider(market);
	}

	@Override
	public void registerProvider(MarketServiceProvider<?> provider) {
		if (marketProviders.putIfAbsent(provider.getProviderName(), provider) != null) {
			throw new IllegalStateException("Market service provider '%s' is already registered"
				.formatted(provider.getProviderName()));
		}
	}

	@Override
	public void registerProvider(EconomyServiceProvider<?> provider) {
		if (economyProviders.putIfAbsent(provider.getProviderName(), provider) != null) {
			throw new IllegalStateException("Economy service provider '%s' is already registered"
				.formatted(provider.getProviderName()));
		}
	}

	@Override
	public void registerProvider(ProductInfoProvider<?> provider) {
		if (productInfoProviders.putIfAbsent(provider.getTypeName(), provider) != null) {
			throw new IllegalStateException("Product info provider '%s' is already registered"
				.formatted(provider.getTypeName()));
		}

		reverseProviders.put(provider.getInfoClass(), provider.getTypeName());
	}

	/**
	 * <p>
	 * Create a new Stonks for Minecraft (Fabric) instance that is tied to running
	 * Minecraft server.
	 * </p>
	 * 
	 * @param server The Minecraft server instance.
	 * @param paths  All the paths to different kind of configurations.
	 * @return A new Stonks for Minecraft instance.
	 * @throws IOException
	 */
	public StonksMcInstanceImpl createInstance(MinecraftServer server, ConfigPaths paths) throws IOException {
		MainConfig config;
		try (Reader reader = Files.newBufferedReader(paths.mainConfig(), StandardCharsets.UTF_8)) {
			JsonElement json = JsonParser.parseReader(reader);
			config = mainConfigMapCodec.codec()
				.decode(JsonOps.INSTANCE, json)
				.getPartialOrThrow()
				.getFirst();
		}

		// Market
		MarketServiceProvider<?> marketProvider = marketProviders.get(config.services().market().type());
		if (marketProvider == null)
			throw new IllegalStateException("Market service provider %s is not installed"
				.formatted(config.services().market().type()));
		MarketServiceHost marketHost = marketProvider.createHost(config.services().market().config());

		// Economy
		EconomyService economyService = config.services().economy().map(s -> s.get(server)).orElse(null);

		return new StonksMcInstanceImpl(logger, marketHost, economyService, config);
	}

	// Serializing
	private MapCodec<EconomyServiceProvider.ServiceSupplier<?>> economySupplierMapCodec = Codec.STRING.dispatchMap(
		"type",
		s -> s.provider().getProviderName(),
		n -> {
			EconomyServiceProvider<?> provider = economyProviders.get(n);
			if (provider == null)
				throw new IllegalArgumentException("Economy provider is not registered: %s".formatted(n));
			return provider.createSupplierCodec();
		});

	private MapCodec<CategoryProduct> categoryProductMapCodec = RecordCodecBuilder.mapCodec(i -> i.group(
		Codec.STRING.fieldOf("productId").forGetter(CategoryProduct::productId),
		Codec.STRING.<ProductInfo>dispatchMap(
			"type",
			info -> reverseProviders.get(info.getClass()),
			type -> {
				ProductInfoProvider<?> provider = productInfoProviders.get(type);
				if (provider == null)
					throw new IllegalArgumentException("Product info provider is not registered: %s".formatted(type));
				return provider.getCodec();
			}).forGetter(CategoryProduct::info))
		.apply(i, CategoryProduct::new));

	private MapCodec<Category> categoryMapCodec = RecordCodecBuilder.mapCodec(i -> i.group(
		ItemStack.CODEC
			.fieldOf("icon")
			.forGetter(Category::icon),
		categoryProductMapCodec.codec().listOf()
			.optionalFieldOf("products", List.of())
			.forGetter(Category::products))
		.apply(i, Category::new));

	private MapCodec<MainConfig> mainConfigMapCodec = MainConfig.createCodec(
		economySupplierMapCodec,
		categoryMapCodec);
}
