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

import java.util.NoSuchElementException;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;

public interface OrdersIterator {
	public Product getProduct();

	public boolean hasNext();

	public OrderInfo next();

	public void update(OrderInfo newInfo);

	public static OrdersIterator empty(Product product) {
		return new OrdersIterator() {
			@Override
			public void update(OrderInfo newInfo) {}

			@Override
			public OrderInfo next() {
				throw new NoSuchElementException();
			}

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Product getProduct() { return product; }
		};
	}
}
