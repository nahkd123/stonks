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
package io.github.nahkd123.stonks.market.impl.queue;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import io.github.nahkd123.stonks.market.MarketService;
import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;
import io.github.nahkd123.stonks.market.impl.sql.SqlMarketService;
import io.github.nahkd123.stonks.market.result.ClaimResult;
import io.github.nahkd123.stonks.market.result.InstantBuyResult;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.utils.Response;
import stonks.core.service.Emittable;

/**
 * <p>
 * A requests queue variant of {@link MarketService}. This will queue the task
 * while the executor process each requests one by one.
 * </p>
 * <p>
 * The use case for this is to avoid race condition, as well as executing tasks
 * without blocking current thread, unlike {@link SqlMarketService} for example.
 * </p>
 */
public class QueuedMarketService implements MarketService {
	private static record QueueEntry<T>(Supplier<CompletableFuture<T>> executor, CompletableFuture<T> result) {
		public QueueEntry(Supplier<CompletableFuture<T>> executor) {
			this(executor, new CompletableFuture<>());
		}

		public void executeInCurrentThread() {
			executor.get()
				.handle((val, err) -> {
					if (err != null) result.completeExceptionally(err);
					else result.complete(val);
					return err != null ? null : val;
				})
				.thenAccept($ -> {});
		}
	}

	private Queue<QueueEntry<?>> pending = new ConcurrentLinkedQueue<>();
	private MarketService underlying;
	private Executor executor;
	private boolean isIterating = false;

	public QueuedMarketService(MarketService underlying, Executor executor) {
		this.underlying = underlying;
		this.executor = executor;
	}

	public MarketService getUnderlying() { return underlying; }

	public Executor getExecutor() { return executor; }

	public <T> CompletableFuture<T> queueTask(Supplier<CompletableFuture<T>> taskExecutor) {
		QueueEntry<T> entry = new QueueEntry<>(taskExecutor);
		pending.add(entry);

		if (!isIterating) {
			isIterating = true;

			executor.execute(() -> {
				while (!pending.isEmpty()) {
					QueueEntry<?> e = pending.poll();
					e.executeInCurrentThread();
				}

				isIterating = false;
			});
		}

		return entry.result;
	}

	@Override
	public CompletableFuture<Response<ProductsCatalogue>> queryCatalogue() {
		return queueTask(() -> underlying.queryCatalogue());
	}

	@Override
	public CompletableFuture<Response<ProductSummary>> querySummary(Product product) {
		return queueTask(() -> underlying.querySummary(product));
	}

	@Override
	public CompletableFuture<Response<List<OrderInfo>>> queryOrders(UUID user) {
		return queueTask(() -> underlying.queryOrders(user));
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> queryOrder(UUID orderId) {
		return queueTask(() -> underlying.queryOrder(orderId));
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeBuyOrder(UUID user, Product product, long amount, long pricePerUnit) {
		return queueTask(() -> underlying.makeBuyOrder(user, product, amount, pricePerUnit));
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeSellOrder(UUID user, Product product, long amount, long pricePerUnit) {
		return queueTask(() -> underlying.makeSellOrder(user, product, amount, pricePerUnit));
	}

	@Override
	public CompletableFuture<Response<ClaimResult>> claimOrder(UUID orderId) {
		return queueTask(() -> underlying.claimOrder(orderId));
	}

	@Override
	public CompletableFuture<Response<InstantBuyResult>> instantBuy(UUID user, Product product, long amount, long balance) {
		return queueTask(() -> underlying.instantBuy(user, product, amount, balance));
	}

	@Override
	public CompletableFuture<Response<InstantSellResult>> instantSell(UUID user, Product product, long amount) {
		return queueTask(() -> underlying.instantSell(user, product, amount));
	}

	@Override
	public Emittable<OrderInfo> onOrderFilled() {
		return underlying.onOrderFilled();
	}
}
