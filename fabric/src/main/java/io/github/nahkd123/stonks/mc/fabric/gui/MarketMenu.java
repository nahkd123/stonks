package io.github.nahkd123.stonks.mc.fabric.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.gui.player.PlayerOffersMenu;
import io.github.nahkd123.stonks.mc.fabric.gui.tasked.TaskedMenu;
import io.github.nahkd123.stonks.mc.fabric.product.Category;
import io.github.nahkd123.stonks.mc.fabric.product.CategoryProduct;
import io.github.nahkd123.stonks.mc.fabric.utils.PaginationUtils;
import io.github.nahkd123.stonks.mc.fabric.utils.StonksTextUtils;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.ServiceException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MarketMenu extends TaskedMenu<Void> {
	private StonksMcInstance instance;
	private Category selectedCategory;
	private int categoryPage = 0, productPage = 0;

	private CompletableFuture<Map<String, Product>> products;
	private Map<String, CompletableFuture<ProductOverview>> overviewCache = new HashMap<>();

	public MarketMenu(ServerPlayerEntity player, StonksMcInstance instance) {
		super(ScreenHandlerType.GENERIC_9X6, player, false);
		this.instance = instance;
		setTitle(Text.literal("Market"));
		selectedCategory = instance.getCatalog().get(0);

		products = markDirtyOnFinish(instance.getMarketService().queryCatalog()).thenApply(catalog -> catalog
			.stream()
			.collect(Collectors.toMap(p -> p.getId(), Function.identity())));

		onUpdate();
	}

	@Override
	protected void onUpdate() {
		updateFrame();
		updateCategories();
		updateProducts();
	}

	private CompletableFuture<Product> serviceProductOf(CategoryProduct p) {
		return products.thenApply(map -> {
			Product serviceProduct = map.get(p.productId());
			if (serviceProduct == null)
				throw new ServiceException("Product with ID '%s' does not exists".formatted(p.productId()));
			return serviceProduct;
		});
	}

	private CompletableFuture<ProductOverview> overviewOf(CategoryProduct p) {
		CompletableFuture<ProductOverview> task = overviewCache.get(p.productId());

		if (task == null) {
			task = markDirtyOnFinish(serviceProductOf(p).thenCompose(Product::queryOverview));
			overviewCache.put(p.productId(), task);
		}

		return task;
	}

	private void updateFrame() {
		for (int i = 0; i < 9; i++) setSlot(i, StonksGuiElements.FRAME);
		for (int i = 0; i < 5; i++) setSlot(9 * (i + 1) + 1, StonksGuiElements.FRAME);

		setSlot(1, GuiElementBuilder.from(StonksGuiElements.BACK.getItemStack())
			.setCallback((index, type, action, gui) -> resolve(null))
			.build());

		setSlot(4, GuiElementBuilder.from(new ItemStack(Items.CHEST))
			.setItemName(Text.literal("View your offers").formatted(Formatting.YELLOW))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("View a list of offers that you've").formatted(Formatting.GRAY))
			.addLoreLine(Text.literal("placed, as well as claim and cancel").formatted(Formatting.GRAY))
			.addLoreLine(Text.literal("offers.").formatted(Formatting.GRAY))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.empty()
				.append(Text.literal("Click ").formatted(Formatting.YELLOW))
				.append("to view your offers")
				.formatted(Formatting.GRAY))
			.setCallback((index, type, action, gui) -> openAnotherMenu(new PlayerOffersMenu(player, instance)))
			.build());
	}

	private void updateCategories() {
		List<Category> page = PaginationUtils.paginate(categoryPage, 5, instance.getCatalog());

		for (int i = 0; i < page.size(); i++) {
			Category category = page.get(i);
			int slot = 9 + i * 9;

			if (category == null) {
				clearSlot(slot);
				continue;
			}

			setSlot(slot, GuiElementBuilder.from(category.icon())
				.glow(selectedCategory == category)
				.setCallback((index, type, action, gui) -> {
					if (selectedCategory != category) {
						selectedCategory = category;
						updateCategories();
					}

					productPage = 0;
					updateFrame();
					updateProducts();
				})
				.build());
		}
	}

	private void updateProducts() {
		List<CategoryProduct> page = PaginationUtils.paginate(productPage, 7 * 5, selectedCategory.products());

		for (int i = 0; i < page.size(); i++) {
			CategoryProduct product = page.get(i);
			int slot = (1 + i / 7) * 9 + (i % 7) + 2;

			if (product == null) {
				clearSlot(slot);
				continue;
			}

			setSlot(slot, StonksGuiElements.lazy(
				overviewOf(product),
				overview -> GuiElementBuilder.from(product.info().display())
					.addLoreLine(Text.empty())

					.addLoreLine(Text.literal("Instant buy price: ").formatted(Formatting.GRAY)
						.append(overview.sellOffers().topOffers().size() > 0
							? StonksTextUtils.currencyOf(overview.sellOffers().topOffers().get(0).price(), instance)
							: Text.literal("<n/a>").formatted(Formatting.RED)))
					.addLoreLine(Text.literal("Instant sell price: ").formatted(Formatting.GRAY)
						.append(overview.buyOffers().topOffers().size() > 0
							? StonksTextUtils.currencyOf(overview.buyOffers().topOffers().get(0).price(), instance)
							: Text.literal("<n/a>").formatted(Formatting.RED)))

					.addLoreLine(Text.literal("Avg. top buy offer: ").formatted(Formatting.GRAY)
						.append(overview.buyOffers().topOffers().size() > 0
							? StonksTextUtils.currencyOf(overview.buyOffers().averagePrice(), instance)
							: Text.literal("<n/a>").formatted(Formatting.RED)))
					.addLoreLine(Text.literal("Avg. top sell offer: ").formatted(Formatting.GRAY)
						.append(overview.sellOffers().topOffers().size() > 0
							? StonksTextUtils.currencyOf(overview.sellOffers().averagePrice(), instance)
							: Text.literal("<n/a>").formatted(Formatting.RED)))

					.addLoreLine(Text.empty())
					.addLoreLine(Text.empty().formatted(Formatting.GRAY)
						.append(Text.literal("Click").formatted(Formatting.YELLOW))
						.append(" to open this product"))

					.setCallback((index, type, action, gui) -> {
						Product serviceProduct = products.getNow(null).get(product.productId());
						ProductMenu menu = new ProductMenu(player, instance, product, serviceProduct);
						openAnotherMenu(menu).exceptionally(t -> {
							reject(t);
							return null;
						});
					})
					.build()));
		}
	}
}
