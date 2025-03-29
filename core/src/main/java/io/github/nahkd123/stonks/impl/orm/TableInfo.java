package io.github.nahkd123.stonks.impl.orm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.impl.orm.meta.ColumnMeta;

public record TableInfo<R>(String name, RecordInfo<R> recordInfo, List<TableIndex> indexes) {
	public void migrate(Connection connection) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();

		try (ResultSet set = meta.getTables(null, null, name, null)) {
			if (!set.next()) {
				String tableEntries = recordInfo.fields().stream()
					.map(RecordField::sqlTableEntry)
					.collect(Collectors.joining(", "));

				try (Statement s = connection.createStatement()) {
					s.execute("CREATE TABLE [%s] (%s)".formatted(name, tableEntries));
					for (TableIndex index : indexes) s.execute(index.sqlCreateIndex(name));
				}

				return;
			} else {
				Map<String, RecordField<R, ?>> missingColumns = new HashMap<>(recordInfo.fields()
					.stream()
					.collect(Collectors.toMap(RecordField::name, Function.identity())));
				Set<String> extraColumns = new HashSet<>();

				// Migrate columns
				try (var columnsSet = ColumnMeta.RECORD.wrap(meta.getColumns(null, null, name, null))) {
					for (ColumnMeta col : columnsSet) {
						if (missingColumns.remove(col.columnName()) == null) extraColumns.add(name);
					}
				}

				if (missingColumns.size() != 0 || extraColumns.size() != 0) {
					try (Statement s = connection.createStatement()) {
						for (String name : extraColumns)
							s.execute("ALTER TABLE [%s] DROP COLUMN [%s]".formatted(this.name, name));
						for (RecordField<R, ?> field : missingColumns.values())
							s.execute("ALTER TABLE [%s] ADD %s".formatted(name, field.sqlTableEntry()));
					}
				}

				// Migrate indexes
				Map<String, TableIndex> missingIndexes = new HashMap<>(indexes.stream()
					.collect(Collectors.toMap(TableIndex::name, Function.identity())));
				Set<String> extraIndexes = new HashSet<>();

				try (var indexes = meta.getIndexInfo(null, null, name, false, true)) {
					while (indexes.next()) {
						boolean nonUnique = indexes.getBoolean("NON_UNIQUE");
						String indexName = indexes.getString("INDEX_NAME");
						if (!nonUnique) continue;
						if (indexName == null) continue;
						if (missingIndexes.remove(indexName) == null) extraIndexes.add(indexName);
					}
				}

				if (missingIndexes.size() != 0 || extraIndexes.size() != 0) {
					try (Statement s = connection.createStatement()) {
						for (String name : extraIndexes) s.execute("DROP INDEX [%s]".formatted(name));
						for (TableIndex index : indexes) s.execute(index.sqlCreateIndex(name));
					}
				}
			}
		}
	}

	public String sqlSelect(Query query) {
		String sql = "SELECT * FROM [%s]".formatted(name);

		if (query != null) {
			if (query.condition() != null) sql += " WHERE " + query.condition();
			if (query.sortBy() != null) sql += ' ' + query.sortBy().sql();
			if (query.limit() != -1) sql += " LIMIT %d".formatted(query.limit());
		}

		return sql;
	}

	public Select<R> select(Connection connection, Query query, BiConsumer<PreparedStatement, R> paramsSetter) throws SQLException {
		return new Select<>(this, connection.prepareStatement(sqlSelect(query)), paramsSetter);
	}

	public Select<R> select(Connection connection, Query query) throws SQLException {
		return new Select<>(this, connection.prepareStatement(sqlSelect(query)), (a, b) -> {
			throw new IllegalArgumentException("This select statement does not implement params setter");
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Select<R> select(Connection connection) throws SQLException {
		RecordField<R, ?> primary = recordInfo.fields().stream()
			.filter(RecordField::primaryKey)
			.findAny().get();
		return select(connection, Query.ofCondition("%s=?".formatted(primary.name())), (s, v) -> {
			try {
				Object primaryKey = primary.getter().apply(v);
				((RecordFieldType) primary.type()).setTo(s, 1, primaryKey);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public String sqlInsert() throws SQLException {
		return "INSERT INTO [%s] (%s) VALUES (%s)".formatted(
			name,
			recordInfo.fields().stream().map(RecordField::name).collect(Collectors.joining(", ")),
			recordInfo.fields().stream().map($ -> "?").collect(Collectors.joining(", ")));
	}

	public Insert<R> insert(Connection connection) throws SQLException {
		return new Insert<>(this, connection.prepareStatement(sqlInsert()));
	}

	public String sqlUpdate(String condition) {
		String sql = "UPDATE [%s] SET %s".formatted(
			name,
			recordInfo.fields().stream().map(f -> "%s=?".formatted(f.name())).collect(Collectors.joining(", ")));
		if (condition != null) sql += " WHERE " + condition;
		return sql;
	}

	public Update<R> update(Connection connection, String condition, BiConsumer<PreparedStatement, R> paramsSetter) throws SQLException {
		return new Update<>(this, connection.prepareStatement(sqlUpdate(condition)), (s, v) -> {
			try {
				recordInfo.setTo(s, 0, v);
				paramsSetter.accept(s, v);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Update<R> update(Connection connection) throws SQLException {
		RecordField<R, ?> primary = recordInfo.fields().stream()
			.filter(RecordField::primaryKey)
			.findAny().get();
		int condIndex = recordInfo.fields().size() + 1;
		return update(connection, "%s=?".formatted(primary.name()), (s, v) -> {
			try {
				Object primaryKey = primary.getter().apply(v);
				((RecordFieldType) primary.type()).setTo(s, condIndex, primaryKey);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public String sqlDelete(String condition) {
		String sql = "DELETE FROM [%s]".formatted(name);
		if (condition != null) sql += " WHERE " + condition;
		return sql;
	}

	public Update<R> delete(Connection connection, String condition, BiConsumer<PreparedStatement, R> paramsSetter) throws SQLException {
		return new Update<>(this, connection.prepareStatement(sqlDelete(condition)), paramsSetter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Update<R> delete(Connection connection) throws SQLException {
		RecordField<R, ?> primary = recordInfo.fields().stream()
			.filter(RecordField::primaryKey)
			.findAny().get();
		return delete(connection, "%s=?".formatted(primary.name()), (s, v) -> {
			try {
				Object primaryKey = primary.getter().apply(v);
				((RecordFieldType) primary.type()).setTo(s, 1, primaryKey);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static record Select<R>(TableInfo<R> table, PreparedStatement statement, BiConsumer<PreparedStatement, R> paramsSetter) implements AutoCloseable {
		public RecordInfo.RecordSet<R> query() throws SQLException {
			ResultSet resultSet = statement.executeQuery();
			return table.recordInfo.wrap(resultSet);
		}

		public RecordInfo.RecordSet<R> query(R rec) throws SQLException {
			paramsSetter.accept(statement, rec);
			return query();
		}

		@Override
		public void close() throws SQLException {
			statement.close();
		}
	}

	public static record Insert<R>(TableInfo<R> table, PreparedStatement statement) implements AutoCloseable {
		public void insert(R rec) throws SQLException {
			table.recordInfo.setTo(statement, 0, rec);
			statement.executeUpdate();
		}

		@Override
		public void close() throws SQLException {
			statement.close();
		}
	}

	public static record Update<R>(TableInfo<R> table, PreparedStatement statement, BiConsumer<PreparedStatement, R> paramsSetter) implements AutoCloseable {
		public void update(R rec) throws SQLException {
			paramsSetter.accept(statement, rec);
			statement.executeUpdate();
		}

		@Override
		public void close() throws SQLException {
			statement.close();
		}
	}
}
