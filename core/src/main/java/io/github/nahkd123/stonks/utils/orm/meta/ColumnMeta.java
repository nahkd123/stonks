package io.github.nahkd123.stonks.utils.orm.meta;

import io.github.nahkd123.stonks.utils.orm.RecordField;
import io.github.nahkd123.stonks.utils.orm.RecordFieldType;
import io.github.nahkd123.stonks.utils.orm.RecordInfo;

public record ColumnMeta(String tableCatalog, String tableSchema, String tableName, String columnName, int dataType, String typeName) {
	public static final RecordInfo<ColumnMeta> RECORD = RecordInfo.of(
		RecordField.of("TABLE_CAT", RecordFieldType.TEXT, ColumnMeta::tableCatalog),
		RecordField.of("TABLE_SCHEM", RecordFieldType.TEXT, ColumnMeta::tableSchema),
		RecordField.of("TABLE_NAME", RecordFieldType.TEXT, ColumnMeta::tableName),
		RecordField.of("COLUMN_NAME", RecordFieldType.TEXT, ColumnMeta::columnName),
		RecordField.of("DATA_TYPE", RecordFieldType.INT, ColumnMeta::dataType),
		RecordField.of("TYPE_NAME", RecordFieldType.TEXT, ColumnMeta::typeName),
		ColumnMeta::new);
}
