package io.github.nahkd123.stonks.service.provided.remote;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.provided.remote.message.CatalogMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.PlaceOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.SlippageMessageData;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantBuyMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantOfferResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantSellMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.ProductOverviewMessage;

record RemoteProduct(RemoteMarketService service, CatalogMessage.Product id) implements Product {
	@Override
	public String getId() { return id.id(); }

	@Override
	public CompletableFuture<ProductOverview> queryOverview() {
		return service.request(new QueryProductOverviewMessage(getId()))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((ProductOverviewMessage) message).overview());
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		Optional<SlippageMessageData> slippageData = slippage != null
			? Optional.of(new SlippageMessageData(slippage))
			: Optional.empty();
		return service.request(new InstantBuyMessage(getId(), balance, units, slippageData))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((InstantOfferResultMessage) message).instantBuy());
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		Optional<SlippageMessageData> slippageData = slippage != null
			? Optional.of(new SlippageMessageData(slippage))
			: Optional.empty();
		return service.request(new InstantSellMessage(getId(), units, slippageData))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((InstantOfferResultMessage) message).instantSell());
	}

	@Override
	public CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units) {
		return service.request(new PlaceOfferMessage(getId(), owner, type, price, units))
			.thenCompose(service::throwOnError)
			.thenApply(message -> new RemoteOffer(service, (OfferMessage) message));
	}
}
