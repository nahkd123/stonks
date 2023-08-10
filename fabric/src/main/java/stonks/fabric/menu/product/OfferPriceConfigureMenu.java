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
import stonks.fabric.StonksFabricUtils;
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

		setTitle(Text.literal("Market > " + product.getProductName() + " > " + switch (offerType) {
		case BUY -> "Buy";
		case SELL -> "Sell";
		} + " offer"));

		setSlot(7, GuiElementBuilder.from(StonksFabric.getServiceProvider(getPlayer())
			.getStonksAdapter()
			.createDisplayStack(product))
			.setCount(Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal(amount + "x " + product.getProductName())
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

		var topOfferPPU = computed.map(v -> switch (offerType) {
		case BUY -> v.max();
		case SELL -> v.min();
		});
		var topOfferDelta = topOfferPPU.map(v -> v + switch (offerType) {
		case BUY -> 0.1;
		case SELL -> -0.1;
		}).map(v -> v > 0d ? v : null);
		var averageOffer = computed.map(v -> v.average());

		var totalString = switch (offerType) {
		case BUY -> "spending";
		case SELL -> "earning";
		};

		setSlot(19, new GuiElementBuilder(computed.isPresent() ? Items.GOLD_INGOT : Items.BARRIER)
			.setName(Text.literal("Top offer " + switch (offerType) {
			case BUY -> "+";
			case SELL -> "-";
			} + " 0.1").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.literal("Get your offer filled first").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Top offer: ").styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(topOfferPPU, true)))
			.addLoreLine(Text.literal("Your offer: ").styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(topOfferDelta, true)))
			.addLoreLine(Text.literal("Total " + totalString + ": ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(topOfferDelta.map(v -> v * amount), true)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(topOfferDelta.isPresent()
				? "Click to place offer"
				: "Can't make this offer")
				.styled(s -> s.withColor(topOfferDelta.isPresent() ? Formatting.GRAY : Formatting.RED)))
			.setCallback((index, type, action, gui) -> {
				if (!topOfferDelta.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferDelta.get())
					.open();
			}));

		setSlot(21, new GuiElementBuilder(topOfferPPU.isPresent() ? Items.GOLD_BLOCK : Items.BARRIER)
			.setName(Text.literal("Same as top offer").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Top offer: ").styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(topOfferPPU, true)))
			.addLoreLine(Text.literal("Total " + totalString + ": ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(topOfferPPU.map(v -> v * amount), true)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(topOfferPPU.isPresent()
				? "Click to place offer"
				: "Can't make this offer")
				.styled(s -> s.withColor(topOfferPPU.isPresent() ? Formatting.GRAY : Formatting.RED)))
			.setCallback((index, type, action, gui) -> {
				if (!topOfferPPU.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, topOfferPPU.get())
					.open();
			}));

		setSlot(23, new GuiElementBuilder(averageOffer.isPresent() ? Items.CHEST : Items.BARRIER)
			.setName(Text.literal("Average of top offers").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Average: ").styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(averageOffer, true)))
			.addLoreLine(Text.literal("Total " + totalString + ": ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(averageOffer.map(v -> v * amount), true)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(averageOffer.isPresent()
				? "Click to place offer"
				: "Can't make this offer")
				.styled(s -> s.withColor(averageOffer.isPresent() ? Formatting.GRAY : Formatting.RED)))
			.setCallback((index, type, action, gui) -> {
				if (!averageOffer.isPresent()) return;
				new OfferConfirmMenu(this, getPlayer(), getProduct(), getOfferType(), amount, averageOffer.get())
					.open();
			}));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Text.literal("Custom price").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.literal("Get rich in your own way.").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Click to specify custom price").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> new OfferCustomPriceInput(player, this).open()));
	}
}
