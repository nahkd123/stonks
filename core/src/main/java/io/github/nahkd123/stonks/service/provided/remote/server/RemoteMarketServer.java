package io.github.nahkd123.stonks.service.provided.remote.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.List;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.utils.net.Connection;
import io.github.nahkd123.stonks.utils.net.Server;

class RemoteMarketServer extends Server {
	private Thread thread;
	private MarketService service;
	private List<InetSocketAddress> addresses;
	boolean running = true;

	public RemoteMarketServer(Thread thread, MarketService service, List<InetSocketAddress> addresses) {
		this.thread = thread;
		this.service = service;
		this.addresses = addresses;
	}

	@Override
	protected ServerSocketChannel createListener() throws IOException {
		ServerSocketChannel listener = ServerSocketChannel.open();
		ServerSocket socket = listener.socket();
		for (InetSocketAddress addr : addresses) socket.bind(addr);
		return listener;
	}

	@Override
	protected Connection createNewConnection() {
		return new RemoteServiceServerConnection(service);
	}

	@Override
	protected boolean shouldKeepRunning() {
		return !thread.isInterrupted();
	}
}
