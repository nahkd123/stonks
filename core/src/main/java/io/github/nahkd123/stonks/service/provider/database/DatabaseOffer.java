package io.github.nahkd123.stonks.service.provider.database;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceException;

class DatabaseOffer implements Offer {
	private DatabaseMarketService service;
	private OfferData lastData;
	private DatabaseProduct product;

	public DatabaseOffer(DatabaseMarketService service, OfferData lastData, DatabaseProduct product) {
		this.service = service;
		this.lastData = lastData;
		this.product = product;
	}

	@Override
	public UUID id() {
		return lastData.id();
	}

	@Override
	public UUID owner() {
		return lastData.owner();
	}

	@Override
	public OfferType type() {
		return lastData.type();
	}

	@Override
	public Product product() {
		return product;
	}

	@Override
	public long price() {
		return lastData.price();
	}

	@Override
	public long totalUnits() {
		return lastData.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		return service.queueTask(() -> {
			OfferData data = service.offers.query(id()).first();
			if (data == null) return new Offer.Status(lastData.filledUnits(), lastData.claimedUnits(), true);
			lastData = data;
			return new Offer.Status(lastData.filledUnits(), lastData.claimedUnits(), false);
		}, false);
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		return service.queueTask(() -> {
			OfferData data = service.offers.query(id()).first();
			if (data == null) throw new ServiceException("Offer no longer exist");
			long toClaim = data.filledUnits() - data.claimedUnits();
			long pending = data.totalUnits() - data.filledUnits();
			data = data.withClaimedUnits(data.filledUnits());

			if (data.filledUnits() == data.totalUnits()) {
				if (!service.offers.delete(id()))
					throw new ServiceException("Offer claim failed: Unable to delete %s in database".formatted(id()));
			} else {
				if (!service.offers.update(data))
					throw new ServiceException("Offer claim failed: Unable to update %s in database".formatted(id()));
			}

			lastData = data;
			return new Offer.ClaimResult(toClaim, pending, data.filledUnits() == data.totalUnits());
		}, false);
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		return service.queueTask(() -> {
			OfferData data = service.offers.query(id()).first();
			if (data == null) throw new ServiceException("Offer no longer exist");
			long toClaim = data.filledUnits() - data.claimedUnits();
			long pending = data.totalUnits() - data.filledUnits();
			lastData = data.withClaimedUnits(data.filledUnits());

			if (!service.offers.delete(id()))
				throw new ServiceException("Offer cancel failed: Unable to delete %s in database".formatted(id()));

			return new Offer.ClaimResult(toClaim, pending, true);
		}, false);
	}
}
