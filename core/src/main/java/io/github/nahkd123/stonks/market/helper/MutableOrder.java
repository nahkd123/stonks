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
package io.github.nahkd123.stonks.market.helper;

import java.util.UUID;
import java.util.function.Function;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;

/**
 * <p>
 * A mutable version of {@link OrderInfo}.
 * </p>
 */
public class MutableOrder implements OrderInfo {
	private Function<String, Product> productsRegistry;
	private Product product;
	private UUID orderId;
	private UUID ownerId;
	private String productId;
	private boolean isBuy;
	private long units;
	private long pricePerUnit;
	private long filled;
	private long claimed;

	public MutableOrder(Function<String, Product> productsRegistry, UUID orderId, UUID ownerId, String productId, boolean isBuy, long units, long pricePerUnit, long filled, long claimed) {
		this.productsRegistry = productsRegistry;
		this.isBuy = isBuy;
		this.orderId = orderId;
		this.ownerId = ownerId;
		this.productId = productId;
		this.units = units;
		this.pricePerUnit = pricePerUnit;
		this.filled = filled;
		this.claimed = claimed;
	}

	public MutableOrder() {}

	@Override
	public UUID getOrderId() { return orderId; }

	public void setOrderId(UUID orderId) { this.orderId = orderId; }

	@Override
	public UUID getOwnerUuid() { return ownerId; }

	public void setOwnerUuid(UUID ownerId) { this.ownerId = ownerId; }

	@Override
	public Product getProduct() { return product != null ? product : (product = productsRegistry.apply(productId)); }

	public void setProduct(Product product) {
		this.product = product;
		this.productId = product.getId();
	}

	public String getProductId() { return productId; }

	public void setProductId(String productId) {
		this.productId = productId;
		if (productsRegistry != null) this.product = productsRegistry.apply(productId);
	}

	@Override
	public long getTotalUnits() { return units; }

	public void setTotalUnits(long units) { this.units = units; }

	@Override
	public long getFilledUnits() { return filled; }

	public void setFilledUnits(long filled) { this.filled = filled; }

	@Override
	public long getClaimedUnits() { return claimed; }

	public void setClaimedUnits(long claimed) { this.claimed = claimed; }

	@Override
	public long getPricePerUnit() { return pricePerUnit; }

	public void setPricePerUnits(long pricePerUnit) { this.pricePerUnit = pricePerUnit; }

	@Override
	public boolean isBuyOffer() { return isBuy; }

	public void setBuyOffer(boolean isBuy) { this.isBuy = isBuy; }

	@Override
	public MutableOrder asMutable() {
		return this;
	}
}
