package io.github.nahkd123.stonks.impl.service.sql;

import io.github.nahkd123.stonks.impl.orm.RecordField;
import io.github.nahkd123.stonks.impl.orm.RecordFieldType;
import io.github.nahkd123.stonks.impl.orm.RecordInfo;

public record ProductRecord(String id) {
	public static final RecordInfo<ProductRecord> RECORD = RecordInfo.of(
		RecordField.ofPrimary("Id", RecordFieldType.of(String.class, "tinytext"), ProductRecord::id),
		ProductRecord::new);
}
