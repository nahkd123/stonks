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

import java.util.UUID;

import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.helper.MutableOrder;

public interface OrderInfo {
	/**
	 * <p>
	 * Get the database ID for this order entry.
	 * </p>
	 * 
	 * @return The order ID.
	 */
	public UUID getOrderId();

	/**
	 * <p>
	 * Get the user's UUID that made this order. In Minecraft, this is player's
	 * UUID.
	 * </p>
	 * 
	 * @return The user's UUID.
	 */
	public UUID getOwnerUuid();

	public Product getProduct();

	public long getTotalUnits();

	public long getFilledUnits();

	public long getClaimedUnits();

	default long getUnclaimedUnits() { return getFilledUnits() - getClaimedUnits(); }

	public long getPricePerUnit();

	default long getTotalPrice() { return getPricePerUnit() * getTotalUnits(); }

	default long getUnclaimedMoney() { return getPricePerUnit() * getUnclaimedUnits(); }

	public boolean isBuyOffer();

	default boolean isSellOffer() { return !isBuyOffer(); }

	default boolean shouldRemoveFromListing() {
		return getFilledUnits() >= getTotalUnits();
	}

	default boolean shouldRemoveFromPlayer() {
		return getClaimedUnits() >= getTotalUnits();
	}

	default MutableOrder asMutable() {
		MutableOrder mutable = new MutableOrder();
		mutable.setOrderId(getOrderId());
		mutable.setOwnerUuid(getOwnerUuid());
		mutable.setProduct(getProduct());
		mutable.setTotalUnits(getTotalUnits());
		mutable.setFilledUnits(getFilledUnits());
		mutable.setClaimedUnits(getClaimedUnits());
		mutable.setPricePerUnits(getPricePerUnit());
		mutable.setBuyOffer(isBuyOffer());
		return mutable;
	}
}
