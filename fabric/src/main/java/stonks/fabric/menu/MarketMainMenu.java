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
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.handling.WaitableGuiElement;
import stonks.fabric.menu.product.ProductMenu;

public class MarketMainMenu extends StackedMenu {
	private int selectedCategoryIndex = 0;

	public MarketMainMenu(StackedMenu previous, ServerPlayerEntity player) {
		super(previous, ScreenHandlerType.GENERIC_9X6, player, false);
		setTitle(Text.literal("Market"));

		for (int i = 0; i < getHeight() - 1; i++) {
			var slot = (i + 1) * getWidth();
			setSlot(slot, WaitableGuiElement.ANIMATED_LOADING);
		}

		var cx = (getWidth() - 2) / 2;
		var cy = (getHeight() - 1) / 2;
		setSlot(2 + cx + (1 + cy) * getWidth(), WaitableGuiElement.ANIMATED_LOADING);

		getGuiTasksHandler().handle(StonksFabric.getServiceProvider(player).getStonksCache().getAllCategories(),
			(categories, error) -> {
				if (error != null) {
					var icon = new GuiElementBuilder(Items.BARRIER)
						.setName(Text.literal("An error occured").styled(s -> s.withColor(Formatting.RED)))
						.addLoreLine(Text.literal("Failed to get categories list")
							.styled(s -> s.withColor(Formatting.GRAY)));

					for (int i = 0; i < getHeight() - 1; i++) {
						var slot = (i + 1) * getWidth();
						setSlot(slot, icon);
					}

					setSlot(2 + cx + (1 + cy) * getWidth(), icon);
					error.printStackTrace();
					return;
				}

				refresh(categories);
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

	private void refresh(List<Category> categories) {
		placeCategories(categories);
		placeCategory(categories.get(selectedCategoryIndex));
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
						selectedCategoryIndex = i2;
						refresh(categories);
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

		setSlot(slot, new WaitableGuiElement<>(cache.getOverview(product).get()) {
			@Override
			public ItemStack createStackWhenLoaded(ProductMarketOverview overview, Throwable error) {
				if (error != null) return new GuiElementBuilder(Items.BARRIER)
					.setName(Text.literal("An error occured!").styled(s -> s.withColor(Formatting.RED)))
					// TODO use message from UserException
					.addLoreLine(Text.literal("Failed to query quick price details")
						.styled(s -> s.withColor(Formatting.GRAY)))
					.asStack();

				// TODO should we use avg price?
				var instantBuyPrice = overview.getSellOffers().compute().map(v -> v.min());
				var instantSellPrice = overview.getBuyOffers().compute().map(v -> v.min());

				return GuiElementBuilder.from(dispStack)
					.setLore(new ArrayList<>()) // Clear all lore
					.addLoreLine(Text.literal(category.getCategoryName())
						.styled(s -> s.withItalic(false).withColor(Formatting.DARK_GRAY)))
					.addLoreLine(Text.empty())
					.addLoreLine(Text.literal("Instant Buy: ")
						.styled(s -> s.withColor(Formatting.GRAY))
						.append(StonksFabricUtils.currencyText(instantBuyPrice, true)))
					.addLoreLine(Text.literal("Instant Sell: ")
						.styled(s -> s.withColor(Formatting.GRAY))
						.append(StonksFabricUtils.currencyText(instantSellPrice, true)))
					.addLoreLine(Text.empty())
					.addLoreLine(Text.literal("Click").styled(s -> s.withColor(Formatting.YELLOW))
						.append(" to open product info").styled(s -> s.withColor(Formatting.GRAY)))
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
