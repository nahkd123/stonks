package stonks.fabric.menu;

import java.util.ArrayList;
import java.util.List;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import nahara.common.tasks.Task;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.caching.StonksServiceCache;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.product.ProductMenu;

public class MarketMainMenu extends StackedMenu {
	private Task<List<Category>> queryTask;
	private boolean categoriesPlaced = false;
	private int selectedCategoryIndex = 0;
	private boolean disableCategorySwitching = false;

	public MarketMainMenu(StackedMenu previous, ServerPlayerEntity player) {
		super(previous, ScreenHandlerType.GENERIC_9X6, player, false);
		setTitle(Text.literal("Market"));
		queryTask = StonksFabric.getServiceProvider(player).getStonksCache().getAllCategories();
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

	@Override
	public void onTick() {
		super.onTick();

		if (queryTask.get().isEmpty()) {
			for (int i = 0; i < getHeight() - 1; i++) {
				var slot = (i + 1) * getWidth();
				placeLoadingSpinner(slot);
			}

			var cx = (getWidth() - 2) / 2;
			var cy = (getHeight() - 1) / 2;
			placeLoadingSpinner(2 + cx + (1 + cy) * getWidth());
		} else if (!categoriesPlaced) {
			refresh();
			categoriesPlaced = true;
		}
	}

	private void refresh() {
		placeCategories(queryTask.get().get().getSuccess());
		placeCategory(queryTask.get().get().getSuccess().get(selectedCategoryIndex));
	}

	private void placeCategories(List<Category> categories) {
		for (int i = 0; i < getHeight() - 1; i++) {
			var i2 = i;
			var slot = (i + 1) * getWidth();
			if (i >= categories.size()) clearSlot(slot);
			else {
				var category = categories.get(i);
				var selected = i == selectedCategoryIndex;

				var a = new GuiElementBuilder(Items.PAPER)
					.setName(Text.literal(category.getCategoryName()).styled(s -> s.withColor(Formatting.AQUA)))
					.addLoreLine(Text.literal(selected ? "Selected" : "Click to open")
						.styled(s -> s.withColor(Formatting.GRAY)))
					.setCallback((index, type, action, gui) -> {
						if (selected || disableCategorySwitching) return;
						selectedCategoryIndex = i2;
						refresh();
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
				var i = x + y * (getWidth() - 2);

				if (i >= category.getProducts().size()) {
					clearSlot(slot);
				} else {
					var product = category.getProducts().get(i);
					placeItem(slot, cache, category, product);
				}
			}
		}
	}

	private void placeItem(int slot, StonksServiceCache cache, Category category, Product product) {
		var dispStack = StonksFabric.getDisplayStack(StonksFabric
			.getServiceProvider(getPlayer())
			.getStonksAdapter(), product);

		var overview = cache.getOverview(product).get();
		var loading = overview.get().isEmpty();
		var loadingText = Text.literal("Loading...").styled(s -> s.withColor(Formatting.GRAY));

		var instantBuyPrice = overview.get()
			.flatMap(v -> v.getSuccess().getSellOffers().compute())
			.map(v -> v.min()); // TODO should we use avg price?
		var instantSellPrice = overview.get()
			.flatMap(v -> v.getSuccess().getBuyOffers().compute())
			.map(v -> v.min());

		setSlot(slot, GuiElementBuilder.from(dispStack)
			.setLore(new ArrayList<>()) // Clear all lore
			.addLoreLine(Text.literal(category.getCategoryName())
				.styled(s -> s.withItalic(false).withColor(Formatting.DARK_GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Instant Buy: ")
				.styled(s -> s.withItalic(false).withColor(Formatting.GRAY))
				.append(loading
					? loadingText
					: StonksFabricUtils.currencyText(instantBuyPrice, true)))
			.addLoreLine(Text.literal("Instant Sell: ")
				.styled(s -> s.withItalic(false).withColor(Formatting.GRAY))
				.append(loading
					? loadingText
					: StonksFabricUtils.currencyText(instantSellPrice, true)))
			.setCallback((index, type, action, gui) -> new ProductMenu(this, player, product).open()));

		if (overview.get().isEmpty()) {
			disableCategorySwitching = true;

			overview.afterThatDo($ -> {
				disableCategorySwitching = false;
				placeItem(slot, cache, category, product);
			});
		}
	}
}
