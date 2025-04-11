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
package io.github.nahkd123.stonks.service.provider.remote;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import com.google.auto.service.AutoService;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;

/**
 * <p>
 * Remote service provider that provides {@link RemoteServiceClient} upon
 * creating service. The service can connect to server using either TCP or Unix
 * socket.
 * </p>
 * {@snippet :
 * {
 * 	"type": "tcp or unix",
 * 	"address": "hostname:port (if 'host' is absent)",
 * 	"host": "hostname (if 'address' is absent)",
 * 	"port": 7727,
 * 	"path": "path/to/unixsocket (if 'type' is 'unix')"
 * }
 * }
 * <p>
 * Additionally, the above object can be swapped with a single JSON string. If
 * the string starts with {@code ./} or {@code .\}, the address will be
 * interpreted as Unix socket path, other it will be TCP address following the
 * {@code hostname:port} format.
 * </p>
 */
@AutoService(MarketServiceProvider.class)
public class RemoteServiceProvider implements MarketServiceProvider {
	@Override
	public String getProviderName() { return "remote"; }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MarketServiceHost createHost(Object config) {
		SocketAddress addr = null;

		switch (config) {
		case String s:
			addr = (s.startsWith("./") || s.startsWith(".\\"))
				? UnixDomainSocketAddress.of(s)
				: parseInetSocketAddress(s);
			break;
		case Map m:
			String type = (String) m.getOrDefault("type", "tcp");
			addr = switch (type) {
			case "tcp":
				String address = (String) m.get("address");
				String host = (String) m.get("host");

				if (address != null) {
					yield parseInetSocketAddress(address);
				} else if (host != null) {
					Integer port = (Integer) m.get("port");
					if (port == null) throw new IllegalArgumentException("Missing 'port' property");
					yield new InetSocketAddress(host, port);
				}

				throw new IllegalArgumentException("Missing 'address' or 'host' property");
			case "unix":
				String pathStr = (String) m.get("path");
				if (pathStr == null) throw new IllegalArgumentException("Missing 'path' property");
				yield UnixDomainSocketAddress.of(pathStr);
			default:
				throw new IllegalArgumentException("Unknown socket type: " + type);
			};
			break;
		default:
			throw new IllegalArgumentException("Unable to resolve config object: %s".formatted(config));
		}

		try {
			return new Host(addr);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private InetSocketAddress parseInetSocketAddress(String s) {
		String[] split = s.split(":", 2);
		if (split.length != 2) throw new IllegalArgumentException("Missing port number");
		String host = split[0];
		int port = Integer.parseInt(split[1]);
		return new InetSocketAddress(host, port);
	}

	class Host implements MarketServiceHost {
		private SocketAddress address;
		private SocketChannel channel;
		private RemoteServiceClient client;

		public Host(SocketAddress address) throws IOException {
			this.address = address;
		}

		@Override
		public MarketService getService() { return client; }

		@Override
		public void startService() {
			Thread.startVirtualThread(() -> {
				try {
					channel = SocketChannel.open(address);
					channel.configureBlocking(false);
					client = new RemoteServiceClient(channel);
					client.setThreadToUnpark(Thread.currentThread());

					while (!client.isClosed()) {
						boolean b = client.channelRead(channel);
						b |= client.channelWrite(channel);
						if (!b) LockSupport.parkNanos(1000000L);
					}

					channel.close();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} finally {
					channel = null;
					client = null;
				}
			});
		}

		@Override
		public void stopService() {
			if (client != null) client.close();
		}
	}
}
