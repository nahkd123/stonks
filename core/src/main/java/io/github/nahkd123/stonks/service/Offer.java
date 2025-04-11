/*
 * Copyright (c) 2023-2025 nahkd
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

	record ClaimResult(long claimedUnits, long pendingUnits, boolean remove) {
	}

	@Override
	default int compareTo(Offer o) {
		return switch (type()) {
		case BUY -> Long.compare(o.price(), price());
		case SELL -> Long.compare(price(), o.price());
		};
	}
}
