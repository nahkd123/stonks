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
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import nahara.common.tasks.Task;
import nahara.common.tasks.TaskResult;
import stonks.core.net.Connection;
import stonks.core.net.ServerHandler;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;
import stonks.core.service.remote.message.MessageC2SQueryProducts;
import stonks.core.service.remote.message.MessageS2CQueryProductsPartial;
import stonks.core.service.remote.message.MessagesHandler;

public class StonksRemoteServer {
	private StonksService service;
	private ServerHandler server;
	private MessagesHandler messagesHandler;

	private static record WaitingTask(Task<?> task, Consumer<TaskResult<?>> onFinished) {
	}

	private Queue<WaitingTask> waiting = new ConcurrentLinkedQueue<>();

	@SuppressWarnings("unchecked")
	public StonksRemoteServer(StonksService service, ServerSocketChannel channel) throws IOException {
		this.service = service;
		this.server = new ServerHandler(channel, this::onNewConnection) {
			@Override
			protected void selectLoop() throws IOException {
				StonksRemoteServer.this.beforeSelectLoop();
				super.selectLoop();
			}
		};

		messagesHandler = new MessagesHandler();
		messagesHandler.registerDeserializer(MessageC2SQueryProducts.ID, MessageC2SQueryProducts::new);
		messagesHandler.listenForMessage(MessageC2SQueryProducts.class, msg -> {
			waiting.add(new WaitingTask(service.queryAllCategories(), result -> {
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
			}));
		});
	}

	public StonksService getService() { return service; }

	public ServerHandler getServer() { return server; }

	public MessagesHandler getMessagesHandler() { return messagesHandler; }

	protected void onNewConnection(Connection connection) {
		// TODO log new connection to console
		messagesHandler.handleConnection(connection);
	}

	protected void beforeSelectLoop() {
		var iter = waiting.iterator();
		while (iter.hasNext()) {
			var waiting = iter.next();
			var now = waiting.task.get();
			if (now.isEmpty()) continue;
			iter.remove();
			waiting.onFinished().accept(now.get());
		}
	}
}
