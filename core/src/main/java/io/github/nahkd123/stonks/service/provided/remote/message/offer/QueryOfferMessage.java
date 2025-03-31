package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record QueryOfferMessage(UUID id) implements Message {
	public static final BufferCodec<QueryOfferMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, QueryOfferMessage::id,
		QueryOfferMessage::new);
}
