package stonks.fabric.menu.product;

import java.util.Optional;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import nahara.common.tasks.Task;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
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

public class ProductMenu extends StackedMenu {
	private Product product;
	private Task<ProductMarketOverview> queryTask;
	private boolean infoPlaced = false;

	public ProductMenu(StackedMenu previous, ServerPlayerEntity player, Product product) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Text.literal("Market > " + product.getProductName()));
		this.product = product;
		this.queryTask = StonksFabric.getServiceProvider(getPlayer()).getStonksCache().getOverview(product).get();

		setSlot(22, GuiElementBuilder.from(StonksFabric.getDisplayStack(StonksFabric
			.getServiceProvider(player)
			.getStonksAdapter(), product)));
	}

	public Product getProduct() { return product; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	@Override
	public void onTick() {
		super.onTick();

		if (queryTask.get().isEmpty()) {
			placeLoadingSpinner(19);
			placeLoadingSpinner(20);
			placeLoadingSpinner(24);
			placeLoadingSpinner(25);
		} else if (!infoPlaced) {
			var info = queryTask.get().get().getSuccess();
			var computedBuyOffers = info.getBuyOffers().compute();
			var computedSellOffers = info.getSellOffers().compute();

			setSlot(19, createInstantOfferButton(OfferType.BUY, computedSellOffers));
			setSlot(20, createInstantOfferButton(OfferType.SELL, computedBuyOffers));
			setSlot(24, createOfferButton(info.getBuyOffers(), OfferType.BUY, computedBuyOffers));
			setSlot(25, createOfferButton(info.getSellOffers(), OfferType.SELL, computedSellOffers));
			infoPlaced = true;
		}
	}

	private GuiElementBuilder createInstantOfferButton(OfferType type, Optional<ComputedOffersList> computed) {
		return new GuiElementBuilder(switch (type) {
		case BUY -> Items.DIAMOND;
		case SELL -> Items.GOLD_INGOT;
		})
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
			.setCallback((index, clickType, action, gui) -> {
				if (computed.isEmpty()) return;

				if (type == OfferType.BUY) {
					var avgPrice = computed.get().average();
					var bonus = avgPrice * 0.01d;
					new InstantBuyMenu(this, player, product, avgPrice, avgPrice + bonus).open();
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
			});
	}

	private GuiElementBuilder createOfferButton(OverviewOffersList list, OfferType type, Optional<ComputedOffersList> computed) {
		var elem = new GuiElementBuilder(switch (type) {
		case BUY -> Items.DIAMOND_BLOCK;
		case SELL -> Items.GOLD_BLOCK;
		})
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
				? Text.literal("Click to become the first person to get rich").styled(s -> s.withColor(Formatting.GRAY))
				: Text.literal("Click to make offer").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, clickType, action, gui) -> {
				var now = queryTask.get();
				if (now.isPresent()) {
					new OfferAmountConfigureMenu(this, player, product, type, now.get().getSuccess()).open();
					return;
				}

				close();
				queryTask.afterThatDo(overview -> {
					new OfferAmountConfigureMenu(this, player, product, type, overview).open();
				});
			});
	}
}
