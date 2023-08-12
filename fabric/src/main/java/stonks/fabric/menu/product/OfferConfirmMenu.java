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
package stonks.fabric.menu.product;

import java.util.ArrayList;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricHelper;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.translation.Translations;

public class OfferConfirmMenu extends StackedMenu {
	public OfferConfirmMenu(StackedMenu previous, ServerPlayerEntity player, Product product, OfferType offerType, int amount, double pricePerUnit) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Translations.Menus.ConfirmOffer.ConfirmOffer);

		setSlot(7, GuiElementBuilder.from(StonksFabric.getPlatform(getPlayer())
			.getStonksAdapter()
			.createDisplayStack(product))
			.setCount(Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal(amount + "x " + product.getProductName())
				.styled(s -> s.withColor(Formatting.AQUA)))
			.setLore(new ArrayList<>()));

		setSlot(22, new GuiElementBuilder(Items.GREEN_TERRACOTTA)
			.setName(offerType == OfferType.BUY
				? Translations.Menus.ConfirmOffer.Buy
				: Translations.Menus.ConfirmOffer.Sell)
			.addLoreLine(Text.empty())
			.addLoreLine(offerType == OfferType.BUY
				? Translations.Menus.ConfirmOffer.Buying(product, amount)
				: Translations.Menus.ConfirmOffer.Selling(product, amount))
			.addLoreLine(Translations.Menus.ConfirmOffer.PricePerUnit(pricePerUnit))
			.addLoreLine(Translations.Menus.ConfirmOffer.TotalPrice(amount, pricePerUnit))
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.ConfirmOffer.ClickToConfirm)
			.setCallback((index, type, action, gui) -> {
				close();
				StonksFabricHelper.placeOffer(player, product, offerType, amount, pricePerUnit);
			}));
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}
}
