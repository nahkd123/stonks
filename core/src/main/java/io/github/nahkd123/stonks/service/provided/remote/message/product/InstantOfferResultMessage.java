package io.github.nahkd123.stonks.service.provided.remote.message.product;

import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record InstantOfferResultMessage(long first, long second) implements Message {
	public static final BufferCodec<InstantOfferResultMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.LONG, InstantOfferResultMessage::first,
		BufferCodec.LONG, InstantOfferResultMessage::second,
		InstantOfferResultMessage::new);

	public static InstantOfferResultMessage from(Product.InstantBuyResult result) {
		return new InstantOfferResultMessage(result.bought(), result.leftoverBalance());
	}

	public static InstantOfferResultMessage from(Product.InstantSellResult result) {
		return new InstantOfferResultMessage(result.earning(), result.leftoverUnits());
	}

	public Product.InstantBuyResult instantBuy() {
		return new Product.InstantBuyResult(this.first, this.second);
	}

	public Product.InstantSellResult instantSell() {
		return new Product.InstantSellResult(this.first, this.second);
	}
}
