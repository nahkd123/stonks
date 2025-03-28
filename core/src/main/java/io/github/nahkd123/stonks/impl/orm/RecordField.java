package io.github.nahkd123.stonks.impl.orm;

import java.util.function.Function;

public record RecordField<R, T>(String name, RecordFieldType<?, T> type, Function<R, T> getter, boolean primaryKey, boolean useNull, T defaultValue) {
	public static <R, T> RecordField<R, T> ofPrimary(String name, RecordFieldType<?, T> type, Function<R, T> getter) {
		return new RecordField<>(name, type, getter, true, false, null);
	}

	public static <R, T> RecordField<R, T> of(String name, RecordFieldType<?, T> type, Function<R, T> getter) {
		return new RecordField<>(name, type, getter, false, false, null);
	}

	public RecordField<R, T> withDefault(T defaultValue) {
		return new RecordField<>(name, type, getter, primaryKey, useNull, defaultValue);
	}

	public RecordField<R, T> withNull(boolean useNull) {
		return new RecordField<>(name, type, getter, primaryKey, useNull, defaultValue);
	}

	public String sqlTableEntry() {
		String sql = "%s %s".formatted(name, type.typeName());
		if (primaryKey) sql += " primary key";
		if (!useNull) sql += " not null";
		return sql;
	}
}
