package stonks.fabric.adapter.provided;

import eu.pb4.common.economy.api.CommonEconomy;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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

	public double toDouble(long balance) {
		return balance / Math.pow(10, decimals);
	}

	public long fromDouble(double money) {
		return (long) Math.round(money * Math.pow(10, decimals));
	}

	@Override
	public double accountBalance(ServerPlayerEntity player) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountBalance(player);
		return toDouble(playerAccount.balance());
	}

	@Override
	public boolean accountDeposit(ServerPlayerEntity player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountDeposit(player, money);
		var txn = playerAccount.increaseBalance(fromDouble(money));
		if (txn.isSuccessful()) return true;
		return StonksFabricAdapter.super.accountDeposit(player, money);
	}

	@Override
	public boolean accountWithdraw(ServerPlayerEntity player, double money) {
		var playerAccount = CommonEconomy.getAccount(player, account);
		if (playerAccount == null) return StonksFabricAdapter.super.accountWithdraw(player, money);
		var txn = playerAccount.decreaseBalance(fromDouble(money));
		if (txn.isSuccessful()) return true;
		return StonksFabricAdapter.super.accountWithdraw(player, money);
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(CommonEconomyAdapter.class, (server, config) -> {
			var id = config.firstChild("id")
				.map(v -> new Identifier(v.getValue()))
				.orElse(new Identifier(StonksFabric.MODID, "default_account"));
			var decimals = config.firstChild("decimals").map(v -> Integer.parseInt(v.getValue())).orElse(0);

			return new CommonEconomyAdapter(id, decimals);
		});
	}
}
