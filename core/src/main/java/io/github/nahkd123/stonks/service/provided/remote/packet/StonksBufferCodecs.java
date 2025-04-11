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
package io.github.nahkd123.stonks.service.provided.remote.packet;

import java.util.List;
import java.util.UUID;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public interface StonksBufferCodecs {
	BufferCodec<java.util.UUID> UUID = BufferCodec.I64.asSequence(2).map(
		list -> new UUID(list.get(0), list.get(1)),
		uuid -> List.of(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));

	BufferCodec<SlippageOption> SLIPPAGE = BufferCodec.tupleOf(
		BufferCodec.I64, SlippageOption::targetPrice,
		BufferCodec.F64, SlippageOption::maxRate,
		SlippageOption::new);

	BufferCodec<OfferType> OFFER_TYPE = BufferCodec.I8.map(
		v -> OfferType.values()[v & 0xFF],
		o -> (byte) o.ordinal());

	BufferCodec<Offer.Status> OFFER_STATUS = BufferCodec.tupleOf(
		BufferCodec.I64, Offer.Status::filledUnits,
		BufferCodec.I64, Offer.Status::claimedUnits,
		BufferCodec.BOOL, Offer.Status::removed,
		Offer.Status::new);

	BufferCodec<Offer.ClaimResult> OFFER_CLAIM = BufferCodec.tupleOf(
		BufferCodec.I64, Offer.ClaimResult::claimedUnits,
		BufferCodec.I64, Offer.ClaimResult::pendingUnits,
		BufferCodec.BOOL, Offer.ClaimResult::remove,
		Offer.ClaimResult::new);
}
