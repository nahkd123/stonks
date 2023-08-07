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
