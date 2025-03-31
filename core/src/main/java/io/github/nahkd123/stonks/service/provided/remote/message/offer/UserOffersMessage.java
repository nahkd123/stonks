package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record UserOffersMessage(UUID uuid, Set<OfferMessage> offers) implements Message {
	public static final BufferCodec<UserOffersMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, UserOffersMessage::uuid,
		OfferMessage.CODEC.asList().map(Set::copyOf, List::copyOf), UserOffersMessage::offers,
		UserOffersMessage::new);
}
