package io.github.nahkd123.stonks.utils.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;

public abstract class Server {
	/**
	 * <p>
	 * Run the I/O loop in current thread. This <em>will not close the listener
	 * </em> when returning from this method.
	 * </p>
	 * 
	 * @param listener The listener that will be used to accept incoming
	 *                 connections.
	 */
	public void runInCurrentThread(ServerSocketChannel listener) {
		try {
			listener.configureBlocking(false);
			Selector selector = SelectorProvider.provider().openSelector();
			SelectionKey acceptKey = listener.register(selector, SelectionKey.OP_ACCEPT);

			while (shouldKeepRunning()) {
				boolean didSomething = false;

				if (selector.selectNow() > 0) {
					Iterator<SelectionKey> keysIter = selector.selectedKeys().iterator();

					while (keysIter.hasNext()) {
						SelectionKey key = keysIter.next();
						keysIter.remove();

						if (key.isAcceptable()) {
							ServerSocketChannel listener2 = (ServerSocketChannel) key.channel();
							SocketChannel channel = listener2.accept();
							channel.configureBlocking(false);
							SelectionKey rwKey = channel.register(
								selector,
								SelectionKey.OP_READ | SelectionKey.OP_WRITE);
							Connection connection = createNewConnection();
							rwKey.attach(connection);
							didSomething = true;
						}

						if (key.isReadable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							Connection connection = (Connection) key.attachment();
							didSomething |= connection.handleRead(channel);
						}

						if (key.isWritable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							Connection connection = (Connection) key.attachment();
							didSomething |= connection.handleWrite(channel);
						}
					}
				}

				for (SelectionKey key : selector.keys()) {
					if (key.attachment() instanceof Connection conn) {
						if (conn.isCloseRequested()) {
							conn.running = false;
							conn.onConnectionClosing(false);
						}

						if (!conn.running) {
							key.cancel();
							key.channel().close();
						}
					}
				}

				if (!didSomething) LockSupport.parkNanos(1000000L);
			}

			acceptKey.cancel();

			for (SelectionKey key : selector.keys()) {
				key.cancel();
				key.channel().close();

				if (key.attachment() instanceof Connection conn) {
					if (!conn.running) continue;
					conn.running = false;
					conn.onConnectionClosing(false);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected abstract Connection createNewConnection() throws IOException;

	protected abstract boolean shouldKeepRunning();
}
