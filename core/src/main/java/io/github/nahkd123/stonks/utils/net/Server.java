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

public abstract class Server implements Runnable {
	@Override
	public void run() {
		try {
			ServerSocketChannel listener = createListener();
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
							connection.handleRead(channel);
							didSomething = true;
						}

						if (key.isWritable()) {
							SocketChannel channel = (SocketChannel) key.channel();
							Connection connection = (Connection) key.attachment();
							connection.handleWrite(channel);
							didSomething = true;
						}
					}
				}

				for (SelectionKey key : selector.keys()) {
					if (key.attachment() instanceof Connection conn) {
						didSomething |= conn.loopInServerThread();

						if (!conn.shouldKeepRunning()) {
							key.cancel();
							key.channel().close();
							conn.cleanInServerThread();
						}
					}
				}

				if (!didSomething) LockSupport.parkNanos(1000000L);
			}

			acceptKey.cancel();
			listener.close();

			for (SelectionKey key : selector.keys()) {
				key.cancel();
				key.channel().close();
				if (key.attachment() instanceof Connection conn) conn.cleanInServerThread();
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected abstract ServerSocketChannel createListener() throws IOException;

	protected abstract Connection createNewConnection() throws IOException;

	protected abstract boolean shouldKeepRunning();
}
