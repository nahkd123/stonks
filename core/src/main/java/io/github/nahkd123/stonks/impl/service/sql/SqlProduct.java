package io.github.nahkd123.stonks.impl.service.sql;

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

	private ProductOffersOverview calculateOfferOverview(OfferType type, int samples) {
		List<OfferOverviewEntry> entries = new ArrayList<>();
		long totalUnits = 0L;
		long totalValue = 0L;
		String sqlCode = switch (type) {
		case BUY -> "select * from %s where Type='BUY' order by Price desc limit %d";
		case SELL -> "select * from %s where Type='SELL' order by Price asc limit %d";
		};
		sqlCode = sqlCode.formatted(SqlMarketService.OFFERS.name(), samples);

		try (var s = service.sql.createStatement();
			var set = s.executeQuery(sqlCode)) {
			while (set.next()) {
				OfferRecord offer = OfferRecord.RECORD.getFrom(set);
				long available = offer.totalUnits() - offer.filledUnits();
				totalUnits += available;
				totalValue += available * offer.price();
				entries.add(new OfferOverviewEntry(offer.price(), available));
			}

			return new ProductOffersOverview(type, totalValue / totalUnits, entries);
		} catch (SQLException e) {
			throw new ServiceException("Internal error", e);
		}
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			String sqlCode = "select * from %s where Type='SELL' order by Price asc"
				.formatted(SqlMarketService.OFFERS.name());
			long balance0 = balance;
			long units0 = units;
			long bought = 0L;

			try (var s = service.sql.createStatement();
				var set = s.executeQuery(sqlCode)) {
				while (set.next()) {
					OfferRecord offer = OfferRecord.RECORD.getFrom(set);
					if (slippage != null && slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long canBuy = Math.min(balance0 / offer.price(), units0);
					if (canBuy == 0L) break;

					long toBuy = Math.min(canBuy, available);
					balance0 -= toBuy * offer.price();
					units0 -= toBuy;
					offer = offer.withFilledUnits(offer.filledUnits() + toBuy);
					bought += toBuy;

					sqlCode = "update %s set FilledUnits=? where Id=?".formatted(SqlMarketService.OFFERS.name());
					try (var upd = service.sql.prepareStatement(sqlCode)) {
						upd.setLong(1, offer.filledUnits());
						upd.setString(2, offer.id().toString());
						upd.execute();
					}

					if (offer.filledUnits() >= offer.totalUnits()) {
						SqlOffer handle = new SqlOffer(service, offer);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}

				return new Product.InstantBuyResult(bought, balance0);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			String sqlCode = "select * from %s where Type='BUY' order by Price desc"
				.formatted(SqlMarketService.OFFERS.name());
			long inventory = units;
			long balance = 0L;

			try (var s = service.sql.createStatement();
				var set = s.executeQuery(sqlCode)) {
				while (set.next()) {
					OfferRecord offer = OfferRecord.RECORD.getFrom(set);
					if (slippage != null && slippage.check(offer.price())) break;
					long available = offer.totalUnits() - offer.filledUnits();
					long toSell = Math.min(inventory, available);
					if (toSell == 0L) break;

					inventory -= toSell;
					balance += toSell * offer.price();
					offer = offer.withFilledUnits(offer.filledUnits() + toSell);

					sqlCode = "update %s set FilledUnits=? where Id=?".formatted(SqlMarketService.OFFERS.name());
					try (var upd = service.sql.prepareStatement(sqlCode)) {
						upd.setLong(1, offer.filledUnits());
						upd.setString(2, offer.id().toString());
						upd.execute();
					}

					if (offer.filledUnits() >= offer.totalUnits()) {
						SqlOffer handle = new SqlOffer(service, offer);
						service.listeners.beginEmit(listener -> listener.onOfferFilled(service, handle));
					}
				}

				return new Product.InstantSellResult(balance, inventory);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		return service.queueTransaction(() -> {
			try {
				OfferRecord rec = new OfferRecord(UUID.randomUUID(), owner, type, getId(), price, units, 0L, 0L);
				SqlMarketService.OFFERS.insert(service.sql, rec);
				return new SqlOffer(service, rec);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}
}
