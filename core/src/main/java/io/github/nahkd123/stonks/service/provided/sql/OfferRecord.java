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
