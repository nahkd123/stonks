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
package io.github.nahkd123.stonks.service.provided.remote;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import io.github.nahkd123.stonks.service.MarketService;

public class RemoteMarketServiceServer {
	private MarketService service;

	public RemoteMarketServiceServer(MarketService service) {
		this.service = service;
	}

	/**
	 * <p>
	 * Run the market service server's I/O loop in current thread until stop signal
	 * returns {@code true}.
	 * </p>
	 * 
	 * @param listener   The listener socket that listen for connections.
	 * @param stopSignal The stop signaler that signal the I/O loop to stop.
	 * @throws IOException If I/O error occurred during server setup phase.
	 */
	public void runServer(ServerSocketChannel listener, BooleanSupplier stopSignal) throws IOException {
		listener.configureBlocking(false);
		try (Selector selector = Selector.open()) {
			SelectionKey acceptKey = listener.register(selector, SelectionKey.OP_ACCEPT);

			while (!stopSignal.getAsBoolean()) {
				boolean didSomething = false;
				if (selector.selectNow() > 0) {
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						iter.remove();

						if (key.isAcceptable()) {
							SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
							clientChannel.configureBlocking(false);
							SelectionKey clientKey = clientChannel.register(
								selector,
								SelectionKey.OP_READ | SelectionKey.OP_WRITE);
							clientKey.attach(new RemoteServiceServerConnection(clientChannel, service));
							didSomething = true;
						}

						try {
							if (key.isReadable()) {
								var connection = (RemoteServiceServerConnection) key.attachment();
								didSomething |= connection.channelRead((ByteChannel) key.channel());
							}

							if (key.isWritable()) {
								var connection = (RemoteServiceServerConnection) key.attachment();
								didSomething |= connection.channelWrite((ByteChannel) key.channel());
							}
						} catch (IOException e) {
							e.printStackTrace();
							if (key.attachment() instanceof RemoteServiceConnection rsc) rsc.close();
							key.cancel();
						}
					}
				}

				for (SelectionKey key : selector.keys()) {
					if (key.attachment() instanceof RemoteServiceConnection rsc && rsc.isClosed()) key.cancel();
				}

				if (!didSomething) LockSupport.parkNanos(1000000L);
			}

			acceptKey.cancel();
			for (SelectionKey key : selector.keys()) {
				if (key.attachment() instanceof RemoteServiceConnection rsc) {
					rsc.close();
					key.cancel();
				}
			}
		}
	}
}
