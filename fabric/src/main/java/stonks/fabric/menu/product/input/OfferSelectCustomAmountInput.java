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
package stonks.fabric.menu.product.input;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.product.OfferAmountConfigureMenu;
import stonks.fabric.menu.product.OfferPriceConfigureMenu;
import stonks.fabric.translation.Translations;

public class OfferSelectCustomAmountInput extends SignGui {
	private OfferAmountConfigureMenu menu;

	public OfferSelectCustomAmountInput(ServerPlayerEntity player, OfferAmountConfigureMenu menu) {
		super(player);
		this.menu = menu;

		setSignType(Blocks.ACACIA_WALL_SIGN);
		setColor(DyeColor.BLACK);
		setLine(0, Text.empty());
		setLine(1, Translations.SignInputs.Separator);
		setLine(2, Translations.SignInputs.AmountInput);
		setLine(3, menu.getOfferType() == OfferType.BUY
			? Translations.SignInputs.CurrentBuyTarget(menu.getProduct())
			: Translations.SignInputs.CurrentSellTarget(menu.getProduct()));
	}

	public OfferAmountConfigureMenu getMenu() { return menu; }

	@Override
	public void onClose() {
		var input = getLine(0).getString().trim().toLowerCase();
		if (input.isEmpty()) {
			getMenu().open();
			return;
		}

		var mul = 1;
		if (input.endsWith("k")) {
			mul = 1000;
			input = input.substring(0, input.length() - 1);
		}

		try {
			var base = Integer.parseInt(input);
			var amount = base * mul;

			if (amount <= 0) {
				getPlayer().sendMessage(Translations.Messages.AmountAtLeastOne, true);
				StonksFabric.getPlatform(getPlayer()).getSounds().playFailedSound(getPlayer());
				return;
			}

			var currentAmount = StonksFabric.getPlatform(getPlayer())
				.getStonksAdapter()
				.getUnits(getPlayer(), getMenu().getProduct());
			if (menu.getOfferType() == OfferType.SELL && amount > currentAmount) {
				getPlayer().sendMessage(Translations.Messages.NotEnoughItems(currentAmount, amount), true);
				return;
			}

			var type = getMenu().getOfferType();
			new OfferPriceConfigureMenu(getMenu(), getPlayer(), type, amount, getMenu().getOverview()).open();
		} catch (NumberFormatException e) {
			getPlayer().sendMessage(Translations.Messages.InvaildInput(input), true);
			StonksFabric.getPlatform(getPlayer()).getSounds().playFailedSound(getPlayer());
		}
	}
}
