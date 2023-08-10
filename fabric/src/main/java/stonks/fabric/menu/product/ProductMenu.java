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
import net.minecraft.util.Formatting;
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

public class ProductMenu extends StackedMenu {
	private Product product;
	private Task<ProductMarketOverview> queryTask;

	public ProductMenu(StackedMenu previous, ServerPlayerEntity player, Product product) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Text.literal("Market > " + product.getProductName()));
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
					return new GuiElementBuilder(Items.BARRIER)
						.setName(Text.literal("An error occured!").styled(s -> s.withColor(Formatting.RED)))
						// TODO use message from UserException
						.addLoreLine(Text.literal("Failed to query quick offers details")
							.styled(s -> s.withColor(Formatting.GRAY)))
						.asStack();
				}

				return new GuiElementBuilder(icon)
					.setName(Text.literal("Instant " + type.toString().toLowerCase())
						.styled(s -> s.withColor(Formatting.YELLOW))
						.append(Text.literal(" (").styled(s -> s.withColor(Formatting.GOLD)))
						.append("Avg. ")
						.append(StonksFabricUtils.currencyText(computed.map(v -> v.average()), false))
						.append(Text.literal(")").styled(s -> s.withColor(Formatting.GOLD))))
					.addLoreLine(Text.empty())
					.addLoreLine(Text.literal("Top Offered Price: ").styled(s -> s.withColor(Formatting.GRAY))
						.append(StonksFabricUtils.currencyText(computed.map(v -> switch (type) {
						case BUY -> v.min();
						case SELL -> v.max();
						}), true)))
					.addLoreLine(Text.literal("Average Offered Price: ").styled(s -> s.withColor(Formatting.GRAY))
						.append(StonksFabricUtils.currencyText(computed.map(v -> v.average()), true)))
					.addLoreLine(Text.empty())
					.addLoreLine(computed.isEmpty()
						? Text.literal("No offers!").styled(s -> s.withColor(Formatting.RED))
						: type == OfferType.BUY ? Text.literal("Click to setup instant buy")
							.styled(s -> s.withColor(Formatting.GRAY))
						: Text.literal("Click to sell all")
							.styled(s -> s.withColor(Formatting.GRAY)))
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
						getPlayer().sendMessage(Text.literal("You don't have " + product.getProductName() + " to sell!")
							.styled(s -> s.withColor(Formatting.RED)), true);
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
					return new GuiElementBuilder(Items.BARRIER)
						.setName(Text.literal("An error occured!").styled(s -> s.withColor(Formatting.RED)))
						// TODO use message from UserException
						.addLoreLine(Text.literal("Failed to query quick offers details")
							.styled(s -> s.withColor(Formatting.GRAY)))
						.asStack();
				}

				var elem = new GuiElementBuilder(icon)
					.setName(Text.literal("Create " + type.toString().toLowerCase() + " offer")
						.styled(s -> s.withColor(Formatting.YELLOW)))
					.addLoreLine(Text.empty());

				if (list.getEntries().size() > 0) {
					for (var e : list.getEntries()) { elem.addLoreLine(StonksFabricUtils.offerText(type, e)); }
				} else {
					elem.addLoreLine(Text.literal("No offers!").styled(s -> s.withColor(Formatting.RED)));
				}

				return elem
					.addLoreLine(Text.empty())
					.addLoreLine(list.getEntries().size() == 0
						? Text.literal("Click to become the first person to get rich")
							.styled(s -> s.withColor(Formatting.GRAY))
						: Text.literal("Click to make offer").styled(s -> s.withColor(Formatting.GRAY)))
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