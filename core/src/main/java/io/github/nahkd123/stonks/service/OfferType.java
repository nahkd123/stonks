package io.github.nahkd123.stonks.service;

import io.github.nahkd123.stonks.utils.net.BufferCodec;

public enum OfferType {
	/**
	 * <p>
	 * Buy offer. For placing offers, higher price takes priority.
	 * </p>
	 */
	BUY,
	/**
	 * <p>
	 * Sell offer. For placing offers, lower price takes priority.
	 * </p>
	 */
	SELL;

	public static final BufferCodec<OfferType> CODEC = BufferCodec.ofEnum(values());
}
