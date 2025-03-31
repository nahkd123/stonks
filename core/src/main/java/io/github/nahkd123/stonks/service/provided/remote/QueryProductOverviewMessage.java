package io.github.nahkd123.stonks.service.provided.remote;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record QueryProductOverviewMessage(String productId) implements Message {
	public static final BufferCodec<QueryProductOverviewMessage> CODEC = BufferCodec.ID.map(
		QueryProductOverviewMessage::new,
		QueryProductOverviewMessage::productId);
}
