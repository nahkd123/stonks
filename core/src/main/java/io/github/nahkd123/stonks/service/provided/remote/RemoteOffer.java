package io.github.nahkd123.stonks.service.provided.remote;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.CancelOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.ClaimOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOfferStatus;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.RemoteOfferData;

record RemoteOffer(RemoteServiceClient client, RemoteOfferData data) implements Offer {
	@Override
	public UUID id() {
		return data.id();
	}

	@Override
	public UUID owner() {
		return data.owner();
	}

	@Override
	public OfferType type() {
		return data.type();
	}

	@Override
	public Product product() {
		return new RemoteProduct(client, data.productId());
	}

	@Override
	public long price() {
		return data.price();
	}

	@Override
	public long totalUnits() {
		return data.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		return client.request(new QueryOfferStatus(data.id()));
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		return client.request(new ClaimOfferRequest(data.id()));
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		return client.request(new CancelOfferRequest(data.id()));
	}
}
