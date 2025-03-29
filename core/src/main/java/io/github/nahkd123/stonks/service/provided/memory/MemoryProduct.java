package io.github.nahkd123.stonks.service.provided.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOffersOverview;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.ServiceException;

class MemoryProduct implements Product {
	private String id;
	MemoryMarketService service;
	List<MemoryOffer> buyOffers = new ArrayList<>();
	List<MemoryOffer> sellOffers = new ArrayList<>();

	public MemoryProduct(MemoryMarketService service, String id) {
		this.service = service;
		this.id = id;
	}

	@Override
	public String getId() { return id; }

	@Override
	public CompletableFuture<ProductOverview> queryOverview() {
		ProductOffersOverview buyOverview = calculateOfferOverview(OfferType.BUY, service.config.overviewSamples());
		ProductOffersOverview sellOverview = calculateOfferOverview(OfferType.SELL, service.config.overviewSamples());
		return CompletableFuture.completedFuture(new ProductOverview(buyOverview, sellOverview));
	}

	private ProductOffersOverview calculateOfferOverview(OfferType type, int samples) {
		List<MemoryOffer> list = getOffersListByType(type);
		List<OfferOverviewEntry> entries = new ArrayList<>();
		long totalUnits = 0L;
		long totalValue = 0L;

		for (int i = 0; i < Math.min(samples, list.size()); i++) {
			MemoryOffer offer = list.get(i);
			long available = offer.totalUnits() - offer.filledUnits;
			totalUnits += available;
			totalValue += available * offer.price();
			entries.add(new OfferOverviewEntry(offer.price(), available));
		}

		return new ProductOffersOverview(type, totalValue / totalUnits, entries);
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		long bought = 0L;

		for (MemoryOffer sellOffer : sellOffers) {
			if (slippage != null && slippage.check(sellOffer.price())) break;
			long available = sellOffer.totalUnits() - sellOffer.filledUnits;
			long canBuy = Math.min(balance / sellOffer.price(), units);
			if (canBuy == 0L) break;

			long toBuy = Math.min(canBuy, available);
			balance -= toBuy * sellOffer.price();
			units -= toBuy;
			sellOffer.filledUnits += toBuy;
			bought += toBuy;

			if (sellOffer.filledUnits >= sellOffer.totalUnits()) {
				service.listeners.beginEmit(listener -> listener.onOfferFilled(service, sellOffer));
			}
		}

		return CompletableFuture.completedFuture(new Product.InstantBuyResult(bought, balance));
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		long earning = 0L;

		for (MemoryOffer buyOffer : buyOffers) {
			if (slippage != null && slippage.check(buyOffer.price())) break;
			long available = buyOffer.totalUnits() - buyOffer.filledUnits;
			long toSell = Math.min(units, available);
			if (toSell == 0L) break;

			units -= toSell;
			buyOffer.filledUnits += toSell;
			earning += toSell * buyOffer.price();

			if (buyOffer.filledUnits >= buyOffer.totalUnits()) {
				service.listeners.beginEmit(listener -> listener.onOfferFilled(service, buyOffer));
			}
		}

		return CompletableFuture.completedFuture(new Product.InstantSellResult(earning, units));
	}

	@Override
	public CompletableFuture<MemoryOffer> placeOffer(UUID owner, OfferType type, long price, long units) {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		MemoryOffer offer = new MemoryOffer(UUID.randomUUID(), owner, type, this, price, units, 0L, 0L, false);
		service.userOffers.computeIfAbsent(owner, $ -> new HashMap<>()).put(offer.id(), offer);
		List<MemoryOffer> productOffersList = getOffersListByType(type);
		int search = Collections.binarySearch(productOffersList, offer);
		int insertAt = search >= 0 ? search : -search - 1;
		productOffersList.add(insertAt, offer);
		return CompletableFuture.completedFuture(offer);
	}

	List<MemoryOffer> getOffersListByType(OfferType type) {
		return switch (type) {
		case BUY -> buyOffers;
		case SELL -> sellOffers;
		};
	}
}
