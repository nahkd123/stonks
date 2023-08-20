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
package stonks.fabric.adapter.provided;

import eu.pb4.common.economy.api.CommonEconomy;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.AdapterResponse;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.provider.StonksProvidersRegistry;

public class CommonEconomyAdapter implements StonksFabricAdapter {
	private Identifier account;
	private int decimals;

	public CommonEconomyAdapter(Identifier account, int decimals) {
		this.account = account;
		this.decimals = decimals;
	}

	public double toDouble(long balance) {
		return balance / Math.pow(10, decimals);
	}

	public long fromDouble(double money) {
		return (long) Math.round(money * Math.pow(10, decimals));
	}

	@Override
	public AdapterResponse<Double> accountBalance(ServerPlayerEntity player) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return AdapterResponse.pass();
		return AdapterResponse.success(toDouble(playerAccount.balance()));
	}

	@Override
	public AdapterResponse<Void> accountDeposit(ServerPlayerEntity player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return AdapterResponse.pass();
		var txn = playerAccount.increaseBalance(fromDouble(money));
		return txn.isSuccessful() ? AdapterResponse.success(null) : AdapterResponse.failed(txn.message());
	}

	@Override
	public AdapterResponse<Void> accountWithdraw(ServerPlayerEntity player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return AdapterResponse.pass();
		var txn = playerAccount.decreaseBalance(fromDouble(money));
		return txn.isSuccessful() ? AdapterResponse.success(null) : AdapterResponse.failed(txn.message());
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(CommonEconomyAdapter.class, (server, config) -> {
			var id = config.firstChild("id")
				.map(v -> new Identifier(v.getValue().get()))
				.orElse(new Identifier(StonksFabric.MODID, "default_account"));
			var decimals = config.firstChild("decimals").flatMap(v -> v.getValue(Integer::parseInt)).orElse(0);
			return new CommonEconomyAdapter(id, decimals);
		});
	}
}
