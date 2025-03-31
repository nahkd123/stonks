package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record CancelOfferMessage(UUID id) implements Message {
	public static final BufferCodec<CancelOfferMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, CancelOfferMessage::id,
		CancelOfferMessage::new);
}
