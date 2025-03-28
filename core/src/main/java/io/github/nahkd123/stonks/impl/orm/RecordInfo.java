package io.github.nahkd123.stonks.impl.orm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public record RecordInfo<R>(Function<List<?>, R> factory, List<RecordField<R, ?>> fields) {
	private static final Map<Class<?>, ResultSetGetter<?>> GETTERS = Map.of(
		byte.class, (ResultSetGetter<Byte>) (set, name) -> set.getByte(name),
		short.class, (ResultSetGetter<Short>) (set, name) -> set.getShort(name),
		int.class, (ResultSetGetter<Integer>) (set, name) -> set.getInt(name),
		long.class, (ResultSetGetter<Long>) (set, name) -> set.getLong(name),
		String.class, (ResultSetGetter<String>) (set, name) -> set.getString(name));
	private static final Map<Class<?>, PreparedStatementSetter<?>> SETTERS = Map.of(
		byte.class, (PreparedStatementSetter<Byte>) (s, paramIndex, value) -> s.setByte(paramIndex, value),
		short.class, (PreparedStatementSetter<Short>) (s, paramIndex, value) -> s.setShort(paramIndex, value),
		int.class, (PreparedStatementSetter<Integer>) (s, paramIndex, value) -> s.setInt(paramIndex, value),
		long.class, (PreparedStatementSetter<Long>) (s, paramIndex, value) -> s.setLong(paramIndex, value),
		String.class, (PreparedStatementSetter<String>) (s, paramIndex, value) -> s.setString(paramIndex, value));

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
			ResultSetGetter<?> getter = GETTERS.get(type.sqlType());
			if (getter == null) throw new SQLException("Unknown SQL type: %s".formatted(type.sqlType()));
			Object param = ((Function) type.toJava()).apply(getter.get(set, name));
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
			PreparedStatementSetter<?> setter = SETTERS.get(field.type().sqlType());
			if (setter == null) throw new SQLException("Unknown SQL type: %s".formatted(field.type().sqlType()));
			((PreparedStatementSetter) setter).set(
				s, paramIndex,
				((Function) field.type().toSql()).apply(field.getter().apply(rec)));
		}
	}

	@FunctionalInterface
	private interface ResultSetGetter<T> {
		T get(ResultSet set, String name) throws SQLException;
	}

	@FunctionalInterface
	private interface PreparedStatementSetter<T> {
		void set(PreparedStatement s, int paramIndex, T value) throws SQLException;
	}

	@FunctionalInterface
	public static interface RecordFactory3<R, T1, T2, T3> {
		R create(T1 t1, T2 t2, T3 t3);
	}

	@FunctionalInterface
	public static interface RecordFactory8<R, T1, T2, T3, T4, T5, T6, T7, T8> {
		R create(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);
	}
}
