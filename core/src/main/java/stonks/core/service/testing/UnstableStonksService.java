package stonks.core.service.testing;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import nahara.common.tasks.Task;
import stonks.core.exec.InstantOfferExecuteResult;
import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.market.ProductMarketOverview;
import stonks.core.product.Category;
import stonks.core.product.Product;
import stonks.core.service.StonksService;

/**
 * <p>
 * Simulate service calls in unstable environment. Stonks must be able to handle
 * unexpected results from service.
 * </p>
 */
public class UnstableStonksService implements StonksService {
	private StonksService underlying;
	private double failRate;
	private long maxLag;

	public UnstableStonksService(StonksService underlying, double failRate, long maxLag) {
		this.underlying = underlying;
		this.failRate = failRate;
		this.maxLag = maxLag;
	}

	public StonksService getUnderlying() { return underlying; }

	protected <T> Task<T> wrapTask(Task<T> task) {
		var rng = new Random();
		var shouldFail = rng.nextDouble() < failRate;
		var waitMs = rng.nextLong(maxLag);

		return task
			.andThen(t -> Task.async(() -> {
				Thread.sleep(rng.nextLong(waitMs));
				return t;
			}, ForkJoinPool.commonPool()))
			.afterThatDo(t -> {
				if (shouldFail) throw new UnstableException("Simulated failure (" + UnstableStonksService.class + ")");
				return t;
			});
	}

	@Override
	public Task<List<Category>> queryAllCategories() {
		return wrapTask(underlying.queryAllCategories());
	}

	@Override
	public Task<ProductMarketOverview> queryProductMarketOverview(Product product) {
		return wrapTask(underlying.queryProductMarketOverview(product));
	}

	@Override
	public Task<List<Offer>> getOffers(UUID offerer) {
		return wrapTask(underlying.getOffers(offerer));
	}

	@Override
	public Task<Offer> claimOffer(Offer offer) {
		return wrapTask(underlying.claimOffer(offer));
	}

	@Override
	public Task<Offer> cancelOffer(Offer offer) {
		return wrapTask(underlying.cancelOffer(offer));
	}

	@Override
	public Task<Offer> listOffer(UUID offerer, Product product, OfferType type, int units, double pricePerUnit) {
		return wrapTask(underlying.listOffer(offerer, product, type, units, pricePerUnit));
	}

	@Override
	public Task<InstantOfferExecuteResult> instantOffer(Product product, OfferType type, int units, double balance) {
		return wrapTask(underlying.instantOffer(product, type, units, balance));
	}

}
