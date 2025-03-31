package io.github.nahkd123.stonks.service.provided.remote.message;

import java.util.List;
import java.util.Set;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record CatalogMessage(Set<Product> products) implements Message {
	public static record Product(String id) {
		public static final BufferCodec<Product> CODEC = BufferCodec.ID.map(Product::new, Product::id);
	}

	public static final BufferCodec<CatalogMessage> CODEC = Product.CODEC
		.asList()
		.map(list -> new CatalogMessage(Set.copyOf(list)), msg -> List.copyOf(msg.products));
}
