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
package io.github.nahkd123.stonks.utils.dynamic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.nahkd123.stonks.utils.OneOf;

public interface DynamicCodec<T> extends DynamicEncoder<T>, DynamicDecoder<T> {
	record PrimitiveCodec<T>(DynamicEncoder<T> encoder, DynamicDecoder<T> decoder) implements DynamicCodec<T> {
		@Override
		public void encodeTo(DynamicWriter writer, T value) throws IOException {
			encoder.encodeTo(writer, value);
		}

		@Override
		public T decodeFrom(DynamicReader reader) throws IOException {
			return decoder.decodeFrom(reader);
		}
	}

	static <T> DynamicCodec<T> always(Supplier<T> factory) {
		return new PrimitiveCodec<>((writer, value) -> {
			writer.beginObject();
			writer.endObject();
		}, reader -> {
			skipValue(reader);
			return factory.get();
		});
	}

	private static void skipValue(DynamicReader reader) throws IOException {
		int token = reader.nextToken();
		switch (token) {
		case DynamicReader.TYPE_NULL:
			return;
		case DynamicReader.TYPE_NUMBER:
			reader.nextNumber();
			return;
		case DynamicReader.TYPE_STRING:
			reader.nextString();
			return;
		case DynamicReader.TYPE_BOOLEAN:
			reader.nextBoolean();
			return;
		case DynamicReader.TYPE_OBJECT_HEAD:
			while (token != DynamicReader.TYPE_OBJECT_TAIL) {
				token = reader.nextToken();
				if (token == DynamicReader.TYPE_OBJECT_TAIL) return;
				reader.nextObjectKey();
				skipValue(reader);
			}
			return;
		case DynamicReader.TYPE_ARRAY_HEAD:
			while (token != DynamicReader.TYPE_ARRAY_TAIL) {
				token = reader.nextToken();
				if (token == DynamicReader.TYPE_ARRAY_TAIL) return;
				reader.undoNextToken(token);
				skipValue(reader);
			}
			return;
		}
	}

	DynamicCodec<Void> VOID = always(() -> null);

	DynamicCodec<Number> NUMBER = new PrimitiveCodec<>((writer, value) -> {
		if (value == null) writer.nextNull();
		else writer.nextNumber(value);
	}, reader -> {
		int token = reader.nextToken();
		switch (token) {
		case DynamicReader.TYPE_NULL:
			return null;
		case DynamicReader.TYPE_NUMBER:
			return reader.nextNumber();
		default:
			reader.undoNextToken(token);
			throw new UnsupportedOperationException("Only number is allowed here");
		}
	});

	DynamicCodec<String> STRING = new PrimitiveCodec<>((writer, value) -> {
		if (value == null) writer.nextNull();
		else writer.nextString(value);
	}, reader -> {
		int token = reader.nextToken();
		switch (token) {
		case DynamicReader.TYPE_NULL:
			return null;
		case DynamicReader.TYPE_STRING:
			return reader.nextString();
		default:
			reader.undoNextToken(token);
			throw new UnsupportedOperationException("Only string is allowed here");
		}
	});

	DynamicCodec<Boolean> BOOLEAN = new PrimitiveCodec<>((writer, value) -> {
		if (value == null) writer.nextNull();
		else writer.nextBoolean(value);
	}, reader -> {
		int token = reader.nextToken();
		switch (token) {
		case DynamicReader.TYPE_NULL:
			return null;
		case DynamicReader.TYPE_BOOLEAN:
			return reader.nextBoolean();
		default:
			reader.undoNextToken(token);
			throw new UnsupportedOperationException("Only boolean is allowed here");
		}
	});

	record MapCodec<A, B>(DynamicCodec<A> underlying, Function<A, B> forward, Function<B, A> backward) implements DynamicCodec<B> {
		@Override
		public void encodeTo(DynamicWriter writer, B value) throws IOException {
			underlying.encodeTo(writer, backward.apply(value));
		}

		@Override
		public B decodeFrom(DynamicReader reader) throws IOException {
			return forward.apply(underlying.decodeFrom(reader));
		}
	}

	default <U> DynamicCodec<U> mapWithNull(Function<T, U> forward, Function<U, T> backward) {
		return new MapCodec<>(this, forward, backward);
	}

	default <U> DynamicCodec<U> map(Function<T, U> forward, Function<U, T> backward) {
		return mapWithNull(
			a -> a != null ? forward.apply(a) : null,
			b -> b != null ? backward.apply(b) : null);
	}

	DynamicCodec<Byte> BYTE = NUMBER.map(Number::byteValue, v -> v);
	DynamicCodec<Short> SHORT = NUMBER.map(Number::shortValue, v -> v);
	DynamicCodec<Integer> INTEGER = NUMBER.map(Number::intValue, v -> v);
	DynamicCodec<Long> LONG = NUMBER.map(Number::longValue, v -> v);
	DynamicCodec<Float> FLOAT = NUMBER.map(Number::floatValue, v -> v);
	DynamicCodec<Double> DOUBLE = NUMBER.map(Number::doubleValue, v -> v);

