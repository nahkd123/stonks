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

import nahara.common.tasks.Task;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.product.Product;
import stonks.fabric.translation.Translations;

public class StonksFabricHelper {
	public static Task<Void> instantOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double balance) {
		var provider = StonksFabric.getPlatform(player);

		// Take out stuffs first
		if (type == OfferType.BUY) {
			provider.getStonksAdapter().accountWithdraw(player, balance);
		} else {
			provider.getStonksAdapter().removeUnitsFrom(player, product, units);
		}

		player.sendMessage(Translations.Messages.PleaseWait, true);
		var task = provider.getStonksService().instantOffer(product, type, units, balance);

		provider.getTasksHandler()
			.handle(task, (result, error) -> {
				if (error != null) {
					player.sendMessage(Translations.Messages.ErrorRefunding, true);
					if (type == OfferType.BUY) provider.getStonksAdapter().accountDeposit(player, balance);
					else provider.getStonksAdapter().addUnitsTo(player, product, units);
					error.printStackTrace();
					return;
				}

				if (type == OfferType.BUY) {
					var unitsLeft = result.units();
					var unitsBought = units - unitsLeft;
					var moneyLeft = result.balance();
					var moneySpent = balance - moneyLeft;

					provider.getStonksAdapter().accountDeposit(player, moneyLeft);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsBought);

					player.sendMessage(unitsLeft == 0
						? Translations.Messages.Bought(unitsBought, product, moneySpent)
						: Translations.Messages.BoughtWithExtras(unitsBought, product, moneySpent, unitsLeft),
						true);
				} else {
					var config = StonksFabric.getPlatform(player).getPlatformConfig();
					var unitsLeft = result.units();
					var unitsSold = units - unitsLeft;
					var earnings = config.applyTax(result.balance());

					provider.getStonksAdapter().accountDeposit(player, earnings);
					provider.getStonksAdapter().addUnitsTo(player, product, unitsLeft);

					player.sendMessage(unitsLeft == 0
						? Translations.Messages.Sold(unitsSold, product, earnings)
						: Translations.Messages.SoldWithExtras(unitsSold, product, earnings, unitsLeft),
						true);
				}

				StonksFabric.getPlatform(player).getSounds().playInstantOfferSound(player);
			});

		return task.afterThatDo($ -> null);
	}

	public static void placeOffer(ServerPlayerEntity player, Product product, OfferType type, int units, double pricePerUnit) {
		var provider = StonksFabric.getPlatform(player);
		var adapter = provider.getStonksAdapter();
		var totalPrice = units * pricePerUnit;

		if (type == OfferType.BUY) {
			var balance = adapter.accountBalance(player);

			if (balance < totalPrice) {
				player.sendMessage(Translations.Messages.NotEnoughMoney(balance, totalPrice), true);
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
				player.sendMessage(Translations.Messages.NotEnoughItems(currentUnits, units), true);
				return;
			}

			adapter.removeUnitsFrom(player, product, units);
		}

		player.sendMessage(Translations.Messages.PleaseWait, true);
		provider.getTasksHandler()
			.handle(provider.getStonksService().listOffer(player.getUuid(), product, type, units, pricePerUnit),
				(offer, error) -> {
					if (error != null) {
						player.sendMessage(Translations.Messages.ErrorRefunding, true);

						if (type == OfferType.BUY) {
							adapter.accountDeposit(player, totalPrice);
						} else {
							adapter.addUnitsTo(player, product, units);
						}

						error.printStackTrace();
						return;
					}

					player.sendMessage(offer.getType() == OfferType.BUY
						? Translations.Messages.PlacedBuyOffer(units, product, totalPrice, pricePerUnit)
						: Translations.Messages.PlacedSellOffer(units, product, totalPrice, pricePerUnit),
						true);

					StonksFabric.getPlatform(player).getSounds().playOfferPlacedSound(player);
				});
	}

	public static void sendOfferFilledMessage(MinecraftServer server, Offer filledOffer) {
		var player = server.getPlayerManager().getPlayer(filledOffer.getOffererId());
		if (player == null) return;
		player.sendMessage(filledOffer.getType() == OfferType.BUY
			? Translations.Messages.BuyOfferFilled(filledOffer)
			: Translations.Messages.SellOfferFilled(filledOffer));
		StonksFabric.getPlatform(server).getSounds().playOfferFilledSound(player);
	}
}
