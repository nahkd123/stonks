package io.github.nahkd123.stonks.service.provided.memory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.ServiceException;

class MemoryOffer implements Offer {
	private UUID id;
	private UUID owner;
	private OfferType type;
	private MemoryProduct product;
	private long price;
	private long totalUnits;
	long filledUnits;
	private long claimedUnits;
	private boolean removed;

	public MemoryOffer(UUID id, UUID owner, OfferType type, MemoryProduct product, long price, long totalUnits, long filledUnits, long claimedUnits, boolean removed) {
		this.id = id;
		this.owner = owner;
		this.type = type;
		this.product = product;
		this.price = price;
		this.totalUnits = totalUnits;
		this.filledUnits = filledUnits;
		this.claimedUnits = claimedUnits;
		this.removed = removed;
	}

	@Override
	public UUID id() {
		return id;
	}

	@Override
	public UUID owner() {
		return owner;
	}

	@Override
	public OfferType type() {
		return type;
	}

	@Override
	public MemoryProduct product() {
		return product;
	}

	@Override
	public long price() {
		return price;
	}

	@Override
	public long totalUnits() {
		return totalUnits;
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		return CompletableFuture.completedFuture(new Offer.Status(filledUnits, claimedUnits, removed));
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		if (product.service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		long toClaim = filledUnits - claimedUnits;
		claimedUnits = filledUnits;
		boolean toRemove = claimedUnits >= totalUnits;
		if (toRemove) removeThisOffer();
		return CompletableFuture.completedFuture(new Offer.ClaimResult(toClaim, toRemove));
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		if (product.service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		long toClaim = filledUnits - claimedUnits;
		claimedUnits = filledUnits;
		removeThisOffer();
		return CompletableFuture.completedFuture(new Offer.ClaimResult(toClaim, true));
	}

	private void removeThisOffer() {
		Map<UUID, MemoryOffer> userOffers = product.service.userOffers.get(owner);
		if (userOffers != null) userOffers.remove(id);
		List<MemoryOffer> list = product.getOffersListByType(type);
		list.remove(this);
		removed = true;
	}
}
