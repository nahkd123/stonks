package io.github.nahkd123.stonks.service.provided.remote.packet.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record ClaimOfferRequest(UUID id) {
	public static final BufferCodec<ClaimOfferRequest> CODEC = StonksBufferCodecs.UUID.map(ClaimOfferRequest::new, ClaimOfferRequest::id);
}
