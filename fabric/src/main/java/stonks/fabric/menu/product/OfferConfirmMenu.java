package stonks.fabric.menu.product;

import java.util.ArrayList;
import java.util.Optional;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricHelper;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.MenuIcons;
import stonks.fabric.menu.StackedMenu;

public class OfferConfirmMenu extends StackedMenu {
	public OfferConfirmMenu(StackedMenu previous, ServerPlayerEntity player, Product product, OfferType offerType, int amount, double pricePerUnit) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		setTitle(Text.literal("Confirm offer"));

		setSlot(7, GuiElementBuilder.from(StonksFabric.getServiceProvider(getPlayer())
			.getStonksAdapter()
			.createDisplayStack(product))
			.setCount(Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal(amount + "x " + product.getProductName())
				.styled(s -> s.withColor(Formatting.AQUA)))
			.setLore(new ArrayList<>()));

		setSlot(22, new GuiElementBuilder(Items.GREEN_TERRACOTTA)
			.setName(Text.literal("Confirm " + offerType.toString().toLowerCase() + " offer")
				.styled(s -> s.withColor(Formatting.GREEN)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(switch (offerType) {
			case BUY -> "Buying ";
			case SELL -> "Selling ";
			} + amount + "x " + product.getProductName())
				.styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.literal("Price per unit: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(pricePerUnit), true)))
			.addLoreLine(Text.literal("Total: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(pricePerUnit * amount), true)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Click to confirm").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> {
				close();
				StonksFabricHelper.placeOffer(player, product, offerType, amount, pricePerUnit);
			}));
	}

	@Override
	protected void placeButtons() {
		super.placeButtons();
		setSlot(3, MenuIcons.MAIN_MENU);
		setSlot(5, MenuIcons.VIEW_SELF_OFFERS);
	}
}
