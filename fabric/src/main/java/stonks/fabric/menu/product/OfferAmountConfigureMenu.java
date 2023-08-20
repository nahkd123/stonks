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

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferSelectCustomAmountInput;
import stonks.fabric.translation.Translations;

public class OfferAmountConfigureMenu extends StackedMenu {
	private Product product;
	private OfferType offerType;
	private ProductMarketOverview overview;

	public OfferAmountConfigureMenu(StackedMenu previous, ServerPlayerEntity player, Product product, OfferType offerType, ProductMarketOverview overview) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		this.product = product;
		this.offerType = offerType;
		this.overview = overview;

		setTitle(offerType == OfferType.BUY
			? Translations.Menus.CreateOffer.Title$Buy(product)
			: Translations.Menus.CreateOffer.Title$Sell(product));

		var icons = switch (offerType) {
		case BUY -> new Item[] { Items.IRON_INGOT, Items.DIAMOND, Items.DIAMOND_BLOCK };
		case SELL -> new Item[] { Items.GOLD_NUGGET, Items.GOLD_INGOT, Items.GOLD_BLOCK };
		};

		var displayStack = StonksFabric.getPlatform(getPlayer()).getStonksAdapter().createDisplayStack(product);
		if (displayStack != null) setSlot(7, displayStack);

		setSlot(19, createOfferSelectButton(64, icons[0]));
		setSlot(20, createOfferSelectButton(256, icons[1]));
		setSlot(21, createOfferSelectButton(512, icons[1]));
		setSlot(22, createOfferSelectButton(1024, icons[1]));
		setSlot(23, createOfferSelectButton(offerType == OfferType.BUY ? 4096 : -1, icons[2]));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Translations.Menus.CreateOffer.CustomAmount)
			.addLoreLine(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.CreateOffer.ClickForAmount)
			.setCallback((index, type, action, gui) -> new OfferSelectCustomAmountInput(player, this).open()));
	}

	public Product getProduct() { return product; }

	public OfferType getOfferType() { return offerType; }

	public ProductMarketOverview getOverview() { return overview; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	private GuiElementBuilder createOfferSelectButton(int amount, Item icon) {
		// Only for selling
		var fillAll = amount == -1;
		var currentUnits = StonksFabric.getPlatform(getPlayer()).getStonksAdapter().getUnits(player, product);
		if (fillAll) amount = currentUnits;
		var disabled = offerType == OfferType.SELL && (amount == 0 || currentUnits < amount);
		var amount2 = amount;

		var buttonName = offerType == OfferType.BUY
			? Translations.Menus.CreateOffer.BuyFixed(amount)
			: fillAll ? Translations.Menus.CreateOffer.SellAll
			: Translations.Menus.CreateOffer.SellFixed(amount);

		return new GuiElementBuilder(disabled ? Items.BARRIER : icon, Math.min(Math.max(amount / 64, 1), 64))
			.setName(buttonName)
			.addLoreLine(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(disabled
				? Translations.Menus.CreateOffer.NoOfferForYou
				: Translations.Menus.CreateOffer.ClickForPrice)
			.setCallback((index, type, action, gui) -> {
				if (disabled) return;
				new OfferPriceConfigureMenu(this, getPlayer(), getOfferType(), amount2, getOverview()).open();
			});
	}
}
