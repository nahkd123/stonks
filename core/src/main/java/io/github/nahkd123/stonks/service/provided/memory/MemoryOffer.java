/*
 * Copyright (c) 2023-2025 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
		long pending = totalUnits - filledUnits;
		claimedUnits = filledUnits;
		boolean toRemove = claimedUnits >= totalUnits;
		if (toRemove) removeThisOffer();
		return CompletableFuture.completedFuture(new Offer.ClaimResult(toClaim, pending, toRemove));
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		if (product.service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		long toClaim = filledUnits - claimedUnits;
		long pending = totalUnits - filledUnits;
		claimedUnits = filledUnits;
		removeThisOffer();
		return CompletableFuture.completedFuture(new Offer.ClaimResult(toClaim, pending, true));
	}

	void removeThisOffer() {
		Map<UUID, MemoryOffer> userOffers = product.service.userOffers.get(owner);
		if (userOffers != null) userOffers.remove(id);
		product.service.offers.remove(id);
		List<MemoryOffer> list = product.getOffersListByType(type);
		list.remove(this);
		removed = true;
	}
}
