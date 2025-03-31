package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record ClaimOfferMessage(UUID id) implements Message {
	public static final BufferCodec<ClaimOfferMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, ClaimOfferMessage::id,
		ClaimOfferMessage::new);
}
