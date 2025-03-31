package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record OfferMessage(UUID id, UUID owner, OfferType type, String productId, long price, long totalUnits) implements Message {

	public static final BufferCodec<OfferMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, OfferMessage::id,
		BufferCodec.UUID, OfferMessage::owner,
		OfferType.CODEC, OfferMessage::type,
		BufferCodec.ID, OfferMessage::productId,
		BufferCodec.LONG, OfferMessage::price,
		BufferCodec.LONG, OfferMessage::totalUnits,
		OfferMessage::new);

	public OfferMessage(Offer offer) {
		this(offer.id(), offer.owner(), offer.type(), offer.product().getId(), offer.price(), offer.totalUnits());
	}
}