	record ObjectField<T, F>(DynamicCodec<F> type, Function<T, F> getter, BiConsumer<T, F> setter) {
		public void encodeField(DynamicWriter writer, T object) throws IOException {
			type.encodeTo(writer, getter.apply(object));
		}

		public void decodeField(DynamicReader reader, T object) throws IOException {
			setter.accept(object, type.decodeFrom(reader));
		}
	}

	record ObjectCodec<T>(Supplier<T> factory, Map<String, ObjectField<T, ?>> fields) implements DynamicCodec<T> {
		@Override
		public void encodeTo(DynamicWriter writer, T value) throws IOException {
			if (value == null) {
				writer.nextNull();
				return;
			}

			writer.beginObject();

			for (Map.Entry<String, ObjectField<T, ?>> field : fields.entrySet()) {
				writer.nextObjectKey(field.getKey());
				field.getValue().encodeField(writer, value);
			}

			writer.endObject();
		}

		@Override
		public T decodeFrom(DynamicReader reader) throws IOException {
			int token = reader.nextToken();
			if (token == DynamicReader.TYPE_NULL) return null;

			if (token != DynamicReader.TYPE_OBJECT_HEAD) {
				reader.undoNextToken(token);
				throw new UnsupportedOperationException("Only object is allowed here");
			}

			T object = factory.get();
			while (token != DynamicReader.TYPE_OBJECT_TAIL) {
				token = reader.nextToken();
				switch (token) {
				case DynamicReader.TYPE_OBJECT_KEY:
					String key = reader.nextObjectKey();
					ObjectField<T, ?> field = fields.get(key);
					if (field == null) throw new IOException("Unknown field: %s".formatted(key));
					field.decodeField(reader, object);
					break;
				case DynamicReader.TYPE_OBJECT_TAIL:
					break;
				default:
					// can't undo here :(
					throw new IOException("Expecting object tail but %d found".formatted(token));
				}
			}

			return object;
		}
	}

	static <T> DynamicCodec<T> object(Supplier<T> factory, Map<String, ObjectField<T, ?>> fields) {
		return new ObjectCodec<>(factory, fields);
	}

	record ListCodec<T>(DynamicCodec<T> elementType) implements DynamicCodec<List<T>> {
		@Override
		public void encodeTo(DynamicWriter writer, List<T> value) throws IOException {
			if (value == null) {
				writer.nextNull();
				return;
			}

			writer.beginArray();
			for (int i = 0; i < value.size(); i++) elementType.encodeTo(writer, value.get(i));
			writer.endArray();
		}

		@Override
		public List<T> decodeFrom(DynamicReader reader) throws IOException {
			int token = reader.nextToken();
			if (token == DynamicReader.TYPE_NULL) return null;

			if (token != DynamicReader.TYPE_ARRAY_HEAD) {
				reader.undoNextToken(token);
				throw new UnsupportedOperationException("Only array is allowed here");
			}

			List<T> list = new ArrayList<>();
			while (token != DynamicReader.TYPE_ARRAY_TAIL) {
				token = reader.nextToken();
				switch (token) {
				case DynamicReader.TYPE_ARRAY_TAIL:
					break;
				default:
					T elem = elementType.decodeFrom(reader);
					list.add(elem);
				}
			}

			return list;
		}
	}

	default DynamicCodec<List<T>> asList() {
		return new ListCodec<>(this);
	}

	record OneOfCodec<A, B>(DynamicCodec<A> a, DynamicCodec<B> b) implements DynamicCodec<OneOf<A, B>> {
		@Override
		public void encodeTo(DynamicWriter writer, OneOf<A, B> value) throws IOException {
			switch (value) {
			case OneOf.First(A valueA):
				a.encodeTo(writer, valueA);
				break;
			case OneOf.Second(B valueB):
				b.encodeTo(writer, valueB);
				break;
			case null:
				writer.nextNull();
				break;
			default:
				throw new RuntimeException("Not implemented: %s".formatted(value));
			}
		}

		@Override
		public OneOf<A, B> decodeFrom(DynamicReader reader) throws IOException {
			try {
				A valueA = a.decodeFrom(reader);
				return new OneOf.First<>(valueA);
			} catch (UnsupportedOperationException e) {
				B valueB = b.decodeFrom(reader);
				return new OneOf.Second<>(valueB);
			}
		}
	}

	static <A, B> DynamicCodec<OneOf<A, B>> oneOf(DynamicCodec<A> a, DynamicCodec<B> b) {
		return new OneOfCodec<>(a, b);
	}

	default <U> DynamicCodec<OneOf<T, U>> or(DynamicCodec<U> another) {
		return new OneOfCodec<>(this, another);
	}
}
