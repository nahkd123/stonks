package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record QueryUserOffersMessage(UUID uuid) implements Message {
	public static final BufferCodec<QueryUserOffersMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, QueryUserOffersMessage::uuid,
		QueryUserOffersMessage::new);
}
