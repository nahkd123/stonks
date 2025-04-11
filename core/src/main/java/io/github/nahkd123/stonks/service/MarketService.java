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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Represent an interface for interacting with market system.
 * </p>
 */
public interface MarketService {
	void addNotificationListener(ServiceNotificationListener listener);

	void removeNotificationListener(ServiceNotificationListener listener);

	/**
	 * <p>
	 * Query entire catalog from this service. Typically you may only use this while
	 * initializing instance; for watching catalog updates you may use
	 * {@link #addNotificationListener(ServiceNotificationListener)}.
	 * </p>
	 * 
	 * @return Async task that resolves to collection of products.
	 */
	CompletableFuture<Set<? extends Product>> queryCatalog();

	/**
	 * <p>
	 * Query all offers made by specific user with given user's UUID.
	 * </p>
	 * 
	 * @param uuid The UUID of user.
	 * @return Async task that resolves to list of offers made by user, ordered from
	 *         oldest to newest.
	 */
	CompletableFuture<List<? extends Offer>> queryUserOffers(UUID uuid);

	/**
	 * <p>
	 * Query a single offer with specific ID.
	 * </p>
	 * 
	 * @param id The UUID of offer (from {@link Offer#id()}).
	 * @return Async task that resolves to offer with specific ID.
	 */
	CompletableFuture<? extends Offer> queryOffer(UUID id);
}
