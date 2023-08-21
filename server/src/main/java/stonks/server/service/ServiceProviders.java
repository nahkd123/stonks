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
package stonks.server.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import stonks.core.service.memory.MemoryCategory;
import stonks.core.service.memory.MemoryProduct;
import stonks.core.service.memory.StonksMemoryService;
import stonks.server.Main;

public class ServiceProviders {
	private static final Map<String, ConfigurableProvider> PROVIDERS = new HashMap<>();

	public static ConfigurableProvider add(String id, ConfigurableProvider provider) {
		PROVIDERS.put(id, provider);
		return provider;
	}

	public static Map<String, ConfigurableProvider> getProviders() { return Collections.unmodifiableMap(PROVIDERS); }

	public static final ConfigurableProvider MEMORY = add("stonks:memory", config -> {
		Main.LOGGER.warn("Memory service provider: You are using in-memory service, "
			+ "which will lose all data when the server is stopped!");
		var service = new StonksMemoryService();

		for (var child : config.children("category").toList()) {
			var categoryId = child.getValue();
			if (categoryId.isEmpty()) {
				Main.LOGGER.warn("Memory service provider: Category ID is missing");
				continue;
			}

			var categoryName = child.firstChild("name")
				.flatMap(v -> v.getValue())
				.or(() -> categoryId)
				.get();
			var category = new MemoryCategory(categoryId.get(), categoryName);

			for (var productConfig : child.children("product").toList()) {
				var productId = productConfig.getValue();
				if (productId.isEmpty()) {
					Main.LOGGER.warn("Memory service provider: Product ID in " + categoryId.get() + " is missing");
					continue;
				}

				var productName = productConfig.firstChild("name")
					.flatMap(v -> v.getValue())
					.orElse(productId.get());
				var constructionData = productConfig.firstChild("construction")
					.flatMap(v -> v.getValue())
					.orElse("");
				category.getModifiableMockProducts()
					.add(new MemoryProduct(category, productId.get(), productName, constructionData));
			}

			service.getModifiableCategories().add(category);
		}

		return service;
	});
}
