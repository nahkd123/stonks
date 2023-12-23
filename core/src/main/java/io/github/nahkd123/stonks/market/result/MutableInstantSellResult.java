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

public class MutableInstantSellResult implements InstantSellResult {
	private UUID user;
	private Product product;
	private long requested;
	private long leftover;
	private long balance;

	public MutableInstantSellResult(UUID user, Product product, long requested, long leftover, long balance) {
		this.user = user;
		this.product = product;
		this.requested = requested;
		this.leftover = leftover;
		this.balance = balance;
	}

	public MutableInstantSellResult(UUID user, Product product, long requested) {
		this(user, product, requested, requested, 0L);
	}

	@Override
	public long getRequestedAmount() { return requested; }

	public void setRequestedAmount(long requested) { this.requested = requested; }

	@Override
	public UUID getUserId() { return user; }

	public void setUserId(UUID user) { this.user = user; }

	@Override
	public Product getProduct() { return product; }

	public void setProduct(Product product) { this.product = product; }

	@Override
	public long getLeftoverAmount() { return leftover; }

	public void setLeftoverAmount(long leftover) { this.leftover = leftover; }

	@Override
	public long getCollectedBalance() { return balance; }

	public void setCollectedBalance(long balance) { this.balance = balance; }

}
