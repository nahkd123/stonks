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
package io.github.nahkd123.stonks.service.provider.remote.packet.offer;

import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.provider.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record RemoteOfferData(UUID id, UUID owner, OfferType type, String productId, long price, long totalUnits) {

	public static final BufferCodec<RemoteOfferData> CODEC = BufferCodec.tupleOf(
		StonksBufferCodecs.UUID, RemoteOfferData::id,
		StonksBufferCodecs.UUID, RemoteOfferData::owner,
		StonksBufferCodecs.OFFER_TYPE, RemoteOfferData::type,
		BufferCodec.UTF8, RemoteOfferData::productId,
		BufferCodec.I64, RemoteOfferData::price,
		BufferCodec.I64, RemoteOfferData::totalUnits,
		RemoteOfferData::new);

	public static RemoteOfferData deriveFrom(Offer offer) {
		UUID id = offer.id();
		UUID owner = offer.owner();
		OfferType type = offer.type();
		String productId = offer.product().getId();
		long price = offer.price();
		long totalUnits = offer.totalUnits();
		return new RemoteOfferData(id, owner, type, productId, price, totalUnits);
	}
}
