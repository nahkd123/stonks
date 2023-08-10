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
package stonks.core.service;

import java.util.List;
import java.util.UUID;

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
	 * Obtain all categories defined in this service.
	 * </p>
	 * 
	 * @return A list of categories.
	 */
	public Task<List<Category>> queryAllCategories();

	/**
	 * <p>
	 * Obtain market overview for a product
	 * </p>
	 * 
	 * @return Market overview.
	 */
	public Task<ProductMarketOverview> queryProductMarketOverview(Product product);

	/**
	 * <p>
	 * Get all offers made by this offerer.
	 * </p>
	 * 
	 * @param offerer Offerer's unique id.
	 * @return A list of offers.
	 */
	public Task<List<Offer>> getOffers(UUID offerer);

	/**
	 * <p>
	 * Claim specified offer.
	 * </p>
	 * 
	 * @param offer Offer to claim.
	 * @return The new offer object with latest information.
	 */
	public Task<Offer> claimOffer(Offer offer);

	/**
	 * <p>
	 * Cancel offer.
	 * </p>
	 * 
	 * @param offer Offer to cancel.
	 * @return The new offer with latest information on how much units was filled
	 *         and how much units can be refunded.
	 */
	public Task<Offer> cancelOffer(Offer offer);

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
	 */
	public Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit);

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
	 */
	public Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance);

	default Task<InstantOfferExecuteResult> instantBuy(Product product, int units, double balance) {
		return instantOffer(product, OfferType.BUY, units, balance);
	}

	default Task<InstantOfferExecuteResult> instantSell(Product product, int units) {
		return instantOffer(product, OfferType.SELL, units, 0d);
	}
}
