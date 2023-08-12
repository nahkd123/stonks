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

import java.util.Optional;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import nahara.common.tasks.Task;
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
import stonks.fabric.Translations;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.handling.WaitableGuiElement;

public class ProductMenu extends StackedMenu {
	private Product product;
	private Task<ProductMarketOverview> queryTask;

	public ProductMenu(StackedMenu previous, ServerPlayerEntity player, Product product) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Translations.menus$productInfo(product));
		this.product = product;
		this.queryTask = StonksFabric.getServiceProvider(getPlayer()).getStonksCache().getOverview(product).get();

		setSlot(22, GuiElementBuilder.from(StonksFabric.getDisplayStack(StonksFabric
			.getServiceProvider(player)
			.getStonksAdapter(), product)));

		setSlot(19, createInstantOfferButton(OfferType.BUY, queryTask.afterThatDo(v -> {
			return v.getSellOffers().compute();
		})));
		setSlot(20, createInstantOfferButton(OfferType.SELL, queryTask.afterThatDo(v -> {
			return v.getBuyOffers().compute();
		})));
		setSlot(24, createOfferButton(queryTask.afterThatDo(v -> {
			return v.getBuyOffers();
		}), OfferType.BUY));
		setSlot(25, createOfferButton(queryTask.afterThatDo(v -> {
			return v.getSellOffers();
		}), OfferType.SELL));
	}

	public Product getProduct() { return product; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	private GuiElementInterface createInstantOfferButton(OfferType type, Task<Optional<ComputedOffersList>> computeTask) {
		var icon = switch (type) {
		case BUY -> Items.DIAMOND;
		case SELL -> Items.GOLD_INGOT;
		};

		return new WaitableGuiElement<>(computeTask) {
			@Override
			public ItemStack createStackWhenLoaded(Optional<ComputedOffersList> computed, Throwable error) {
				if (error != null) {
					error.printStackTrace();
					// TODO use message from UserException
					return new GuiElementBuilder(Items.BARRIER)
						.setName(Translations.errors)
						.addLoreLine(Translations.errors$quickPriceDetails)
						.asStack();
				}

				var topPrice = computed.map(v -> type == OfferType.BUY ? v.min() : v.max());
				var tax = StonksFabric.getServiceProvider(getPlayer()).getPlatformConfig().tax;
				var out = new GuiElementBuilder(icon)
					.setName(type == OfferType.BUY
						? Translations.menus$productInfo$instantBuy(computed)
						: Translations.menus$productInfo$instantSell(computed))
					.addLoreLine(Text.empty())
					.addLoreLine(Translations.menus$productInfo$topOfferedPrice(topPrice))
					.addLoreLine(Translations.menus$productInfo$avgOfferedPrice(computed));

				if (type == OfferType.SELL && tax > 0d) out.addLoreLine(Translations.menus$productInfo$instantSellTax(tax));

				return out
					.addLoreLine(Text.empty())
					.addLoreLine(computed.isEmpty()
						? Translations.menus$productInfo$noOffers
						: type == OfferType.BUY ? Translations.menus$productInfo$clickToInstantBuy
						: Translations.menus$productInfo$clickToInstantSell)
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

					var provider = StonksFabric.getServiceProvider(getPlayer());
					var units = provider.getStonksAdapter().getUnits(getPlayer(), product);
					if (units <= 0) {
						getPlayer().sendMessage(Translations.messages$noUnitsToInstantSell(product), true);
						return;
					}

					StonksFabricHelper.instantOffer(player, product, OfferType.SELL, units, 0);
				}
			}
		};
	}

	private GuiElementInterface createOfferButton(Task<OverviewOffersList> list, OfferType type) {
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
						.setName(Translations.errors)
						.addLoreLine(Translations.errors$quickPriceDetails)
						.asStack();
				}

				var elem = new GuiElementBuilder(icon)
					.setName(type == OfferType.BUY
						? Translations.menus$productInfo$buyOffer
						: Translations.menus$productInfo$sellOffer)
					.addLoreLine(Text.empty());

				if (list.getEntries().size() > 0) {
					for (var e : list.getEntries()) { elem.addLoreLine(StonksFabricUtils.offerText(type, e)); }
				} else {
					elem.addLoreLine(Translations.menus$productInfo$noOffers);
				}

				return elem
					.addLoreLine(Text.empty())
					.addLoreLine(list.getEntries().size() == 0
						? Translations.menus$productInfo$makeOffer$noOffers
						: Translations.menus$productInfo$makeOffer)
					.asStack();
			}

			@Override
			public void onSlotClick(int index, ClickType clickType, SlotActionType action, SlotGuiInterface gui, OverviewOffersList success, Throwable error) {
				if (error != null) return;
				var now = queryTask.get();
				new OfferAmountConfigureMenu(ProductMenu.this, player, product, type, now.get().getSuccess()).open();
			}
		};
	}
}
