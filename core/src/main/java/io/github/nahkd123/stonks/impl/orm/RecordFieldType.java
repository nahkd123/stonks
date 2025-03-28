package io.github.nahkd123.stonks.impl.orm;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RecordFieldType<S, J>(Class<S> sqlType, String typeName, Function<S, J> toJava, Function<J, S> toSql) {

	public static final Map<Class<?>, String> TYPENAMES = Map.of(
		byte.class, "tinyint",
		short.class, "smallint",
		int.class, "int",
		long.class, "bigint",
		String.class, "mediumtext");

	public static final RecordFieldType<Byte, Byte> TINYINT = of(byte.class, "tinyint");
	public static final RecordFieldType<Short, Short> SMALLINT = of(short.class, "smallint");
	public static final RecordFieldType<Integer, Integer> INT = of(int.class, "int");
	public static final RecordFieldType<Long, Long> BIGINT = of(long.class, "bigint");
	public static final RecordFieldType<String, String> TEXT = of(String.class, "text");

	public static final RecordFieldType<String, UUID> UNIQUE_ID = RecordFieldType
		.ofString(36)
		.xmap(UUID::fromString, UUID::toString);

	public static <S> RecordFieldType<S, S> of(Class<S> sqlType) {
		String name = TYPENAMES.get(sqlType);
		if (name == null) throw new IllegalArgumentException("Unable to resolve typename for %s (try manual typename)"
			.formatted(sqlType));
		return of(sqlType, name);
	}

	public static <S> RecordFieldType<S, S> of(Class<S> sqlType, String typeName) {
		return new RecordFieldType<>(sqlType, typeName, Function.identity(), Function.identity());
	}

	public static RecordFieldType<String, String> ofString(int maxLength) {
		return of(String.class, "varchar(%d)".formatted(maxLength));
	}

	public static <J extends Enum<J>> RecordFieldType<String, J> ofEnum(Class<J> enumType, J[] values) {
		Integer maxLength = Stream.of(values)
			.map(v -> v.toString().length())
			.collect(Collectors.maxBy(Integer::compare)).get();
		return ofString(maxLength).xmap(key -> Enum.valueOf(enumType, key), Enum::toString);
	}

	public <T> RecordFieldType<S, T> xmap(Function<J, T> toTarget, Function<T, J> fromTarget) {
		Function<S, T> forward = sql -> toTarget.apply(toJava.apply(sql));
		Function<T, S> backward = target -> toSql.apply(fromTarget.apply(target));
		return new RecordFieldType<S, T>(sqlType, typeName, forward, backward);
	}
}
