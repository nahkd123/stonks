package io.github.nahkd123.stonks.service.provided.remote;

import io.github.nahkd123.stonks.service.provided.remote.message.CatalogMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SimpleQueryMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SpecialMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.CancelOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.ClaimOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferClaimResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferStatusMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.PlaceOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferStatusMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryUserOffersMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.UserOffersMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantBuyMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantOfferResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantSellMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.ProductOverviewMessage;
import io.github.nahkd123.stonks.utils.net.MessageConnection;

public abstract class RemoteServiceConnection extends MessageConnection {
	public RemoteServiceConnection() {
		registerMessageType(0, SpecialMessage.class, SpecialMessage.CODEC);
		registerMessageType(1, SimpleQueryMessage.class, SimpleQueryMessage.CODEC);
		registerMessageType(2, CatalogMessage.class, CatalogMessage.CODEC);

		registerMessageType(100, QueryProductOverviewMessage.class, QueryProductOverviewMessage.CODEC);
		registerMessageType(101, ProductOverviewMessage.class, ProductOverviewMessage.CODEC);
		registerMessageType(102, InstantBuyMessage.class, InstantBuyMessage.CODEC);
		registerMessageType(103, InstantSellMessage.class, InstantSellMessage.CODEC);
		registerMessageType(104, InstantOfferResultMessage.class, InstantOfferResultMessage.CODEC);

		registerMessageType(200, PlaceOfferMessage.class, PlaceOfferMessage.CODEC);
		registerMessageType(201, QueryOfferMessage.class, QueryOfferMessage.CODEC);
		registerMessageType(202, OfferMessage.class, OfferMessage.CODEC);
		registerMessageType(203, QueryUserOffersMessage.class, QueryUserOffersMessage.CODEC);
		registerMessageType(204, UserOffersMessage.class, UserOffersMessage.CODEC);
		registerMessageType(205, QueryOfferStatusMessage.class, QueryOfferStatusMessage.CODEC);
		registerMessageType(206, OfferStatusMessage.class, OfferStatusMessage.CODEC);
		registerMessageType(207, ClaimOfferMessage.class, ClaimOfferMessage.CODEC);
		registerMessageType(208, CancelOfferMessage.class, CancelOfferMessage.CODEC);
		registerMessageType(209, OfferClaimResultMessage.class, OfferClaimResultMessage.CODEC);
	}
}
