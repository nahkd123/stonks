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
			var offer = Offer.deserialize(this::productGetter, stream);
			if (offer != null) insertOffer(offer);
			else {
				StonksFabric.LOGGER.info("Loaded data from {}", saveFilePath);
				return;
			}
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
				var categoryId = child.getValue();
				var categoryName = child.firstChild("name").map(v -> v.getValue()).orElse(categoryId);
				var category = new MemoryCategory(categoryId, categoryName);
				service.getModifiableCategories().add(category);

				for (var child1 : child.getChildren()) if (child1.getKey().equals("product")) {
					var productId = child1.getValue();
					var productName = child1.firstChild("name").map(v -> v.getValue()).orElse(productId);
					var productConstruction = child1.firstChild("construction").map(v -> v.getValue()).orElse(null);
					var product = new MemoryProduct(category, productId, productName, productConstruction);
					category.getModifiableMockProducts().add(product);
				}
			}

			return service;
		});
	}
}
