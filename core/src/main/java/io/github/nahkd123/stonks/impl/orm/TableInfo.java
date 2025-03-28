package io.github.nahkd123.stonks.impl.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public record TableInfo<R>(String name, RecordInfo<R> recordInfo) {
	public String sqlCreateTable() {
		return "create table %s (%s)".formatted(
			name,
			recordInfo.fields().stream().map(RecordField::sqlTableEntry).collect(Collectors.joining(", ")));
	}

	public void createTable(Connection sql) throws SQLException {
		try (var s = sql.createStatement()) {
			s.execute(sqlCreateTable());
		}
	}

	public String preparedSqlInsert(int count) {
		String sql = "insert into %s (%s) values ".formatted(
			name,
			recordInfo.fields().stream().map(RecordField::name).collect(Collectors.joining(", ")));
		for (int i = 0; i < count; i++)
			sql += "(%s)".formatted(recordInfo.fields().stream().map($ -> "?").collect(Collectors.joining(", ")));
		return sql;
	}

	public void insert(Connection sql, Collection<R> rows) throws SQLException {
		try (var s = sql.prepareStatement(preparedSqlInsert(rows.size()))) {
			int i = 0;
			for (R row : rows) recordInfo.setTo(s, i++, row);
			s.execute();
		}
	}

	@SafeVarargs
	public final void insert(Connection sql, R... rows) throws SQLException {
		try (var s = sql.prepareStatement(preparedSqlInsert(rows.length))) {
			int i = 0;
			for (R row : rows) recordInfo.setTo(s, i++, row);
			s.execute();
		}
	}

	public void delete(Connection sql, Collection<R> rows) throws SQLException {
		String sqlCode = "delete from %s where %s".formatted(
			name,
			recordInfo.fields().stream()
				.map(f -> "%s=?".formatted(f.name()))
				.collect(Collectors.joining(" and ")));

		try (var s = sql.prepareStatement(sqlCode)) {
			for (R row : rows) {
				recordInfo.setTo(s, 0, row);
				s.execute();
			}
		}
	}

	@SafeVarargs
	public final void delete(Connection sql, R... rows) throws SQLException {
		String sqlCode = "delete from %s where %s".formatted(
			name,
			recordInfo.fields().stream()
				.map(f -> "%s=?".formatted(f.name()))
				.collect(Collectors.joining(" and ")));

		try (var s = sql.prepareStatement(sqlCode)) {
			for (R row : rows) {
				recordInfo.setTo(s, 0, row);
				s.execute();
			}
		}
	}

	public void migrate(Connection sql) throws SQLException {
		try (var set = sql.getMetaData().getTables(null, null, name, null)) {
			if (!set.next()) {
				createTable(sql);
			} else {
				Set<String> currentNames = new HashSet<>();
				Map<String, RecordField<R, ?>> required = recordInfo.fields()
					.stream()
					.collect(Collectors.toMap(RecordField::name, Function.identity()));

				try (var colSet = sql.getMetaData().getColumns(null, null, name, null)) {
					while (colSet.next()) {
						String colName = colSet.getString("COLUMN_NAME");
						currentNames.add(colName);
					}
				}

				Set<String> missingNames = new HashSet<>();
				for (String name : required.keySet()) if (!currentNames.contains(name)) missingNames.add(name);
				Set<String> extraNames = new HashSet<>();
				for (String name : currentNames) if (!required.containsKey(name)) extraNames.add(name);
				if (missingNames.size() == 0 && extraNames.size() == 0) return;

				try (var s = sql.createStatement()) {
					for (String name : missingNames) s.execute("alter table %s add %s %s".formatted(
						name, name, required.get(name).type().typeName()));
					for (String name : extraNames)
						s.execute("alter table %s drop column %s".formatted(name, name));
				}
			}
		}
	}
}
