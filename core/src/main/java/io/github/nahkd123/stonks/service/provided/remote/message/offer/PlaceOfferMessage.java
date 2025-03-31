package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record PlaceOfferMessage(String productId, UUID owner, OfferType type, long price, long units) implements Message {
	public static final BufferCodec<PlaceOfferMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.ID, PlaceOfferMessage::productId,
		BufferCodec.UUID, PlaceOfferMessage::owner,
		OfferType.CODEC, PlaceOfferMessage::type,
		BufferCodec.LONG, PlaceOfferMessage::price,
		BufferCodec.LONG, PlaceOfferMessage::units,
		PlaceOfferMessage::new);
}
