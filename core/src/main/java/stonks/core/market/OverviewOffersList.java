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
	 * For sell offers, this will sort from lowest PPU to highest PPU.
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
