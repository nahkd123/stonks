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
package stonks.fabric.menu.product.input;

import java.util.Optional;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.product.OfferConfirmMenu;
import stonks.fabric.menu.product.OfferPriceConfigureMenu;

public class OfferCustomPriceInput extends SignGui {
	private OfferPriceConfigureMenu menu;

	public OfferCustomPriceInput(ServerPlayerEntity player, OfferPriceConfigureMenu menu) {
		super(player);
		this.menu = menu;

		setSignType(Blocks.DARK_OAK_SIGN);
		setColor(DyeColor.WHITE);
		setLine(0, Text.empty());
		setLine(1, Text.literal("--------"));
		setLine(2, Text.literal("Specify your price per unit"));
		setLine(3, Text.literal("You're " + switch (menu.getOfferType()) {
		case BUY -> "buying";
		case SELL -> "selling";
		} + " " + menu.getAmount() + "x " + menu.getProduct().getProductName()));
	}

	public OfferPriceConfigureMenu getMenu() { return menu; }

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
		} else if (input.endsWith("m")) { // Only price can reach up to millions
			mul = 1_000_000;
			input = input.substring(0, input.length() - 1);
		}

		try {
			var base = Double.parseDouble(input);
			var price = base * mul;

			if (price <= 0) {
				getPlayer().sendMessage(Text.literal("You must specify price more than $0")
					.styled(s -> s.withColor(Formatting.RED)), true);
				return;
			}

			var balance = StonksFabric.getServiceProvider(getPlayer())
				.getStonksAdapter()
				.accountBalance(player);
			var totalPrice = price * menu.getAmount();

			if (menu.getOfferType() == OfferType.BUY && totalPrice > balance) {
				getPlayer().sendMessage(Text.literal("Not enough money! (")
					.styled(s -> s.withColor(Formatting.RED))
					.append(StonksFabricUtils.currencyText(Optional.of(totalPrice), true))
					.append("/")
					.append(StonksFabricUtils.currencyText(Optional.of(balance), true))
					.append(")"), true);
				return;
			}

			new OfferConfirmMenu(menu, player, menu.getProduct(), menu.getOfferType(), menu.getAmount(), price)
				.open();
		} catch (NumberFormatException e) {
			getPlayer().sendMessage(
				Text.literal("Invaild input: " + input).styled(s -> s.withColor(Formatting.RED)),
				true);
		}
	}
}