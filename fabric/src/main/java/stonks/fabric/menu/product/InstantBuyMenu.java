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

	public InstantBuyMenu(StackedMenu previous, ServerPlayerEntity player, Product product, double originalPricePerUnit, double instantPricePerUnit) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
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
			.addLoreLine(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.InstantBuy.CustomAmount$0)
			.setCallback((index, type, action, gui) -> new OfferInstantBuyAmountInput(player, this).open()));
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
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.InstantBuy.AveragePrice(amount, originalPricePerUnit))
			.addLoreLine(Translations.Menus.InstantBuy.MinimumBalance(moneyToSpend))
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.InstantBuy.GuideText$0)
			.addLoreLine(Translations.Menus.InstantBuy.GuideText$1)
			.addLoreLine(Text.empty())
			.addLoreLine(Translations.Menus.InstantBuy.HoldShift)
			.addLoreLine(canBuy
				? Translations.Menus.InstantBuy.ClickToBuy
				: Translations.Menus.InstantBuy.NoBuy)
			.setCallback((index, type, action, gui) -> {
				var provider = StonksFabric.getPlatform(getPlayer());
				var adapter = provider.getStonksAdapter();

				if (adapter.accountBalance(getPlayer()) < moneyToSpend) {
					getPlayer().sendMessage(Translations.Messages.NoMoneyToInstantBuy(moneyToSpend), true);
					close();
					return;
				}

				var task = StonksFabricHelper.instantOffer(getPlayer(), product, OfferType.BUY, amount, moneyToSpend);
				if (!type.shift) close();
				else {
					if (task.get().isPresent()) {
						placeBuyButtons();
						return;
					}

					blockBuyButtons();
					getGuiTasksHandler().handle(task, ($void, error) -> {
						if (error != null) {
							close();
							error.printStackTrace();
							return;
						}

						new InstantBuyMenu(getPrevious(), getPlayer(), getProduct(), originalPricePerUnit, instantPricePerUnit)
							.open();
					});
				}
			});
	}
}
