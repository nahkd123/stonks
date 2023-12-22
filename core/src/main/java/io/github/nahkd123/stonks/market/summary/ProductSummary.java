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
package io.github.nahkd123.stonks.market.summary;

import java.util.List;

import io.github.nahkd123.stonks.market.catalogue.Product;

public class ProductSummary {
	private Product product;
	private List<SummaryEntry> buySummary;
	private List<SummaryEntry> sellSummary;
	private long instantBuyPrice, instantSellPrice;

	public ProductSummary(Product product, List<SummaryEntry> buySummary, List<SummaryEntry> sellSummary) {
		this.product = product;
		this.buySummary = buySummary;
		this.sellSummary = sellSummary;
		this.instantBuyPrice = avg(sellSummary);
		this.instantSellPrice = avg(buySummary);
	}

	public Product getProduct() { return product; }

	public List<SummaryEntry> getBuySummary() { return buySummary; }

	public List<SummaryEntry> getSellSummary() { return sellSummary; }

	public long getInstantBuyPrice() { return instantBuyPrice; }

	public long getInstantSellPrice() { return instantSellPrice; }

	public static long avg(List<SummaryEntry> entries) {
		long sum = 0;
		long totalUnits = 0;

		for (SummaryEntry entry : entries) {
			totalUnits += entry.totalUnits();
			sum += entry.totalUnits() * entry.pricePerUnit();
		}

		return totalUnits == 0 ? 0L : (sum / totalUnits);
	}
}
