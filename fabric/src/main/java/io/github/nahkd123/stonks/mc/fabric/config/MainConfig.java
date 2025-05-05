package io.github.nahkd123.stonks.mc.fabric.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.mc.fabric.econ.provider.EconomyServiceProvider;
import io.github.nahkd123.stonks.mc.fabric.product.Category;

public record MainConfig(Services services, List<Category> catalog) {
	public static record Services(Market market, Optional<EconomyServiceProvider.ServiceSupplier<?>> economy) {
		public static record Market(String type, Dynamic<?> config, boolean createMissingProducts, boolean deleteExtraProducts, double tax) {
			public static final MapCodec<Market> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.fieldOf("type").forGetter(Market::type),
				Codec.PASSTHROUGH.optionalFieldOf("config", null).forGetter(Market::config),
				Codec.BOOL.optionalFieldOf("createMissingProduct", true).forGetter(Market::createMissingProducts),
				Codec.BOOL.optionalFieldOf("deleteExtraProducts", false).forGetter(Market::deleteExtraProducts),
				Codec.DOUBLE.optionalFieldOf("tax", 0d).forGetter(Market::tax))
				.apply(i, Market::new));
		}

		public static MapCodec<Services> createMapCodec(MapCodec<EconomyServiceProvider.ServiceSupplier<?>> economy) {
			return RecordCodecBuilder.mapCodec(i -> i.group(
				Market.MAP_CODEC.fieldOf("market").forGetter(Services::market),
				economy.codec().optionalFieldOf("economy").forGetter(Services::economy))
				.apply(i, Services::new));
		}
	}

	public static MapCodec<MainConfig> createCodec(MapCodec<EconomyServiceProvider.ServiceSupplier<?>> economy, MapCodec<Category> category) {
		return RecordCodecBuilder.mapCodec(i -> i.group(
			Services.createMapCodec(economy).fieldOf("services").forGetter(MainConfig::services),
			category.codec().listOf().optionalFieldOf("catalog", List.of()).forGetter(MainConfig::catalog))
			.apply(i, MainConfig::new));
	}
}
