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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collector;

public class MappedResultSet<R> {
	private Table<R> table;
	private ResultSet underlying;

	public MappedResultSet(Table<R> table, ResultSet set) {
		this.table = table;
		this.underlying = set;
	}

	public Table<R> getTable() { return table; }

	public ResultSet getUnderlying() { return underlying; }

	public boolean next() throws SQLException {
		return underlying.next();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public R getCurrent() throws SQLException {
		R rec = table.newRecord();

		for (int i = 0; i < table.getColumns().size(); i++) {
			Column<R, ?> column = table.getColumns().get(i);
			((Column) column).set(rec, column.getType().mapper().apply(underlying.getObject(i + 1)));
		}

		return rec;
	}

	public Optional<R> first() throws SQLException {
		return next() ? Optional.of(getCurrent()) : Optional.empty();
	}

	public <A, L> L collect(Collector<R, A, L> collector) throws SQLException {
		A list = collector.supplier().get();
		while (next()) collector.accumulator().accept(list, getCurrent());
		return collector.finisher().apply(list);
	}
}
