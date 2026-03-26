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
package stonks.fabric.menu.player;

import java.util.ArrayList;
import java.util.List;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import stonks.core.caching.FutureCache;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.handling.WaitableGuiElement;
import stonks.fabric.translation.Translations;

public class ViewOffersMenu extends StackedMenu {
	private FutureCache<List<Offer>> offersCache;
	private boolean isUpdating = false;
	private boolean isInitialized = false;
	private List<Offer> loadedOffers;
	private int page = 0;
	private int maxPages = 1;

	public ViewOffersMenu(StackedMenu previous, ServerPlayer player) {
		super(previous, MenuType.GENERIC_9x6, player, false);
		setTitle(Translations.Menus.ViewOffers.ViewOffers);
		offersCache = StonksFabric.getPlatform(player).getStonksCache().getOffers(player.getUUID());

		setSlot((getHeight() / 2) * getWidth() + getWidth() / 2, WaitableGuiElement.ANIMATED_LOADING);
		placePagesNavigations();
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(4, MenuIcons.MAIN_MENU);
	}

	protected void placePagesNavigations() {
		setSlot(2, page <= 0
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.ARROW, Math.max(Math.min(page, 64), 1))
				.setName(Translations.Icons.PreviousPage)
				.addLoreLine(Translations.Icons.PreviousPage$0(page, maxPages))
				.setCallback((_, _, _, _) -> {
					if (page <= 0 || isUpdating) return;
					page--;
					placeOffers(loadedOffers);
					placePagesNavigations();
				}));
		setSlot(6, page >= (maxPages - 1)
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.ARROW, Math.max(Math.min(page + 2, 64), 1))
				.setName(Translations.Icons.NextPage)
				.addLoreLine(Translations.Icons.NextPage$0(page, maxPages))
				.setCallback((_, _, _, _) -> {
					if (page >= (maxPages - 1) || isUpdating) return;
					page++;
					placeOffers(loadedOffers);
					placePagesNavigations();
				}));
	}

	@Override
	public void onTick() {
		super.onTick();

		if (!isInitialized || (isUpdating == false && offersCache.shouldFetch())) {
			isInitialized = true;
			isUpdating = true;

			offersCache.get()
				.thenAccept(offers -> {
					isUpdating = false;
					loadedOffers = offers;
					maxPages = Math.max(offers.size() / getOffersPerPage(), 1);
					placeOffers(offers);
					placePagesNavigations();
				})
				.exceptionally(error -> {
					isUpdating = false;
					loadedOffers = null;
					setSlot((getHeight() / 2) * getWidth() + getWidth() / 2, new GuiElementBuilder(Items.BARRIER)
						.setName(Translations.Errors.Errors)
						.addLoreLine(Translations.Menus.ViewOffers.Retrying)
						.asStack());
					StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
					error.printStackTrace();
					return null;
				});
		}
	}

	public int getOffersPerPage() { return getWidth() * (getHeight() - 1); }

	public void placeOffers(List<Offer> offers) {
		if (offers == null) return;

		if (offers.size() == 0) {
			var middle = (getHeight() / 2) * getWidth() + getWidth() / 2;

			for (int y = 1; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					var slot = x + y * 9;
					if (slot != middle) clearSlot(slot);
				}
			}

			setSlot(middle, new GuiElementBuilder(Items.BARRIER)
				.setName(Translations.Menus.ViewOffers.NoOffers)
				.addLoreLine(Translations.Menus.ViewOffers.NoOffers$0)
				.addLoreLine(Translations.Menus.ViewOffers.NoOffers$1));
			return;
		}

		for (int i = 0; i < getOffersPerPage(); i++) {
			var offerIndex = getOffersPerPage() * page + i;
			if (offerIndex >= offers.size()) {
				clearSlot(getWidth() + i);
				continue;
			}

			var offer = offers.get(offerIndex);
			setSlot(getWidth() + i, createOfferButton(player, offer)
				.addLoreLine(Component.empty())
				.addLoreLine(Translations.Menus.ViewOffers.Offer$ClickToOpen)
				.setCallback((_, _, _, _) -> new OfferInfoMenu(this, player, offer).open()));
		}
	}

	public static GuiElementBuilder createOfferButton(ServerPlayer player, Offer offer) {
		return GuiElementBuilder.from(StonksFabric
			.getPlatform(player)
			.getStonksAdapter()
			.createDisplayStack(offer.getProduct()))
			.setName(offer.getType() == OfferType.BUY
				? Translations.Menus.ViewOffers.Offer$Buy(offer)
				: Translations.Menus.ViewOffers.Offer$Sell(offer))
			.setLore(new ArrayList<>())
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.ViewOffers.Offer$Progress(offer))
			.addLoreLine(Translations.Menus.ViewOffers.Offer$ProgressLegends)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.ViewOffers.Offer$PricePerUnit(offer))
			.addLoreLine(Translations.Menus.ViewOffers.Offer$TotalPrice(offer));
	}
}
