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
package io.github.nahkd123.stonks.service.provided.sql;

import java.util.UUID;

import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.utils.orm.RecordField;
import io.github.nahkd123.stonks.utils.orm.RecordFieldType;
import io.github.nahkd123.stonks.utils.orm.RecordInfo;

public record OfferRecord(UUID id, UUID owner, OfferType type, String productId, long price, long totalUnits, long filledUnits, long claimedUnits) {

	public static final RecordInfo<OfferRecord> RECORD = RecordInfo.of(
		RecordField.ofPrimary("Id", RecordFieldType.UNIQUE_ID, OfferRecord::id),
		RecordField.of("Owner", RecordFieldType.UNIQUE_ID, OfferRecord::owner),
		RecordField.of("Type", RecordFieldType.ofEnum(OfferType.class, OfferType.values()), OfferRecord::type),
		RecordField.of("ProductId", RecordFieldType.of(String.class, "tinytext"), OfferRecord::productId),
		RecordField.of("Price", RecordFieldType.BIGINT, OfferRecord::price),
		RecordField.of("TotalUnits", RecordFieldType.BIGINT, OfferRecord::totalUnits),
		RecordField.of("FilledUnits", RecordFieldType.BIGINT, OfferRecord::filledUnits),
		RecordField.of("ClaimedUnits", RecordFieldType.BIGINT, OfferRecord::claimedUnits),
		OfferRecord::new);

	public OfferRecord withClaimedUnits(long claimedUnits) {
		return new OfferRecord(id, owner, type, productId, price, totalUnits, filledUnits, claimedUnits);
	}

	public OfferRecord withFilledUnits(long filledUnits) {
		return new OfferRecord(id, owner, type, productId, price, totalUnits, filledUnits, claimedUnits);
	}
}
