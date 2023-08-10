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
package stonks.core.exec;

import java.util.Iterator;

import stonks.core.market.Offer;

public class InstantOfferExecutor {
	private double currentBalance;
	private int currentUnits;

	public InstantOfferExecutor(double currentBalance, int currentUnits) {
		this.currentBalance = currentBalance;
		this.currentUnits = currentUnits;
	}

	public double getCurrentBalance() { return currentBalance; }

	public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

	public int getCurrentUnits() { return currentUnits; }

	public void setCurrentUnits(int currentUnits) { this.currentUnits = currentUnits; }

	// TODO send event when offer is filled
	public InstantOfferExecutor executeInstantBuy(Iterator<Offer> sellOffersIterator) {
		while (sellOffersIterator.hasNext()) {
			var offer = sellOffersIterator.next();
			var canBuyUnits = (int) Math.floor(getCurrentBalance() / offer.getPricePerUnit());
			var availableUnits = offer.getAvailableUnits();
			var toBuy = Math.min(Math.min(availableUnits, canBuyUnits), currentUnits);

			currentBalance -= toBuy * offer.getPricePerUnit();
			currentUnits -= toBuy;
			offer.fillOffer(toBuy);

			if (offer.isFilled()) {
				sellOffersIterator.remove();
			} else return this;
		}

		return this;
	}

	public InstantOfferExecutor executeInstantSell(Iterator<Offer> buyOffersIterator) {
		while (buyOffersIterator.hasNext()) {
			var offer = buyOffersIterator.next();
			var availableUnits = offer.getAvailableUnits();
			var toSell = Math.min(availableUnits, currentUnits);

			currentBalance += toSell * offer.getPricePerUnit();
			currentUnits -= toSell;
			offer.fillOffer(toSell);

			if (offer.isFilled()) {
				buyOffersIterator.remove();
			} else return this;
		}

		return this;
	}
}
