/*
 * Copyright (c) 2023-2025 nahkd
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
package io.github.nahkd123.stonks.service;

import java.util.Set;

/**
 * <p>
 * A listener that listens to notifications from {@link MarketService}.
 * </p>
 */
public interface ServiceNotificationListener {
	/**
	 * <p>
	 * Called when the product catalog of service changed (added, removed or
	 * updated). This will not be called when product's overview data changed.
	 * </p>
	 * 
	 * @param sender   The service that emit this notification.
	 * @param products A collection of products in new catalog.
	 */
	default void onCatalogUpdate(MarketService sender, Set<? extends Product> products) {}

	/**
	 * <p>
	 * Called when an offer is fully filled but not yet fully claimed. This will be
	 * used to push notification to user.
	 * </p>
	 * 
	 * @param sender The service that emit this notification.
	 * @param offer  The offer that has been fully filled.
	 */
	default void onOfferFilled(MarketService sender, Offer offer) {}
}
