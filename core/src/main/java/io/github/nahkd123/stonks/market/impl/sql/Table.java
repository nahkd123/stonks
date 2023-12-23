/*
 * Copyright (c) 2023 nahkd
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
package io.github.nahkd123.stonks.market.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Table<R> {
	private String tableName;
	private Supplier<R> factory;
	private String primary = null;
	private List<Column<R, ?>> columns = new ArrayList<>();

	public Table(String tableName, Supplier<R> factory) {
		this.tableName = tableName;
		this.factory = factory;
	}

	public String getTableName() { return tableName; }

	public R newRecord() {
		return factory.get();
	}

	public List<Column<R, ?>> getColumns() { return Collections.unmodifiableList(columns); }

	public <T> Table<R> addColumn(String name, ColumnType<T> type, Function<R, T> getter, BiConsumer<R, T> setter) {
		columns.add(new Column<>(name, type, getter, setter));
		return this;
	}

	public boolean removeColumn(String name) {
		Iterator<Column<R, ?>> iterator = columns.iterator();

		while (iterator.hasNext()) {
			Column<R, ?> column = iterator.next();
			if (column.getName().equals(name)) {
				iterator.remove();
				return true;
			}
		}

		return false;
	}

	public Column<R, ?> getColumn(String name) {
		return columns.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	public Table<R> setPrimary(String primary) {
		this.primary = primary;
		return this;
	}

	public String getPrimary() { return primary; }

	public Table<R> initializeTable(Connection connection) throws SQLException {
		try (ResultSet set = connection.getMetaData().getTables(null, null, tableName, null)) {
			if (!set.next()) {
				// No table, create a new one
				Statement s = connection.createStatement();
				String tableSpec = columns.stream()
					.map(c -> {
						String columnSpec = c.getColumnSpec();
						if (c.getName().equals(primary)) columnSpec += " PRIMARY KEY";
						return columnSpec;
					})
					.collect(Collectors.joining(", "));
				s.execute("CREATE TABLE " + tableName + " (" + tableSpec + ")");
				return this;
			}
		}

		// TODO check columns to see if we need to update anything
		return this;
	}

	public String getColumnsAsString() {
		return columns.stream().map(Column::getName).collect(Collectors.joining(", "));
	}

	public Table<R> insert(Connection connection, R data) throws SQLException {
		String cols = getColumnsAsString();
		String colsPlaceholder = columns.stream().map($ -> "?").collect(Collectors.joining(", "));
		PreparedStatement s = connection
			.prepareStatement("INSERT INTO " + tableName + " (" + cols + ") VALUES (" + colsPlaceholder + ")");
		fillIn(s, data);
		s.execute();
		return this;
	}

	public Table<R> update(Connection connection, R data) throws SQLException {
		if (primary == null) return insert(connection, data);
		String cols = getColumnsAsString();
		String colsPlaceholder = columns.stream().map($ -> "?").collect(Collectors.joining(", "));
		PreparedStatement s = connection
			.prepareStatement("REPLACE INTO " + tableName + " (" + cols + ") VALUES (" + colsPlaceholder + ")");
		fillIn(s, data);
		s.execute();
		return this;
	}

	public Table<R> delete(Connection connection, R data) throws SQLException {
		if (primary == null) return this;
		PreparedStatement s = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + primary + "=?");
		s.setObject(1, getColumn(primary).get(data));
		s.execute();
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fillIn(PreparedStatement s, R data) throws SQLException {
		for (int i = 0; i < columns.size(); i++) {
			Column<R, ?> column = columns.get(i);
			s.setObject(i + 1, ((Function) column.getType().reverseMapper()).apply(column.get(data)));
		}
	}

	public MappedResultSet<R> mapResults(ResultSet set) {
		return new MappedResultSet<>(this, set);
	}
}
