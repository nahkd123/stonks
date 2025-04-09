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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public record RecordInfo<R>(Function<List<?>, R> factory, List<RecordField<R, ?>> fields) {

	@SuppressWarnings("unchecked")
	public static <R, T1> RecordInfo<R> of(RecordField<R, T1> f1, Function<T1, R> factory) {
		Function<List<?>, R> factory0 = params -> factory.apply(
			(T1) params.get(0));
		return new RecordInfo<>(factory0, List.of(f1));
	}

	@SuppressWarnings("unchecked")
	public static <R, T1, T2> RecordInfo<R> of(RecordField<R, T1> f1, RecordField<R, T2> f2, BiFunction<T1, T2, R> factory) {
		Function<List<?>, R> factory0 = params -> factory.apply(
			(T1) params.get(0),
			(T2) params.get(1));
		return new RecordInfo<>(factory0, List.of(f1, f2));
	}

	@SuppressWarnings("unchecked")
	public static <R, T1, T2, T3, T4, T5, T6> RecordInfo<R> of(RecordField<R, T1> f1, RecordField<R, T2> f2, RecordField<R, T3> f3, RecordField<R, T4> f4, RecordField<R, T5> f5, RecordField<R, T6> f6, RecordFactory6<R, T1, T2, T3, T4, T5, T6> factory) {
		Function<List<?>, R> factory0 = params -> factory.create(
			(T1) params.get(0),
			(T2) params.get(1),
			(T3) params.get(2),
			(T4) params.get(3),
			(T5) params.get(4),
			(T6) params.get(5));
		return new RecordInfo<>(factory0, List.of(f1, f2, f3, f4, f5, f6));
	}

	@SuppressWarnings("unchecked")
	public static <R, T1, T2, T3, T4, T5, T6, T7, T8> RecordInfo<R> of(RecordField<R, T1> f1, RecordField<R, T2> f2, RecordField<R, T3> f3, RecordField<R, T4> f4, RecordField<R, T5> f5, RecordField<R, T6> f6, RecordField<R, T7> f7, RecordField<R, T8> f8, RecordFactory8<R, T1, T2, T3, T4, T5, T6, T7, T8> factory) {
		Function<List<?>, R> factory0 = params -> factory.create(
			(T1) params.get(0),
			(T2) params.get(1),
			(T3) params.get(2),
			(T4) params.get(3),
			(T5) params.get(4),
			(T6) params.get(5),
			(T7) params.get(6),
			(T8) params.get(7));
		return new RecordInfo<>(factory0, List.of(f1, f2, f3, f4, f5, f6, f7, f8));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public R getFrom(ResultSet set) throws SQLException {
		List<?> params = new ArrayList<>();

		for (RecordField<R, ?> field : fields) {
			String name = field.name();
			RecordFieldType<?, ?> type = field.type();
			Object param = type.getFrom(set, name);
			((List) params).add(param);
		}

		return factory.apply(params);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setTo(PreparedStatement s, int index, R rec) throws SQLException {
		int base = index * fields.size() + 1;

		for (int i = 0; i < fields.size(); i++) {
			RecordField<R, ?> field = fields.get(i);
			int paramIndex = base + i;
			((RecordFieldType) field.type()).setTo(s, paramIndex, field.getter().apply(rec));
		}
	}

	public RecordSet<R> wrap(ResultSet resultSet) {
		return new RecordSet<>(this, resultSet);
	}

	@FunctionalInterface
	public static interface RecordFactory3<R, T1, T2, T3> {
		R create(T1 t1, T2 t2, T3 t3);
	}

	@FunctionalInterface
	public static interface RecordFactory6<R, T1, T2, T3, T4, T5, T6> {
		R create(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);
	}

	@FunctionalInterface
	public static interface RecordFactory8<R, T1, T2, T3, T4, T5, T6, T7, T8> {
		R create(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
	}

	public static class RecordSet<R> implements Iterable<R>, Iterator<R>, AutoCloseable {
		private RecordInfo<R> recordInfo;
		private ResultSet resultSet;
		private boolean holdingNext = false;
		private boolean ended = false;

		private RecordSet(RecordInfo<R> recordInfo, ResultSet resultSet) {
			this.recordInfo = recordInfo;
			this.resultSet = resultSet;
		}

		@Override
		public Iterator<R> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			try {
				if (ended) return false;
				if (holdingNext) return true;
				holdingNext = resultSet.next();
				ended = !holdingNext;
				return holdingNext;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public R next() {
			try {
				if (ended || (!holdingNext && !hasNext())) throw new IllegalStateException("No more element");
				holdingNext = false;
				return recordInfo.getFrom(resultSet);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() throws SQLException {
			resultSet.close();
		}

		public R firstOrThrow() throws SQLException {
			R value = next();
			close();
			return value;
		}

		public R firstOr(R def) throws SQLException {
			if (!hasNext()) {
				close();
				return def;
			} else {
				return firstOrThrow();
			}
		}
	}
}
