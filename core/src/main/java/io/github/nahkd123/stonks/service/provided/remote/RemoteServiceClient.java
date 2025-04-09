package io.github.nahkd123.stonks.service.provided.remote;

import java.nio.channels.ByteChannel;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

	public RemoteServiceClient(ByteChannel channel) {
		super(channel);
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
	public CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid) {
		return this.<QueryUserOffers.Response>request(new QueryUserOffers(uuid))
			.thenApply(response -> response.offers().stream()
				.map(offer -> new RemoteOffer(this, offer))
				.collect(Collectors.toUnmodifiableSet()));
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
