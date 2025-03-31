package io.github.nahkd123.stonks.service.provided.remote.server;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.MarketService;

public class RemoteMarketServiceServerThread extends Thread {
	private MarketService service;
	private List<InetSocketAddress> addresses;
	private CompletableFuture<Void> serverStartTask = null;

	public RemoteMarketServiceServerThread(MarketService service, List<InetSocketAddress> addresses) {
		this.service = service;
		this.addresses = addresses;
		setName("Market Service Server");
	}

	public CompletableFuture<Void> startServer() {
		if (serverStartTask != null) throw new IllegalStateException("Server already started or is starting");
		serverStartTask = new CompletableFuture<>();
		start();
		return serverStartTask;
	}

	@Override
	public void run() {
		RemoteMarketServer server = new RemoteMarketServer(this, service, addresses);
		serverStartTask.complete(null);
		server.run();
	}
}
