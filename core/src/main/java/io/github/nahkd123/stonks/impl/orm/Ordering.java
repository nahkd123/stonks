package io.github.nahkd123.stonks.impl.orm;

public enum Ordering {
	ASCENDING("ASC"),
	DESCENDING("DESC");

	private String sqlKeyword;

	private Ordering(String sqlKeyword) {
		this.sqlKeyword = sqlKeyword;
	}

	public String getSqlKeyword() { return sqlKeyword; }
}