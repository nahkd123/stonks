/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.fabric.adapter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;

public class AdaptersContainer implements StonksFabricAdapter {
	private List<StonksFabricAdapter> adapters = new ArrayList<>();
	private boolean isEconomyWarningLogged = false;

	public AdaptersContainer add(StonksFabricAdapter adapter) {
		adapters.add(adapter);
		return this;
	}

	public List<StonksFabricAdapter> getAdapters() { return adapters; }

	@Override
	public ItemStack createDisplayStack(Product product) {
		ItemStack out;
		for (var a : adapters) { if ((out = a.createDisplayStack(product)) != null) return out; }
		return StonksFabricAdapter.super.createDisplayStack(product);
	}

	@Override
	public int getUnits(ServerPlayerEntity player, Product product) {
		int out;
		for (var a : adapters) { if ((out = a.getUnits(player, product)) != -1) return out; }
		return StonksFabricAdapter.super.getUnits(player, product);
	}

	@Override
	public boolean addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
		for (var a : adapters) { if (a.addUnitsTo(player, product, amount)) return true; }
		return StonksFabricAdapter.super.addUnitsTo(player, product, amount);
	}

	@Override
	public boolean removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
		for (var a : adapters) { if (a.removeUnitsFrom(player, product, amount)) return true; }
		return StonksFabricAdapter.super.removeUnitsFrom(player, product, amount);
	}

	@Override
	public double accountBalance(ServerPlayerEntity player) {
		var out = StonksFabricAdapter.super.accountBalance(player);
		for (var a : adapters) { if ((out = a.accountBalance(player)) >= 0d) return out; }
		logEconomyWarning(player, 0);
		return 0d;
	}

	@Override
	public boolean accountDeposit(ServerPlayerEntity player, double money) {
		for (var a : adapters) { if (a.accountDeposit(player, money)) return true; }
		logEconomyWarning(player, money);
		return StonksFabricAdapter.super.accountDeposit(player, money);
	}

	@Override
	public boolean accountWithdraw(ServerPlayerEntity player, double money) {
		for (var a : adapters) { if (a.accountWithdraw(player, money)) return true; }
		logEconomyWarning(player, -money);
		return StonksFabricAdapter.super.accountWithdraw(player, money);
	}

	private void logEconomyWarning(ServerPlayerEntity player, double moneyMoved) {
		if (!isEconomyWarningLogged) {
			isEconomyWarningLogged = true;
			StonksFabric.LOGGER.warn("");
			StonksFabric.LOGGER.warn("=====");
			StonksFabric.LOGGER.warn("Warning: Economy adapter for Stonks is not configured!");
			StonksFabric.LOGGER.warn("Please configure Stonks to use with your economy mod!");
			StonksFabric.LOGGER.warn("");
			StonksFabric.LOGGER.warn("Example:");
			StonksFabric.LOGGER.warn("    // Use myScoreboard for economy (with 3 decimals place)");
			StonksFabric.LOGGER.warn("    useAdapter stonks.fabric.adapter.provided.ScoreboardEconomyAdapter");
			StonksFabric.LOGGER.warn("        objective myObjective");
			StonksFabric.LOGGER.warn("        decimals 3 // score == 12300 -> balance == $12.3");
			StonksFabric.LOGGER.warn("    // Or use economy from mods with Patbox's Common Economy API");
			StonksFabric.LOGGER.warn("    useAdapter stonks.fabric.adapter.provided.CommonEconomyAdapter");
			StonksFabric.LOGGER.warn("        account modid:account");
			StonksFabric.LOGGER.warn("=====");
			StonksFabric.LOGGER.warn("");
		}

		if (moneyMoved != 0d) StonksFabric.LOGGER.info("(emulated) ECONOMY TRANSACTION: {} {} {}",
			player.getDisplayName().getString(),
			moneyMoved > 0 ? "<--" : "-->",
			StonksFabricUtils.CURRENCY_FORMATTER.format(moneyMoved));
	}
}
