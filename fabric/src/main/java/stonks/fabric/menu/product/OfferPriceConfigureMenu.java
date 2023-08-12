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
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.Translations;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferCustomPriceInput;

public class OfferPriceConfigureMenu extends StackedMenu {
	private Product product;
	private OfferType offerType;
	private int amount;
	private ProductMarketOverview overview;

	public OfferPriceConfigureMenu(StackedMenu previous, ServerPlayerEntity player, OfferType offerType, int amount, ProductMarketOverview overview) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		this.offerType = offerType;
		this.amount = amount;
		this.overview = overview;
		product = overview.getProduct();

		setTitle(offerType == OfferType.BUY
			? Translations.menus$createOffer$buy(product)
			: Translations.menus$createOffer$sell(product));

		setSlot(7, GuiElementBuilder.from(StonksFabric.getServiceProvider(getPlayer())
			.getStonksAdapter()
			.createDisplayStack(product))
			.setCount(Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal(amount + "x " + product.getProductName()) // TODO
				.styled(s -> s.withColor(Formatting.AQUA)))
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

		var delta = StonksFabric.getServiceProvider(getPlayer()).getPlatformConfig().topOfferPriceDelta;
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
		case BUY -> Translations.menus$createOffer$topBuyDelta(delta);
		case SELL -> Translations.menus$createOffer$topSellDelta(delta);
		};
		var totalTextDelta = switch (offerType) {
		case BUY -> Translations.menus$createOffer$totalSpending(topOfferDelta, amount);
		case SELL -> Translations.menus$createOffer$totalEarning(topOfferDelta, amount);
		};

		setSlot(19, new GuiElementBuilder(computed.isPresent() ? Items.GOLD_INGOT : Items.BARRIER)
			.setName(topOfferTextDelta)
			.addLoreLine(Translations.menus$createOffer$topOfferDelta)
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.menus$createOffer$topOfferPrice(topOfferPPU))
			.addLoreLine(Translations.menus$createOffer$yourOfferPrice(topOfferDelta))
			.addLoreLine(totalTextDelta)
			.addLoreLine(Text.empty())
			.addLoreLine(topOfferDelta.isPresent()
				? Translations.menus$createOffer$clickForConfirmation
				: Translations.menus$createOffer$noOfferForYou)
			.setCallback((index, type, action, gui) -> {
				if (!topOfferDelta.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferDelta.get())
					.open();
			}));

		var totalTextTop = switch (offerType) {
		case BUY -> Translations.menus$createOffer$totalSpending(topOfferPPU, amount);
		case SELL -> Translations.menus$createOffer$totalEarning(topOfferPPU, amount);
		};

		setSlot(21, new GuiElementBuilder(topOfferPPU.isPresent() ? Items.GOLD_BLOCK : Items.BARRIER)
			.setName(Translations.menus$createOffer$sameAsTopOffer)
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.menus$createOffer$topOfferPrice(topOfferPPU))
			.addLoreLine(totalTextTop)
			.addLoreLine(Text.empty())
			.addLoreLine(topOfferPPU.isPresent()
				? Translations.menus$createOffer$clickForConfirmation
				: Translations.menus$createOffer$noOfferForYou)
			.setCallback((index, type, action, gui) -> {
				if (!topOfferPPU.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferPPU.get())
					.open();
			}));

		var totalTextAverage = switch (offerType) {
		case BUY -> Translations.menus$createOffer$totalSpending(averageOffer, amount);
		case SELL -> Translations.menus$createOffer$totalEarning(averageOffer, amount);
		};

		setSlot(23, new GuiElementBuilder(averageOffer.isPresent() ? Items.CHEST : Items.BARRIER)
			.setName(Translations.menus$createOffer$averageOfTopOffers)
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.menus$createOffer$avgOfferPrice(averageOffer))
			.addLoreLine(totalTextAverage)
			.addLoreLine(Text.empty())
			.addLoreLine(averageOffer.isPresent()
				? Translations.menus$createOffer$clickForConfirmation
				: Translations.menus$createOffer$noOfferForYou)
			.setCallback((index, type, action, gui) -> {
				if (!averageOffer.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, averageOffer.get())
					.open();
			}));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Translations.menus$createOffer$customPrice)
			.addLoreLine(Translations.menus$createOffer$customPrice$0)
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.menus$createOffer$clickForCustomPrice)
			.setCallback((index, type, action, gui) -> new OfferCustomPriceInput(player, this).open()));
	}
}
