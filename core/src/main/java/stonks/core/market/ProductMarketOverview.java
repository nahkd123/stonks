package stonks.core.market;

import stonks.core.product.Product;

public class ProductMarketOverview {
	private Product product;
	private OverviewOffersList buyOffers;
	private OverviewOffersList sellOffers;

	public ProductMarketOverview(Product product, OverviewOffersList buyOffers, OverviewOffersList sellOffers) {
		this.product = product;
		this.buyOffers = buyOffers;
		this.sellOffers = sellOffers;
	}

	public Product getProduct() { return product; }

	public OverviewOffersList getBuyOffers() { return buyOffers; }

	public OverviewOffersList getSellOffers() { return sellOffers; }
}
