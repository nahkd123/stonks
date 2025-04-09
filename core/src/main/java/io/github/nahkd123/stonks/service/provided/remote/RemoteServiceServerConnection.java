package io.github.nahkd123.stonks.service.provided.remote;

import java.nio.channels.ByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.service.provided.remote.packet.QueryCatalog;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.CancelOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.ClaimOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOffer;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOfferStatus;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryUserOffers;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.RemoteOfferData;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.InstantBuyRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.InstantSellRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.PlaceOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.QueryProductOverview;

class RemoteServiceServerConnection extends RemoteServiceConnection implements ServiceNotificationListener {
	private MarketService service;
	private Map<String, Product> catalog = null;

	public RemoteServiceServerConnection(ByteChannel channel, MarketService service) {
		super(channel);
		this.service = service;
		this.service.addNotificationListener(this);
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
