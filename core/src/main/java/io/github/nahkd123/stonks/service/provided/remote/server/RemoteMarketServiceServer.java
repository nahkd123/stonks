package io.github.nahkd123.stonks.service.provided.remote.server;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.utils.net.Connection;
import io.github.nahkd123.stonks.utils.net.Server;

public class RemoteMarketServiceServer extends Server implements AutoCloseable {
	private MarketService service;
	private boolean running = true;

	public RemoteMarketServiceServer(MarketService service) {
		this.service = service;
	}

	@Override
	protected Connection createNewConnection() {
		return new RemoteServiceServerConnection(service);
	}

	@Override
	protected boolean shouldKeepRunning() {
		return !Thread.currentThread().isInterrupted() && running;
	}

	@Override
	public void close() {
		running = false;
	}
}
