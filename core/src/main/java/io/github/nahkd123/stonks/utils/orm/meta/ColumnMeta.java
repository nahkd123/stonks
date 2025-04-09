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
