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
package io.github.nahkd123.stonks.service.provided.sql;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceException;

class SqlOffer implements Offer {
	private SqlMarketService service;
	private OfferRecord rec;
	private long lastFilled;
	private long lastClaimed;
	private boolean removed = false;

	public SqlOffer(SqlMarketService service, OfferRecord rec) {
		this.service = service;
		this.rec = rec;
		this.lastFilled = rec.filledUnits();
		this.lastClaimed = rec.claimedUnits();
	}

	@Override
	public UUID id() {
		return rec.id();
	}

	@Override
	public UUID owner() {
		return rec.owner();
	}

	@Override
	public OfferType type() {
		return rec.type();
	}

	@Override
	public Product product() {
		return new SqlProduct(service, new ProductRecord(rec.productId()));
	}

	@Override
	public long price() {
		return rec.price();
	}

	@Override
	public long totalUnits() {
		return rec.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		if (removed) return CompletableFuture.completedFuture(new Offer.Status(lastFilled, lastClaimed, true));

		return service.queueTransaction(() -> {
			OfferRecord updatedRec = service.selectOfferById.query(rec).firstOr(null);

			if (updatedRec == null) {
				removed = true;
				return new Offer.Status(lastFilled, lastClaimed, true);
			}

			rec = updatedRec;
			lastFilled = rec.filledUnits();
			lastClaimed = rec.claimedUnits();
			return new Offer.Status(lastFilled, lastClaimed, false);
		});
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		return service.queueTransaction(() -> {
			OfferRecord updatedRec = service.selectOfferById.query(rec).firstOr(null);

			if (updatedRec == null) {
				removed = true;
				return new Offer.ClaimResult(0L, true);
			} else {
				rec = updatedRec;
				lastFilled = rec.filledUnits();
				lastClaimed = rec.claimedUnits();
			}

			long toClaim = lastFilled - lastClaimed;
			rec = rec.withClaimedUnits(lastFilled);

			if (rec.claimedUnits() >= rec.totalUnits()) {
				removeOffer();
				return new Offer.ClaimResult(toClaim, true);
			} else {
				service.updateOffer.update(rec);
				return new Offer.ClaimResult(toClaim, false);
			}
		});
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		return service.queueTransaction(() -> {
			OfferRecord updatedRec = service.selectOfferById.query(rec).firstOr(null);

			if (updatedRec == null) {
				removed = true;
				return new Offer.ClaimResult(0L, true);
			} else {
				rec = updatedRec;
				lastFilled = rec.filledUnits();
				lastClaimed = rec.claimedUnits();
			}

			long toClaim = lastFilled - lastClaimed;
			rec = rec.withClaimedUnits(lastFilled);
			removeOffer();
			return new Offer.ClaimResult(toClaim, true);
		});
	}

	private void removeOffer() throws SQLException {
		service.deleteOffer.update(rec);
		removed = true;
	}
}
