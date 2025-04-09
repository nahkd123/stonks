package io.github.nahkd123.stonks.service.provided.remote.packet.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record QueryOffer(UUID id) {
	public static final BufferCodec<QueryOffer> CODEC = StonksBufferCodecs.UUID.map(QueryOffer::new, QueryOffer::id);
}
