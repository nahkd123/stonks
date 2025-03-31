package io.github.nahkd123.stonks.service;

import java.util.List;

/**
 * <p>
 * A general overview of top offers for specific offer type. This will be used
 * for displaying in menu and setting up instant buy/sell.
 * </p>
 */
public record ProductOffersOverview(OfferType type, long averagePrice, List<OfferOverviewEntry> topOffers) {
}
