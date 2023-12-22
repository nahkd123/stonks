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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.market.summary.SummaryEntry;

public class ProductSummaryGenerator {
	private Product product;
	private long lastGenerated;
	private long cacheRetentionDuration;
	private ProductSummary cached;
	private Supplier<OrdersIterator> buyOrders;
	private Supplier<OrdersIterator> sellOrders;

	public ProductSummaryGenerator(Product product, long cacheRetentionDuration, Supplier<OrdersIterator> buyOrders, Supplier<OrdersIterator> sellOrders) {
		this.product = product;
		this.cacheRetentionDuration = cacheRetentionDuration;
		this.lastGenerated = -1L;
		this.buyOrders = buyOrders;
		this.sellOrders = sellOrders;
	}

	public Product getProduct() { return product; }

	public long getLastGeneratedTime() { return lastGenerated; }

	public ProductSummary forceGenerate(int buyEntries, int sellEntries) {
		List<SummaryEntry> buy = new ArrayList<>();
		List<SummaryEntry> sell = new ArrayList<>();
		fill(buy, buyOrders.get(), buyEntries);
		fill(sell, sellOrders.get(), sellEntries);
		lastGenerated = System.currentTimeMillis();
		cached = new ProductSummary(product, Collections.unmodifiableList(buy), Collections.unmodifiableList(sell));
		return cached;
	}

	public ProductSummary generate(int buyEntries, int sellEntries) {
		long timeDelta = System.currentTimeMillis() - lastGenerated;
		if (cached == null || timeDelta >= cacheRetentionDuration) return forceGenerate(buyEntries, sellEntries);
		return cached;
	}

	private void fill(List<SummaryEntry> list, OrdersIterator orders, int entries) {
		long currentOffers = 0L;
		long currentUnits = 0L;
		long currentPrice = -1L;

		while (orders.hasNext() && list.size() < entries) {
			OrderInfo info = orders.next();
			if (info.getFilledUnits() >= info.getTotalUnits()) continue;

			if (currentPrice == -1L) currentPrice = info.getPricePerUnit();
			else if (currentPrice != info.getPricePerUnit()) {
				list.add(new SummaryEntry(currentUnits, currentOffers, currentPrice));
				currentOffers = 0L;
				currentUnits = 0L;
				currentPrice = info.getPricePerUnit();

				if (list.size() >= entries) return;
			}

			currentOffers += 1L;
			currentUnits += info.getTotalUnits() - info.getFilledUnits();
		}

		if (currentOffers > 0L) list.add(new SummaryEntry(currentUnits, currentOffers, currentPrice));
	}
}
