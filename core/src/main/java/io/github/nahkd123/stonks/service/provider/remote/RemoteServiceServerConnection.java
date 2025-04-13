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

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.service.provider.remote.packet.QueryCatalog;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.CancelOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.ClaimOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.QueryOffer;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.QueryOfferStatus;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.QueryUserOffers;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.RemoteOfferData;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.InstantBuyRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.InstantSellRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.PlaceOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.QueryProductOverview;

class RemoteServiceServerConnection extends RemoteServiceConnection implements ServiceNotificationListener {
	private MarketService service;
	private Map<String, Product> catalog = null;
	private Thread serverThread;

	public RemoteServiceServerConnection(ByteChannel channel, MarketService service, Thread serverThread) {
		super(channel);
		this.service = service;
		this.serverThread = serverThread;
		this.service.addNotificationListener(this);
	}

	@Override
	protected void queueRawPacketWrite(PacketMode mode, int type, int reqId, Consumer<ByteBuffer> writer) {
		super.queueRawPacketWrite(mode, type, reqId, writer);
		LockSupport.unpark(serverThread);
	}

	@Override
	public void onCatalogUpdate(MarketService sender, Set<? extends Product> products) {
		catalog = products.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
		queueNotification(new QueryCatalog.Response(List.copyOf(catalog.keySet())));
	}

	@Override
	public void onOfferFilled(MarketService sender, Offer offer) {
		queueNotification(RemoteOfferData.deriveFrom(offer));
	}

	@Override
	protected void onRequest(Request request) throws Throwable {
		switch (request.packet()) {

		// Root
		case QueryCatalog():
			service.queryCatalog()
				.thenAccept(products -> {
					catalog = products.stream().collect(Collectors.toMap(v -> v.getId(), v -> v));
					request.responseSuccess(new QueryCatalog.Response(List.copyOf(catalog.keySet())));
				})
				.exceptionally(t -> handleException(request, t));
			break;

		// Product
		case QueryProductOverview(String productId):
			getProduct(productId).queryOverview()
				.thenAccept(overview -> request.responseSuccess(overview))
				.exceptionally(t -> handleException(request, t));
			break;
		case InstantBuyRequest(String productId, long balance, long units, SlippageOption slippage):
			getProduct(productId).instantBuy(balance, units, slippage)
				.thenAccept(result -> request.responseSuccess(result))
				.exceptionally(t -> handleException(request, t));
			break;
		case InstantSellRequest(String productId, long units, SlippageOption slippage):
			getProduct(productId).instantSell(units, slippage)
				.thenAccept(result -> request.responseSuccess(result))
				.exceptionally(t -> handleException(request, t));
			break;
		case PlaceOfferRequest(String productId, UUID owner, OfferType type, long price, long units):
			getProduct(productId).placeOffer(owner, type, price, units)
				.thenApply(RemoteOfferData::deriveFrom)
				.thenAccept(offer -> request.responseSuccess(new PlaceOfferRequest.Response(offer)))
				.exceptionally(t -> handleException(request, t));
			break;

		// Offer
		case QueryOffer(UUID id):
			service.queryOffer(id)
				.thenAccept(offer -> request.responseSuccess(RemoteOfferData.deriveFrom(offer)))
				.exceptionally(t -> handleException(request, t));
			break;
		case QueryUserOffers(UUID uuid):
			service.queryUserOffers(uuid)
				.thenApply(offers -> offers.stream().map(RemoteOfferData::deriveFrom).toList())
				.thenAccept(offers -> request.responseSuccess(new QueryUserOffers.Response(offers)))
				.exceptionally(t -> handleException(request, t));
			break;
		case QueryOfferStatus(UUID id):
			service.queryOffer(id)
				.thenCompose(Offer::queryStatus)
				.thenAccept(status -> request.responseSuccess(status))
				.exceptionally(t -> handleException(request, t));
			break;
		case ClaimOfferRequest(UUID id):
			service.queryOffer(id)
				.thenCompose(Offer::claimOffer)
				.thenAccept(status -> request.responseSuccess(status))
				.exceptionally(t -> handleException(request, t));
			break;
		case CancelOfferRequest(UUID id):
			service.queryOffer(id)
				.thenCompose(Offer::cancelOffer)
				.thenAccept(status -> request.responseSuccess(status))
				.exceptionally(t -> handleException(request, t));
			break;

		default:
			request.responseFailure("Not implemented");
			break;
		}
	}

	private Product getProduct(String id) {
		if (catalog == null) throw new ServiceException("You must query catalog first");
		Product product = catalog.get(id);
		if (product == null) throw new ServiceException("Product with ID %s not found".formatted(id));
		return product;
	}

	private Void handleException(Request request, Throwable t) {
		if (t instanceof CompletionException e && e.getCause() != null) t = e.getCause();
		if (t instanceof ServiceException sve) {
			request.responseFailure(sve.getMessage());
		} else {
			t.printStackTrace();
			request.responseFailure("Internal server error");
		}

		return null;
	}

	@Override
	protected void onClose(boolean remote, Throwable error) {
		service.removeNotificationListener(this);
		super.onClose(remote, error);
	}
}
