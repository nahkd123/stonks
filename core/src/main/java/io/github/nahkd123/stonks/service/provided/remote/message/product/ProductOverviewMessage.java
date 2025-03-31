package io.github.nahkd123.stonks.service.provided.remote.message.product;

import java.util.List;

import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record ProductOverviewMessage(TopOffers buy, TopOffers sell) implements Message {
	public static final BufferCodec<ProductOverviewMessage> CODEC = BufferCodec.ofTuple(
		TopOffers.CODEC, ProductOverviewMessage::buy,
		TopOffers.CODEC, ProductOverviewMessage::sell,
		ProductOverviewMessage::new);

	public static record TopOffers(long averagePrice, List<TopOffer> list) {
		public static final BufferCodec<TopOffers> CODEC = BufferCodec.ofTuple(
			BufferCodec.LONG, TopOffers::averagePrice,
			TopOffer.CODEC.asList(), TopOffers::list,
			TopOffers::new);

		public TopOffers(ProductOffersOverview offers) {
			this(offers.averagePrice(), offers.topOffers().stream().map(TopOffer::new).toList());
		}

		public ProductOffersOverview offers(OfferType type) {
			return new ProductOffersOverview(type, averagePrice, list.stream().map(TopOffer::entry).toList());
		}
	}

	public static record TopOffer(long price, long units) {
		public static final BufferCodec<TopOffer> CODEC = BufferCodec.ofTuple(
			BufferCodec.LONG, TopOffer::price,
			BufferCodec.LONG, TopOffer::units,
			TopOffer::new);

		public TopOffer(OfferOverviewEntry entry) {
			this(entry.price(), entry.units());
		}

		public OfferOverviewEntry entry() {
			return new OfferOverviewEntry(price, units);
		}
	}

	public ProductOverviewMessage(ProductOverview overview) {
		this(new TopOffers(overview.buyOffers()), new TopOffers(overview.sellOffers()));
	}

	public ProductOverview overview() {
		return new ProductOverview(buy.offers(OfferType.BUY), sell.offers(OfferType.SELL));
	}
}
