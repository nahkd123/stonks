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
package io.github.nahkd123.stonks.market;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;
import io.github.nahkd123.stonks.market.result.ClaimResult;
import io.github.nahkd123.stonks.market.result.InstantBuyResult;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.utils.Response;
import nahara.common.tasks.Task;
import stonks.core.service.Emittable;
import stonks.core.service.StonksService;

/**
 * <p>
 * The modern implementation of {@link StonksService}. We are transitioning away
 * from the problematic {@link Task} to the battle-tested
 * {@link CompletableFuture} API.
 * </p>
 * <p>
 * The {@link StonksService} will not be deprecated for now. We'll deprecate it
 * once {@link MarketService} is fully completed.
 * </p>
 * <p>
 * First off, we will keep the same "ease of setting up" idea from
 * {@link StonksService}. The categories and products information will still be
 * provided by this service. This allows users to deploy Stonks to multiple game
 * servers while having only a single configuration that can be shared and
 * updated to all connected game servers.
 * </p>
 * <p>
 * Next, we will use {@link CompletableFuture} instead of {@link Task}. This
 * allows smaller mod size (is it though?) and less dependencies wacks because,
 * well, we have 1 less dependency to care about.
 * </p>
 */
public interface MarketService {
	/**
	 * <p>
	 * Get the catalogue information, including all products and categories. The
	 * implementation must returns the latest catalogue information (a.k.a no
	 * caching.
	 * </p>
	 * 
	 * @return The catalogue information.
	 */
	public CompletableFuture<Response<ProductsCatalogue>> queryCatalogue();

	/**
	 * <p>
	 * Get the product summary. This includes product's instant buy/sell price and
	 * top offers for buy and sell.
	 * </p>
	 * 
	 * @param product The product.
	 * @return The product summary.
	 */
	public CompletableFuture<Response<ProductSummary>> querySummary(Product product);

	/**
	 * <p>
	 * Get a list of order made by user.
	 * </p>
	 * 
	 * @param user User's UUID.
	 * @return A list of orders, including buy and sell offers.
	 */
	public CompletableFuture<Response<List<OrderInfo>>> queryOrders(UUID user);

	/**
	 * <p>
	 * Get the order with given order ID. If the order with specified ID does not
	 * exists, it will returns {@code null} (null inside {@link Response}).
	 * </p>
	 * 
	 * @param orderId The ID of the order.
	 * @return Order info.
	 */
	public CompletableFuture<Response<OrderInfo>> queryOrder(UUID orderId);

	/**
	 * <p>
	 * List a new buy order offer. It might takes a while before the offer become
	 * available for instant sell.
	 * </p>
	 * 
	 * @param user         The user's ID.
	 * @param product      The product.
	 * @param amount       How much units you want to purchase.
	 * @param pricePerUnit The price per unit for the product.
	 * @return The order information. This can be used for opening offer info menu
	 *         instantly.
	 */
	public CompletableFuture<Response<OrderInfo>> makeBuyOrder(UUID user, Product product, long amount, long pricePerUnit);

	/**
	 * <p>
	 * List a new sell order offer. It might takes a while before the offer become
	 * available for instant buy.
	 * </p>
	 * 
	 * @param user         The user's ID.
	 * @param product      The product.
	 * @param amount       How much units you are willing to sell.
	 * @param pricePerUnit The price per unit for the product.
	 * @return The order information. This can be used for opening offer info menu
	 *         instantly.
	 */
	public CompletableFuture<Response<OrderInfo>> makeSellOrder(UUID user, Product product, long amount, long pricePerUnit);

	/**
	 * <p>
	 * Attempt to claim offer. If {@link ClaimResult#isFullAfterClaim()} returns
	 * {@code true}, the offer data on the service could have been removed (because
	 * it is fully claimed).
	 * </p>
	 * 
	 * @param orderId The ID of the offer to claim.
	 * @return The claim result.
	 */
	public CompletableFuture<Response<ClaimResult>> claimOrder(UUID orderId);

	// Lowest in sell offer -> Top offer for instant buy
	//
	// SQL: SELECT * FROM sellOffers ORDER BY PricePerUnit ASC LIMIT <rows>, <rows>;
	// This statement allows us to make a "sliding window" that slides
	// across the database until we decided "Yep, that's enough data".
	// This approach needs index on PricePerUnit column to speed up query time.
	//
	// Another approach is to use Statement.setFetchSize(Integer.MIN_VALUE) and keep
	// calling ResultSet.next() until we have enough data. We need a way to get the
	// rows one by one without having to sort every time we request new row.
	/**
	 * <p>
	 * Perform instant buy.
	 * </p>
	 * <p>
	 * Example implementation details: The market operator define the minimum units
	 * for buffer; The service attempts to fill the buffer by collecting top offers
	 * until the buffer is filled; When an instant offer is made, the service runs
	 * through its buffer and update the records (eg: perform SQL statement); When
	 * the buffer is below the defined minimum units, the service will collect top
	 * offers once again (the existing buffer will be discarded). Note that the
	 * refilling buffer phase should be performed every N seconds to avoid
	 * overloading the database.
	 * </p>
	 * <p>
	 * We choose to use buffer instead of collecting an entire database because we
	 * don't want to fill the memory quickly, but we also want to perform instant
	 * offers as fast as possible.
	 * </p>
	 * 
	 * @param user    User's UUID.
	 * @param product The product.
	 * @param amount  The units count (a.k.a how much you want to buy).
	 * @param balance The amount of money that user is willing to spend, typically
	 *                101% of total instant buy price for slippage tolerance.
	 * @return The instant buy result.
	 * @see #instantSell(Product, long)
	 */
	public CompletableFuture<Response<InstantBuyResult>> instantBuy(UUID user, Product product, long amount, long balance);

	/**
	 * <p>
	 * Perform instant sell.
	 * </p>
	 * <p>
	 * See {@link #instantBuy(Product, long, long)} for example on how to
	 * implementation the system.
	 * </p>
	 * 
	 * @param user    User's UUID.
	 * @param product The product.
	 * @param amount  The units to sell.
	 * @return The instant sell result.
	 * @see #instantBuy(Product, long, long)
	 */
	public CompletableFuture<Response<InstantSellResult>> instantSell(UUID user, Product product, long amount);

	/**
	 * <p>
	 * Get the {@link Emittable} that emits event when an order is marked as filled.
	 * This will be used for notifying player about their filled order.
	 * </p>
	 * 
	 * @return The emittable.
	 */
	public Emittable<OrderInfo> onOrderFilled();
}
