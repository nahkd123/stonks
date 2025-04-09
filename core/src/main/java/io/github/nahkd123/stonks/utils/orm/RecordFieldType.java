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
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RecordFieldType<S, J>(Class<S> sqlType, String typeName, Function<S, J> toJava, Function<J, S> toSql) {

	public static final Map<Class<?>, String> TYPENAMES = Map.of(
		byte.class, "TINYINT",
		short.class, "SMALLINT",
		int.class, "INT",
		long.class, "BIGINT",
		String.class, "TEXT");

	public static final RecordFieldType<Byte, Byte> TINYINT = of(byte.class, "TINYINT");
	public static final RecordFieldType<Short, Short> SMALLINT = of(short.class, "SMALLINT");
	public static final RecordFieldType<Integer, Integer> INT = of(int.class, "INT");
	public static final RecordFieldType<Long, Long> BIGINT = of(long.class, "BIGINT");
	public static final RecordFieldType<String, String> TEXT = of(String.class, "TEXT");
	public static final RecordFieldType<String, UUID> UNIQUE_ID = RecordFieldType
		.varchar(36)
		.xmap(UUID::fromString, UUID::toString);

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

	public static <S> RecordFieldType<S, S> of(Class<S> sqlType) {
		String name = TYPENAMES.get(sqlType);
		if (name == null) throw new IllegalArgumentException("Unable to resolve typename for %s (try manual typename)"
			.formatted(sqlType));
		return of(sqlType, name);
	}

	public static <S> RecordFieldType<S, S> of(Class<S> sqlType, String typeName) {
		return new RecordFieldType<>(sqlType, typeName, Function.identity(), Function.identity());
	}

	public static RecordFieldType<String, String> varchar(int maxLength) {
		return of(String.class, "VARCHAR(%d)".formatted(maxLength));
	}

	public static <J extends Enum<J>> RecordFieldType<String, J> ofEnum(Class<J> enumType, J[] values) {
		Integer maxLength = Stream.of(values)
			.map(v -> v.toString().length())
			.collect(Collectors.maxBy(Integer::compare)).get();
		return varchar(maxLength).xmap(key -> Enum.valueOf(enumType, key), Enum::toString);
	}

	public <T> RecordFieldType<S, T> xmap(Function<J, T> toTarget, Function<T, J> fromTarget) {
		Function<S, T> forward = sql -> toTarget.apply(toJava.apply(sql));
		Function<T, S> backward = target -> toSql.apply(fromTarget.apply(target));
		return new RecordFieldType<S, T>(sqlType, typeName, forward, backward);
	}

	@SuppressWarnings("unchecked")
	public void setTo(PreparedStatement s, int index, J value) throws SQLException {
		S sqlValue = toSql.apply(value);
		PreparedStatementSetter<S> setter = (PreparedStatementSetter<S>) SETTERS.get(sqlType);
		if (setter == null) throw new IllegalArgumentException("No SQL value setter found for %s".formatted(sqlType));
		setter.set(s, index, sqlValue);
	}

	@SuppressWarnings("unchecked")
	public J getFrom(ResultSet set, String typeName) throws SQLException {
		ResultSetGetter<S> getter = (ResultSetGetter<S>) GETTERS.get(sqlType);
		if (getter == null) throw new IllegalArgumentException("No SQL value getter found for %s".formatted(sqlType));
		S sqlValue = getter.get(set, typeName);
		return toJava.apply(sqlValue);
	}

	@FunctionalInterface
	private interface ResultSetGetter<T> {
		T get(ResultSet set, String name) throws SQLException;
	}

	@FunctionalInterface
	private interface PreparedStatementSetter<T> {
		void set(PreparedStatement s, int paramIndex, T value) throws SQLException;
	}

}
