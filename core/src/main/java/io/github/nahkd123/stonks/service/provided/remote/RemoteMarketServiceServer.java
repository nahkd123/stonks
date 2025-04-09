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
