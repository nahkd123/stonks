package stonks.fabric;

import java.util.Optional;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.product.Product;

public class StonksFabricHelper {
	public static void instantOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double balance) {
		var provider = StonksFabric.getServiceProvider(player);

		// Take out stuffs first
		if (type == OfferType.BUY) {
			provider.getStonksAdapter().accountWithdraw(player, balance);
		} else {
			provider.getStonksAdapter().removeUnitsFrom(player, product, units);
		}

		provider.getStonksService()
			.instantOffer(product, type, units, balance)
			.afterThatDo(result -> {
				if (type == OfferType.BUY) {
					var unitsLeft = result.units();
					var unitsBought = units - unitsLeft;
					var moneyLeft = result.balance();
					var moneySpent = balance - moneyLeft;

					provider.getStonksAdapter().accountDeposit(player, moneyLeft);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsBought);

					var text = Text.literal("Bought ")
						.append(Text.literal(Integer.toString(unitsBought)).styled(s -> s.withColor(Formatting.AQUA)))
						.append("x ")
						.append(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.AQUA)))
						.append(" for ").append(StonksFabricUtils.currencyText(Optional.of(moneySpent), true));

					if (unitsLeft > 0) text = text
						.append(" with ")
						.append(Text.literal(Integer.toString(unitsLeft)).styled(s -> s.withColor(Formatting.AQUA)))
						.append(" units can't be bought");

					player.sendMessage(text, true);
				} else {
					var unitsLeft = result.units();
					var unitsSold = units - unitsLeft;
					var earnings = result.balance();

					provider.getStonksAdapter().accountDeposit(player, earnings);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsLeft);

					var text = Text.literal("Sold ")
						.append(Text.literal(Integer.toString(unitsSold)).styled(s -> s.withColor(Formatting.AQUA)))
						.append("x ")
						.append(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.AQUA)))
						.append(" for ").append(StonksFabricUtils.currencyText(Optional.of(earnings), true));

					if (unitsLeft > 0) text = text
						.append(" with ")
						.append(Text.literal(Integer.toString(unitsLeft)).styled(s -> s.withColor(Formatting.AQUA)))
						.append(" units can't be sold");

					player.sendMessage(text, true);
				}
			});
	}

	public static void placeOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double pricePerUnit) {
		var adapter = StonksFabric.getServiceProvider(player).getStonksAdapter();
		var totalPrice = units * pricePerUnit;

		if (type == OfferType.BUY) {
			var balance = adapter.accountBalance(player);

			if (balance < totalPrice) {
				player.sendMessage(Text.literal("Not enough money!").styled(s -> s.withColor(Formatting.RED)), true);
				return;
			}

			adapter.accountWithdraw(player, totalPrice);
		} else {
			var currentUnits = adapter.getUnits(player, product);
			if (currentUnits == -1) {
				StonksFabric.LOGGER.error("Unable to process {} product. Have you added adapter for this yet?",
					product.getProductId());
				return;
			}

			if (currentUnits < units) {
				player.sendMessage(Text.literal("Not enough units!").styled(s -> s.withColor(Formatting.RED)));
				return;
			}

			adapter.removeUnitsFrom(player, product, units);
		}

		StonksFabric.getServiceProvider(player)
			.getStonksService()
			.listOffer(player.getUuid(), product, type, units, pricePerUnit)
			.afterThatDo(offer -> {
				// Placed sell offer: 100x Exam Paper for $100 @ $1/ea
				player.sendMessage(Text.literal("Placed " + type.toString().toLowerCase() + " offer: ")
					.append(Text.literal(Integer.toString(units)).styled(s -> s.withColor(Formatting.AQUA)))
					.append("x ")
					.append(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.AQUA)))
					.append(" for ")
					.append(StonksFabricUtils.currencyText(Optional.of(totalPrice), true))
					.append(" @ ")
					.append(StonksFabricUtils.currencyText(Optional.of(pricePerUnit), true))
					.append("/ea"), true);
			});
	}
}
