package io.github.nahkd123.stonks.mc.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import io.github.nahkd123.stonks.mc.fabric.bridge.MinecraftServerBridge;
import io.github.nahkd123.stonks.mc.fabric.command.DevelopmentTestCommands;
import io.github.nahkd123.stonks.mc.fabric.command.StonksCommands;
import io.github.nahkd123.stonks.mc.fabric.config.ConfigPaths;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.ItemEconomyServiceProvider;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.PatboxCommonEconomyApiServiceProvider;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ItemProductInfoProvider;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ScoreProductInfoProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;

/**
 * <p>
 * Contains the entry point for initializing Stonks Fabric mod.
 * </p>
 */
public class StonksMod {
	public static final Logger LOGGER = stonks.fabric.StonksFabric.LOGGER;
	public static final String MODID = stonks.fabric.StonksFabric.MODID;

	/**
	 * <p>
	 * Stonks' Fabric mod entry point.
	 * </p>
	 */
	public static void entryPoint() {
		StonksMcCallbacks.UNIVERSE_INIT.register(universe -> {
			// Most market services are loaded through ServiceLoader
			// TODO: Include market service for loading legacy Stonks service data

			// Economy services
			universe.registerProvider(new ItemEconomyServiceProvider());
			universe.registerProvider(new PatboxCommonEconomyApiServiceProvider());

			// Product info
			universe.registerProvider(new ItemProductInfoProvider());
			universe.registerProvider(new ScoreProductInfoProvider());
		});

		ServerLifecycleEvents.SERVER_STARTING.register(StonksMod::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(StonksMod::onServerStopping);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(StonksCommands.MARKET);
			dispatcher.register(StonksCommands.ADMIN_ROOT);
		});

		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOGGER.info("Development environment detected");

			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				dispatcher.register(DevelopmentTestCommands.ROOT);
			});
		}
	}

	private static void onServerStarting(MinecraftServer server) {
		try {
			// TODO we are good to cache universe
			LOGGER.info("Initializing universe...");
			StonksMcUniverseImpl universe = new StonksMcUniverseImpl(LOGGER);
			StonksMcCallbacks.UNIVERSE_INIT.invoker().onUniverseInit(universe);

			LOGGER.info("Initializing configuration files...");
			Path configRoot = FabricLoader.getInstance().getConfigDir().resolve(MODID);
			ConfigPaths configPaths = new ConfigPaths(configRoot);
			copyModResource(configPaths.mainConfig(), "stonks.config.json", false);

			LOGGER.info("Initializing instance...");
			StonksMcInstanceImpl instance = universe.createInstance(server, configPaths);
			StonksMcCallbacks.INSTANCE_INIT.invoker().onInstanceInit(instance);
			((MinecraftServerBridge) server).attachStonks(instance);
			instance.onServerStarting();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void onServerStopping(MinecraftServer server) {
		LOGGER.info("Stopping instance...");
		StonksMcInstanceImpl instance = (StonksMcInstanceImpl) ((MinecraftServerBridge) server).getStonks();
		instance.onServerStopping();
		((MinecraftServerBridge) server).attachStonks(null);
	}

	private static void copyModResource(Path dest, String resName, boolean copyOnExists) throws IOException {
		if (!copyOnExists && Files.exists(dest)) return;
		ModContainer mod = FabricLoader.getInstance().getModContainer(MODID).get();
		Path resPath = mod.findPath(resName).get();
		Files.copy(resPath, dest);
		LOGGER.info("Copied '%s' from mod to '%s'".formatted(resName, dest));
	}

	/**
	 * <p>
	 * Get the Stonks instance from existing Minecraft server. Stonks instances are
	 * always started with Minecraft servers. Each instance contains market, economy
	 * and product inventory service.
	 * </p>
	 * 
	 * @param server The server.
	 * @return The running Stonks instance.
	 */
	public static StonksMcInstance getInstance(MinecraftServer server) {
		return ((MinecraftServerBridge) server).getStonks();
	}
}
