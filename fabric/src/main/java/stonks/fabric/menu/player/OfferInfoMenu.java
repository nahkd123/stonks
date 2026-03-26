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

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.translation.Translations;

public class OfferInfoMenu extends StackedMenu {
	private ViewOffersMenu previous;
	private Offer offer;

	public OfferInfoMenu(ViewOffersMenu previous, ServerPlayer player, Offer offer) {
		super(previous, MenuType.GENERIC_9x4, player, false);
		this.previous = previous;
		this.offer = offer;
		setTitle(Translations.Menus.OfferInfo._OfferInfo(offer));

		setSlot(7, ViewOffersMenu.createOfferButton(player, offer));

		setSlot(20, createClaimAllButton());
		setSlot(24, createCancelButton());
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	public GuiElementBuilder createClaimAllButton() {
		var canClaim = offer.canClaim();
		var unitsToClaim = offer.getAvailableToClaim();
		var config = StonksFabric.getPlatform(getPlayer()).getPlatformConfig();
		var tax = config.tax;

		return new GuiElementBuilder(canClaim ? Items.GOLD_INGOT : Items.BARRIER)
			.setName(Translations.Menus.OfferInfo.ClaimOffer)
			.addLoreLine(Component.empty())
			.addLoreLine(offer.getType() == OfferType.BUY
				? Translations.Menus.OfferInfo.ClaimOffer$Units(unitsToClaim)
				: tax > 0d
					? Translations.Menus.OfferInfo.ClaimOffer$MoneyWithTax(unitsToClaim, offer, config)
				: Translations.Menus.OfferInfo.ClaimOffer$Money(unitsToClaim, offer))
			.addLoreLine(Component.empty())
			.addLoreLine(canClaim
				? Translations.Menus.OfferInfo.ClaimOffer$ClickToClaim
				: Translations.Menus.OfferInfo.ClaimOffer$NoClaim)
			.setCallback((index, _, _, _) -> {
				if (!canClaim) return;

				var adapter = StonksFabric.getPlatform(getPlayer()).getStonksAdapter();
				var service = StonksFabric.getPlatform(getPlayer()).getStonksService();

				setSlot(index, new GuiElementBuilder(Items.CLOCK)
					.setName(Translations.Menus.OfferInfo.ClaimOffer$Claiming));

				var previousUnits = offer.getClaimedUnits();
				service.claimOfferAsync(offer.getOfferId())
					.thenAcceptAsync(newOffer -> {
						if (newOffer.isEmpty()) {
							if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
								.setName(Translations.Menus.OfferInfo.ClaimOffer$ClaimFailed));
							else getPlayer().sendSystemMessage(Translations.Messages.OfferClaimFailed, true);
							StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
							StonksFabric.LOGGER.warn("Offer with ID {} no longer exists!", offer.getOfferId());
							return;
						}

						var newUnits = newOffer.get().getClaimedUnits();
						var delta = newUnits - previousUnits;

						if (offer.getType() == OfferType.BUY) {
							adapter.addUnitsTo(getPlayer(), offer.getProduct(), delta);
						} else {
							adapter.accountDeposit(getPlayer(), config.applyTax(delta * offer.getPricePerUnit()));
						}

						previous.invalidateCache(); // Force update entire list
						new OfferInfoMenu(previous, getPlayer(), newOffer.get()).open();
						StonksFabric.getPlatform(getPlayer()).getSounds().playClaimedSound(getPlayer());
					}, player.createCommandSourceStack().getServer())
					.exceptionallyAsync(error -> {
						if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
							.setName(Translations.Menus.OfferInfo.ClaimOffer$ClaimFailed));
						else getPlayer().sendSystemMessage(Translations.Messages.OfferClaimFailed, true);
						StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
						error.printStackTrace();
						return null;
					}, player.createCommandSourceStack().getServer());
			});
	}

	public GuiElementBuilder createCancelButton() {
		return new GuiElementBuilder(Items.RED_TERRACOTTA)
			.setName(Translations.Menus.OfferInfo.CancelOffer)
			.addLoreLine(Translations.Menus.OfferInfo.CancelOffer$0)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.OfferInfo.CancelOffer$ClickToCancel)
			.setCallback((index, _, _, _) -> {
				var adapter = StonksFabric.getPlatform(getPlayer()).getStonksAdapter();
				var service = StonksFabric.getPlatform(getPlayer()).getStonksService();

				setSlot(index, new GuiElementBuilder(Items.CLOCK)
					.setName(Translations.Menus.OfferInfo.CancelOffer$Cancelling));

				var previousClaimedUnits = offer.getClaimedUnits();
				service.cancelOfferAsync(offer.getOfferId())
					.thenAccept(newOffer -> player.createCommandSourceStack().getServer().execute(() -> {
						if (newOffer.isEmpty()) {
							if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
								.setName(Translations.Menus.OfferInfo.CancelOffer$CancelFailed));
							else getPlayer().sendSystemMessage(Translations.Messages.OfferCancelFailed, true);
							StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
							StonksFabric.LOGGER.warn("Offer with ID {} no longer exists!", offer.getOfferId());
							return;
						}

						var offer = newOffer.get();
						var totalUnits = offer.getTotalUnits();
						int refundUnits;
						double refundMoney;
						var ppu = offer.getPricePerUnit();

						if (offer.getType() == OfferType.BUY) {
							refundUnits = offer.getFilledUnits() - previousClaimedUnits;
							refundMoney = (totalUnits - offer.getFilledUnits()) * ppu;
						} else {
							refundUnits = totalUnits - offer.getFilledUnits();
							refundMoney = StonksFabric.getPlatform(getPlayer()).getPlatformConfig()
								.applyTax((offer.getFilledUnits() - previousClaimedUnits) * ppu);
						}

						adapter.addUnitsTo(getPlayer(), offer.getProduct(), refundUnits);
						adapter.accountDeposit(getPlayer(), refundMoney);

						close();
						StonksFabric.getPlatform(getPlayer()).getSounds().playCancelledSound(getPlayer());
						getPlayer().sendSystemMessage(
							Translations.Messages.OfferCancelled(offer, refundUnits, refundMoney),
							true);
					}))
					.exceptionally(error -> {
						if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
							.setName(Translations.Menus.OfferInfo.CancelOffer$CancelFailed));
						else getPlayer().sendSystemMessage(Translations.Messages.OfferCancelFailed, true);
						StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
						error.printStackTrace();
						return null;
					});
			});
	}
}
