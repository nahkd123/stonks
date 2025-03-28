package io.github.nahkd123.stonks.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Offer extends Comparable<Offer> {
	/**
	 * <p>
	 * Get the ID of this offer. Offer ID is used for referencing.
	 * </p>
	 */
	UUID id();

	/**
	 * <p>
	 * Get the owner's UUID of this offer, or in other words, get the UUID of user
	 * that created this offer.
	 * </p>
	 */
	UUID owner();

	/**
	 * <p>
	 * Get the type of this offer.
	 * </p>
	 */
	OfferType type();

	Product product();

	/**
	 * <p>
	 * Get the price that user had offered for each unit.
	 * </p>
	 */
	long price();

	/**
	 * <p>
	 * Get the total number of units that user had offered.
	 * </p>
	 */
	long totalUnits();

	/**
	 * <p>
	 * Query the current status of this offer. This contains the number of filled
	 * and claimed units.
	 * </p>
	 */
	CompletableFuture<Status> queryStatus();

	/**
	 * <p>
	 * Claim this offer and return claim result, containing the number of units
	 * claimed and whether the offer has been removed from service.
	 * </p>
	 */
	CompletableFuture<ClaimResult> claimOffer();

	/**
	 * <p>
	 * Claim this offer and remove it from service. Offers are always claimed before
	 * cancelling.
	 * </p>
	 */
	CompletableFuture<ClaimResult> cancelOffer();

	record Status(long filledUnits, long claimedUnits, boolean removed) {
	}

	record ClaimResult(long claimedUnits, boolean remove) {
	}

	@Override
	default int compareTo(Offer o) {
		return switch (type()) {
		case BUY -> Long.compare(o.price(), price());
		case SELL -> Long.compare(price(), o.price());
		};
	}
}
