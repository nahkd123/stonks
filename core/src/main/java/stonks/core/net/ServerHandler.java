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
package stonks.core.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import stonks.core.net.listener.NewConnectionsListener;

/**
 * <p>
 * Non-blocking IO server socket handler.
 * </p>
 */
public class ServerHandler {
	private ServerSocketChannel server;
	private NewConnectionsListener listener;
	private boolean isRunning = false;
	private Selector selector;

	public ServerHandler(ServerSocketChannel server, NewConnectionsListener handler) throws IOException {
		this.server = server;
		this.listener = handler;
		this.server.configureBlocking(false);
	}

	public ServerSocketChannel getServer() { return server; }

	/**
	 * <p>
	 * Start the server handler on current thread.
	 * </p>
	 * <p>
	 * This will block the current thread until another thread called the stop
	 * method.
	 * </p>
	 * <p>
	 * If the server is already running, this will do nothing.
	 * </p>
	 */
	public void startOnCurrentThread() throws IOException {
		if (isRunning) return;
		isRunning = true;

		selector = Selector.open();
		server.register(selector, SelectionKey.OP_ACCEPT);

		while (isRunning) selectLoop();
		// TODO logger or emit events
	}

	public Thread createThread() {
		var thread = new Thread(() -> {
			try {
				startOnCurrentThread();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.setName("Server Networking Thread");
		return thread;
	}

	protected void selectLoop() throws IOException {
		selector.select();
		var selectionKeys = selector.selectedKeys();
		var selectionKeysIter = selectionKeys.iterator();

		while (selectionKeysIter.hasNext()) {
			var key = selectionKeysIter.next();
			selectionKeysIter.remove();

			if (key.isAcceptable()) accept(key);

			if (key.isReadable()) {
				var attachment = (Connection) key.attachment();
				attachment.handleRead((SocketChannel) key.channel());

				if (attachment.isCloseNeeded()) {
					close(key, attachment);
					continue;
				}
			}

			if (key.isWritable()) {
				var attachment = (Connection) key.attachment();
				attachment.handleWrite((SocketChannel) key.channel());

				if (attachment.isCloseNeeded()) {
					close(key, attachment);
					continue;
				}
			}
		}
	}

	protected void accept(SelectionKey key) throws IOException {
		var keyChannel = (ServerSocketChannel) key.channel();
		var socket = keyChannel.accept();
		socket.configureBlocking(false);

		var attachment = new Connection();
		socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, attachment);
		listener.onNewConnection(attachment);
	}

	protected void close(SelectionKey key, Connection attachment) throws IOException {
		attachment.closeAttachment();
		attachment.setClosed(true);
		attachment.emitClose();
		key.channel().close();
	}

	public void stop() {
		isRunning = false;
		selector.wakeup();
	}
}
