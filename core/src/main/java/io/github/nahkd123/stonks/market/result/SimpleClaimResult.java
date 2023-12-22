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
package io.github.nahkd123.stonks.market.result;

import java.util.UUID;

import io.github.nahkd123.stonks.market.catalogue.Product;

public class SimpleClaimResult implements ClaimResult {
	private UUID user;
	private Product product;
	private UUID orderId;
	private long claimedAmount;
	private long claimedWorth;
	private boolean fullAfterClaim;

	public SimpleClaimResult(UUID user, Product product, UUID orderId, long claimedAmount, long claimedWorth, boolean fullAfterClaim) {
		this.user = user;
		this.product = product;
		this.orderId = orderId;
		this.claimedAmount = claimedAmount;
		this.claimedWorth = claimedWorth;
		this.fullAfterClaim = fullAfterClaim;
	}

	@Override
	public UUID getUserId() { return user; }

	@Override
	public Product getProduct() { return product; }

	@Override
	public UUID getOrderId() { return orderId; }

	@Override
	public long getClaimedAmount() { return claimedAmount; }

	@Override
	public long getClaimedWorth() { return claimedWorth; }

	@Override
	public boolean isFullAfterClaim() { return fullAfterClaim; }

}
