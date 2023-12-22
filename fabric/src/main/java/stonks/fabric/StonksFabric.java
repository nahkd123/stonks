/*
 * Copyright (c) 2023 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package stonks.fabric;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.nahkd123.stonks.minecraft.fabric.ModernStonksFabric;
import nahara.common.configurations.Config;
import nahara.modkit.annotations.v1.Dependencies;
import nahara.modkit.annotations.v1.Dependency;
import nahara.modkit.annotations.v1.EntryPoint;
import nahara.modkit.annotations.v1.Mod;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.product.Product;
import stonks.core.service.LocalStonksService;
import stonks.core.service.memory.StonksMemoryService;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.adapter.provided.CommonEconomyAdapter;
import stonks.fabric.adapter.provided.ItemsAdapter;
import stonks.fabric.adapter.provided.ScoreboardEconomyAdapter;
import stonks.fabric.adapter.provided.ScoreboardUnitAdapter;
import stonks.fabric.command.MarketCommand;
import stonks.fabric.command.StonksCommand;
import stonks.fabric.provider.StonksProvidersRegistry;
import stonks.fabric.service.IntegratedStonksService;
import stonks.fabric.service.IntegratedUnstableStonksService;
import stonks.fabric.service.StonksServiceProvider;

@Mod(
	modid = StonksFabric.MODID,
	name = "Stonks2",
	version = "${stonks_version}",
	description = "Stonks2 for Fabric",
	authors = "nahkd123",
	license = "MIT")
@Dependencies({
	@Dependency(value = "fabricloader", version = ">=${loader_version}"),
	@Dependency(value = "minecraft", version = ">=${minecraft_version}"),
	@Dependency(value = "java", version = ">=17"),
	@Dependency(value = "fabric-api", version = ">=${fabric_version}")
})
public class StonksFabric {
	public static final String MODID = "stonks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@EntryPoint
	public static void init() {
		LOGGER.info("Stonks2 is now initializing...");
		LOGGER.info("Configuration directory is '{}'", getConfigDir());

		// Register configurable stuffs
		IntegratedStonksService.register();
		IntegratedUnstableStonksService.register();

		ItemsAdapter.register();
		ScoreboardUnitAdapter.register();

		ScoreboardEconomyAdapter.register();
		CommonEconomyAdapter.register();

		// Events
		ServerLifecycleEvents.SERVER_STARTING.register(StonksFabric::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPING.register(StonksFabric::onServerStop);
		ServerTickEvents.END_SERVER_TICK.register(StonksFabric::onServerTick);

		ModernStonksFabric.initialize();

		// Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(StonksCommand.ROOT);
			dispatcher.register(MarketCommand.ROOT);
		});
	}

	private static void onServerStart(MinecraftServer server) {
		StonksServiceProvider service = $ -> new StonksMemoryService();
		var adapters = new ArrayList<StonksFabricAdapterProvider>();
		var config = new PlatformConfig();

		for (var child : getOrCreateConfig("config").getChildren()) {
			if (child.getKey().equals("useService")) {
				var altService = StonksProvidersRegistry.getServiceProvider(child);
				if (altService != null) service = altService;
				continue;
			}

			if (child.getKey().equals("useAdapter")) {
				var newAdapter = StonksProvidersRegistry.getAdapterProvider(child);
				if (newAdapter != null) adapters.add(newAdapter);
				continue;
			}

			if (child.getKey().equals("platformConfig") || child.getKey().equals("fabric.platformConfig")) {
				LOGGER.info("Found platform configurations!");
				config.fromConfig(server, child);
			}
		}

		LOGGER.info("Loading Stonks...");
		((StonksFabricPlatform) server).startStonks(
			config,
			service,
			adapters);

		LOGGER.info("Subscribing to service events...");
		((StonksFabricPlatform) server).getStonksService().subscribeToOfferFilledEvents(filled -> {
			StonksFabricHelper.sendOfferFilledMessage(server, filled);
		});

		LOGGER.info("Platform configurations:");
		LOGGER.info("  Decimal points: {} (minimum of ${})",
			config.decimals,
			StonksFabricUtils.CURRENCY_FORMATTER.format(1d / Math.pow(10, config.decimals)));
		LOGGER.info("  Tax: {}", StonksFabricUtils.TAX_FORMATTER.format(config.tax));
		LOGGER.info("  Top offer delta: ${}",
			StonksFabricUtils.CURRENCY_FORMATTER.format(config.topOfferPriceDelta));
	}

	private static void onServerStop(MinecraftServer server) {
		if (getPlatform(server).getStonksService() instanceof LocalStonksService local) {
			LOGGER.info("Saving data for local service...");
			local.saveServiceData();
		}
	}

	private static void onServerTick(MinecraftServer server) {
		((StonksFabricPlatform) server).getTasksHandler().tick();
		((StonksFabricPlatform) server).getSounds().tick();
	}

	public static StonksFabricPlatform getPlatform(MinecraftServer server) {
		return (StonksFabricPlatform) server;
	}

	public static StonksFabricPlatform getPlatform(ServerPlayerEntity player) {
		return getPlatform(player.getServer());
	}

	public static ItemStack getDisplayStack(StonksFabricAdapter adapter, Product product) {
		var out = adapter.createDisplayStack(product);

		if (out == null) {
			out = new ItemStack(Items.BARRIER);
			out.setCustomName(Text.literal(product.getProductName() + " (Invaild display)")
				.styled(s -> s.withColor(Formatting.RED)));
		}

		return out;
	}

	public static Path getConfigDir() {
		var dir = FabricLoader.getInstance().getConfigDir().resolve(MODID);
		if (Files.notExists(dir)) {
			try {
				Files.createDirectories(dir);
				return dir;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return dir;
	}

	public static Config getOrCreateConfig(String name) {
		Path file = getConfigDir();
		if (file == null) {
			LOGGER.error("Unable to obtain path to configuration file");
			return new Config();
		}

		file = file.resolve(name);
		if (Files.notExists(file)) {
			LOGGER.warn("Configuration file '{}' not found! Creating new one...", name);
			ClassLoader classLoader = StonksFabric.class.getClassLoader();

			try (InputStream stream = classLoader.getResourceAsStream(name)) {
				Files.copy(stream, file);
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.error("Failed to create {} file, skipping", name);
				return new Config();
			}
		}

		try {
			return Config.parseConfig(file);
		} catch (IOException e) {
			e.printStackTrace();
			return new Config();
		}
	}
}
