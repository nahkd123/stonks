package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record OfferStatusMessage(UUID id, long filledUnits, long claimedUnits, boolean removed) implements Message {

	public static final BufferCodec<OfferStatusMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, OfferStatusMessage::id,
		BufferCodec.LONG, OfferStatusMessage::filledUnits,
		BufferCodec.LONG, OfferStatusMessage::claimedUnits,
		BufferCodec.BOOLEAN, OfferStatusMessage::removed,
		OfferStatusMessage::new);

	public OfferStatusMessage(UUID id, Offer.Status status) {
		this(id, status.filledUnits(), status.claimedUnits(), status.removed());
	}

	public Offer.Status status() {
		return new Offer.Status(filledUnits, claimedUnits, removed);
	}
}
