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
package io.github.nahkd123.stonks;

import io.github.nahkd123.stonks.service.MarketService;

/**
 * <p>
 * Represent an instance of Stonks. Stonks instance is usually bounds to an
 * instance of Minecraft server (either integrated or dedicated) or a process
 * (one process can spawn multiple instances of Stonks).
 * </p>
 * <p>
 * Stonks instances are implemented by platform implementation (they are not
 * supposed to be implemented by API consumers). Getting an instance of
 * {@link Stonks} rely on platform-specific API. The instance is usually
 * configured by user, and in some rare cases, by code.
 * </p>
 */
public interface Stonks {
	/**
	 * <p>
	 * Get the current market service that is running in this {@link Stonks}
	 * instance. The lifecycle of market service is managed by platform's
	 * implementation. It is not possible for {@link Stonks} to have no market
	 * service, which means the return value of this method will never be
	 * {@code null}.
	 * </p>
	 * 
	 * @return The current market service.
	 */
	MarketService getMarketService();
}
