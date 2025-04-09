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
import java.util.ArrayList;
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
import io.github.nahkd123.stonks.utils.orm.TableInfo;

record SqlProduct(SqlMarketService service, ProductRecord rec) implements Product {
	@Override
	public String getId() { return rec.id(); }

	@Override
	public CompletableFuture<ProductOverview> queryOverview() {
		return service.queueTransaction(() -> {
			try (ForkJoinPool pool = new ForkJoinPool(2)) {
				int samples = service.config.overviewSamples();
				var buyCalcTask = pool.submit(() -> calculateOfferOverview(OfferType.BUY, samples));
				var sellCalcTask = pool.submit(() -> calculateOfferOverview(OfferType.SELL, samples));
				return new ProductOverview(buyCalcTask.join(), sellCalcTask.join());
			}
		});
	}

	private ProductOffersOverview calculateOfferOverview(OfferType type, int samples) throws SQLException {
		List<OfferOverviewEntry> entries = new ArrayList<>();
		long totalUnits = 0L;
		long totalValue = 0L;

		TableInfo.Select<OfferRecord> query = switch (type) {
		case BUY -> service.selectTopBuyOffers;
		case SELL -> service.selectTopSellOffers;
		};

		try (var set = query.query()) {
			while (set.hasNext()) {
				OfferRecord offer = set.next();
				long available = offer.totalUnits() - offer.filledUnits();
				totalUnits += available;
				totalValue += available * offer.price();
				entries.add(new OfferOverviewEntry(offer.price(), available));
			}

			return new ProductOffersOverview(type, totalUnits > 0 ? totalValue / totalUnits : 0, entries);
		}
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			long balance0 = balance;
			long units0 = units;
			long bought = 0L;

			try (var set = service.selectSellOffers.query()) {
				while (set.hasNext()) {
					OfferRecord offer = set.next();
					if (slippage != null && !slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long canBuy = Math.min(balance0 / offer.price(), units0);
					if (canBuy == 0L) break;

					long toBuy = Math.min(canBuy, available);
					balance0 -= toBuy * offer.price();
					units0 -= toBuy;
					offer = offer.withFilledUnits(offer.filledUnits() + toBuy);
					bought += toBuy;
					service.updateOffer.update(offer);

					if (offer.filledUnits() >= offer.totalUnits()) {
						SqlOffer handle = new SqlOffer(service, offer);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}

				return new Product.InstantBuyResult(bought, balance0);
			}
		});
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			long inventory = units;
			long balance = 0L;

			try (var set = service.selectBuyOffers.query()) {
				while (set.hasNext()) {
					OfferRecord offer = set.next();
					if (slippage != null && !slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long toSell = Math.min(inventory, available);
					if (toSell == 0L) break;

					inventory -= toSell;
					balance += toSell * offer.price();
					offer = offer.withFilledUnits(offer.filledUnits() + toSell);
					service.updateOffer.update(offer);

					if (offer.filledUnits() >= offer.totalUnits()) {
						SqlOffer handle = new SqlOffer(service, offer);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}

				return new Product.InstantSellResult(balance, inventory);
			}
		});
	}

	@Override
	public CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			OfferRecord rec = new OfferRecord(UUID.randomUUID(), owner, type, getId(), price, units, 0L, 0L);
			service.insertOffer.insert(rec);
			return new SqlOffer(service, rec);
		});
	}
}
