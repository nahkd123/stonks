package io.github.nahkd123.stonks.service.provided.remote.packet.offer;

import java.util.List;
import java.util.UUID;

import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record QueryUserOffers(UUID uuid) {
	public static final BufferCodec<QueryUserOffers> CODEC = StonksBufferCodecs.UUID.map(
		QueryUserOffers::new,
		QueryUserOffers::uuid);

	public static record Response(List<RemoteOfferData> offers) {
		public static final BufferCodec<Response> CODEC = RemoteOfferData.CODEC
			.asVarSequence(BufferCodec.VUINT)
			.map(Response::new, Response::offers);
	}
}
