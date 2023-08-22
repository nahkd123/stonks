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

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import nahara.common.tasks.Task;
import nahara.common.tasks.TaskResult;
import stonks.core.market.Offer;
import stonks.core.market.ProductMarketOverview;
import stonks.core.net.Connection;
import stonks.core.net.ServerHandler;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;
import stonks.core.service.remote.message.MessageC2SAddOffer;
import stonks.core.service.remote.message.MessageC2SGetOffers;
import stonks.core.service.remote.message.MessageC2SInstantOffer;
import stonks.core.service.remote.message.MessageC2SOfferOption;
import stonks.core.service.remote.message.MessageC2SQueryProductOverview;
import stonks.core.service.remote.message.MessageC2SQueryProducts;
import stonks.core.service.remote.message.MessageS2CAddOffer;
import stonks.core.service.remote.message.MessageS2CInstantOffer;
import stonks.core.service.remote.message.MessageS2COfferFilled;
import stonks.core.service.remote.message.MessageS2COfferOption;
import stonks.core.service.remote.message.MessageS2COffersList;
import stonks.core.service.remote.message.MessageS2CQueryProductOverview;
import stonks.core.service.remote.message.MessageS2CQueryProductsPartial;
import stonks.core.service.remote.message.MessagesHandler;

public class StonksRemoteServer {
	private StonksService service;
	private ServerHandler server;
	private MessagesHandler messagesHandler;

	private static record WaitingTask<T>(Task<T> task, Consumer<TaskResult<T>> onFinished) {
	}

	private Queue<WaitingTask<?>> waiting = new ConcurrentLinkedQueue<>();
	private Queue<Offer> filledOffersQueue = new ConcurrentLinkedQueue<>();
	private Map<String, Product> productsLookupMap = new HashMap<>();
	private Task<List<Category>> productsQueryTask;

