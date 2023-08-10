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
package stonks.fabric.provider;

import java.util.HashMap;
import java.util.Map;

import nahara.common.configurations.Config;
import stonks.core.service.StonksService;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.service.StonksServiceProvider;

public class StonksProvidersRegistry {
	private static final String NOT_SPECIFIED = "<Not specified>";
	private static Map<String, ConfigurableProvider<StonksService>> services = new HashMap<>();
	private static Map<String, ConfigurableProvider<StonksFabricAdapter>> adapters = new HashMap<>();

	public static StonksServiceProvider getServiceProvider(Config config) {
		var provider = config.getValue().map(v -> services.get(v.trim())).orElse(null);
		if (provider == null) {
			StonksFabric.LOGGER.warn("Unknown service provider: {}", config.getValue().orElse(NOT_SPECIFIED));
			return null;
		}

		return server -> provider.configure(server, config);
	}

	public static StonksFabricAdapterProvider getAdapterProvider(Config config) {
		var provider = config.getValue().map(v -> adapters.get(v.trim())).orElse(null);
		if (provider == null) {
			StonksFabric.LOGGER.warn("Unknown adapter provider: {}", config.getValue().orElse(NOT_SPECIFIED));
			return null;
		}

		return server -> provider.configure(server, config);
	}

	@SuppressWarnings("unchecked")
	public static <T extends StonksService> void registerService(Class<T> serviceClass, ConfigurableProvider<T> provider) {
		services.put(serviceClass.getCanonicalName(), (ConfigurableProvider<StonksService>) provider);
	}

	@SuppressWarnings("unchecked")
	public static <T extends StonksFabricAdapter> void registerAdapter(Class<T> serviceClass, ConfigurableProvider<T> provider) {
		adapters.put(serviceClass.getCanonicalName(), (ConfigurableProvider<StonksFabricAdapter>) provider);
	}
}
