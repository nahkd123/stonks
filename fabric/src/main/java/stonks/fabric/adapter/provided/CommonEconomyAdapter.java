/*
 * Copyright (c) 2023-2026 nahkd
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
package stonks.fabric.adapter.provided;

import java.math.BigInteger;

import eu.pb4.common.economy.api.CommonEconomy;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.provider.StonksProvidersRegistry;

public class CommonEconomyAdapter implements StonksFabricAdapter {
	private Identifier account;
	private int decimals;

	public CommonEconomyAdapter(Identifier account, int decimals) {
		this.account = account;
		this.decimals = decimals;
	}

	public double toDouble(BigInteger balance) {
		// TODO: Proper implementation
		return balance.doubleValue() / Math.pow(10, decimals);
	}

	public long fromDouble(double money) {
		return (long) Math.round(money * Math.pow(10, decimals));
	}

	@Override
	public double accountBalance(ServerPlayer player) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountBalance(player);
		return toDouble(playerAccount.balance());
	}

	@Override
	public boolean accountDeposit(ServerPlayer player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountDeposit(player, money);
		var txn = playerAccount.increaseBalance(fromDouble(money));
		if (txn.isSuccessful()) return true;
		return StonksFabricAdapter.super.accountDeposit(player, money);
	}

	@Override
	public boolean accountWithdraw(ServerPlayer player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountWithdraw(player, money);
		var txn = playerAccount.decreaseBalance(fromDouble(money));
		if (txn.isSuccessful()) return true;
		return StonksFabricAdapter.super.accountWithdraw(player, money);
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(CommonEconomyAdapter.class, (server, config) -> {
			var id = config.firstChild("id")
				.map(v -> Identifier.parse(v.getValue().get()))
				.orElse(Identifier.fromNamespaceAndPath(StonksFabric.MODID, "default_account"));
			var decimals = config.firstChild("decimals").flatMap(v -> v.getValue(Integer::parseInt)).orElse(0);
			return new CommonEconomyAdapter(id, decimals);
		});
	}
}
