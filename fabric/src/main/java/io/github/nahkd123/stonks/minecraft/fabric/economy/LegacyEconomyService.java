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
package io.github.nahkd123.stonks.minecraft.fabric.economy;

import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.economy.Currency;
import io.github.nahkd123.stonks.minecraft.economy.DoubleBackedCurrency;
import io.github.nahkd123.stonks.minecraft.economy.EconomyService;
import io.github.nahkd123.stonks.minecraft.economy.Transaction;
import io.github.nahkd123.stonks.minecraft.economy.TransactionType;
import io.github.nahkd123.stonks.minecraft.fabric.PlayerWrapper;
import io.github.nahkd123.stonks.minecraft.fabric.text.TextProxy;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import net.minecraft.text.Text;
import stonks.fabric.adapter.AdapterResponse;
import stonks.fabric.adapter.StonksFabricAdapter;

public class LegacyEconomyService implements EconomyService {
	private DoubleBackedCurrency currency;
	private StonksFabricAdapter adapter;

	public LegacyEconomyService(TextFactory textFactory, int decimals, StonksFabricAdapter adapter) {
		this.adapter = adapter;
		this.currency = new DoubleBackedCurrency(textFactory, decimals);
	}

	@Override
	public TextComponent getDisplayName() { return new TextProxy(Text.literal("Legacy Economy Service")); }

	@Override
	public Currency getCurrency() { return currency; }

	public long getBalanceNow(Player player) {
		if (!(player instanceof PlayerWrapper wrapper)) throw new IllegalArgumentException("Not a PlayerWrapper");
		AdapterResponse<Double> res = adapter.accountBalance(wrapper.getEntity());
		return currency.fromDouble(res.or(0d));
	}

	@Override
	public CompletableFuture<Long> getBalance(Player player) {
		return CompletableFuture.completedFuture(getBalanceNow(player));
	}

	@Override
	public CompletableFuture<Transaction> depositTo(Player player, long amount) {
		if (!(player instanceof PlayerWrapper wrapper)) throw new IllegalArgumentException("Not a PlayerWrapper");
		AdapterResponse<Void> res = adapter.accountDeposit(
			wrapper.getEntity(),
			currency.toDouble(amount));
		return CompletableFuture.completedFuture(new AdapterResponseToTransaction<>(player
			.getUuid(), res, TransactionType.DEPOSIT, amount, getBalanceNow(player)));
	}

	@Override
	public CompletableFuture<Transaction> withdrawFrom(Player player, long amount) {
		if (!(player instanceof PlayerWrapper wrapper)) throw new IllegalArgumentException("Not a PlayerWrapper");
		AdapterResponse<Void> res = adapter.accountWithdraw(
			wrapper.getEntity(),
			currency.toDouble(amount));
		return CompletableFuture.completedFuture(new AdapterResponseToTransaction<>(player
			.getUuid(), res, TransactionType.WITHDRAW, amount, getBalanceNow(player)));
	}
}
