package io.github.nahkd123.stonks.service.provided.remote;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.provided.remote.message.CatalogMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.CancelOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.ClaimOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferClaimResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferStatusMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferStatusMessage;

record RemoteOffer(RemoteMarketService service, OfferMessage message) implements Offer {
	@Override
	public UUID id() {
		return message.id();
	}

	@Override
	public UUID owner() {
		return message.owner();
	}

	@Override
	public OfferType type() {
		return message.type();
	}

	@Override
	public Product product() {
		return new RemoteProduct(service, new CatalogMessage.Product(message.productId()));
	}

	@Override
	public long price() {
		return message.price();
	}

	@Override
	public long totalUnits() {
		return message.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		return service.request(new QueryOfferStatusMessage(id()))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((OfferStatusMessage) message).status());
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		return service.request(new ClaimOfferMessage(id()))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((OfferClaimResultMessage) message).result());
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		return service.request(new CancelOfferMessage(id()))
			.thenCompose(service::throwOnError)
			.thenApply(message -> ((OfferClaimResultMessage) message).result());
	}
}
