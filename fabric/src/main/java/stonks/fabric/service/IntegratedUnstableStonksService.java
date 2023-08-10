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
package stonks.fabric.service;

import stonks.core.service.LocalStonksService;
import stonks.core.service.memory.MemoryCategory;
import stonks.core.service.memory.MemoryProduct;
import stonks.core.service.testing.UnstableStonksService;
import stonks.fabric.StonksFabric;
import stonks.fabric.provider.StonksProvidersRegistry;

public class IntegratedUnstableStonksService extends UnstableStonksService implements LocalStonksService {
	private IntegratedStonksService underlying;

	public IntegratedUnstableStonksService(IntegratedStonksService underlying, double failRate, long maxLag) {
		super(underlying, failRate, maxLag);
		this.underlying = underlying;
	}

	@Override
	public void saveServiceData() {
		underlying.saveServiceData();
	}

	@Override
	public void loadServiceData() {
		underlying.loadServiceData();
	}

	public static void register() {
		StonksProvidersRegistry.registerService(IntegratedUnstableStonksService.class, (server, config) -> {
			for (int i = 0; i < 10; i++) StonksFabric.LOGGER.warn("Unstable service is loaded! Testing something?");

			var failRate = config.firstChild("failRate").flatMap(v -> v.getValue(Double::parseDouble)).orElse(0.0);
			var maxLag = config.firstChild("maxLag").flatMap(v -> v.getValue(Long::parseLong)).orElse(0L);
			var service = new IntegratedUnstableStonksService(new IntegratedStonksService(server), failRate, maxLag);

			for (var child : config.getChildren()) if (child.getKey().equals("category")) {
				var categoryId = child.getValue().get();
				var categoryName = child.firstChild("name").flatMap(v -> v.getValue()).orElse(categoryId);
				var category = new MemoryCategory(categoryId, categoryName);
				service.underlying.getModifiableCategories().add(category);

				for (var child1 : child.getChildren()) if (child1.getKey().equals("product")) {
					var productId = child1.getValue().get();
					var productName = child1.firstChild("name").flatMap(v -> v.getValue()).orElse(productId);
					var productConstruction = child1.firstChild("construction").flatMap(v -> v.getValue()).orElse(null);
					var product = new MemoryProduct(category, productId, productName, productConstruction);
					category.getModifiableMockProducts().add(product);
				}
			}

			return service;
		});
	}
}
