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

import java.util.Optional;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;

public class OfferInfoMenu extends StackedMenu {
	private Offer offer;

	public OfferInfoMenu(StackedMenu previous, ServerPlayerEntity player, Offer offer) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		this.offer = offer;
		setTitle(Text.literal("Market > Offers > " + offer.getProduct().getProductName()));

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

		return new GuiElementBuilder(canClaim ? Items.GOLD_INGOT : Items.BARRIER)
			.setName(Text.literal("Claim offer").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("You have ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(switch (offer.getType()) {
				case BUY -> Text.literal(unitsToClaim + " units").styled(s -> s.withColor(Formatting.AQUA));
				case SELL -> StonksFabricUtils.currencyText(Optional.of(unitsToClaim * offer.getPricePerUnit()), true);
				})
				.append(" to claim"))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(canClaim
				? "Click to claim all"
				: "Can't claim")
				.styled(s -> s.withColor(canClaim ? Formatting.GRAY : Formatting.RED)))
			.setCallback((index, type, action, gui) -> {
				if (!canClaim) return;

				var adapter = StonksFabric.getServiceProvider(getPlayer()).getStonksAdapter();
				var service = StonksFabric.getServiceProvider(getPlayer()).getStonksService();
				var handler = StonksFabric.getServiceProvider(getPlayer()).getTasksHandler();

				setSlot(index, new GuiElementBuilder(Items.CLOCK)
					.setName(Text.literal("Claiming...").styled(s -> s.withColor(Formatting.GRAY))));

				var previousUnits = offer.getClaimedUnits();
				handler.handle(service.claimOffer(offer), (newOffer, error) -> {
					if (error != null) {
						if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
							.setName(Text.literal("Claim failed!").styled(s -> s.withColor(Formatting.RED))));
						else getPlayer().sendMessage(Text.literal("An error occured. Claim failed!")
							.styled(s -> s.withColor(Formatting.RED)), true);

						error.printStackTrace();
						return;
					}

					var newUnits = newOffer.getClaimedUnits();
					var delta = newUnits - previousUnits;

					if (offer.getType() == OfferType.BUY) {
						adapter.addUnitsTo(getPlayer(), offer.getProduct(), delta);
					} else {
						adapter.accountDeposit(getPlayer(), delta * offer.getPricePerUnit());
					}

					new OfferInfoMenu(getPrevious(), getPlayer(), newOffer).open();
					getPlayer().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
				});
			});
	}

	public GuiElementBuilder createCancelButton() {
		return new GuiElementBuilder(Items.RED_TERRACOTTA)
			.setName(Text.literal("Cancel offer").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.literal("Pending items/money will be").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.literal("refunded.").styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Click to cancel").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> {
				var adapter = StonksFabric.getServiceProvider(getPlayer()).getStonksAdapter();
				var service = StonksFabric.getServiceProvider(getPlayer()).getStonksService();
				var handler = StonksFabric.getServiceProvider(getPlayer()).getTasksHandler();

				setSlot(index, new GuiElementBuilder(Items.CLOCK)
					.setName(Text.literal("Cancelling...").styled(s -> s.withColor(Formatting.GRAY))));

				var previousClaimedUnits = offer.getClaimedUnits();
				handler.handle(service.cancelOffer(offer), (newOffer, error) -> {
					if (error != null) {
						if (isOpen()) setSlot(index, new GuiElementBuilder(Items.BARRIER)
							.setName(Text.literal("Cancel failed!").styled(s -> s.withColor(Formatting.RED))));
						else getPlayer().sendMessage(Text.literal("An error occured. Cancel failed!")
							.styled(s -> s.withColor(Formatting.RED)), true);

						error.printStackTrace();
						return;
					}

					var totalUnits = newOffer.getTotalUnits();
					int refundUnits;
					double refundMoney;
					var ppu = newOffer.getPricePerUnit();

					if (offer.getType() == OfferType.BUY) {
						refundUnits = newOffer.getFilledUnits() - previousClaimedUnits;
						refundMoney = (totalUnits - newOffer.getFilledUnits()) * ppu;
					} else {
						refundUnits = totalUnits - newOffer.getFilledUnits();
						refundMoney = (newOffer.getFilledUnits() - previousClaimedUnits) * ppu;
					}

					adapter.addUnitsTo(getPlayer(), newOffer.getProduct(), refundUnits);
					adapter.accountDeposit(getPlayer(), refundMoney);

					close();
					getPlayer().playSound(SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1f, 1f);
					getPlayer().sendMessage(Text.literal("Offer cancelled! Refunded ")
						.append(Text.literal(Integer.toString(refundUnits))
							.styled(s -> s.withColor(Formatting.AQUA)))
						.append("x " + newOffer.getProduct().getProductName() + " and ")
						.append(StonksFabricUtils.currencyText(Optional.of(refundMoney), true)), true);
				});
			});
	}
}
