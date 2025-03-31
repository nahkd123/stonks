package io.github.nahkd123.stonks.utils.orm;

import java.util.List;
import java.util.stream.Collectors;

public record TableIndex(String name, List<Entry> columns) {
	public String sqlCreateIndex(String table) {
		return "CREATE INDEX [%s] ON [%s] (%s)".formatted(
			name, table,
			columns.stream().map(Entry::sql).collect(Collectors.joining(", ")));
	}

	public static record Entry(String field, Ordering ordering) {
		public Entry(String field) {
			this(field, null);
		}

		public String sql() {
			return ordering != null
				? "[%s] %s".formatted(field, ordering.getSqlKeyword())
				: "[%s]".formatted(field);
		}
	}
}
