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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import stonks.core.market.Offer;
import stonks.core.product.Product;
import stonks.core.service.memory.MemoryCategory;
import stonks.core.service.memory.MemoryProduct;
import stonks.core.service.memory.StonksMemoryService;
import stonks.fabric.StonksFabric;
import stonks.fabric.provider.StonksProvidersRegistry;

public class IntegratedStonksService extends StonksMemoryService {
	private Path saveFilePath;

	public IntegratedStonksService(MinecraftServer server) {
		saveFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("stonks.bin");
	}

	@Override
	public void saveServiceData() {
		super.saveServiceData();
		var iter = offersIterator();

		try (var stream = Files.newOutputStream(saveFilePath)) {
			while (iter.hasNext()) {
				var offer = iter.next();
				Offer.serializeV1(offer, stream);
			}

			Offer.serializeV1(null, stream);
			StonksFabric.LOGGER.info("Saved data to {}", saveFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			StonksFabric.LOGGER.error("Unable to save data to {}", saveFilePath);
		}
	}

	@Override
	public void loadServiceData() {
		super.loadServiceData();

		if (Files.notExists(saveFilePath)) {
			StonksFabric.LOGGER.warn("Saved market data not found, skipping...");
			return;
		}

		try (var stream = Files.newInputStream(saveFilePath)) {
			Offer offer;

			do {
				offer = Offer.deserialize(this::productGetter, stream);
				if (offer != null) insertOffer(offer);
			} while (offer != null);

			StonksFabric.LOGGER.info("Loaded data from {}", saveFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			StonksFabric.LOGGER.error("Unable to load data from {}", saveFilePath);
		}
	}

	private Optional<Product> productGetter(String id) {
		return getModifiableCategories().stream()
			.flatMap(v -> v.getProducts().stream())
			.filter(v -> v.getProductId().equals(id))
			.findFirst();
	}

	public static void register() {
		StonksProvidersRegistry.registerService(IntegratedStonksService.class, (server, config) -> {
			var service = new IntegratedStonksService(server);

			for (var child : config.getChildren()) if (child.getKey().equals("category")) {
				var categoryId = child.getValue().get();
				var categoryName = child.firstChild("name").flatMap(v -> v.getValue()).orElse(categoryId);
				var category = new MemoryCategory(categoryId, categoryName);
				service.getModifiableCategories().add(category);

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
