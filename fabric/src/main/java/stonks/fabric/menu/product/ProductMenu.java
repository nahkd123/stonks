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
package stonks.fabric.menu.product;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import stonks.core.market.ComputedOffersList;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffersList;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricHelper;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.handling.WaitableGuiElement;
import stonks.fabric.translation.Translations;

public class ProductMenu extends StackedMenu {
	private Product product;
	private CompletableFuture<ProductMarketOverview> queryTask;

	public ProductMenu(StackedMenu previous, ServerPlayerEntity player, Product product) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Translations.Menus.ProductInfo._ProductInfo(product));
		this.product = product;
		this.queryTask = StonksFabric.getPlatform(getPlayer()).getStonksCache().getOverview(product).get();

		setSlot(22, GuiElementBuilder.from(StonksFabric.getDisplayStack(StonksFabric
			.getPlatform(player)
			.getStonksAdapter(), product)));

		setSlot(19, createInstantOfferButton(OfferType.BUY, queryTask.thenApply(v -> v.getSellOffers().compute())));
		setSlot(20, createInstantOfferButton(OfferType.SELL, queryTask.thenApply(v -> v.getBuyOffers().compute())));
		setSlot(24, createOfferButton(queryTask.thenApply(v -> v.getBuyOffers()), OfferType.BUY));
		setSlot(25, createOfferButton(queryTask.thenApply(v -> v.getSellOffers()), OfferType.SELL));
	}

	public Product getProduct() { return product; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	private GuiElementInterface createInstantOfferButton(OfferType type, CompletableFuture<Optional<ComputedOffersList>> computeTask) {
		var icon = switch (type) {
		case BUY -> Items.DIAMOND;
		case SELL -> Items.GOLD_INGOT;
		};

		return new WaitableGuiElement<>(computeTask) {
			@Override
			public ItemStack createStackWhenLoaded(Optional<ComputedOffersList> computed, Throwable error) {
				if (error != null) {
					error.printStackTrace();
					StonksFabric.getPlatform(getPlayer()).getSounds().playErrorSound(getPlayer());
					// TODO use message from UserException
					return new GuiElementBuilder(Items.BARRIER)
						.setName(Translations.Errors.Errors)
						.addLoreLine(Translations.Errors.QuickPriceDetails)
						.asStack();
				}

				var topPrice = computed.map(v -> type == OfferType.BUY ? v.min() : v.max());
				var tax = StonksFabric.getPlatform(getPlayer()).getPlatformConfig().tax;
				var out = new GuiElementBuilder(icon)
					.setName(type == OfferType.BUY
						? Translations.Menus.ProductInfo.InstantBuy(computed)
						: Translations.Menus.ProductInfo.InstantSell(computed))
					.addLoreLine(Text.empty())
					.addLoreLine(Translations.Menus.ProductInfo.TopOfferedPrice(topPrice))
					.addLoreLine(Translations.Menus.ProductInfo.AvgOfferedPrice(computed));

				if (type == OfferType.SELL && tax > 0d)
					out.addLoreLine(Translations.Menus.ProductInfo.InstantSellTax(tax));

				return out
					.addLoreLine(Text.empty())
					.addLoreLine(computed.isEmpty()
						? Translations.Menus.ProductInfo.NoOffers
						: type == OfferType.BUY ? Translations.Menus.ProductInfo.ClickToInstantBuy
						: Translations.Menus.ProductInfo.ClickToInstantSell)
					.asStack();
			}

			@Override
			public void onSlotClick(int index, ClickType clickType, SlotActionType action, SlotGuiInterface gui, Optional<ComputedOffersList> computed, Throwable error) {
				if (error != null) return;
				if (computed.isEmpty()) return;

				if (type == OfferType.BUY) {
					var avgPrice = computed.get().average();
					var bonus = avgPrice * 0.01d;
					new InstantBuyMenu(ProductMenu.this, player, product, avgPrice, avgPrice + bonus).open();
				} else {
					close();

					var provider = StonksFabric.getPlatform(getPlayer());
					var units = provider.getStonksAdapter().getUnits(getPlayer(), product);
					if (units <= 0) {
						getPlayer().sendMessage(Translations.Messages.NoUnitsToInstantSell(product),
							true);
						return;
					}

					StonksFabricHelper.instantOffer(player, product, OfferType.SELL, units, 0);
				}
			}
		};
	}

	private GuiElementInterface createOfferButton(CompletableFuture<OverviewOffersList> list, OfferType type) {
		var icon = switch (type) {
		case BUY -> Items.DIAMOND_BLOCK;
		case SELL -> Items.GOLD_BLOCK;
		};

		return new WaitableGuiElement<>(list) {
			@Override
			public ItemStack createStackWhenLoaded(OverviewOffersList list, Throwable error) {
				if (error != null) {
					error.printStackTrace();
					// TODO use message from UserException
					return new GuiElementBuilder(Items.BARRIER)
						.setName(Translations.Errors.Errors)
						.addLoreLine(Translations.Errors.QuickPriceDetails)
						.asStack();
				}

				var elem = new GuiElementBuilder(icon)
					.setName(type == OfferType.BUY
						? Translations.Menus.ProductInfo.BuyOffer
						: Translations.Menus.ProductInfo.SellOffer)
					.addLoreLine(Text.empty());

				if (list.getEntries().size() > 0) {
					for (var e : list.getEntries()) { elem.addLoreLine(StonksFabricUtils.offerText(type, e)); }
				} else {
					elem.addLoreLine(Translations.Menus.ProductInfo.NoOffers);
				}

				return elem
					.addLoreLine(Text.empty())
					.addLoreLine(list.getEntries().size() == 0
						? Translations.Menus.ProductInfo.MakeOffer$NoOffers
						: Translations.Menus.ProductInfo.MakeOffer)
					.asStack();
			}

			@Override
			public void onSlotClick(int index, ClickType clickType, SlotActionType action, SlotGuiInterface gui, OverviewOffersList success, Throwable error) {
				if (error != null) return;

				try {
					new OfferAmountConfigureMenu(ProductMenu.this, player, product, type, queryTask.get()).open();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
	}
}
