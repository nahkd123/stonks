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
package stonks.fabric.menu.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.caching.Cached;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.handling.WaitableGuiElement;

public class ViewOffersMenu extends StackedMenu {
	private Cached<List<Offer>> offersCache;
	private boolean isUpdating = false;
	private boolean isInitialized = false;
	private List<Offer> loadedOffers;
	private int page = 0;
	private int maxPages = 1;

	public ViewOffersMenu(StackedMenu previous, ServerPlayerEntity player) {
		super(previous, ScreenHandlerType.GENERIC_9X6, player, false);
		setTitle(Text.literal("Market > Offers"));
		offersCache = StonksFabric.getServiceProvider(player).getStonksCache().getOffers(player.getUuid());

		setSlot((getHeight() / 2) * getWidth() + getWidth() / 2, WaitableGuiElement.ANIMATED_LOADING);
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(4, MenuIcons.MAIN_MENU);
		setSlot(2, new GuiElementBuilder(Items.ARROW, Math.max(Math.min(page, 64), 1))
			.setName(Text.literal("<-- Previous Page").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.literal("Current Page: " + (page + 1) + "/" + maxPages))
			.setCallback((index, type, action, gui) -> {
				if (page <= 0 || isUpdating) return;
				page--;
				placeOffers(loadedOffers);
				placeButtons();
			}));
		setSlot(6, new GuiElementBuilder(Items.ARROW, Math.max(Math.min(page + 2, 64), 1))
			.setName(Text.literal("Next Page -->").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.literal("Current Page: " + (page + 1) + "/" + maxPages))
			.setCallback((index, type, action, gui) -> {
				if (page >= (maxPages - 1) || isUpdating) return;
				page++;
				placeOffers(loadedOffers);
				placeButtons();
			}));
	}

	@Override
	public void onTick() {
		super.onTick();

		if (!isInitialized || (isUpdating == false && offersCache.shouldUpdate())) {
			isInitialized = true;
			isUpdating = true;

			getGuiTasksHandler().handle(offersCache.get(), (offers, error) -> {
				isUpdating = false;

				if (error != null) {
					loadedOffers = null;
					setSlot((getHeight() / 2) * getWidth() + getWidth() / 2, new GuiElementBuilder(Items.BARRIER)
						.setName(Text.literal("An error occured").styled(s -> s.withColor(Formatting.RED)))
						.addLoreLine(Text.literal("Retrying in few seconds, please wait...")
							.styled(s -> s.withColor(Formatting.GRAY)))
						.asStack());
					return;
				}

				loadedOffers = offers;
				maxPages = Math.max((offers.size() / getOffersPerPage()) + 1, 1);
				placeOffers(offers);
				placeButtons();
			});
		}
	}

	public int getOffersPerPage() { return getWidth() * (getHeight() - 1); }

	public void placeOffers(List<Offer> offers) {
		if (offers == null) return;
		if (offers.size() == 0) {
			setSlot((getHeight() / 2) * getWidth() + getWidth() / 2, new GuiElementBuilder(Items.BARRIER)
				.setName(Text.literal("No offers!").styled(s -> s.withColor(Formatting.RED)))
				.addLoreLine(Text.literal("Go back and place offers to see").styled(s -> s.withColor(Formatting.GRAY)))
				.addLoreLine(Text.literal("them here!").styled(s -> s.withColor(Formatting.GRAY))));
			return;
		}

		for (int i = 0; i < getOffersPerPage(); i++) {
			var offerIndex = getOffersPerPage() * page + i;
			if (offerIndex >= offers.size()) {
				clearSlot(getWidth() + offerIndex);
				continue;
			}

			var offer = offers.get(offerIndex);
			setSlot(getWidth() + i, createOfferButton(player, offer)
				.addLoreLine(Text.empty())
				.addLoreLine(Text.literal("Click to open").styled(s -> s.withColor(Formatting.GRAY)))
				.setCallback((index, type, action, gui) -> new OfferInfoMenu(this, player, offer).open()));
		}
	}

	public static GuiElementBuilder createOfferButton(ServerPlayerEntity player, Offer offer) {
		var totalUnits = (double) offer.getTotalUnits();
		return GuiElementBuilder.from(StonksFabric
			.getServiceProvider(player)
			.getStonksAdapter()
			.createDisplayStack(offer.getProduct()))
			.setName(
				Text.empty()
					.styled(s -> s.withColor(Formatting.GRAY))
					.append(Text.literal(offer.getType().toString() + " OFFER")
						.styled(s -> s
							.withBold(true)
							.withColor(offer.getType() == OfferType.BUY ? Formatting.GREEN : Formatting.YELLOW)))
					.append(": ")
					.append(Text.literal(offer.getProduct().getProductName())
						.styled(s -> s.withColor(Formatting.AQUA))))
			.setLore(new ArrayList<>())
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Progress: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.progressBar(
					20, Formatting.DARK_GRAY,
					new double[] { offer.getClaimedUnits() / totalUnits, offer.getFilledUnits() / totalUnits },
					new Formatting[] { Formatting.GREEN, Formatting.YELLOW }))
				.append(" ").append(Text.literal(Integer.toString(offer.getClaimedUnits()))
					.styled(s -> s.withColor(Formatting.GREEN)))
				.append("/").append(Text.literal(Integer.toString(offer.getFilledUnits()))
					.styled(s -> s.withColor(Formatting.YELLOW)))
				.append("/").append(Text.literal(Integer.toString(offer.getTotalUnits()))
					.styled(s -> s.withColor(Formatting.WHITE))))
			.addLoreLine(Text.literal("(Claimed/Filled/Total)").styled(s -> s.withColor(Formatting.DARK_GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Price per unit: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(offer.getPricePerUnit()), true)))
			.addLoreLine(Text.literal("Total price: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(offer.getPricePerUnit() * offer.getTotalUnits()),
					true)));
	}
}
