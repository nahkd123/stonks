package io.github.nahkd123.stonks.service.provided.remote.packet.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record CancelOfferRequest(UUID id) {
	public static final BufferCodec<CancelOfferRequest> CODEC = StonksBufferCodecs.UUID.map(CancelOfferRequest::new, CancelOfferRequest::id);
}
