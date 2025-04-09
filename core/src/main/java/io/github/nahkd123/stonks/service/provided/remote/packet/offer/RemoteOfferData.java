package io.github.nahkd123.stonks.service.provided.remote.packet.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record RemoteOfferData(UUID id, UUID owner, OfferType type, String productId, long price, long totalUnits) {

	public static final BufferCodec<RemoteOfferData> CODEC = BufferCodec.tupleOf(
		StonksBufferCodecs.UUID, RemoteOfferData::id,
		StonksBufferCodecs.UUID, RemoteOfferData::owner,
		StonksBufferCodecs.OFFER_TYPE, RemoteOfferData::type,
		BufferCodec.UTF8, RemoteOfferData::productId,
		BufferCodec.I64, RemoteOfferData::price,
		BufferCodec.I64, RemoteOfferData::totalUnits,
		RemoteOfferData::new);

	public static RemoteOfferData deriveFrom(Offer offer) {
		UUID id = offer.id();
		UUID owner = offer.owner();
		OfferType type = offer.type();
		String productId = offer.product().getId();
		long price = offer.price();
		long totalUnits = offer.totalUnits();
		return new RemoteOfferData(id, owner, type, productId, price, totalUnits);
	}
}
