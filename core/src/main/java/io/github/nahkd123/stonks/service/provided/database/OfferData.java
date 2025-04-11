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
package io.github.nahkd123.stonks.service.provided.database;

import static io.github.nahkd123.tableschema.schema.Constraint.defaulted;
import static io.github.nahkd123.tableschema.schema.Constraint.notNull;
import static io.github.nahkd123.tableschema.schema.Constraint.unique;

import java.util.List;
import java.util.UUID;

import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.tableschema.SortOrder;
import io.github.nahkd123.tableschema.query.Filter;
import io.github.nahkd123.tableschema.query.SortBy;
import io.github.nahkd123.tableschema.schema.Field;
import io.github.nahkd123.tableschema.schema.Index;
import io.github.nahkd123.tableschema.schema.Schema;
import io.github.nahkd123.tableschema.schema.type.FieldType;

record OfferData(UUID id, UUID owner, OfferType type, String productId, long price, long totalUnits, long filledUnits, long claimedUnits) {

	// @formatter:off
	public static final Field<OfferData, UUID> OWNER = new Field<>(FieldType.UUID, "Owner", OfferData::owner);
	public static final Field<OfferData, OfferType> TYPE = new Field<>(FieldType.ofEnum(OfferType.values()), "Type", OfferData::type);
	public static final Field<OfferData, String> PRODUCT_ID = new Field<>(FieldType.fixedString(50), "ProductId", OfferData::productId);
	public static final Field<OfferData, Long> PRICE = new Field<>(FieldType.LONG, "Price", OfferData::price);
	// @formatter:on

	public static final Schema<UUID, OfferData> SCHEMA = Schema.of(
		new Field<>(FieldType.UUID, "Id", OfferData::id).with(notNull()).with(unique()),
		OWNER.with(notNull()),
		TYPE.with(notNull()),
		PRODUCT_ID.with(notNull()),
		PRICE.with(notNull()),
		new Field<>(FieldType.LONG, "TotalUnits", OfferData::totalUnits).with(notNull()),
		new Field<>(FieldType.LONG, "FilleUnits", OfferData::filledUnits).with(notNull()).with(defaulted(0L)),
		new Field<>(FieldType.LONG, "ClaimedUnits", OfferData::claimedUnits).with(notNull()).with(defaulted(0L)),
		OfferData::new)
		.withVersion(0) // TODO change this when updating schema
		.withIndexes(List.of(
			new Index<OfferData>("ByBuyOffers")
				.appendField(PRODUCT_ID)
				.appendFilter(Filter.eq(TYPE, OfferType.BUY))
				.appendOrdering(new SortBy<>(PRICE, SortOrder.DESCENDING)),
			new Index<OfferData>("BySellOffers")
				.appendField(PRODUCT_ID)
				.appendFilter(Filter.eq(TYPE, OfferType.SELL))
				.appendOrdering(new SortBy<>(PRICE, SortOrder.ASCENDING)),
			new Index<OfferData>("ByOwner")
				.appendField(OWNER)));

	public OfferData withClaimedUnits(long claimedUnits) {
		return new OfferData(id, owner, type, productId, price, totalUnits, filledUnits, claimedUnits);
	}

	public OfferData withFilledUnits(long filledUnits) {
		return new OfferData(id, owner, type, productId, price, totalUnits, filledUnits, claimedUnits);
	}
}
