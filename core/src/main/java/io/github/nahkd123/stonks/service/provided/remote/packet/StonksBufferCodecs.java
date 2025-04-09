package io.github.nahkd123.stonks.service.provided.remote.packet;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public interface StonksBufferCodecs {
	BufferCodec<java.util.UUID> UUID = BufferCodec.I64.asSequence(2).map(
		list -> new UUID(list.get(0), list.get(1)),
		uuid -> List.of(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));

	BufferCodec<Double> F64 = BufferCodec.of((v, b) -> b.putDouble(v), ByteBuffer::getDouble);
	BufferCodec<Boolean> BOOL = BufferCodec.I8.map(v -> v != 0, b -> b ? (byte) 1 : 0);

	BufferCodec<SlippageOption> SLIPPAGE = BufferCodec.tupleOf(
		BufferCodec.I64, SlippageOption::targetPrice,
		F64, SlippageOption::maxRate,
		SlippageOption::new);

	BufferCodec<OfferType> OFFER_TYPE = BufferCodec.I8.map(
		v -> OfferType.values()[v & 0xFF],
		o -> (byte) o.ordinal());

	BufferCodec<Offer.Status> OFFER_STATUS = BufferCodec.tupleOf(
		BufferCodec.I64, Offer.Status::filledUnits,
		BufferCodec.I64, Offer.Status::claimedUnits,
		BOOL, Offer.Status::removed,
		Offer.Status::new);

	BufferCodec<Offer.ClaimResult> OFFER_CLAIM = BufferCodec.tupleOf(
		BufferCodec.I64, Offer.ClaimResult::claimedUnits,
		BOOL, Offer.ClaimResult::remove,
		Offer.ClaimResult::new);
}
