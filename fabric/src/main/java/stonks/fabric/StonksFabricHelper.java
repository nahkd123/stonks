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

import java.util.Optional;

import nahara.common.tasks.Task;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.product.Product;

public class StonksFabricHelper {
	public static Task<Void> instantOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double balance) {
		var provider = StonksFabric.getServiceProvider(player);

		// Take out stuffs first
		if (type == OfferType.BUY) {
			provider.getStonksAdapter().accountWithdraw(player, balance);
		} else {
			provider.getStonksAdapter().removeUnitsFrom(player, product, units);
		}

		player.sendMessage(Translations.message$pleaseWait, true);
		var task = provider.getStonksService().instantOffer(product, type, units, balance);

		provider.getTasksHandler()
			.handle(task, (result, error) -> {
				if (error != null) {
					player.sendMessage(Translations.message$errorRefunding, true);
					if (type == OfferType.BUY) provider.getStonksAdapter().accountDeposit(player, balance);
					else provider.getStonksAdapter().addUnitsTo(player, product, units);
					error.printStackTrace();
					return;
				}

				var productNameText = Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.AQUA));

				if (type == OfferType.BUY) {
					var unitsLeft = result.units();
					var unitsBought = units - unitsLeft;
					var moneyLeft = result.balance();
					var moneySpent = balance - moneyLeft;

					provider.getStonksAdapter().accountDeposit(player, moneyLeft);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsBought);

					var amountText = Text.literal(Integer.toString(unitsBought))
						.styled(s -> s.withColor(Formatting.AQUA));
					var moneySpentText = StonksFabricUtils.currencyText(Optional.of(moneySpent), true);
					var unitsLeftText = Text.literal(Integer.toString(unitsLeft))
						.styled(s -> s.withColor(Formatting.AQUA));
					var text = unitsLeft == 0
						? Translations.messages$bought(amountText, productNameText, moneySpentText)
						: Translations.messages$boughtWithExtras(amountText, productNameText, moneySpentText,
							unitsLeftText);
					player.sendMessage(text, true);
				} else {
					var config = StonksFabric.getServiceProvider(player).getPlatformConfig();
					var unitsLeft = result.units();
					var unitsSold = units - unitsLeft;
					var earnings = config.applyTax(result.balance());

					provider.getStonksAdapter().accountDeposit(player, earnings);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsLeft);

					var amountText = Text.literal(Integer.toString(unitsSold))
						.styled(s -> s.withColor(Formatting.AQUA));
					var moneyReceivedText = StonksFabricUtils.currencyText(Optional.of(earnings), true);
					var unitsLeftText = Text.literal(Integer.toString(unitsLeft))
						.styled(s -> s.withColor(Formatting.AQUA));
					var text = unitsLeft == 0
						? Translations.messages$sold(amountText, productNameText, moneyReceivedText)
						: Translations.messages$soldWithExtras(amountText, productNameText, moneyReceivedText,
							unitsLeftText);
					player.sendMessage(text, true);
				}
			});

		return task.afterThatDo($ -> null);
	}

	public static void placeOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double pricePerUnit) {
		var provider = StonksFabric.getServiceProvider(player);
		var adapter = provider.getStonksAdapter();
		var totalPrice = units * pricePerUnit;

		if (type == OfferType.BUY) {
			var balance = adapter.accountBalance(player);

			if (balance < totalPrice) {
				player.sendMessage(Translations.messages$notEnoughMoney(balance, totalPrice), true);
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
				player.sendMessage(Translations.messages$notEnoughItems(currentUnits, units));
				return;
			}

			adapter.removeUnitsFrom(player, product, units);
		}

		player.sendMessage(Translations.message$pleaseWait, true);
		provider.getTasksHandler()
			.handle(provider.getStonksService().listOffer(player.getUuid(), product, type, units, pricePerUnit),
				(offer, error) -> {
					if (error != null) {
						player.sendMessage(Translations.message$errorRefunding, true);

						if (type == OfferType.BUY) {
							adapter.accountDeposit(player, totalPrice);
						} else {
							adapter.addUnitsTo(player, product, units);
						}

						error.printStackTrace();
						return;
					}

					var unitsText = Text.literal(Integer.toString(units)).styled(s -> s.withColor(Formatting.AQUA));
					var productNameText = Text.literal(product.getProductName())
						.styled(s -> s.withColor(Formatting.AQUA));
					var totalPriceText = StonksFabricUtils.currencyText(Optional.of(totalPrice), true);
					var pricePerUnitText = StonksFabricUtils.currencyText(Optional.of(pricePerUnit), true);
					player.sendMessage(offer.getType() == OfferType.BUY
						? Translations.messages$placedBuyOffer(unitsText, productNameText, totalPriceText, pricePerUnitText)
						: Translations.messages$placedSellOffer(unitsText, productNameText, totalPriceText,
							pricePerUnitText),
						true);
				});
	}

	public static void sendOfferFilledMessage(MinecraftServer server, Offer filledOffer) {
		var player = server.getPlayerManager().getPlayer(filledOffer.getOffererId());
		if (player == null) return;
		player.sendMessage(filledOffer.getType() == OfferType.BUY
			? Translations.messages$buyOfferFilled(filledOffer)
			: Translations.messages$sellOfferFilled(filledOffer));
	}
}
