package io.github.nahkd123.stonks.service.provided.remote.packet.product;

import java.util.UUID;

import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.RemoteOfferData;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record PlaceOfferRequest(String productId, UUID owner, OfferType type, long price, long units) {

	public static final BufferCodec<PlaceOfferRequest> CODEC = BufferCodec.tupleOf(
		BufferCodec.UTF8, PlaceOfferRequest::productId,
		StonksBufferCodecs.UUID, PlaceOfferRequest::owner,
		StonksBufferCodecs.OFFER_TYPE, PlaceOfferRequest::type,
		BufferCodec.I64, PlaceOfferRequest::price,
		BufferCodec.I64, PlaceOfferRequest::units,
		PlaceOfferRequest::new);

	public static record Response(RemoteOfferData offer) {
		public static final BufferCodec<Response> CODEC = RemoteOfferData.CODEC.map(Response::new, Response::offer);
	}
}
