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
package io.github.nahkd123.stonks.cli;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.provider.remote.RemoteMarketServiceServer;

public class MarketServerInfo {
	private ProtocolFamily protocolFamily;
	private SocketAddress listenerAddress;
	private boolean running = false;
	private CompletableFuture<Void> stopTask = new CompletableFuture<>();

	public MarketServerInfo(ProtocolFamily protocolFamily, SocketAddress listenerAddress) {
		this.protocolFamily = protocolFamily;
		this.listenerAddress = listenerAddress;
	}

	public void startServer(MarketService service) {
		if (running) throw new IllegalStateException("Server is already running");

		Thread.startVirtualThread(() -> {
			try {
				running = true;
				RemoteMarketServiceServer server = new RemoteMarketServiceServer(service);
				ServerSocketChannel listener = protocolFamily != null
					? ServerSocketChannel.open(protocolFamily)
					: ServerSocketChannel.open();
				listener.bind(listenerAddress);
				server.runServer(listener, () -> !running);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (listenerAddress instanceof UnixDomainSocketAddress unix) {
					try {
						Files.deleteIfExists(unix.getPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				running = false;
				stopTask.complete(null);
			}
		});
	}

	public void stopServer() {
		running = false;
		stopTask.join();
	}

	@Override
	public String toString() {
		return listenerAddress.toString();
	}
}
