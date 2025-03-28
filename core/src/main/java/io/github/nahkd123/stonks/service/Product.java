package io.github.nahkd123.stonks.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Represent a product. When adding a new product, you must also add product to
 * configurations across all instances that are using Stonks alongside adding to
 * market service. The product representation is how the product is being
 * presented in the instance (whether as an item, a kind of point or anything
 * else). If there is no configuration, the product will remains hidden in menu,
 * because Stonks does not know which category the product belongs (or list it
 * under {@code <Not configured>} category but only visible to gamemasters).
 * </p>
 * <p>
 * In case of mass deployment, where there are hundreds of instances, you may
 * choose to derive the product representation from just product ID and metadata
 * alone.
 * </p>
 */
public interface Product {
	/**
	 * <p>
	 * Get the ID of this product. The ID is database-friendly, which is suitable
	 * for use as key in any databases.
	 * </p>
	 * 
	 * @return The product ID.
	 */
	String getId();

	/**
	 * <p>
	 * Calculate product overview, including top offers from {@link OfferType#BUY}
	 * and {@link OfferType#SELL}.
	 * </p>
	 * 
	 * @return Async task that resolves to product overview.
	 */
	CompletableFuture<ProductOverview> queryOverview();

	/**
	 * <p>
	 * Perform instant buy of this product.
	 * </p>
	 * 
	 * @param balance  The balance that user is willing to spend.
	 * @param units    The number of units that user want to buy.
	 * @param slippage An optional slippage option to ensure the price don't stray
	 *                 too far from user's expectation.
	 * @return Async task that resolves to instant buy result. Use the result to
	 *         refund leftovers.
	 */
	CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage);

	/**
	 * <p>
	 * Perform instant sell of this product.
	 * </p>
	 * 
	 * @param units    The number of units that user want to sell.
	 * @param slippage An optional slippage option to ensure the price don't stray
	 *                 too far from user's expectation.
	 * @return Async task that resolves to instant sell result. Use the result to
	 *         refund leftovers.
	 */
	CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage);

	/**
	 * <p>
	 * Place a new offer for this product.
	 * </p>
	 * 
	 * @param owner The owner of the new offer (the user that is making the offer).
	 * @param type  The type of offer.
	 * @param price The price for each unit of this product that user is willing to
	 *              offer.
	 * @param units The number of units that user want.
	 * @return Async task that resolves to offer handle.
	 */
	CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units);

	record SlippageOption(long targetPrice, double maxRate) {
		public boolean check(long price) {
			double actualRate = price / (double) targetPrice;
			return Math.abs(actualRate - 1d) <= maxRate;
		}
	}

	record InstantBuyResult(long bought, long leftoverBalance) {
	}

	record InstantSellResult(long earning, long leftoverUnits) {
	}
}