	public StonksRemoteServer(StonksService service, ServerSocketChannel channel) throws IOException {
		this.service = service;
		this.server = new ServerHandler(channel, this::onNewConnection) {
			@Override
			protected void selectLoop() throws IOException {
				StonksRemoteServer.this.beforeSelectLoop();
				super.selectLoop();
			}
		};

		// We query all products
		productsQueryTask = service.queryAllCategories()
			.afterThatDo(categories -> {
				productsLookupMap.clear();

				for (var category : categories) {
					for (var product : category.getProducts())
						productsLookupMap.put(product.getProductId(), product);
				}

				return categories;
			});

		messagesHandler = new MessagesHandler();

		messagesHandler.registerDeserializer(MessageC2SQueryProducts.ID, MessageC2SQueryProducts::new);
		messagesHandler.listenForMessage(MessageC2SQueryProducts.class, msg -> {
			wait(productsQueryTask, result -> {
				if (!result.isSuccess()) {
					msg.connection()
						.sendRawPacket(new MessageS2CQueryProductsPartial("Query failed").createRawPacket());
					result.getFailure().printStackTrace();
					return;
				}

				var list = (List<Category>) result.getSuccess();
				Product last = null;

				for (var category : list) {
					for (var product : category.getProducts()) {
						if (last != null) msg.connection()
							.sendRawPacket(new MessageS2CQueryProductsPartial(last, false).createRawPacket());
						last = product;
						continue;
					}
				}

				if (last == null) {
					msg.connection()
						.sendRawPacket(new MessageS2CQueryProductsPartial("", "", "", "", "", true).createRawPacket());
				} else {
					msg.connection()
						.sendRawPacket(new MessageS2CQueryProductsPartial(last, true).createRawPacket());
				}
			});
		});

		messagesHandler.registerDeserializer(MessageC2SQueryProductOverview.ID, MessageC2SQueryProductOverview::new);
		messagesHandler.listenForMessage(MessageC2SQueryProductOverview.class, msg -> {
			wait(productsQueryTask, $ -> {
				var product = productsLookupMap.get(msg.message().getProductId());

				if (product == null) {
					var message = new MessageS2CQueryProductOverview(msg.message().getProductId(), "Product not found");
					msg.connection().sendRawPacket(message.createRawPacket());
					return;
				}

				wait(service.queryProductMarketOverview(product), result -> {
					if (!result.isSuccess()) {
						var message = new MessageS2CQueryProductOverview(product.getProductId(), "Query failed");
						result.getFailure().printStackTrace();
						msg.connection().sendRawPacket(message.createRawPacket());
						return;
					}

					var overview = (ProductMarketOverview) result.getSuccess();
					msg.connection().sendRawPacket(new MessageS2CQueryProductOverview(overview).createRawPacket());
				});
			});
		});

		messagesHandler.registerDeserializer(MessageC2SGetOffers.ID, MessageC2SGetOffers::new);
		messagesHandler.listenForMessage(MessageC2SGetOffers.class, msg -> {
			wait(service.getOffers(msg.message().getOfferer()), result -> {
				if (!result.isSuccess()) {
					var message = new MessageS2COffersList(msg.message().getOfferer(), "An error occrued");
					result.getFailure().printStackTrace();
					msg.connection().sendRawPacket(message.createRawPacket());
					return;
				}

				var list = (List<Offer>) result.getSuccess();
				msg.connection().sendRawPacket(new MessageS2COffersList(msg.message().getOfferer(), list)
					.createRawPacket());
			});
		});

		messagesHandler.registerDeserializer(MessageC2SAddOffer.ID, MessageC2SAddOffer::new);
		messagesHandler.listenForMessage(MessageC2SAddOffer.class, msg -> {
			var rid = msg.message().getResponseId();

			wait(productsQueryTask, $ -> {
				var product = productsLookupMap.get(msg.message().getProductId());

				if (product == null) {
					var message = new MessageS2CQueryProductOverview(msg.message().getProductId(), "Product not found");
					msg.connection().sendRawPacket(message.createRawPacket());
					return;
				}

				wait(service.listOffer(msg.message().getOfferer(), product, msg.message().getType(),
					msg.message().getUnits(), msg.message().getPricePerUnit()), result -> {
						if (!result.isSuccess()) {
							var message = new MessageS2CAddOffer(rid, "An error occured");
							result.getFailure().printStackTrace();
							msg.connection().sendRawPacket(message.createRawPacket());
							return;
						}

						msg.connection().sendRawPacket(new MessageS2CAddOffer(rid, result.getSuccess())
							.createRawPacket());
					});
			});
		});

		messagesHandler.registerDeserializer(MessageC2SOfferOption.ID, MessageC2SOfferOption::new);
		messagesHandler.listenForMessage(MessageC2SOfferOption.class, msg -> {
			var rid = msg.message().getResponseId();
			BiFunction<UUID, UUID, Task<Offer>> action = switch (msg.message().getType()) {
			case CLAIM -> service::claimOffer;
			case CANCEL -> service::cancelOffer;
			default -> null;
			};

			if (action == null) {
				msg.connection().sendRawPacket(new MessageS2COfferOption(rid, "Unknown option type").createRawPacket());
				return;
			}

			wait(action.apply(msg.message().getUserId(), msg.message().getOfferId()), result -> {
				if (!result.isSuccess()) {
					msg.connection()
						.sendRawPacket(new MessageS2COfferOption(rid, "An error occured").createRawPacket());
					result.getFailure().printStackTrace();
					return;
				}

				msg.connection().sendRawPacket(new MessageS2COfferOption(rid, result.getSuccess()).createRawPacket());
			});
		});

		messagesHandler.registerDeserializer(MessageC2SInstantOffer.ID, MessageC2SInstantOffer::new);
		messagesHandler.listenForMessage(MessageC2SInstantOffer.class, msg -> {
			var rid = msg.message().getResponseId();
			wait(productsQueryTask, $ -> {
				var product = productsLookupMap.get(msg.message().getProductId());

				if (product == null) {
					var message = new MessageS2CQueryProductOverview(msg.message().getProductId(), "Product not found");
					msg.connection().sendRawPacket(message.createRawPacket());
					return;
				}

				wait(service.instantOffer(product, msg.message().getType(), msg.message().getUnits(),
					msg.message().getBalance()), result -> {
						if (!result.isSuccess()) {
							msg.connection()
								.sendRawPacket(new MessageS2CInstantOffer(rid, "An error occured").createRawPacket());
							result.getFailure().printStackTrace();
							return;
						}

						var execResult = result.getSuccess();
						var message = new MessageS2CInstantOffer(rid, execResult.units(), execResult.balance());
						msg.connection().sendRawPacket(message.createRawPacket());
					});
			});
		});

		service.subscribeToOfferFilledEvents(offer -> {
			filledOffersQueue.add(offer);
		});
	}

	public StonksService getService() { return service; }

	public ServerHandler getServer() { return server; }

	public MessagesHandler getMessagesHandler() { return messagesHandler; }

	protected void onNewConnection(Connection connection) {
		// TODO log new connection to console
		messagesHandler.handleConnection(connection);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void beforeSelectLoop() {
		var iter = waiting.iterator();
		while (iter.hasNext()) {
			var waiting = iter.next();
			var now = waiting.task.get();
			if (now.isEmpty()) continue;
			iter.remove();
			waiting.onFinished().accept((TaskResult) now.get());
		}

		while (!filledOffersQueue.isEmpty()) {
			var head = filledOffersQueue.poll();

			for (var k : this.server.getSelector().keys()) {
				if (k.attachment() == null || !(k.attachment() instanceof Connection c)) continue;
				c.sendRawPacket(new MessageS2COfferFilled(head).createRawPacket());
			}
		}
	}

	public <T> void wait(Task<T> task, Consumer<TaskResult<T>> onFinished) {
		if (task.get().isPresent()) {
			onFinished.accept(task.get().get());
			return;
		}

		waiting.add(new WaitingTask<>(task, onFinished));
	}
}
