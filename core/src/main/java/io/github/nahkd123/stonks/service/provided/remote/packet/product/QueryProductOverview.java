package io.github.nahkd123.stonks.service.provided.remote.packet.product;

import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record QueryProductOverview(String productId) {
	public static final BufferCodec<QueryProductOverview> CODEC = BufferCodec.UTF8.map(
		QueryProductOverview::new,
		QueryProductOverview::productId);

	private static final BufferCodec<ProductOffersOverview> RES_OFFERS = BufferCodec.tupleOf(
		StonksBufferCodecs.OFFER_TYPE, ProductOffersOverview::type,
		BufferCodec.I64, ProductOffersOverview::averagePrice,
		BufferCodec.tupleOf(
			BufferCodec.I64, OfferOverviewEntry::price,
			BufferCodec.I64, OfferOverviewEntry::units,
			OfferOverviewEntry::new).asVarSequence(BufferCodec.VUINT),
		ProductOffersOverview::topOffers,
		ProductOffersOverview::new);

	public static final BufferCodec<ProductOverview> RESPONSE = BufferCodec.tupleOf(
		RES_OFFERS, ProductOverview::buyOffers,
		RES_OFFERS, ProductOverview::sellOffers,
		ProductOverview::new);
}
