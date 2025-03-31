package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record OfferClaimResultMessage(UUID id, long claimedUnits, boolean remove) implements Message {

	public static final BufferCodec<OfferClaimResultMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.UUID, OfferClaimResultMessage::id,
		BufferCodec.LONG, OfferClaimResultMessage::claimedUnits,
		BufferCodec.BOOLEAN, OfferClaimResultMessage::remove,
		OfferClaimResultMessage::new);

	public OfferClaimResultMessage(UUID id, Offer.ClaimResult result) {
		this(id, result.claimedUnits(), result.remove());
	}

	public Offer.ClaimResult result() {
		return new Offer.ClaimResult(claimedUnits, remove);
	}
}
