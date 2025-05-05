package io.github.nahkd123.stonks.mc.fabric.econ.provider;

import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.common.economy.api.EconomyTransaction;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyException;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.econ.Transaction;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.PatboxCommonEconomyApiServiceProvider.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PatboxCommonEconomyApiServiceProvider implements EconomyServiceProvider<Config> {
	@Override
	public String getProviderName() { return "patbox/common-economy-api"; }

	@Override
	public MapCodec<Config> getConfigCodec() { return Config.CODEC; }

	@Override
	public EconomyService createService(MinecraftServer server, Config config) {
		EconomyCurrency currency = CommonEconomy.getCurrency(server, config.account);
		if (currency == null) throw new IllegalArgumentException("No such account type: %s".formatted(config.account));
		return new ServiceImpl(server, currency);
	}

	record Config(Identifier account) {
		static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Identifier.CODEC.fieldOf("account").forGetter(Config::account))
			.apply(i, Config::new));
	}

	private class ServiceImpl implements EconomyService {
		private MinecraftServer server;
		private EconomyCurrency currency;

		public ServiceImpl(MinecraftServer server, EconomyCurrency currency) {
			this.server = server;
			this.currency = currency;
		}

		@Override
		public String formatCurrency(long amount) {
			return currency.formatValue(amount, true);
		}

		@Override
		public Text formatDisplayCurrency(long amount) {
			return currency.formatValueText(amount, false);
		}

		@Override
		public long parseCurrency(String formatted) {
			return currency.parseValue(formatted);
		}

		private EconomyAccount accountOf(GameProfile player) {
			return currency.provider().getDefaultAccount(server, player, currency);
		}

		@Override
		public CompletableFuture<Long> queryBalance(GameProfile player) {
			return CompletableFuture.completedFuture(accountOf(player).balance());
		}

		@Override
		public CompletableFuture<Transaction> withdrawFrom(GameProfile player, long amount) {
			EconomyAccount account = accountOf(player);
			EconomyTransaction txn = account.decreaseBalance(amount);
			if (txn.isFailure()) return CompletableFuture.failedFuture(new EconomyException(txn.message()));
			Transaction ctxn = new Transaction(player, Transaction.TxType.WITHDRAW, amount);
			return CompletableFuture.completedFuture(ctxn);
		}

		@Override
		public CompletableFuture<Transaction> depositTo(GameProfile player, long amount) {
			EconomyAccount account = accountOf(player);
			EconomyTransaction txn = account.increaseBalance(amount);
			if (txn.isFailure()) return CompletableFuture.failedFuture(new EconomyException(txn.message()));
			Transaction ctxn = new Transaction(player, Transaction.TxType.DEPOSIT, amount);
			return CompletableFuture.completedFuture(ctxn);
		}

		@Override
		public CompletableFuture<Void> rollback(Transaction txn) {
			EconomyAccount account = accountOf(txn.player());

			switch (txn.type()) {
			case WITHDRAW:
				account.increaseBalance(txn.amount());
				break;
			case DEPOSIT:
				account.decreaseBalance(txn.amount());
				break;
			}

			return CompletableFuture.completedFuture(null);
		}
	}
}
