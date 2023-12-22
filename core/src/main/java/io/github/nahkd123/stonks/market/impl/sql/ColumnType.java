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

import java.util.UUID;
import java.util.function.Function;

public record ColumnType<T>(String sqlType, Function<Object, T> mapper, Function<T, Object> reverseMapper) {
	public <M> ColumnType<M> map(Function<T, M> mapper, Function<M, T> reverseMapper) {
		return new ColumnType<>(sqlType, this.mapper.andThen(mapper), reverseMapper.andThen(this.reverseMapper));
	}

	public static ColumnType<String> varchar(int characters) {
		return new ColumnType<String>("varchar(" + characters + ")", o -> o.toString(), s -> s);
	}

	//@formatter:off
	public static final ColumnType<String> TEXT = new ColumnType<>("text", o -> o.toString(), s -> s);
	public static final ColumnType<Long> BIGINT = new ColumnType<>("bigint", o -> ((Number) o).longValue(), l -> l);
	public static final ColumnType<UUID> UUID = varchar(36).map(java.util.UUID::fromString, java.util.UUID::toString);
	public static final ColumnType<Boolean> BOOL = new ColumnType<Boolean>("bool", o -> ((Integer) o == 1), b -> b);
	//@formatter:on
}
