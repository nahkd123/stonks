package io.github.nahkd123.stonks.mc.fabric.gui;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.gui.tasked.TaskedMenu;
import io.github.nahkd123.stonks.mc.fabric.product.CategoryProduct;
import io.github.nahkd123.stonks.mc.fabric.utils.StonksTextUtils;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ProductMenu extends TaskedMenu<Void> {
	private static final DecimalFormat PERCENTAGE_FORMATTER = new DecimalFormat("#,##0.##%");
	private static final DecimalFormat UNIT_FORMATTER = new DecimalFormat("#,##0");
	private StonksMcInstance instance;
	private CategoryProduct product;
	private Product service;

	private CompletableFuture<ProductOverview> overview;

	public ProductMenu(ServerPlayerEntity player, StonksMcInstance instance, CategoryProduct product, Product service) {
		super(ScreenHandlerType.GENERIC_9X4, player, false);
		this.instance = instance;
		this.product = product;
		this.service = service;
		setTitle(Text.literal("Market > ").append(product.info().display().getFormattedName().getString()));

		overview = markDirtyOnFinish(service.queryOverview());

		onUpdate();
	}

	@Override
	protected void onUpdate() {
		updateFrame();

		setSlot(19, StonksGuiElements.lazy(overview, o -> builderForInsta(o.sellOffers())
			.addLoreLine(Text.empty().formatted(Formatting.GRAY)
				.append(Text.literal("Click").formatted(Formatting.YELLOW))
				.append(" for instant buy options"))
			.build()));

		setSlot(20, StonksGuiElements.lazy(overview, o -> builderForInsta(o.buyOffers())
			.addLoreLine(Text.empty().formatted(Formatting.GRAY)
				.append(Text.literal("Left click").formatted(Formatting.YELLOW))
				.append(" to sell everything"))
			.addLoreLine(Text.empty().formatted(Formatting.GRAY)
				.append(Text.literal("Right click").formatted(Formatting.YELLOW))
				.append(" for instant sell options"))
			.build()));

		setSlot(22, product.info().display());

		setSlot(24, StonksGuiElements.lazy(overview, o -> builderForOffer(o.buyOffers())
			.addLoreLine(Text.empty().formatted(Formatting.GRAY)
				.append(Text.literal("Click").formatted(Formatting.YELLOW))
				.append(" to place buy offer"))
			.build()));

		setSlot(25, StonksGuiElements.lazy(overview, o -> builderForOffer(o.sellOffers())
			.addLoreLine(Text.empty().formatted(Formatting.GRAY)
				.append(Text.literal("Click").formatted(Formatting.YELLOW))
				.append(" to place sell offer"))
			.build()));
	}

	private void updateFrame() {
		for (int i = 0; i < 9; i++) setSlot(i, StonksGuiElements.FRAME);

		setSlot(1, GuiElementBuilder.from(StonksGuiElements.BACK.getItemStack())
			.setCallback((index, type, action, gui) -> resolve(null))
			.build());
	}

	private GuiElementBuilder builderForInsta(ProductOffersOverview overview) {
		// Insta types are reversed of offers types
		OfferType type = overview.type();
		GuiElementBuilder builder = GuiElementBuilder
			.from(new ItemStack(type == OfferType.BUY ? Items.DIAMOND : Items.GOLD_INGOT))
			.setItemName(Text.literal(type == OfferType.BUY ? "Perform instant sell" : "Perform instant buy")
				.formatted(Formatting.YELLOW))
			.addLoreLine(Text.empty())
			.addLoreLine(overview.topOffers().size() > 0
				? Text
					.literal("Instant " + (type == OfferType.BUY ? "sell" : "buy") + " price: ")
					.formatted(Formatting.GRAY)
					.append(StonksTextUtils.currencyOf(overview.topOffers().get(0).price(), instance))
				: Text
					.literal("No one is " + (type == OfferType.BUY ? "buying" : "selling") + " this product :(")
					.formatted(Formatting.RED));

		if (type == OfferType.BUY) {
			long inv = product.info().getInventory(player);
			builder.addLoreLine(Text.literal("Your inventory: ").formatted(Formatting.GRAY)
				.append(Text.literal(UNIT_FORMATTER.format(inv)).formatted(Formatting.YELLOW))
				.append(" units"));
		}

		if (type == OfferType.BUY && instance.getTax() > 0d) builder.addLoreLine(taxText());
		return builder.addLoreLine(Text.empty());
	}

	private GuiElementBuilder builderForOffer(ProductOffersOverview overview) {
		OfferType type = overview.type();
		GuiElementBuilder builder = GuiElementBuilder
			.from(new ItemStack(type == OfferType.BUY ? Items.GOLD_BLOCK : Items.DIAMOND_BLOCK))
			.setItemName(Text.literal(type == OfferType.BUY ? "Create buy offer" : "Create sell offer")
				.formatted(Formatting.YELLOW))
			.addLoreLine(Text.empty());

		if (overview.topOffers().size() > 0) {
			overview.topOffers().stream().map(top -> Text.empty()
				.formatted(Formatting.GRAY)
				.append(StonksTextUtils.topOfferOf(type, top, instance.getEconomyService())))
				.forEach(builder::addLoreLine);
		} else {
			builder.addLoreLine(Text
				.literal("No one is " + (type == OfferType.BUY ? "buying" : "selling") + " this product :(")
				.formatted(Formatting.RED));
		}

		if (type == OfferType.SELL && instance.getTax() > 0d) builder
			.addLoreLine(Text.empty())
			.addLoreLine(taxText());
		return builder.addLoreLine(Text.empty());
	}

	private MutableText taxText() {
		return Text.literal("Income tax: ").formatted(Formatting.GRAY)
			.append(Text.literal(PERCENTAGE_FORMATTER.format(instance.getTax())).formatted(Formatting.YELLOW));
	}
}
