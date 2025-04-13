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
package io.github.nahkd123.stonks.service.provider;

import java.io.IOException;

import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec;
import io.github.nahkd123.stonks.utils.dynamic.DynamicReader;

/**
 * <p>
 * A market service provider, following the SPI (Service Provider Interface)
 * pattern. Services must be declared in
 * {@code META-INF/services/io.github.nahkd123.stonks.service.provider.MarketServiceProvider},
 * with each line is a fully qualified class name of service provider. Below is
 * sample for file content.
 * </p>
 * {@snippet :
 * io.github.nahkd123.stonks.service.provider.database.DatabaseServiceProvider
 * io.github.nahkd123.stonks.service.provider.memory.MemoryServiceProvider
 * io.github.nahkd123.stonks.service.provider.remote.RemoteServiceProvider
 * }
 * 
 * @param <C> The type of configuration object.
 */
public interface MarketServiceProvider<C> {
	/**
	 * <p>
	 * Get the provider name. This will be used by the platform to determine which
	 * provider to create host, using the provider name configured in user's
	 * configuration file.
	 * </p>
	 * 
	 * @return The provider name.
	 */
	String getProviderName();

	/**
	 * <p>
	 * Get the configuration codec, which is used to decode configuration data for
	 * passing to {@link #createHost(Object)}.
	 * </p>
	 * 
	 * @return The configuration codec.
	 */
	DynamicCodec<C> getConfigCodec();

	/**
	 * <p>
	 * Create a new market service host, which manages the lifecycle of market
	 * service. The host will be consumed by platform and will call lifetime methods
	 * accordingly.
	 * </p>
	 * 
	 * @param config The configuration provided by user.
	 * @return The host that will manage the lifecycle of market service.
	 */
	MarketServiceHost createHost(C config);

	default MarketServiceHost createHost(DynamicReader dynamicReader) throws IOException {
		return createHost(dynamicReader != null ? getConfigCodec().decodeFrom(dynamicReader) : null);
	}
}
