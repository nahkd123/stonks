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
package stonks.core.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import nahara.common.tasks.ManualTask;
import nahara.common.tasks.Task;
import stonks.core.exec.InstantOfferExecuteResult;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;

public interface StonksService {
	/**
	 * <p>
	 * Wrap {@link CompletionStage} to {@link Task}.
	 * </p>
	 * 
	 * @param stage The completion stage.
	 * @return The wrapped task.
	 * @deprecated This method is only used for wrapping {@link CompletionStage}
	 *             into {@link Task} and it will be removed in Stonks 2.1.0.
	 */
	@Deprecated(forRemoval = true)
	default <T> Task<T> wrap(CompletionStage<T> stage) {
		ManualTask<T> task = new ManualTask<>();
		stage.thenAccept(task::resolveSuccess).exceptionally(t -> {
			task.resolveFailure(t);
			return null;
		});
		return task;
	}

	/**
	 * <p>
	 * Obtain all categories defined in this service.
	 * </p>
	 * 
	 * @return A list of categories.
	 * @deprecated use {@link #queryAllCategoriesAsync()}
	 */
	@Deprecated(forRemoval = true)
	default Task<List<Category>> queryAllCategories() {
		return wrap(queryAllCategoriesAsync());
	}

	/**
	 * <p>
	 * Obtain all categories defined in this service. The categories are usually
	 * obtained by downloading the market info from remote service, or defined
	 * directly in configuration file if the service is local.
	 * </p>
	 * <p>
	 * For remote service, the categories may be cached.
	 * </p>
	 * 
	 * @return A list of categories, wrapped as {@link CompletableFuture}.
	 */
	public CompletableFuture<List<Category>> queryAllCategoriesAsync();

	/**
	 * <p>
	 * Obtain market overview for a product
	 * </p>
	 * 
	 * @return Market overview.
	 * @deprecated use {@link #queryMarketOverviewAsync(Product)}
	 */
	@Deprecated(forRemoval = true)
	default Task<ProductMarketOverview> queryProductMarketOverview(Product product) {
		return wrap(queryMarketOverviewAsync(product));
	}

	/**
	 * <p>
	 * Obtain the market overview for a given product. The value may be cached for
	 * few seconds (or few minutes, depending on the current load of service). The
	 * overview data may be used to suggest offering price for players.
	 * </p>
	 * 
	 * @param product The product to query market overview.
	 * @return The market overview, including a list of top offers for buy and sell.
	 */
	public CompletableFuture<ProductMarketOverview> queryMarketOverviewAsync(Product product);

	/**
	 * <p>
	 * Get all offers made by this offerer.
	 * </p>
	 * 
	 * @param offerer Offerer's unique id.
	 * @return A list of offers.
	 * @deprecated use {@link #getOffersFromUserAsync(UUID)}
	 */
	@Deprecated(forRemoval = true)
	default Task<List<Offer>> getOffers(UUID offerer) {
		return wrap(getOffersFromUserAsync(offerer));
	}

	/**
	 * <p>
	 * Get a list of offers made by user. This includes pending offers (not fully
	 * filled) and filled offers (and not fully claimed by user).
	 * </p>
	 * 
	 * @param user User's unique ID. Usually Minecraft player's UUID but it can be
	 *             anything.
	 * @return A list of offers made by user.
	 */
	public CompletableFuture<List<Offer>> getOffersFromUserAsync(UUID user);

	/**
	 * <p>
	 * Get a offer with specific ID from this service. The offer is considered to be
	 * valid and can be obtained from this method if the offer is not fully claimed
	 * (which includes not fully filled + filled but not fully claimed).
	 * </p>
	 * 
	 * @param offerId The ID of the offer.
	 * @return An {@link Optional} with value present if the offer is actually
	 *         existed.
	 */
	default CompletableFuture<Optional<Offer>> getOfferAsync(UUID offerId) {
		return getOffersAsync(Collections.singleton(offerId)).thenApply(map -> Optional.ofNullable(map.get(offerId)));
	}

	/**
	 * <p>
	 * Get a bunch of offers with specific IDs from this service. The offer is
	 * considered to be valid and can be obtained from this method if the offer is
	 * not fully claimed (which includes not fully filled + filled but not fully
	 * claimed).
	 * </p>
	 * 
	 * @param offerId The ID of the offer.
	 * @return A mapping from offer ID to offer. The result may have some of
	 *         requested IDs absent from the map if the offer is not valid.
	 */
	public CompletableFuture<Map<UUID, Offer>> getOffersAsync(Collection<UUID> offerIds);

	/**
	 * <p>
	 * Claim specified offer.
	 * </p>
	 * 
	 * @param offer Offer to claim.
	 * @return The new offer object with latest information.
	 * @deprecated use {@link #claimOfferAsync(UUID)} or
	 *             {@link #claimOffersAsync(List)}
	 */
	@Deprecated(forRemoval = true)
	default Task<Offer> claimOffer(Offer offer) {
		return wrap(claimOfferAsync(offer.getOfferId()).thenApply(opt -> opt.orElse(offer)));
	}

