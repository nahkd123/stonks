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

import java.util.UUID;
import java.util.function.LongSupplier;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;

public class LegacyOrderWrapper implements OrderInfo {
	private Offer legacy;
	private LongSupplier decimals;

	public LegacyOrderWrapper(Offer legacy, LongSupplier decimals) {
		this.legacy = legacy;
		this.decimals = decimals;
	}

	public Offer getLegacy() { return legacy; }

	@Override
	public UUID getOrderId() { return legacy.getOfferId(); }

	@Override
	public UUID getOwnerUuid() { return legacy.getOffererId(); }

	@Override
	public Product getProduct() { return new LegacyProductWrapper(legacy.getProduct()); }

	@Override
	public long getTotalUnits() { return legacy.getTotalUnits(); }

	@Override
	public long getFilledUnits() { return legacy.getFilledUnits(); }

	@Override
	public long getClaimedUnits() { return legacy.getClaimedUnits(); }

	@SuppressWarnings("deprecation")
	@Override
	public long getPricePerUnit() { return LegacyMarketService.doubleToLong(legacy.getPricePerUnit(), decimals); }

	@Override
	public boolean isBuyOffer() { return legacy.getType() == OfferType.BUY; }

}
