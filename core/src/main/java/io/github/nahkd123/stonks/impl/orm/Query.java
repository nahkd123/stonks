package io.github.nahkd123.stonks.impl.orm;

public record Query(String condition, SortBy sortBy, int limit) {

	public static final Query ALL = new Query(null, null, -1);

	public static Query ofCondition(String condition) {
		return new Query(condition, null, -1);
	}

	public static Query ofSorted(String field, Ordering ordering) {
		return new Query(null, new SortBy(field, ordering), -1);
	}

	public Query withCondition(String condition) {
		return new Query(condition, sortBy, limit);
	}

	public Query withSorted(String field, Ordering ordering) {
		return new Query(condition, new SortBy(field, ordering), limit);
	}

	public Query withLimit(int limit) {
		return new Query(condition, sortBy, limit);
	}

	public static record SortBy(String field, Ordering ordering) {
		public String sql() {
			return "ORDER BY [%s] %s".formatted(field, ordering.getSqlKeyword());
		}
	}
}
