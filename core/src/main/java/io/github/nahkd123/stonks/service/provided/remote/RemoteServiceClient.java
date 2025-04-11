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
package io.github.nahkd123.stonks.service.provided.remote;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.service.provided.remote.packet.QueryCatalog;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOffer;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryUserOffers;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.RemoteOfferData;
import io.github.nahkd123.stonks.utils.EmitHandler;

public class RemoteServiceClient extends RemoteServiceConnection implements MarketService {
	private EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();
	private Thread threadToUnpark;

	public RemoteServiceClient(ByteChannel channel) {
		super(channel);
	}

	public void setThreadToUnpark(Thread threadToUnpark) { this.threadToUnpark = threadToUnpark; }

	@Override
	protected void queueRawPacketWrite(PacketMode mode, int type, int reqId, Consumer<ByteBuffer> writer) {
		super.queueRawPacketWrite(mode, type, reqId, writer);
		if (threadToUnpark != null) LockSupport.unpark(threadToUnpark);
	}

	<T> CompletableFuture<T> request(Object request) {
		return queueRequest(request);
	}

	@Override
	protected void onRequest(Request request) throws Throwable {
		request.responseFailure("Client does not accept requests");
	}

	@Override
	protected void onNotification(Object data) {
		switch (data) {
		case QueryCatalog.Response(List<String> productIds): {
			Set<RemoteProduct> catalog = productIds.stream()
				.map(id -> new RemoteProduct(this, id))
				.collect(Collectors.toUnmodifiableSet());
			listeners.beginEmit(listener -> listener.onCatalogUpdate(this, catalog));
			break;
		}
		case RemoteOfferData offerData: {
			RemoteOffer offer = new RemoteOffer(this, offerData);
			listeners.beginEmit(listener -> listener.onOfferFilled(this, offer));
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void addNotificationListener(ServiceNotificationListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeNotificationListener(ServiceNotificationListener listener) {
		listeners.removeListener(listener);
	}

	@Override
	public CompletableFuture<Set<? extends Product>> queryCatalog() {
		return this.<QueryCatalog.Response>request(new QueryCatalog())
			.thenApply(response -> response.productIds().stream()
				.map(id -> new RemoteProduct(this, id))
				.collect(Collectors.toUnmodifiableSet()));
	}

	@Override
	public CompletableFuture<List<? extends Offer>> queryUserOffers(UUID uuid) {
		return this.<QueryUserOffers.Response>request(new QueryUserOffers(uuid))
			.thenApply(response -> response.offers().stream()
				.map(offer -> new RemoteOffer(this, offer))
				.toList());
	}

	@Override
	public CompletableFuture<? extends Offer> queryOffer(UUID id) {
		return this.<RemoteOfferData>request(new QueryOffer(id))
			.thenApply(offer -> new RemoteOffer(this, offer));
	}

	@Override
	public String toString() {
		return "RemoteServiceClient(%s)".formatted(getChannel());
	}
}
