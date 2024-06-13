/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.core.service.testing;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import stonks.core.exec.InstantOfferExecuteResult;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;

/**
 * <p>
 * Simulate service calls in unstable environment. Stonks must be able to handle
 * unexpected results from service.
 * </p>
 */
public class UnstableStonksService implements StonksService {
	private StonksService underlying;
	private double failRate;
	private long maxLag;

	public UnstableStonksService(StonksService underlying, double failRate, long maxLag) {
		this.underlying = underlying;
		this.failRate = failRate;
		this.maxLag = maxLag;
	}

	public StonksService getUnderlying() { return underlying; }

	private <T> CompletableFuture<T> wrapStage(CompletionStage<T> stage) {
		var rng = new Random();
		var shouldFail = rng.nextDouble() < failRate;
		var waitMs = rng.nextLong(maxLag);

		return stage
			.thenApplyAsync(result -> {
				try {
					Thread.sleep(rng.nextLong(waitMs));
				} catch (InterruptedException e) {
					// Silently ignore
				}

				if (shouldFail) throw new UnstableException("Simulated failure (" + UnstableStonksService.class + ")");
				return result;
			})
			.toCompletableFuture();
	}

	@Override
	public CompletableFuture<List<Category>> queryAllCategoriesAsync() {
		return wrapStage(underlying.queryAllCategoriesAsync());
	}

	@Override
	public CompletableFuture<ProductMarketOverview> queryMarketOverviewAsync(Product product) {
		return wrapStage(underlying.queryMarketOverviewAsync(product));
	}

	@Override
	public CompletableFuture<List<Offer>> getOffersFromUserAsync(UUID offerer) {
		return wrapStage(underlying.getOffersFromUserAsync(offerer));
	}

	@Override
	public CompletableFuture<Map<UUID, Offer>> getOffersAsync(Collection<UUID> offerIds) {
		return wrapStage(underlying.getOffersAsync(offerIds));
	}

	@Override
	public CompletableFuture<Map<UUID, Offer>> claimOffersAsync(Collection<UUID> offerIds) {
		return wrapStage(underlying.claimOffersAsync(offerIds));
	}

	@Override
	public CompletableFuture<Map<UUID, Offer>> cancelOffersAsync(Collection<UUID> offerIds) {
		return wrapStage(underlying.cancelOffersAsync(offerIds));
	}

	@Override
	public CompletableFuture<Offer> listOfferAsync(UUID user, Product product, OfferType type, int units, double pricePerUnit) {
		return wrapStage(underlying.listOfferAsync(user, product, type, units, pricePerUnit));
	}

	@Override
	public CompletableFuture<InstantOfferExecuteResult> instantOfferAsync(Product product, OfferType type, int units, double balance) {
		return wrapStage(underlying.instantOfferAsync(product, type, units, balance));
	}

	@Override
	public void subscribeToOfferFilledEvents(Consumer<Offer> consumer) {
		underlying.subscribeToOfferFilledEvents(consumer); // TODO add delay
	}
}
