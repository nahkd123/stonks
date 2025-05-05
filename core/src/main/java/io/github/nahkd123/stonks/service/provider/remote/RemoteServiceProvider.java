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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.auto.service.AutoService;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import io.github.nahkd123.stonks.service.provider.remote.RemoteServiceProvider.Config;

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
	public Codec<Config> getConfigCodec() { return Config.CODEC; }

	@Override
	public MarketServiceHost createHost(Config config) {
		try {
			if (config == null) throw new IllegalArgumentException("Configuration must be provided");
			return new Host(config.socketAddress());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	interface Config {
		Type type();

		SocketAddress socketAddress();

		Codec<Config> STRING_CODEC = Codec.either(Tcp.STRING_CODEC, Unix.STRING_CODEC).xmap(
			e -> e.left().map(v -> (Config) v).or(e::right).get(),
			c -> c instanceof Tcp tcp ? Either.left(tcp) : Either.right((Unix) c));
		MapCodec<Config> MAP_CODEC = Type.CODEC.dispatchMap("type", Config::type, Type::getMapCodec);
		Codec<Config> CODEC = Codec.either(STRING_CODEC, MAP_CODEC.codec()).xmap(
			e -> e.left().or(e::right).get(),
			Either::right);

		enum Type {
			TCP(Tcp.MAP_CODEC),
			UNIX(Unix.MAP_CODEC);

			private MapCodec<? extends Config> mapCodec;

			private Type(MapCodec<? extends Config> mapCodec) {
				this.mapCodec = mapCodec;
			}

			public MapCodec<? extends Config> getMapCodec() { return mapCodec; }

			static final Codec<Type> CODEC = Codec.STRING
				.xmap(v -> v.toUpperCase(), v -> v.toLowerCase())
				.xmap(Type::valueOf, Type::toString);
		}

		record Tcp(String host, int port) implements Config {
			static final Pattern ADDRESS_PATTERN = Pattern.compile("^(?<host>[A-Za-z0-9._-]+?):(?<port>\\d+)$");
			static final Codec<Tcp> STRING_CODEC = Codec.STRING.comapFlatMap(
				Tcp::fromAddress,
				c -> "%s:%s".formatted(c.host, c.port));
			static final MapCodec<Tcp> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.fieldOf("host").forGetter(Tcp::host),
				Codec.INT.fieldOf("port").forGetter(Tcp::port))
				.apply(i, Tcp::new));

			static DataResult<Tcp> fromAddress(String addr) {
				Matcher matcher = ADDRESS_PATTERN.matcher(addr);

				if (matcher.matches()) {
					String host = matcher.group("host");
					int port = Integer.parseInt(matcher.group("port"));
					return DataResult.success(new Tcp(host, port));
				}

				return DataResult.error(() -> "%s does not follow <host>:<port> format".formatted(addr));
			}

			@Override
			public Type type() {
				return Type.TCP;
			}

			@Override
			public SocketAddress socketAddress() {
				try {
					return new InetSocketAddress(InetAddress.getByName(host), port);
				} catch (UnknownHostException e) {
					throw new RuntimeException("Unable to resolve %s".formatted(host), e);
				}
			}
		}

		record Unix(Path path) implements Config {
			static final Codec<Path> PATH_CODEC = Codec.STRING.xmap(s -> Path.of(s), p -> p.toString());
			static final Codec<Unix> STRING_CODEC = PATH_CODEC.xmap(Unix::new, Unix::path);
			static final MapCodec<Unix> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				PATH_CODEC.fieldOf("path").forGetter(Unix::path))
				.apply(i, Unix::new));

			@Override
			public Type type() {
				return Type.UNIX;
			}

			@Override
			public SocketAddress socketAddress() {
				return UnixDomainSocketAddress.of(path);
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
