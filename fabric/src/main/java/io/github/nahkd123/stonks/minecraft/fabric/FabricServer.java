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
package io.github.nahkd123.stonks.minecraft.fabric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import io.github.nahkd123.stonks.market.MarketCache;
import io.github.nahkd123.stonks.market.MarketService;
import io.github.nahkd123.stonks.market.impl.legacy.LegacyMarketService;
import io.github.nahkd123.stonks.market.impl.queue.QueuedMarketService;
import io.github.nahkd123.stonks.minecraft.MinecraftPlatform;
import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.commodity.CommoditiesService;
import io.github.nahkd123.stonks.minecraft.economy.CachedEconomyService;
import io.github.nahkd123.stonks.minecraft.fabric.commodity.LegacyCommoditiesService;
import io.github.nahkd123.stonks.minecraft.fabric.economy.LegacyEconomyService;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.GuiTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.utils.ItemStacksDeriver;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.WorldSavePath;
import stonks.fabric.StonksFabricPlatform;
import stonks.fabric.adapter.StonksFabricAdapter;

@SuppressWarnings("deprecation")
public class FabricServer implements MinecraftServer {
	private FabricPlatform platform;
	private net.minecraft.server.MinecraftServer underlying;
	private MarketService market;
	private MarketCache marketCache;
	private CachedEconomyService economy;
	private List<CommoditiesService> commodities = new ArrayList<>();

	private Map<String, GuiTemplate> guiTemplates = new HashMap<>();
	private ItemStacksDeriver stacksDeriver;

	public FabricServer(FabricPlatform platform, net.minecraft.server.MinecraftServer underlying) {
		StonksFabricPlatform legacy = (StonksFabricPlatform) underlying;
		TextFactory textFactory = platform.getTextFactory();
		StonksFabricAdapter adapter = legacy.getStonksAdapter();

		this.platform = platform;
		this.underlying = underlying;
		this.market = new QueuedMarketService(new LegacyMarketService(legacy
			.getStonksService(), () -> legacy.getPlatformConfig().decimals), Executors.newCachedThreadPool());
		this.marketCache = new MarketCache(market);
		this.economy = new CachedEconomyService(new LegacyEconomyService(textFactory, legacy
			.getPlatformConfig().decimals, adapter));
		this.commodities.add(new LegacyCommoditiesService(textFactory, adapter));

		this.stacksDeriver = new ItemStacksDeriver(CommandManager
			.createRegistryAccess(underlying.getRegistryManager()));
	}

	public static FabricServer of(net.minecraft.server.MinecraftServer server) {
		return ((StonksFabricPlatform) server).getModernWrapper();
	}

	@Override
	public MarketService getMarketService() { return market; }

	@Override
	public MarketCache getMarketCache() { return marketCache; }

	@Override
	public MinecraftPlatform getPlatform() { return platform; }

	@Override
	public Path getServerDir() { return underlying.getSavePath(WorldSavePath.ROOT); }

	@Override
	public Player getPlayerByUUID(UUID uuid) {
		// TODO cache player object to somewhere?
		return new PlayerWrapper(underlying, uuid);
	}

	@Override
	public CachedEconomyService getEconomyService() { return economy; }

	@Override
	public List<CommoditiesService> getCommoditiesServices() { return commodities; }

	public Map<String, GuiTemplate> getGuiTemplates() { return guiTemplates; }

	public GuiTemplate getGuiTemplate(ContainerGui backend) {
		return getGuiTemplates().get(backend.getId());
	}

	public ItemStacksDeriver getStacksDeriver() { return stacksDeriver; }
}
