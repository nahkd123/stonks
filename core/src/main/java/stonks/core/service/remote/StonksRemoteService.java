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
package stonks.core.service.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import nahara.common.tasks.ManualTask;
import nahara.common.tasks.Task;
import stonks.core.exec.InstantOfferExecuteResult;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffersList;
import stonks.core.market.ProductMarketOverview;
import stonks.core.net.Connection;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;
import stonks.core.service.memory.MemoryCategory;
import stonks.core.service.memory.MemoryProduct;
import stonks.core.service.remote.message.MessageC2SGetOffers;
import stonks.core.service.remote.message.MessageC2SQueryProductOverview;
import stonks.core.service.remote.message.MessageC2SQueryProducts;
import stonks.core.service.remote.message.MessageS2COffersList;
import stonks.core.service.remote.message.MessageS2CQueryProductOverview;
import stonks.core.service.remote.message.MessageS2CQueryProductsPartial;
import stonks.core.service.remote.message.MessagesHandler;

/**
 * <p>
 * An interface to interact with remote service.
 * </p>
 * <p>
 * Remote service allows you to share Stonks market data to multiple front-ends.
 * You can use this to sync between 2 game servers or just use this if your game
 * server crash a lot (to avoid losing data).
 * </p>
 * <p>
 * Note that tasks will be resolved in networking thread. If you want to make
 * some changes that requires the game server thread, you have to synchronize by
 * either scheduling on next server tick or use
 * {@code MinecraftServer#execute()}.
 * </p>
 * <p>
 * <b>Caching</b>: {@link StonksRemoteService} caches return value of
 * {@link #queryAllCategories()}, so if you just added something from remote
 * service, you'll have to restart your game server OR clear the cache with
 * {@link #clearRemoteCache()}
 * </p>
 */
public class StonksRemoteService implements StonksService {
	private List<Category> cachedCategories;
	private Connection connection;
	private MessagesHandler messagesHandler;

	private ManualTask<List<Category>> categoriesQueryTask;
	private List<Category> scanningCategories;
	private Map<String, Product> productsLookupMap = new HashMap<>();

	public StonksRemoteService(Connection connection) {
		this.connection = connection;

		// TODO
		messagesHandler = new MessagesHandler();

		messagesHandler.registerDeserializer(MessageS2CQueryProductsPartial.ID, MessageS2CQueryProductsPartial::new);
		messagesHandler.listenForMessage(MessageS2CQueryProductsPartial.class, msg -> {
			if (msg.message().getErrorMessage().isPresent()) {
				categoriesQueryTask.resolveFailure(new RemoteServiceException(msg.message().getErrorMessage().get()));
				scanningCategories = null;
				return;
			}

			if (scanningCategories == null) {
				scanningCategories = new ArrayList<>();
				productsLookupMap = new HashMap<>();
			}

			var category = scanningCategories.stream()
				.filter(v -> v.getCategoryId().equals(msg.message().getCategoryId()))
				.findAny();
			if (category.isEmpty()) {
				var c = new MemoryCategory(msg.message().getCategoryId(), msg.message().getCategoryName());
				scanningCategories.add(c);
				category = Optional.of(c);
			}

			var list = ((MemoryCategory) category.get()).getModifiableMockProducts();
			var pId = msg.message().getProductId();
			var pName = msg.message().getProductName();
			var pCData = msg.message().getConstructionData();

			if (pId.length() != 0) {
				var product = new MemoryProduct((MemoryCategory) category.get(), pId, pName, pCData);
				list.add(product);
				productsLookupMap.put(pId, product);
			}

			if (msg.message().isFinished()) {
				cachedCategories = scanningCategories;
				categoriesQueryTask.resolveSuccess(scanningCategories);
				scanningCategories = null;
			}
		});

		messagesHandler.registerDeserializer(MessageS2CQueryProductOverview.ID, MessageS2CQueryProductOverview::new);
		messagesHandler.registerDeserializer(MessageS2COffersList.ID, MessageS2COffersList.createDeserializer(id -> {
			return productsLookupMap.get(id);
		}));

		messagesHandler.handleConnection(connection);
	}

	public void clearRemoteCache() {
		cachedCategories = null;
	}

	/**
	 * <p>
	 * Obtain all categories. This value is controlled by remote service.
	 * </p>
	 * <p>
	 * However, the return value of this method could be cached. If you just added
	 * something from remote service, you'll have to restart game server or use
	 * {@link #clearRemoteCache()}
	 * </p>
	 */
	@Override
	public Task<List<Category>> queryAllCategories() {
		if (cachedCategories != null) return Task.resolved(cachedCategories);

		if (categoriesQueryTask == null) {
			categoriesQueryTask = new ManualTask<>();
			connection.sendRawPacket(new MessageC2SQueryProducts().createRawPacket());
		}

		return categoriesQueryTask;
	}

	@Override
	public Task<ProductMarketOverview> queryProductMarketOverview(Product product) {
		var task = new ManualTask<ProductMarketOverview>();
		connection.sendRawPacket(new MessageC2SQueryProductOverview(product).createRawPacket());
		messagesHandler.waitForMessage(MessageS2CQueryProductOverview.class,
			v -> v.getProductId().equals(product.getProductId()),
			msg -> {
				if (msg.message().getErrorMessage().isPresent()) {
					task.resolveFailure(new RemoteServiceException(msg.message().getErrorMessage().get()));
					return;
				}

				var buyOffers = new OverviewOffersList(OfferType.BUY, msg.message().getBuyOffers());
				var sellOffers = new OverviewOffersList(OfferType.SELL, msg.message().getSellOffers());
				task.resolveSuccess(new ProductMarketOverview(product, buyOffers, sellOffers));
			});
		return task;
	}

	@Override
	public Task<List<Offer>> getOffers(UUID offerer) {
		return queryAllCategories().andThen($ -> {
			var task = new ManualTask<List<Offer>>();
			connection.sendRawPacket(new MessageC2SGetOffers(offerer).createRawPacket());
			messagesHandler.waitForMessage(MessageS2COffersList.class,
				v -> v.getOfferer().equals(offerer),
				msg -> {
					if (msg.message().getErrorMessage().isPresent()) {
						task.resolveFailure(new RemoteServiceException(msg.message().getErrorMessage().get()));
						return;
					}

					task.resolveSuccess(msg.message().getOffers());
				});
			return task;
		});
	}

	@Override
	public Task<Offer> claimOffer(Offer offer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task<Offer> cancelOffer(Offer offer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeToOfferFilledEvents(Consumer<Offer> consumer) {
		// TODO Auto-generated method stub

	}
}
