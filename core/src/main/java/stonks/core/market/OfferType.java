package stonks.core.market;

import java.util.Comparator;

public enum OfferType {
	BUY((a, b) -> Double.compare(b, a)),
	SELL((a, b) -> Double.compare(a, b));

	private Comparator<Double> offerPriceComparator;

	private OfferType(Comparator<Double> offerPriceComparator) {
		this.offerPriceComparator = offerPriceComparator;
	}

	public Comparator<Double> getOfferPriceComparator() { return offerPriceComparator; }
}
