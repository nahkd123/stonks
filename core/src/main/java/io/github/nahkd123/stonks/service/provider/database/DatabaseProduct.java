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

import static io.github.nahkd123.tableschema.query.Filter.allOf;
import static io.github.nahkd123.tableschema.query.Filter.eq;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.tableschema.SortOrder;
import io.github.nahkd123.tableschema.query.QueryResult;
import io.github.nahkd123.tableschema.query.SortBy;

record DatabaseProduct(DatabaseMarketService service, ProductData data) implements Product {
	@Override
	public String getId() { return data.id(); }

	@Override
	public CompletableFuture<ProductOverview> queryOverview() {
		return service.queueTask(() -> {
			try (ForkJoinPool pool = new ForkJoinPool(2)) {
				int samples = service.config.overviewSamples();
				var buyCalcTask = pool.submit(() -> calculateOfferOverview(OfferType.BUY, samples));
				var sellCalcTask = pool.submit(() -> calculateOfferOverview(OfferType.SELL, samples));
				return new ProductOverview(buyCalcTask.join(), sellCalcTask.join());
			}
		}, false);
	}

	private ProductOffersOverview calculateOfferOverview(OfferType type, int samples) throws SQLException {
		List<OfferOverviewEntry> entries = new ArrayList<>();
		long totalUnits = 0L;
		long totalValue = 0L;

		try (QueryResult<OfferData> result = service.offers.query(
			allOf(
				eq(OfferData.TYPE, type),
				eq(OfferData.PRODUCT_ID, data.id())),
			new SortBy<>(OfferData.PRICE, type == OfferType.BUY
				? SortOrder.DESCENDING
				: SortOrder.ASCENDING))) {
			Iterator<OfferData> iter = result.iterator();

			while (iter.hasNext() && samples > 0) {
				OfferData offer = iter.next();
				long available = offer.totalUnits() - offer.filledUnits();
				totalUnits += available;
				totalValue += available * offer.price();
				entries.add(new OfferOverviewEntry(offer.price(), available));
				samples--;
			}
		}

		return new ProductOffersOverview(type, totalUnits > 0 ? totalValue / totalUnits : 0, entries);
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		return service.queueTask(() -> {
			long balance0 = balance;
			long units0 = units;
			long bought = 0L;

			try (QueryResult<OfferData> result = service.offers.query(
				allOf(
					eq(OfferData.TYPE, OfferType.SELL),
					eq(OfferData.PRODUCT_ID, data.id())),
				new SortBy<>(OfferData.PRICE, SortOrder.ASCENDING))) {
				Iterator<OfferData> iter = result.iterator();
				while (iter.hasNext() && balance0 > 0 && units0 > 0) {
					OfferData offer = iter.next();
					if (slippage != null && !slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long canBuy = Math.min(balance0 / offer.price(), units0);
					if (canBuy == 0L) break;

					long toBuy = Math.min(canBuy, available);
					balance0 -= toBuy * offer.price();
					units0 -= toBuy;
					offer = offer.withFilledUnits(offer.filledUnits() + toBuy);
					bought += toBuy;

					if (!service.offers.update(offer)) {
						balance0 += toBuy * offer.price();
						units0 += toBuy;
						bought -= toBuy;
						continue;
					}

					if (offer.filledUnits() >= offer.totalUnits()) {
						DatabaseOffer handle = new DatabaseOffer(service, offer, this);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}
			}

			return new Product.InstantBuyResult(bought, balance0);
		}, false);
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		return service.queueTask(() -> {
			long inventory = units;
			long balance = 0L;

			try (QueryResult<OfferData> result = service.offers.query(
				allOf(
					eq(OfferData.TYPE, OfferType.BUY),
					eq(OfferData.PRODUCT_ID, data.id())),
				new SortBy<>(OfferData.PRICE, SortOrder.DESCENDING))) {
				Iterator<OfferData> iter = result.iterator();
				while (iter.hasNext() && inventory > 0) {
					OfferData offer = iter.next();
					if (slippage != null && !slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long toSell = Math.min(inventory, available);
					if (toSell == 0L) break;

					inventory -= toSell;
					balance += toSell * offer.price();
					offer = offer.withFilledUnits(offer.filledUnits() + toSell);

					if (!service.offers.update(offer)) {
						inventory += toSell;
						balance -= toSell * offer.price();
					}

					if (offer.filledUnits() >= offer.totalUnits()) {
						DatabaseOffer handle = new DatabaseOffer(service, offer, this);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}
			}

			return new Product.InstantSellResult(balance, inventory);
		}, false);
	}

	@Override
	public CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units) {
		return service.queueTask(() -> {
			OfferData data = new OfferData(UUID.randomUUID(), owner, type, getId(), price, units, 0L, 0L);
			if (!service.offers.insert(data))
				throw new ServiceException("Unable to place offer for %s".formatted(getId()));
			return new DatabaseOffer(service, data, this);
		}, false);
	}
}
