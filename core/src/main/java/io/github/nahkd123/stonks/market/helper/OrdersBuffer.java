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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;

/**
 * <p>
 * A buffer that holds a limited number of units. Primarily for generating
 * product summary every N seconds.
 * </p>
 */
public class OrdersBuffer {
	private List<MutableOrder> orders = new ArrayList<>();
	private Product product;
	private boolean isBuy;
	private long trackedUnits = 0L;

	public OrdersBuffer(Product product, boolean isBuy) {
		this.product = product;
		this.isBuy = isBuy;
	}

	public Product getProduct() { return product; }

	public long getTrackedUnits() { return trackedUnits; }

	public List<MutableOrder> getOrders() { return Collections.unmodifiableList(orders); }

	public void insertOrder(MutableOrder order) {
		int search = Collections.binarySearch(orders, order, (a, b) -> isBuy
			? Long.compare(b.getPricePerUnit(), a.getPricePerUnit())
			: Long.compare(a.getPricePerUnit(), b.getPricePerUnit()));

		if (search < 0) {
			// search = -(insertion point) - 1
			// search + 1 = -insertion point
			search = -(search + 1);
		}

		orders.add(search, order);
		trackedUnits += order.getTotalPrice() - order.getFilledUnits();
	}

	public void fillBuffer(long limit, Supplier<MutableOrder> supplier) {
		while (limit == -1 || trackedUnits < limit) {
			MutableOrder next = supplier.get();
			if (next == null) return;
			insertOrder(next);
		}
	}

	public void reset() {
		orders.clear();
		trackedUnits = 0L;
	}

	public OrdersIterator iterator(Consumer<MutableOrder> orderUpdater) {
		return new OrdersIterator() {
			Iterator<MutableOrder> underlying = orders.iterator();
			long currentFilled;

			@Override
			public void update(OrderInfo newInfo) {
				orderUpdater.accept(newInfo.asMutable());
				long unitsFilled = newInfo.getFilledUnits() - currentFilled;
				trackedUnits -= unitsFilled;
				if (newInfo.getFilledUnits() >= newInfo.getTotalUnits()) underlying.remove();
			}

			@Override
			public OrderInfo next() {
				if (!hasNext()) throw new NoSuchElementException();
				OrderInfo current = null;

				do {
					current = underlying.next();
					if (current.getFilledUnits() >= current.getTotalUnits()) underlying.remove();
				} while (current.getFilledUnits() < current.getTotalUnits());

				currentFilled = current.getFilledUnits();
				return current;
			}

			@Override
			public boolean hasNext() {
				return underlying.hasNext();
			}

			@Override
			public Product getProduct() { return product; }
		};
	}

	public long getRecommendedInstantOfferCap(long limit, long requestsPerCycle) {
		// "Rapid limit" is the limit that scales with requests per N seconds.
		long rapidLimit = trackedUnits / Math.max(requestsPerCycle, 1L);
		return Math.min(rapidLimit, limit);
	}
}
