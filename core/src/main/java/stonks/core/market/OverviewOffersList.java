package stonks.core.market;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OverviewOffersList {
	private OfferType type;
	private List<OverviewOffer> entries;

	public OverviewOffersList(OfferType type, List<OverviewOffer> entries) {
		this.type = type;
		this.entries = entries;

		entries.sort((a, b) -> type.getOfferPriceComparator().compare(a.pricePerUnit(), b.pricePerUnit()));
	}

	public OfferType getType() { return type; }

	/**
	 * <p>
	 * Get an overview of top offers.
	 * </p>
	 * <p>
	 * For sell offers, this will sort by lowest PPU to highest PPU.
	 * </p>
	 * 
	 * @return A sorted list.
	 */
	public List<OverviewOffer> getEntries() { return Collections.unmodifiableList(entries); }

	public Optional<ComputedOffersList> compute() {
		if (getEntries().size() == 0) return Optional.empty();

		var entries = getEntries();
		var max = entries.get(0).pricePerUnit();
		var min = entries.get(0).pricePerUnit();
		var sum = entries.get(0).pricePerUnit() * entries.get(0).totalAvailableUnits();
		var count = entries.get(0).totalAvailableUnits();

		for (int i = 1; i < entries.size(); i++) {
			max = Math.max(max, entries.get(i).pricePerUnit());
			min = Math.min(min, entries.get(i).pricePerUnit());
			sum += entries.get(i).pricePerUnit() * entries.get(i).totalAvailableUnits();
			count += entries.get(i).totalAvailableUnits();
		}

		return Optional.of(new ComputedOffersList(this, sum / count, max, min));
	}
}
