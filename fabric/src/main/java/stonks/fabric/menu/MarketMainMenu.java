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
package stonks.fabric.menu;

import java.util.ArrayList;
import java.util.List;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.caching.StonksServiceCache;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.handling.WaitableGuiElement;
import stonks.fabric.menu.product.ProductMenu;
import stonks.fabric.translation.Translations;

public class MarketMainMenu extends StackedMenu {
	private static final int CATEGORIES_PER_PAGE = 5;
	private static final int PRODUCTS_PER_PAGE = 7 * 5;
	private int selectedCategoryIndex = 0;
	private int categoriesPage = 0, categoriesMaxPages = 1;
	private int productsPage = 0, productsMaxPage = 1;

	public MarketMainMenu(StackedMenu previous, ServerPlayerEntity player) {
		super(previous, ScreenHandlerType.GENERIC_9X6, player, false);
		setTitle(Translations.Menus.MainMenu.MainMenu);

		for (int i = 0; i < getHeight() - 1; i++) {
			var slot = (i + 1) * getWidth();
			setSlot(slot, WaitableGuiElement.ANIMATED_LOADING);
		}

		placePagesNavigations(null);

		var cx = (getWidth() - 2) / 2;
		var cy = (getHeight() - 1) / 2;
		setSlot(2 + cx + (1 + cy) * getWidth(), WaitableGuiElement.ANIMATED_LOADING);

		getGuiTasksHandler().handle(StonksFabric.getServiceProvider(player).getStonksCache().getAllCategories(),
			(categories, error) -> {
				if (error != null) {
					var icon = new GuiElementBuilder(Items.BARRIER)
						.setName(Translations.Errors.Errors)
						.addLoreLine(Translations.Errors.CategoriesList);

					for (int i = 0; i < getHeight() - 1; i++) {
						var slot = (i + 1) * getWidth();
						setSlot(slot, icon);
					}

					setSlot(2 + cx + (1 + cy) * getWidth(), icon);
					error.printStackTrace();
					return;
				}

				categoriesMaxPages = Math.max((int) Math.ceil(categories.size() / (double) CATEGORIES_PER_PAGE), 1);
				refresh(categories);
				placePagesNavigations(categories);
			});
	}

	@Override
	protected void placeBorder() {
		super.placeBorder();
		for (int y = 1; y < getHeight(); y++) setSlot(1 + y * 9, MenuIcons.BORDER);
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(4, MenuIcons.VIEW_SELF_OFFERS);
	}

