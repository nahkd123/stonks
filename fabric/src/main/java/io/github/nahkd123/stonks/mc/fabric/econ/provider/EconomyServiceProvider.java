package io.github.nahkd123.stonks.mc.fabric.econ.provider;

import com.mojang.serialization.MapCodec;

import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import net.minecraft.server.MinecraftServer;

public interface EconomyServiceProvider<C> {
	String getProviderName();

	MapCodec<C> getConfigCodec();

	EconomyService createService(MinecraftServer server, C config);

	default MapCodec<ServiceSupplier<C>> createSupplierCodec() {
		return getConfigCodec().xmap(c -> new ServiceSupplier<>(this, c), s -> s.config);
	}

	record ServiceSupplier<C>(EconomyServiceProvider<C> provider, C config) {
		public EconomyService get(MinecraftServer server) {
			return provider.createService(server, config);
		}
	}
}
