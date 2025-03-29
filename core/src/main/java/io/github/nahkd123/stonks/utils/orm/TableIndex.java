package io.github.nahkd123.stonks.utils.orm;

public record TableIndex(String name, String field, Ordering ordering) {
	public String sqlCreateIndex(String table) {
		return "CREATE INDEX [%s] ON [%s] ([%s] %s)".formatted(name, table, field, ordering.getSqlKeyword());
	}
}
