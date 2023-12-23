/*
 * Copyright (c) 2023 nahkd
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
package io.github.nahkd123.stonks.market.impl.legacy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

import io.github.nahkd123.stonks.market.MarketService;
import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;
import io.github.nahkd123.stonks.market.result.ClaimResult;
import io.github.nahkd123.stonks.market.result.InstantBuyResult;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.result.MutableInstantBuyResult;
import io.github.nahkd123.stonks.market.result.MutableInstantSellResult;
import io.github.nahkd123.stonks.market.result.SimpleClaimResult;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.market.summary.SummaryEntry;
import io.github.nahkd123.stonks.utils.Async;
import io.github.nahkd123.stonks.utils.Response;
import io.github.nahkd123.stonks.utils.ServiceException;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffer;
import stonks.core.market.ProductMarketOverview;
import stonks.core.service.Emittable;
import stonks.core.service.StonksService;

/**
 * <p>
 * The market service wrapper for {@link StonksService}.
 * </p>
 */
@Deprecated
public class LegacyMarketService implements MarketService {
	private StonksService legacy;
	private LongSupplier decimals;
	private Emittable<OrderInfo> onOrderFilled = new Emittable<>();

	public LegacyMarketService(StonksService legacy, LongSupplier decimals) {
		this.legacy = legacy;
		this.decimals = decimals;
		legacy.subscribeToOfferFilledEvents(o -> onOrderFilled.emit(new LegacyOrderWrapper(o, decimals)));
	}

	public StonksService getLegacy() { return legacy; }

	public static long doubleToLong(double money, LongSupplier decimals) {
		long d = decimals.getAsLong();
		long wholeDollar = Math.round(Math.pow(10L, d));
		return Math.round(money * wholeDollar);
	}

	public static double longToDouble(long money, LongSupplier decimals) {
		long d = decimals.getAsLong();
		long wholeDollar = Math.round(Math.pow(10L, d));
		long dollars = money / wholeDollar;
		long cents = money % wholeDollar;
		return dollars + (cents / (double) wholeDollar);
	}

	@Override
	public CompletableFuture<Response<ProductsCatalogue>> queryCatalogue() {
		return Async.futureOf(legacy.queryAllCategories())
			.thenApply(LegacyProductsCatalogue::new)
			.thenApply(Response::new);
	}

	public ProductSummary convert(ProductMarketOverview overview) {
		Product w = new LegacyProductWrapper(overview.getProduct());
		List<SummaryEntry> buy = overview.getBuyOffers().getEntries().stream().map(this::convert).toList();
		List<SummaryEntry> sell = overview.getSellOffers().getEntries().stream().map(this::convert).toList();
		return new ProductSummary(w, buy, sell);
	}

	public SummaryEntry convert(OverviewOffer overview) {
		long ppu = doubleToLong(overview.pricePerUnit(), decimals);
		return new SummaryEntry(overview.totalAvailableUnits(), overview.offers(), ppu);
	}

	public OrderInfo convert(Offer offer) {
		if (offer == null) return null;
		return new LegacyOrderWrapper(offer, decimals);
	}

	@Override
	public CompletableFuture<Response<ProductSummary>> querySummary(Product product) {
		if (!(product instanceof LegacyProductWrapper w))
			return CompletableFuture.failedFuture(new IllegalArgumentException("product is not a legacy wrapper"));

		return Async.futureOf(legacy.queryProductMarketOverview(w.getLegacy()))
			.thenApply(this::convert)
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<List<OrderInfo>>> queryOrders(UUID user) {
		return Async.futureOf(legacy.getOffers(user))
			.thenApply(l -> l.stream().map(this::convert).toList())
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> queryOrder(UUID orderId) {
		return Async.futureOf(legacy.getOfferById(orderId))
			.thenApply(this::convert)
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeBuyOrder(UUID user, Product product, long amount, long pricePerUnit) {
		if (!(product instanceof LegacyProductWrapper w))
			return CompletableFuture.failedFuture(new IllegalArgumentException("product is not a legacy wrapper"));

		return Async
			.futureOf(legacy.listOffer(user, w.getLegacy(), OfferType.BUY, (int) amount,
				longToDouble(pricePerUnit, decimals)))
			.thenApply(this::convert)
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeSellOrder(UUID user, Product product, long amount, long pricePerUnit) {
		if (!(product instanceof LegacyProductWrapper w))
			return CompletableFuture.failedFuture(new IllegalArgumentException("product is not a legacy wrapper"));

		return Async
			.futureOf(legacy.listOffer(user, w.getLegacy(), OfferType.SELL, (int) amount,
				longToDouble(pricePerUnit, decimals)))
			.thenApply(this::convert)
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<ClaimResult>> claimOrder(UUID orderId) {
		return queryOrder(orderId).thenCompose(res -> {
			if (res.data() == null)
				return CompletableFuture.failedStage(new ServiceException("No order with given UUID found"));

			OrderInfo previous = res.data();
			long previousClaimed = res.data().getClaimedUnits();
			return Async.futureOf(legacy.claimOffer(res.data().getOwnerUuid(), orderId))
				.thenApply(this::convert)
				.thenApply(next -> {
					long claimed = next.getClaimedUnits() - previousClaimed;
					long worth = claimed * next.getPricePerUnit();
					boolean fullAfterClaim = next.getClaimedUnits() == next.getTotalUnits();
					SimpleClaimResult result = new SimpleClaimResult(previous.getOwnerUuid(), previous
						.getProduct(), orderId, claimed, worth, fullAfterClaim);
					return new Response<>(result);
				});
		});
	}

	@Override
	public CompletableFuture<Response<InstantBuyResult>> instantBuy(UUID user, Product product, long amount, long balance) {
		if (!(product instanceof LegacyProductWrapper w))
			return CompletableFuture.failedFuture(new IllegalArgumentException("product is not a legacy wrapper"));

		return Async.futureOf(legacy.instantBuy(w.getLegacy(), (int) amount, balance))
			.thenApply(result -> {
				long moneySpent = doubleToLong(result.balance(), decimals);
				long moneyLeft = balance - moneySpent;
				return new MutableInstantBuyResult(user, product, amount, amount - result.units(), balance, moneyLeft);
			})
			.thenApply(Response::new);
	}

	@Override
	public CompletableFuture<Response<InstantSellResult>> instantSell(UUID user, Product product, long amount) {
		if (!(product instanceof LegacyProductWrapper w))
			return CompletableFuture.failedFuture(new IllegalArgumentException("product is not a legacy wrapper"));

		return Async.futureOf(legacy.instantSell(w.getLegacy(), (int) amount))
			.thenApply(result -> {
				long moneyEarned = doubleToLong(result.balance(), decimals);
				return new MutableInstantSellResult(user, product, amount, result.units(), moneyEarned);
			})
			.thenApply(Response::new);
	}

	@Override
	public Emittable<OrderInfo> onOrderFilled() {
		return onOrderFilled;
	}
}
