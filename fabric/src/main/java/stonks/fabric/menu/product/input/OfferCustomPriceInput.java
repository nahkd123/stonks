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
package stonks.fabric.menu.product.input;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.product.OfferConfirmMenu;
import stonks.fabric.menu.product.OfferPriceConfigureMenu;
import stonks.fabric.translation.Translations;

public class OfferCustomPriceInput extends SignGui {
	private OfferPriceConfigureMenu menu;

	public OfferCustomPriceInput(ServerPlayer player, OfferPriceConfigureMenu menu) {
		super(player);
		this.menu = menu;

		setSignType(Blocks.ACACIA_WALL_SIGN);
		setColor(DyeColor.BLACK);
		setLine(0, Component.empty());
		setLine(1, Translations.SignInputs.Separator);
		setLine(2, Translations.SignInputs.PriceInput);
		setLine(3, menu.getOfferType() == OfferType.BUY
			? Translations.SignInputs.CurrentBuyTarget(menu.getAmount(), menu.getProduct())
			: Translations.SignInputs.CurrentSellTarget(menu.getAmount(), menu.getProduct()));
	}

	public OfferPriceConfigureMenu getMenu() { return menu; }

	@Override
	public void onManualClose() {
		onClose();
	}

	@Override
	public void onPlayerClose(boolean success) {
		onClose();
	}

	private void onClose() {
		var input = getLine(0).getString().trim().toLowerCase();
		if (input.isEmpty()) {
			getMenu().open();
			return;
		}

		var mul = 1;
		if (input.endsWith("k")) {
			mul = 1000;
			input = input.substring(0, input.length() - 1);
		} else if (input.endsWith("m")) { // Only price can reach up to millions
			mul = 1_000_000;
			input = input.substring(0, input.length() - 1);
		}

		try {
			var base = StonksFabric.getPlatform(getPlayer()).getPlatformConfig()
				.processCurrency(Double.parseDouble(input));
			var price = base * mul;

			if (price <= 0) {
				getPlayer().sendSystemMessage(Translations.Messages.PriceMoreThanZero, true);
				StonksFabric.getPlatform(getPlayer()).getSounds().playFailedSound(getPlayer());
				return;
			}

			var balance = StonksFabric.getPlatform(getPlayer())
				.getStonksAdapter()
				.accountBalance(player);
			var totalPrice = price * menu.getAmount();

			if (menu.getOfferType() == OfferType.BUY && totalPrice > balance) {
				getPlayer().sendSystemMessage(Translations.Messages.NotEnoughMoney(balance, totalPrice), true);
				StonksFabric.getPlatform(getPlayer()).getSounds().playFailedSound(getPlayer());
				return;
			}

			new OfferConfirmMenu(menu, player, menu.getProduct(), menu.getOfferType(), menu.getAmount(), price)
				.open();
		} catch (NumberFormatException e) {
			getPlayer().sendSystemMessage(Translations.Messages.InvaildInput(input), true);
			StonksFabric.getPlatform(getPlayer()).getSounds().playFailedSound(getPlayer());
		}
	}
}
