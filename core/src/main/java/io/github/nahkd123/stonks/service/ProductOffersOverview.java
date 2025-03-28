package io.github.nahkd123.stonks.service;

import java.util.List;

public record ProductOffersOverview(OfferType type, long averagePrice, List<OfferOverviewEntry> topOffers) {
}
