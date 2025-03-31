package io.github.nahkd123.stonks.service.provided.remote.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.service.provided.remote.QueryProductOverviewMessage;
import io.github.nahkd123.stonks.service.provided.remote.RemoteServiceConnection;
import io.github.nahkd123.stonks.service.provided.remote.message.CatalogMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SimpleQueryMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.SpecialMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.CancelOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.ClaimOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferClaimResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.OfferStatusMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.PlaceOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryOfferStatusMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.QueryUserOffersMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.SlippageMessageData;
import io.github.nahkd123.stonks.service.provided.remote.message.offer.UserOffersMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantBuyMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantOfferResultMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.InstantSellMessage;
import io.github.nahkd123.stonks.service.provided.remote.message.product.ProductOverviewMessage;
import io.github.nahkd123.stonks.utils.net.Message;
import io.github.nahkd123.stonks.utils.net.Message.Request;

class RemoteServiceServerConnection extends RemoteServiceConnection implements ServiceNotificationListener {
	private MarketService service;
	private Map<String, Product> idToProduct = new HashMap<>();
	private Map<UUID, Offer> offerCache = new WeakHashMap<>();

	public RemoteServiceServerConnection(MarketService service) {
		this.service = service;
		this.service.addNotificationListener(this);
	}

	@Override
	protected void handleRequest(Request request) {
		switch (request.message()) {
		case SpecialMessage special:
			handleSpecialRequest(request, special);
			break;
		case SimpleQueryMessage queryType:
			handleSimpleQueryRequest(request, queryType);
			break;
		case QueryProductOverviewMessage(String productId):
			getProduct(productId)
				.thenCompose(Product::queryOverview)
				.thenAccept(overview -> responseSucceed(request, new ProductOverviewMessage(overview)))
				.exceptionally(e -> handleException(request, e));
			break;
		case InstantBuyMessage(String productId, long balance, long units, Optional<SlippageMessageData> slippage):
			getProduct(productId)
				.thenCompose(p -> p.instantBuy(balance, units, slippage.map(SlippageMessageData::option).orElse(null)))
				.thenAccept(result -> responseSucceed(request, InstantOfferResultMessage.from(result)))
				.exceptionally(e -> handleException(request, e));
			break;
		case InstantSellMessage(String productId, long units, Optional<SlippageMessageData> slippage):
			getProduct(productId)
				.thenCompose(p -> p.instantSell(units, slippage.map(SlippageMessageData::option).orElse(null)))
				.thenAccept(result -> responseSucceed(request, InstantOfferResultMessage.from(result)))
				.exceptionally(e -> handleException(request, e));
			break;
		case PlaceOfferMessage(String productId, UUID owner, OfferType type, long price, long units):
			getProduct(productId)
				.thenCompose(p -> p.placeOffer(owner, type, price, units))
				.thenAccept(offer -> {
					offerCache.put(offer.id(), offer);
					responseSucceed(request, new OfferMessage(offer));
				})
				.exceptionally(e -> handleException(request, e));
			break;
		case QueryOfferMessage(UUID id):
			service.queryOffer(id)
				.thenAccept(offer -> {
					offerCache.put(offer.id(), offer);
					responseSucceed(request, new OfferMessage(offer));
				})
				.exceptionally(e -> handleException(request, e));
			break;
		case QueryOfferStatusMessage(UUID id):
			cachedOrGet(id)
				.thenCompose(Offer::queryStatus)
				.thenAccept(status -> responseSucceed(request, new OfferStatusMessage(id, status)))
				.exceptionally(e -> handleException(request, e));
			break;
		case ClaimOfferMessage(UUID id):
			cachedOrGet(id)
				.thenCompose(Offer::claimOffer)
				.thenAccept(result -> responseSucceed(request, new OfferClaimResultMessage(id, result)))
				.exceptionally(e -> handleException(request, e));
			break;
		case CancelOfferMessage(UUID id):
			cachedOrGet(id)
				.thenCompose(Offer::cancelOffer)
				.thenAccept(result -> {
					offerCache.remove(id);
					responseSucceed(request, new OfferClaimResultMessage(id, result));
				})
				.exceptionally(e -> handleException(request, e));
			break;
		case QueryUserOffersMessage(UUID uuid):
			service.queryUserOffers(uuid)
				.thenAccept(offers -> {
				// @formatter:off
					for (Offer offer : offers) offerCache.put(offer.id(), offer);
					responseSucceed(request, new UserOffersMessage(uuid, offers.stream()
						.map(o -> new OfferMessage(
							o.id(), o.owner(), o.type(), o.product().getId(),
							o.price(), o.totalUnits())).collect(Collectors.toSet())));
					// @formatter:on
				})
				.exceptionally(e -> handleException(request, e));
			break;
		default:
			responseFailed(request, "Not implemented: %s".formatted(request.message()));
			break;
		}
	}

	private void handleSpecialRequest(Request request, SpecialMessage special) {
		switch (special) {
		case BYE:
			responseSucceed(request, SpecialMessage.BYE);
			requestClose();
			break;
		case PING:
			responseSucceed(request, SpecialMessage.PONG);
			break;
		default:
			responseFailed(request, "Not implemented: %s".formatted(special));
			break;
		}
	}

	private void handleSimpleQueryRequest(Request request, SimpleQueryMessage queryType) {
		switch (queryType) {
		case CATALOG:
			service.queryCatalog()
				.thenAccept(catalog -> {
					updateCatalog(catalog);
					var mapped = catalog.stream()
						.map(p -> new CatalogMessage.Product(p.getId()))
						.collect(Collectors.toSet());
					responseSucceed(request, new CatalogMessage(mapped));
				})
				.exceptionally(e -> handleException(request, e));
			break;
		default:
			responseFailed(request, "Not implemented: %s".formatted(queryType));
			break;
		}
	}

	private void updateCatalog(Set<? extends Product> catalog) {
		idToProduct.clear();
		offerCache.clear();
		for (Product p : catalog) idToProduct.put(p.getId(), p);
	}

	private CompletableFuture<Product> getProduct(String productId) {
		Product product = idToProduct.get(productId);
		if (product != null) return CompletableFuture.completedFuture(product);
		// TODO obtain product from service
		return CompletableFuture.failedFuture(
			new ServiceException("Either the product with ID %s doesn't exists, or you haven't query catalog yet"
				.formatted(productId)));
	}

	private CompletableFuture<Offer> cachedOrGet(UUID id) {
		Offer offer = offerCache.get(id);
		if (offer != null) return CompletableFuture.completedFuture(offer);
		return service.queryOffer(id).thenApply(o -> {
			offerCache.put(id, o);
			return o;
		});
	}

	private Void handleException(Message.Request request, Throwable e) {
		e.printStackTrace();
		responseFailed(request, e.getMessage());
		return null;
	}

	@Override
	protected void handleNotification(Message message) {
		// We don't take notification from client for now.
	}

	@Override
	protected void onConnectionClosing(boolean isRemote) {
		service.removeNotificationListener(this);
		service = null;
	}

	@Override
	public void onCatalogUpdate(MarketService sender, Set<? extends Product> products) {
		updateCatalog(products);
		var mapped = products.stream()
			.map(p -> new CatalogMessage.Product(p.getId()))
			.collect(Collectors.toSet());
		notify(new CatalogMessage(mapped));
	}

	@Override
	public void onOfferFilled(MarketService sender, Offer offer) {
		offerCache.put(offer.id(), offer);
		notify(new OfferMessage(offer));
	}
}