	protected void placePagesNavigations(List<Category> categories) {
		// Categories
		setSlot(10, categories == null || categoriesPage <= 0
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE, Math.max(Math.min(categoriesPage, 64), 1))
				.setName(Translations.Icons.ScrollUp)
				.addLoreLine(Translations.Icons.ScrollUp$0(categoriesPage, categoriesMaxPages))
				.setCallback((index, type, action, gui) -> {
					if (categoriesPage <= 0 || categories == null) return;
					categoriesPage--;
					refresh(categories);
					placePagesNavigations(categories);
				}));
		setSlot(46, categories == null || categoriesPage >= (categoriesMaxPages - 1)
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE, Math.max(Math.min(categoriesPage + 2, 64), 1))
				.setName(Translations.Icons.ScrollDown)
				.addLoreLine(Translations.Icons.ScrollDown$0(categoriesPage, categoriesMaxPages))
				.setCallback((index, type, action, gui) -> {
					if (categoriesPage >= (categoriesMaxPages - 1) || categories == null) return;
					categoriesPage++;
					refresh(categories);
					placePagesNavigations(categories);
				}));

		// Products
		setSlot(2, categories == null || productsPage <= 0
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.ARROW, Math.max(Math.min(productsPage, 64), 1))
				.setName(Translations.Icons.PreviousPage)
				.addLoreLine(Translations.Icons.PreviousPage$0(productsPage, productsMaxPage))
				.setCallback((index, type, action, gui) -> {
					if (productsPage <= 0 || categories == null) return;
					productsPage--;
					refresh(categories);
					placePagesNavigations(categories);
				}));
		setSlot(6, categories == null || productsPage >= (productsMaxPage - 1)
			? MenuIcons.BORDER
			: new GuiElementBuilder(Items.ARROW, Math.max(Math.min(productsPage + 2, 64), 1))
				.setName(Translations.Icons.NextPage)
				.addLoreLine(Translations.Icons.NextPage$0(productsPage, productsMaxPage))
				.setCallback((index, type, action, gui) -> {
					if (productsPage >= (productsMaxPage - 1) || categories == null) return;
					productsPage++;
					refresh(categories);
					placePagesNavigations(categories);
				}));
	}

	private void refresh(List<Category> categories) {
		placeCategories(categories);
		placeCategory(categories.get(selectedCategoryIndex));
	}

	private void placeCategories(List<Category> categories) {
		for (int i = 0; i < getHeight() - 1; i++) {
			var currentCategoryIndex = categoriesPage * CATEGORIES_PER_PAGE + i;
			var slot = (i + 1) * getWidth();
			if (currentCategoryIndex >= categories.size()) clearSlot(slot);
			else {
				var category = categories.get(currentCategoryIndex);
				var selected = currentCategoryIndex == selectedCategoryIndex;

				var a = new GuiElementBuilder(Items.PAPER)
					.setName(Text.literal(category.getCategoryName())
						.styled(s -> s.withColor(Formatting.AQUA)))
					.addLoreLine(selected
						? Translations.Menus.MainMenu.Category$Selected
						: Translations.Menus.MainMenu.Category$Unselected)
					.setCallback((index, type, action, gui) -> {
						selectedCategoryIndex = currentCategoryIndex;
						productsPage = 0;
						productsMaxPage = Math
							.max((int) Math.ceil(category.getProducts().size() / (double) PRODUCTS_PER_PAGE), 1);
						refresh(categories);
						placePagesNavigations(categories);
					});

				if (selected) a.glow();
				setSlot(slot, a);
			}
		}
	}

	private void placeCategory(Category category) {
		var cache = StonksFabric.getServiceProvider(getPlayer()).getStonksCache();

		for (int y = 0; y < getHeight() - 1; y++) {
			for (int x = 0; x < getWidth() - 2; x++) {
				var slot = (2 + x) + (1 + y) * getWidth();
				var productIndex = x + y * (getWidth() - 2);
				productIndex += PRODUCTS_PER_PAGE * productsPage;

				if (productIndex >= category.getProducts().size()) {
					clearSlot(slot);
				} else {
					var product = category.getProducts().get(productIndex);
					placeProduct(slot, cache, category, product);
				}
			}
		}
	}

	private void placeProduct(int slot, StonksServiceCache cache, Category category, Product product) {
		var dispStack = StonksFabric.getDisplayStack(StonksFabric
			.getServiceProvider(getPlayer())
			.getStonksAdapter(), product);

		setSlot(slot, new WaitableGuiElement<>(cache.getOverview(product).get()) {
			@Override
			public ItemStack createStackWhenLoaded(ProductMarketOverview overview, Throwable error) {
				// TODO use message from UserException
				if (error != null) return new GuiElementBuilder(Items.BARRIER)
					.setName(Translations.Errors.Errors)
					.addLoreLine(Translations.Errors.QuickPriceDetails)
					.asStack();

				// TODO should we use avg price?
				var instantBuyPrice = overview.getSellOffers().compute().map(v -> v.min());
				var instantSellPrice = overview.getBuyOffers().compute().map(v -> v.min());

				return GuiElementBuilder.from(dispStack)
					.setLore(new ArrayList<>()) // Clear all lore
					.addLoreLine(Text.literal(category.getCategoryName())
						.styled(s -> s.withColor(Formatting.DARK_GRAY)))
					.addLoreLine(Text.empty())
					.addLoreLine(Translations.Menus.MainMenu.product$instantBuy(instantBuyPrice))
					.addLoreLine(Translations.Menus.MainMenu.product$instantSell(instantSellPrice))
					.addLoreLine(Text.empty())
					.addLoreLine(Translations.Menus.MainMenu.Product$ClickToOpen)
					.asStack();
			}

			@Override
			public void onSlotClick(int index, ClickType type, SlotActionType action, SlotGuiInterface gui, ProductMarketOverview success, Throwable error) {
				if (error != null) return;
				new ProductMenu(MarketMainMenu.this, getPlayer(), product).open();
			}
		});
	}
}
