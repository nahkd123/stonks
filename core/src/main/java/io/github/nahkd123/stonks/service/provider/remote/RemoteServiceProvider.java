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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

import com.google.auto.service.AutoService;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import io.github.nahkd123.stonks.service.provider.remote.RemoteServiceProvider.Config;
import io.github.nahkd123.stonks.utils.OneOf;
import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec;
import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec.ObjectField;

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
public class RemoteServiceProvider implements MarketServiceProvider<Config> {
	@Override
	public String getProviderName() { return "remote"; }

	@Override
	public DynamicCodec<Config> getConfigCodec() {
		DynamicCodec<Config> baseCompound = DynamicCodec.object(Config::new, Map.of(
			"type", new ObjectField<>(DynamicCodec.STRING, c -> c.type, (c, v) -> c.type = v),
			"address", new ObjectField<>(DynamicCodec.STRING, c -> c.address, (c, v) -> c.address = v),
			"host", new ObjectField<>(DynamicCodec.STRING, c -> c.host, (c, v) -> c.host = v),
			"port", new ObjectField<>(DynamicCodec.INTEGER, c -> c.port, (c, v) -> c.port = v),
			"path", new ObjectField<>(DynamicCodec.STRING, c -> c.path, (c, v) -> c.path = v)));
		return baseCompound.or(DynamicCodec.STRING.map(s->{Config c=new Config();if(s.startsWith("./")){c.type="unix";c.path=s;}else{c.type="tcp";c.address=s;}return c;},null)).map(oneOf->switch(oneOf){case OneOf.First(Config c)->c;case OneOf.Second(Config c)->c;default->throw new IllegalArgumentException("Unexpected value: "+oneOf);},c->new OneOf.First<>(c));
	}

	@Override
	public MarketServiceHost createHost(Config config) {
		try {
			if (config == null) throw new IllegalArgumentException("Configuration must be provided");
			return new Host(config.asSocketAddr());
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

	class Config {
		String type, address, host, path;
		Integer port;

		SocketAddress asSocketAddr() {
			switch (type) {
			case "tcp":
				if (address != null) return parseInetSocketAddress(address);
				if (host != null) {
					if (port == null) throw new IllegalArgumentException("Missing 'port' property");
					return new InetSocketAddress(host, port);
				}
				throw new IllegalArgumentException("Missing 'address' or 'host' property");
			case "unix":
				if (path == null) throw new IllegalArgumentException("Missing 'path' property");
				return UnixDomainSocketAddress.of(path);
			case null:
				throw new IllegalArgumentException("Missing 'type' property");
			default:
				throw new IllegalArgumentException("Unknown socket type: %s".formatted(type));
			}
		}
	}

	class Host implements MarketServiceHost {
		private SocketAddress address;
		private RemoteServiceClient client;
		private CompletableFuture<Void> stopTask;

		public Host(SocketAddress address) throws IOException {
			this.address = address;
		}

		@Override
		public MarketService getService() { return client; }

		@Override
		public void startService() {
			if (client == null) Thread.startVirtualThread(() -> {
				try (SocketChannel channel = SocketChannel.open(address)) {
					channel.configureBlocking(false);
					client = new RemoteServiceClient(channel);
					client.setThreadToUnpark(Thread.currentThread());
					stopTask = new CompletableFuture<>();

					while (!client.isClosed()) {
						boolean b = client.channelRead(channel);
						b |= client.channelWrite(channel);
						if (!b) LockSupport.parkNanos(1000000L);
					}

					stopTask.complete(null);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				} finally {
					client = null;
				}
			});
		}

		@Override
		public void stopService() {
			if (client != null) client.close();
			stopTask.join();
		}
	}
}