	/**
	 * <p>
	 * Claim a single offer from offer ID.
	 * </p>
	 * 
	 * @param offerId The ID of the offer.
	 * @return The latest info of claimed offer, or empty optional if that offer
	 *         does not exists.
	 */
	default CompletableFuture<Optional<Offer>> claimOfferAsync(UUID offerId) {
		return claimOffersAsync(Collections.singleton(offerId)).thenApply(map -> Optional.ofNullable(map.get(offerId)));
	}

	/**
	 * <p>
	 * Claim multiple offers from offer IDs.
	 * </p>
	 * 
	 * @param offerIds The ID of the offer.
	 * @return The mapping for claimed offers, with its keys for offer ID and its
	 *         values for latest offer info.
	 */
	public CompletableFuture<Map<UUID, Offer>> claimOffersAsync(Collection<UUID> offerIds);

	/**
	 * <p>
	 * Cancel offer.
	 * </p>
	 * 
	 * @param offer Offer to cancel.
	 * @return The new offer with latest information on how much units was filled
	 *         and how much units can be refunded.
	 * @deprecated use {@link #cancelOfferAsync(UUID)} or
	 *             {@link #cancelOffersAsync(Collection)}
	 */
	@Deprecated(forRemoval = true)
	default Task<Offer> cancelOffer(Offer offer) {
		return wrap(cancelOfferAsync(offer.getOfferId()).thenApply(opt -> opt.orElse(offer)));
	}

	default CompletableFuture<Optional<Offer>> cancelOfferAsync(UUID offerId) {
		return cancelOffersAsync(Collections.singleton(offerId))
			.thenApply(map -> Optional.ofNullable(map.get(offerId)));
	}

	public CompletableFuture<Map<UUID, Offer>> cancelOffersAsync(Collection<UUID> offerIds);

	/**
	 * <p>
	 * List a new offer.
	 * </p>
	 * 
	 * @param offerer      Offerer's unique id.
	 * @param product      Product to list.
	 * @param type         Offer type.
	 * @param units        How much units to offer.
	 * @param pricePerUnit Price for each unit.
	 * @return A new offer info.
	 * @deprecated use
	 *             {@link #listOfferAsync(UUID, Product, OfferType, int, double)}
	 */
	@Deprecated(forRemoval = true)
	default Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit) {
		return wrap(listOfferAsync(offerer, product, type, units, pricePerUnit));
	}

	/**
	 * <p>
	 * List a new offer.
	 * </p>
	 * 
	 * @param user         User's unique ID.
	 * @param product      Product to list.
	 * @param type         Offer type.
	 * @param units        How much units to offer.
	 * @param pricePerUnit Price for each unit.
	 * @return A new offer info.
	 */
	public CompletableFuture<Offer> listOfferAsync(UUID user, Product product, OfferType type, int units, double pricePerUnit);

	/**
	 * <p>
	 * Execute instant offer.
	 * </p>
	 * 
	 * @param product Product to execute offer.
	 * @param type    Offer type.
	 * @param units   How much units you want to buy/sell.
	 * @param balance How much money do you have (only applies for buy offers).
	 * @return Total number of units moved and amount of money received or spent.
	 * @deprecated use {@link #instantOfferAsync(Product, OfferType, int, double)}
	 */
	@Deprecated(forRemoval = true)
	default Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance) {
		return wrap(instantOfferAsync(product, type, units, balance));
	}

	/**
	 * <p>
	 * Execute instant offer.
	 * </p>
	 * 
	 * @param product Product to execute offer.
	 * @param type    Offer type.
	 * @param units   How much units you want to buy or sell.
	 * @param balance How much money do you have (only applies for instant buy
	 *                offers).
	 * @return Total number of units moved and amount of money received or spent.
	 *         For buy offers, the money should be refunded to player. For sell
	 *         offers, it is the amount of money they have collected.
	 */
	public CompletableFuture<InstantOfferExecuteResult> instantOfferAsync(Product product, OfferType type, int units, double balance);

	@Deprecated(forRemoval = true)
	default Task<InstantOfferExecuteResult> instantBuy(Product product, int units, double balance) {
		return instantOffer(product, OfferType.BUY, units, balance);
	}

	default CompletableFuture<InstantOfferExecuteResult> instantBuyAsync(Product product, int units, double balance) {
		return instantOfferAsync(product, OfferType.BUY, units, balance);
	}

	@Deprecated(forRemoval = true)
	default Task<InstantOfferExecuteResult> instantSell(Product product, int units) {
		return instantOffer(product, OfferType.SELL, units, 0d);
	}

	default CompletableFuture<InstantOfferExecuteResult> instantSellAsync(Product product, int units) {
		return instantOfferAsync(product, OfferType.SELL, units, 0d);
	}

	/**
	 * <p>
	 * Subscribe to offer filled events. This will emit fully filled offers to
	 * consumers.
	 * </p>
	 * 
	 * @param offer The events consumer.
	 */
	public void subscribeToOfferFilledEvents(Consumer<Offer> consumer);
}
