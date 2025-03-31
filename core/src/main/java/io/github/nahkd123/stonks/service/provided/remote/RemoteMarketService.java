package io.github.nahkd123.stonks.service.provided.remote;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.service.provided.remote.message.CatalogMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SimpleQueryMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SpecialMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryUserOffersMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.UserOffersMessage;
import io.github.nahkd123.stonks.utils.EmitHandler;
import io.github.nahkd123.stonks.utils.net.Message;
import io.github.nahkd123.stonks.utils.net.Message.Request;

public class RemoteMarketService extends RemoteServiceConnection implements MarketService, Closeable {
	private EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();

	/**
	 * <p>
	 * Run I/O loop in current thread. This will block the thread until
	 * interruption. This <em>does not close</em> the I/O channel when returning
	 * from this method.
	 * </p>
	 * 
	 * @param The channel that the I/O loop will read from.
	 * @throws IOException if I/O error occurred.
	 */
	public void runInCurrentThread(ByteChannel channel) throws IOException {
		while (!Thread.currentThread().isInterrupted() && isRunning()) {
			boolean didSomething = false;
			didSomething |= handleRead(channel);
			didSomething |= handleWrite(channel);

			if (isCloseRequested()) {
				onConnectionClosing(false);
				setRunning(false);
				return;
			}

			if (!didSomething) LockSupport.parkNanos(1000000L);
		}

		onConnectionClosing(true);
	}

	@Override
	public void addNotificationListener(ServiceNotificationListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeNotificationListener(ServiceNotificationListener listener) {
		listeners.removeListener(listener);
	}

	CompletableFuture<Message> throwOnError(Message.Response response) {
		if (response.error() != null) return CompletableFuture.failedFuture(new ServiceException(response.error()));
		return CompletableFuture.completedFuture(response.message());
	}

	@Override
	public CompletableFuture<Set<? extends Product>> queryCatalog() {
		return request(SimpleQueryMessage.CATALOG)
			.thenCompose(this::throwOnError)
			.thenApply(message -> ((CatalogMessage) message).products().stream()
				.map(p -> new RemoteProduct(this, p))
				.collect(Collectors.toSet()));
	}

	@Override
	public CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid) {
		return request(new QueryUserOffersMessage(uuid))
			.thenCompose(this::throwOnError)
			.thenApply(message -> ((UserOffersMessage) message).offers().stream()
				.map(o -> new RemoteOffer(this, o))
				.collect(Collectors.toSet()));
	}

	@Override
	public CompletableFuture<? extends Offer> queryOffer(UUID id) {
		return request(new QueryOfferMessage(id))
			.thenCompose(this::throwOnError)
			.thenApply(message -> new RemoteOffer(this, (OfferMessage) message));
	}

	@Override
	protected void handleRequest(Request request) {
		// We are not accepting requests from server for now.
	}

	@Override
	protected void handleNotification(Message message) {
		switch (message) {
		case CatalogMessage(Set<CatalogMessage.Product> products):
			listeners.beginEmit(l -> l.onCatalogUpdate(this, products.stream()
				.map(p -> new RemoteProduct(this, p))
				.collect(Collectors.toSet())));
			break;
		case OfferMessage offerMessage:
			listeners.beginEmit(l -> l.onOfferFilled(this, new RemoteOffer(this, offerMessage)));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onConnectionClosing(boolean isRemote) {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws IOException {
		if (!isRunning() || isCloseRequested()) return;
		request(SpecialMessage.BYE).join();
		requestClose();
	}
}
