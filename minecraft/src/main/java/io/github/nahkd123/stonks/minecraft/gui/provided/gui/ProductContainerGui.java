package io.github.nahkd123.stonks.minecraft.gui.provided.gui;

import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;

public class ProductContainerGui extends AbstractContainerGui {
	private Product product;

	public ProductContainerGui(ContainerGui previous, MinecraftServer server, Product product) {
		super(previous, server, "product");
		this.product = product;
	}

	public Product getProduct() { return product; }

	@Override
	public TextComponent replacePlaceholder(TextFactory factory, String name) {
		return switch (name) {
		case "product.id" -> factory.literal(product.getId());
		case "product.name" -> factory.literal(product.getDisplayName());
		default -> null;
		};
	}
}
