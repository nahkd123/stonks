/*
 * Copyright (c) 2023 nahkd
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
package stonks.core.service.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import nahara.common.tasks.Task;
import stonks.core.exec.InstantOfferExecuteResult;
import stonks.core.exec.InstantOfferExecutor;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffer;
import stonks.core.market.OverviewOffersList;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.Emittable;
import stonks.core.service.LocalStonksService;

/**
 * <p>
 * A memory service. Mainly for testing but can also be used as lightweight
 * in-memory Stonks service.
 * </p>
 */
public class StonksMemoryService implements LocalStonksService {
	private static record ProductEntry(MemoryProduct product, List<Offer> buyOffers, List<Offer> sellOffers) {
		public ProductMarketOverview calculateOverview() {
			var buyOffers = collectOffers(OfferType.BUY, 5, this.buyOffers.iterator());
			var sellOffers = collectOffers(OfferType.SELL, 5, this.sellOffers.iterator());
			return new ProductMarketOverview(product, buyOffers, sellOffers);
		}

		public OverviewOffersList collectOffers(OfferType type, int maxEntries, Iterator<Offer> offers) {
			var list = new ArrayList<OverviewOffer>();
			int offerers = 0;
			int totalUnits = 0;
			double currentPrice = 0d;

			while (offers.hasNext() && list.size() < maxEntries) {
				var e = offers.next();

				if (currentPrice == 0d) {
					offerers = 1;
					totalUnits = e.getAvailableUnits();
					currentPrice = e.getPricePerUnit();
				} else if (currentPrice != e.getPricePerUnit()) {
					list.add(new OverviewOffer(offerers, totalUnits, currentPrice));
					offerers = 1;
					totalUnits = e.getAvailableUnits();
					currentPrice = e.getPricePerUnit();
				} else {
					offerers++;
					totalUnits += e.getAvailableUnits();
				}
			}

			if (totalUnits > 0) { list.add(new OverviewOffer(offerers, totalUnits, currentPrice)); }
			return new OverviewOffersList(type, list);
		}
	}

	private List<MemoryCategory> categories = new ArrayList<>();
	private Map<MemoryProduct, ProductEntry> entries = new HashMap<>();
	private Map<UUID, List<Offer>> offers = new HashMap<>();
	private Emittable<Offer> offerFilledEvents = new Emittable<>();

	public List<MemoryCategory> getModifiableCategories() { return categories; }

	public Iterator<Offer> offersIterator() {
		return offers.values().stream().flatMap(v -> v.stream()).iterator();
	}

	@Override
	public Task<List<Category>> queryAllCategories() {
		return Task.resolved(Collections.unmodifiableList(categories));
	}

	@Override
	public Task<ProductMarketOverview> queryProductMarketOverview(Product product) {
		try {
			return Task.resolved(getProductEntry(product).calculateOverview());
		} catch (Throwable t) {
			return Task.failed(t);
		}
	}

	protected ProductEntry getProductEntry(Product product) {
		if (!(product instanceof MemoryProduct mock))
			throw new IllegalArgumentException("StonksMemoryService: Must be MemoryProduct");
		var entry = entries.get(product);
		if (entry == null) entries.put(mock, entry = new ProductEntry(mock, new ArrayList<>(), new ArrayList<>()));
		return entry;
	}

	@Override
	public Task<List<Offer>> getOffers(UUID offerer) {
		var offers = this.offers.get(offerer);
		if (offers == null) this.offers.put(offerer, offers = new ArrayList<>());
		return Task.resolved(Collections.unmodifiableList(offers));
	}

	@Override
	public Task<Offer> claimOffer(Offer offer) {
		var playerOffers = offers.get(offer.getOffererId());
		if (playerOffers == null) return Task.resolved(offer);

		var result = playerOffers.stream()
			.filter(v -> v.getOfferId().equals(offer.getOfferId()))
			.findFirst();

		if (result.isPresent()) {
			result.get().claimOffer();
			offer.setClaimedUnits(result.get().getClaimedUnits());
			offer.setFilledUnits(result.get().getFilledUnits());
			if (result.get().isFullyClaimed())
				playerOffers.removeIf(v -> v.getOfferId().equals(result.get().getOfferId()));
		}

		return Task.resolved(offer);
	}

	@Override
	public Task<Offer> cancelOffer(Offer offer) {
		var playerOffers = offers.get(offer.getOffererId());
		if (playerOffers == null) return Task.resolved(offer); // TODO should we throw?
		playerOffers.removeIf(v -> v.getOfferId().equals(offer.getOfferId()));

		var productEntry = entries.get(offer.getProduct());
		if (productEntry == null) return Task.resolved(offer);
		(offer.getType() == OfferType.BUY ? productEntry.buyOffers : productEntry.sellOffers)
			.removeIf(v -> v.getOfferId().equals(offer.getOfferId()));
		return Task.resolved(offer);
	}

	@Override
	public Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit) {
		var offer = new Offer(UUID.randomUUID(), offerer, product, type, units, 0, 0, pricePerUnit);
		insertOffer(offer);
		return Task.resolved(offer);
	}

	protected void insertOffer(Offer offer) {
		var productEntry = getProductEntry(offer.getProduct());
		if (productEntry == null)
			throw new IllegalArgumentException("StonksMemoryService: Unknown product id: "
				+ offer.getProduct().getProductId());

		var playerOffers = this.offers.get(offer.getOffererId());
		if (playerOffers == null) this.offers.put(offer.getOffererId(), playerOffers = new ArrayList<>());

		var list = offer.getType() == OfferType.BUY ? productEntry.buyOffers : productEntry.sellOffers;
		var searchResult = Collections.binarySearch(list, offer,
			(a, b) -> offer.getType().getOfferPriceComparator().compare(a.getPricePerUnit(), b.getPricePerUnit()));

		// list.add(offer);
		// list.sort((a, b) ->
		// type.getOfferPriceComparator().compare(a.getPricePerUnit(),
		// b.getPricePerUnit()));
		if (searchResult >= 0) {
			list.add(searchResult, offer);
		} else {
			// insertAt = -v - 1
			list.add(-searchResult - 1, offer);
		}

		playerOffers.add(offer);
	}

	@Override
	public Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance) {
		var productEntry = getProductEntry(product);
		if (productEntry == null)
			throw new IllegalArgumentException("StonksMemoryService: Unknown product id: " + product.getProductId());
		var exec = new InstantOfferExecutor(balance, units);

		switch (type) {
		case BUY:
			exec.executeInstantBuy(productEntry.sellOffers.iterator(), offerFilledEvents::emit);
		case SELL:
			exec.executeInstantSell(productEntry.buyOffers.iterator(), offerFilledEvents::emit);
		}

		return Task.resolved(new InstantOfferExecuteResult(exec.getCurrentUnits(), exec.getCurrentBalance()));
	}

	@Override
	public void saveServiceData() {
		// TODO I don't know what else should I do when we are about to save data
		// Lock service? Delete old data? hmm...
	}

	@Override
	public void loadServiceData() {
		// Clear all
		offers.clear();
		entries.values().forEach(v -> {
			v.buyOffers.clear();
			v.sellOffers.clear();
		});
	}

	@Override
	public void subscribeToOfferFilledEvents(Consumer<Offer> consumer) {
		offerFilledEvents.listen(consumer);
	}
}
