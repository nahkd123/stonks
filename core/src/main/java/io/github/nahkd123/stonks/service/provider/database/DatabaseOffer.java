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
