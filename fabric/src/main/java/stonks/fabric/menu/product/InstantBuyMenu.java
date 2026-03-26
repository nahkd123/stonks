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

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import stonks.core.market.OfferType;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricHelper;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferInstantBuyAmountInput;
import stonks.fabric.translation.Translations;

public class InstantBuyMenu extends StackedMenu {
	private Product product;
	private double originalPricePerUnit;
	private double instantPricePerUnit;

	public InstantBuyMenu(StackedMenu previous, ServerPlayer player, Product product, double originalPricePerUnit, double instantPricePerUnit) {
		super(previous, MenuType.GENERIC_9x4, player, false);
		this.product = product;
		this.originalPricePerUnit = originalPricePerUnit;
		this.instantPricePerUnit = instantPricePerUnit;

		setTitle(Translations.Menus.InstantBuy._InstantBuy(product));
		placeBuyButtons();
	}

	public Product getProduct() { return product; }

	public double getOriginalPricePerUnit() { return originalPricePerUnit; }

	public double getInstantPricePerUnit() { return instantPricePerUnit; }

	public void placeBuyButtons() {
		var balance = StonksFabric.getPlatform(player).getStonksAdapter().accountBalance(player);
		setSlot(19, createInstantBuyButton(balance, 1, Items.GOLD_NUGGET));
		setSlot(20, createInstantBuyButton(balance, 16, Items.GOLD_INGOT));
		setSlot(21, createInstantBuyButton(balance, 64, Items.GOLD_INGOT));
		setSlot(22, createInstantBuyButton(balance, 256, Items.GOLD_INGOT));
		setSlot(23, createInstantBuyButton(balance, 1024, Items.GOLD_BLOCK));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Translations.Menus.InstantBuy.CustomAmount)
			.addLoreLine(Component.literal(product.getProductName()).withStyle(s -> s.withColor(ChatFormatting.GRAY)))
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.InstantBuy.CustomAmount$0)
			.setCallback((_, _, _, _) -> new OfferInstantBuyAmountInput(player, this).open()));
	}

	public void blockBuyButtons() {
		var blockIcon = new GuiElementBuilder(Items.BARRIER)
			.setName(Translations.Menus.InstantBuy.Buying)
			.addLoreLine(Translations.Menus.InstantBuy.Buying$0);
		setSlot(19, blockIcon);
		setSlot(20, blockIcon);
		setSlot(21, blockIcon);
		setSlot(22, blockIcon);
		setSlot(23, blockIcon);
		setSlot(25, blockIcon);
	}

	public GuiElementBuilder createInstantBuyButton(double balance, int amount, Item icon) {
		var moneyToSpend = amount * instantPricePerUnit;
		var canBuy = balance >= moneyToSpend;

		return new GuiElementBuilder(canBuy ? icon : Items.BARRIER, Math.min(Math.max(amount / 64, 1), 64))
			.setName(Translations.Menus.InstantBuy.FixedAmount(amount))
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.InstantBuy.AveragePrice(amount, originalPricePerUnit))
			.addLoreLine(Translations.Menus.InstantBuy.MinimumBalance(moneyToSpend))
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.InstantBuy.GuideText$0)
			.addLoreLine(Translations.Menus.InstantBuy.GuideText$1)
			.addLoreLine(Component.empty())
			.addLoreLine(Translations.Menus.InstantBuy.HoldShift)
			.addLoreLine(canBuy
				? Translations.Menus.InstantBuy.ClickToBuy
				: Translations.Menus.InstantBuy.NoBuy)
			.setCallback((_, type, _, _) -> {
				var provider = StonksFabric.getPlatform(getPlayer());
				var adapter = provider.getStonksAdapter();

				if (adapter.accountBalance(getPlayer()) < moneyToSpend) {
					getPlayer().sendSystemMessage(Translations.Messages.NoMoneyToInstantBuy(moneyToSpend), true);
					close();
					return;
				}

				var task = StonksFabricHelper.instantOffer(getPlayer(), product, OfferType.BUY, amount, moneyToSpend);
				if (!type.shift) close();
				else {
					if (task.isDone() && !task.isCompletedExceptionally()) {
						placeBuyButtons();
						return;
					}

					blockBuyButtons();
					task
						.thenAcceptAsync(
							_ -> new InstantBuyMenu(getPrevious(), getPlayer(), getProduct(), originalPricePerUnit, instantPricePerUnit)
								.open(),
							player.createCommandSourceStack().getServer())
						.exceptionallyAsync(error -> {
							close();
							error.printStackTrace();
							return null;
						}, player.createCommandSourceStack().getServer());
				}
			});
	}
}
