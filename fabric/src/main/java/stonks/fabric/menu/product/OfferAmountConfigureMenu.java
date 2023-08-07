package stonks.fabric.menu.product;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferSelectCustomAmountInput;

public class OfferAmountConfigureMenu extends StackedMenu {
	private Product product;
	private OfferType offerType;
	private ProductMarketOverview overview;

	public OfferAmountConfigureMenu(StackedMenu previous, ServerPlayerEntity player, Product product, OfferType offerType, ProductMarketOverview overview) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		this.product = product;
		this.offerType = offerType;
		this.overview = overview;

		setTitle(Text.literal("Market > " + product.getProductName() + " > " + switch (offerType) {
		case BUY -> "Buy";
		case SELL -> "Sell";
		} + " offer"));

		var icons = switch (offerType) {
		case BUY -> new Item[] { Items.IRON_INGOT, Items.DIAMOND, Items.DIAMOND_BLOCK };
		case SELL -> new Item[] { Items.GOLD_NUGGET, Items.GOLD_INGOT, Items.GOLD_BLOCK };
		};

		setSlot(7, StonksFabric.getServiceProvider(getPlayer()).getStonksAdapter().createDisplayStack(product));

		setSlot(19, createOfferSelectButton(64, icons[0]));
		setSlot(20, createOfferSelectButton(256, icons[1]));
		setSlot(21, createOfferSelectButton(512, icons[1]));
		setSlot(22, createOfferSelectButton(1024, icons[1]));
		setSlot(23, createOfferSelectButton(offerType == OfferType.BUY ? 4096 : -1, icons[2]));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Text.literal("Custom amount").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Click to specify amount").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> new OfferSelectCustomAmountInput(player, this).open()));
	}

	public Product getProduct() { return product; }

	public OfferType getOfferType() { return offerType; }

	public ProductMarketOverview getOverview() { return overview; }

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}

	private GuiElementBuilder createOfferSelectButton(int amount, Item icon) {
		// Only for selling
		var fillAll = amount == -1;
		var currentUnits = StonksFabric.getServiceProvider(getPlayer()).getStonksAdapter().getUnits(player, product);
		if (fillAll) amount = currentUnits;
		var disabled = offerType == OfferType.SELL && (amount == 0 || currentUnits < amount);
		var amount2 = amount;

		return new GuiElementBuilder(disabled ? Items.BARRIER : icon, Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal(switch (offerType) {
			case BUY -> "Buy";
			case SELL -> "Sell";
			} + (fillAll ? " everything!" : (" x" + amount)))
				.styled(s -> s.withColor(disabled ? Formatting.RED : Formatting.YELLOW)))
			.addLoreLine(Text.literal(amount + "x " + product.getProductName())
				.styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(disabled
				? Text.literal("Can't make offer").styled(s -> s.withColor(Formatting.RED))
				: Text.literal("Click to configure offer pricing").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> {
				if (disabled) return;
				new OfferPriceConfigureMenu(this, getPlayer(), getOfferType(), amount2, getOverview()).open();
			});
	}
}
