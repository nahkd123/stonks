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

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Basically {@link MarketService}, but with extra methods for remotely managing
 * the service. Single instance setup always have {@link ManageableMarketService}
 * in the one and only instance. Multi-instance setup requires extra permission
 * in order to obtain {@link ManageableMarketService}.
 * </p>
 */
public interface ManageableMarketService extends MarketService {
	/**
	 * <p>
	 * Create a new product with specific ID and add it to the catalog.
	 * </p>
	 * 
	 * @param id The ID of the new product.
	 * @return Async task that resolves to new product handle.
	 */
	CompletableFuture<? extends Product> createProduct(String id);

	/**
	 * <p>
	 * Delete an existing product in the catalog.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: This will also cancel all ongoing offers targeting the
	 * product. Use at your own caution.
	 * </p>
	 * 
	 * @param product The product to delete.
	 * @return Async task.
	 */
	CompletableFuture<Void> deleteProduct(Product product);

	/**
	 * <p>
	 * Query the running configuration from this service.
	 * </p>
	 */
	CompletableFuture<ServiceConfig> queryConfig();

	/**
	 * <p>
	 * Ask service to use a new configuration.
	 * </p>
	 * 
	 * @param config The configuration to use.
	 */
	CompletableFuture<Void> useConfig(ServiceConfig config);
}
