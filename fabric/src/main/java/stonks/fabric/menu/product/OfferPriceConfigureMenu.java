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
package stonks.fabric.menu.product;

import java.util.ArrayList;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferCustomPriceInput;
import stonks.fabric.translation.Translations;

public class OfferPriceConfigureMenu extends StackedMenu {
	private Product product;
	private OfferType offerType;
	private int amount;
	private ProductMarketOverview overview;

	public OfferPriceConfigureMenu(StackedMenu previous, ServerPlayer player, OfferType offerType, int amount, ProductMarketOverview overview) {
		super(previous, MenuType.GENERIC_9x4, player, false);
		this.offerType = offerType;
		this.amount = amount;
		this.overview = overview;
		product = overview.getProduct();

		setTitle(offerType == OfferType.BUY
			? Translations.Menus.CreateOffer.Title$Buy(product)
			: Translations.Menus.CreateOffer.Title$Sell(product));

		var displayStack = StonksFabric.getPlatform(getPlayer())
			.getStonksAdapter()
			.createDisplayStack(product);
		var builder = displayStack != null
			? GuiElementBuilder.from(displayStack)
			: new GuiElementBuilder(Items.BARRIER);
		setSlot(7, builder
			.setCount(Math.min(Math.max(amount / 64, 1), 64))
			.setName(Component.literal(amount + "x " + product.getProductName()) // TODO
				.withStyle(s -> s.withColor(ChatFormatting.AQUA)))
			.setLore(new ArrayList<>()));

		placeOfferButtons();
	}

	public Product getProduct() { return product; }

	public OfferType getOfferType() { return offerType; }

	public int getAmount() { return amount; }

	public ProductMarketOverview getOverview() { return overview; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	private void placeOfferButtons() {
		var computed = offerType == OfferType.BUY
			? overview.getBuyOffers().compute()
			: overview.getSellOffers().compute();

		var delta = StonksFabric.getPlatform(getPlayer()).getPlatformConfig().topOfferPriceDelta;
		var topOfferPPU = computed.map(v -> switch (offerType) {
		case BUY -> v.max();
		case SELL -> v.min();
		});
		var topOfferDelta = topOfferPPU.map(v -> v + switch (offerType) {
		case BUY -> delta;
		case SELL -> -delta;
		}).map(v -> v > 0d ? v : null);
		var averageOffer = computed.map(v -> v.average());

		var topOfferTextDelta = switch (offerType) {
		case BUY -> Translations.Menus.CreateOffer.TopBuyDelta(delta);
		case SELL -> Translations.Menus.CreateOffer.TopSellDelta(delta);
		};
		var totalTextDelta = switch (offerType) {
		case BUY -> Translations.Menus.CreateOffer.TotalSpending(topOfferDelta, amount);
		case SELL -> Translations.Menus.CreateOffer.TotalEarning(topOfferDelta, amount);
		};

		setSlot(19, new GuiElementBuilder(computed.isPresent() ? Items.GOLD_INGOT : Items.BARRIER)
			.setName(topOfferTextDelta)
			.addLoreLine(Translations.Menus.CreateOffer.TopOfferDelta)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.CreateOffer.TopOfferPrice(topOfferPPU))
			.addLoreLine(Translations.Menus.CreateOffer.YourOfferPrice(topOfferDelta))
			.addLoreLine(totalTextDelta)
			.addLoreLine(Component.empty())
			.addLoreLine(topOfferDelta.isPresent()
				? Translations.Menus.CreateOffer.ClickForConfirmation
				: Translations.Menus.CreateOffer.NoOfferForYou)
			.setCallback((_, _, _, _) -> {
				if (!topOfferDelta.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferDelta.get())
					.open();
			}));

		var totalTextTop = switch (offerType) {
		case BUY -> Translations.Menus.CreateOffer.TotalSpending(topOfferPPU, amount);
		case SELL -> Translations.Menus.CreateOffer.TotalEarning(topOfferPPU, amount);
		};

		setSlot(21, new GuiElementBuilder(topOfferPPU.isPresent() ? Items.GOLD_BLOCK : Items.BARRIER)
			.setName(Translations.Menus.CreateOffer.SameAsTopOffer)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.CreateOffer.TopOfferPrice(topOfferPPU))
			.addLoreLine(totalTextTop)
			.addLoreLine(Component.empty())
			.addLoreLine(topOfferPPU.isPresent()
				? Translations.Menus.CreateOffer.ClickForConfirmation
				: Translations.Menus.CreateOffer.NoOfferForYou)
			.setCallback((_, _, _, _) -> {
				if (!topOfferPPU.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferPPU.get())
					.open();
			}));

		var totalTextAverage = switch (offerType) {
		case BUY -> Translations.Menus.CreateOffer.TotalSpending(averageOffer, amount);
		case SELL -> Translations.Menus.CreateOffer.TotalEarning(averageOffer, amount);
		};

		setSlot(23, new GuiElementBuilder(averageOffer.isPresent() ? Items.CHEST : Items.BARRIER)
			.setName(Translations.Menus.CreateOffer.AverageOfTopOffers)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.CreateOffer.AvgOfferPrice(averageOffer))
			.addLoreLine(totalTextAverage)
			.addLoreLine(Component.empty())
			.addLoreLine(averageOffer.isPresent()
				? Translations.Menus.CreateOffer.ClickForConfirmation
				: Translations.Menus.CreateOffer.NoOfferForYou)
			.setCallback((_, _, _, _) -> {
				if (!averageOffer.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, averageOffer.get())
					.open();
			}));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Translations.Menus.CreateOffer.CustomPrice)
			.addLoreLine(Translations.Menus.CreateOffer.CustomPrice$0)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.CreateOffer.ClickForCustomPrice)
			.setCallback((_, _, _, _) -> new OfferCustomPriceInput(player, this).open()));
	}
}
