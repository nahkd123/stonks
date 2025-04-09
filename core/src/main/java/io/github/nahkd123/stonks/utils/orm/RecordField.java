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
package io.github.nahkd123.stonks.utils.orm;

import java.util.function.Function;

public record RecordField<R, T>(String name, RecordFieldType<?, T> type, Function<R, T> getter, boolean primaryKey, T defaultValue) {
	public static <R, T> RecordField<R, T> ofPrimary(String name, RecordFieldType<?, T> type, Function<R, T> getter) {
		return new RecordField<>(name, type, getter, true, null);
	}

	public static <R, T> RecordField<R, T> of(String name, RecordFieldType<?, T> type, Function<R, T> getter) {
		return new RecordField<>(name, type, getter, false, null);
	}

	public RecordField<R, T> withDefault(T defaultValue) {
		return new RecordField<>(name, type, getter, primaryKey, defaultValue);
	}

	public String sqlTableEntry() {
		String sql = "[%s] %s".formatted(name, type.typeName());
		if (primaryKey) sql += " PRIMARY KEY";
		return sql;
	}
}
