package io.github.nahkd123.stonks.service.provided.sql;

import io.github.nahkd123.stonks.utils.orm.RecordField;
import io.github.nahkd123.stonks.utils.orm.RecordFieldType;
import io.github.nahkd123.stonks.utils.orm.RecordInfo;

public record ProductRecord(String id) {
	public static final RecordInfo<ProductRecord> RECORD = RecordInfo.of(
		RecordField.ofPrimary("Id", RecordFieldType.of(String.class, "tinytext"), ProductRecord::id),
		ProductRecord::new);
}
