package io.github.nahkd123.stonks.utils.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BufferCodec<T> {
	void write(T value, ByteBuffer buffer);

	T read(ByteBuffer buffer);

	default <U> BufferCodec<U> map(Function<T, U> forward, Function<U, T> backward) {
		BufferCodec<T> self = this;
		return new BufferCodec<>() {
			@Override
			public void write(U value, ByteBuffer buffer) {
				self.write(backward.apply(value), buffer);
			}

			@Override
			public U read(ByteBuffer buffer) {
				return forward.apply(self.read(buffer));
			}
		};
	}

	default BufferCodec<List<T>> asList() {
		BufferCodec<T> self = this;
		return new BufferCodec<>() {
			@Override
			public void write(List<T> list, ByteBuffer buffer) {
				buffer.putShort((short) list.size());
				for (T elem : list) self.write(elem, buffer);
			}

			@Override
			public List<T> read(ByteBuffer buffer) {
				List<T> list = new ArrayList<>();
				int length = buffer.getShort() & 0xFFFF;
				for (int i = 0; i < length; i++) list.add(self.read(buffer));
				return list;
			}
		};
	}

	default BufferCodec<Optional<T>> asOptional() {
		BufferCodec<T> self = this;
		return new BufferCodec<>() {
			@Override
			public void write(Optional<T> value, ByteBuffer buffer) {
				if (value.isPresent()) {
					buffer.put((byte) 1);
					self.write(value.get(), buffer);
				} else {
					buffer.put((byte) 0);
				}
			}

			@Override
			public Optional<T> read(ByteBuffer buffer) {
				boolean present = buffer.get() != 0;
				return present ? Optional.of(self.read(buffer)) : Optional.empty();
			}
		};
	}

	static <T> BufferCodec<T> of(BiConsumer<T, ByteBuffer> writer, Function<ByteBuffer, T> reader) {
		return new BufferCodec<>() {
			@Override
			public void write(T value, ByteBuffer buffer) {
				writer.accept(value, buffer);
			}

			@Override
			public T read(ByteBuffer buffer) {
				return reader.apply(buffer);
			}
		};
	}

	BufferCodec<Byte> BYTE = of((v, b) -> b.put(v), ByteBuffer::get);
	BufferCodec<Short> SHORT = of((v, b) -> b.putShort(v), ByteBuffer::getShort);
	BufferCodec<Integer> INT = of((v, b) -> b.putInt(v), ByteBuffer::getInt);
	BufferCodec<Long> LONG = of((v, b) -> b.putLong(v), ByteBuffer::getLong);
	BufferCodec<Float> FLOAT = of((v, b) -> b.putFloat(v), ByteBuffer::getFloat);
	BufferCodec<Double> DOUBLE = of((v, b) -> b.putDouble(v), ByteBuffer::getDouble);
	BufferCodec<Boolean> BOOLEAN = BYTE.map(v -> v.intValue() != 0, v -> (byte) (v ? 1 : 0));
	BufferCodec<String> ID = of(
		(v, b) -> {
			byte[] bs = v.getBytes(StandardCharsets.UTF_8);
			b.put((byte) bs.length);
			b.put(bs);
		},
		b -> {
			int length = b.get() & 0xFF;
			byte[] bs = new byte[length];
			b.get(bs);
			return new String(bs, StandardCharsets.UTF_8);
		});
	BufferCodec<String> TEXT = of(
		(v, b) -> {
			byte[] bs = v.getBytes(StandardCharsets.UTF_8);
			b.putShort((short) bs.length);
			b.put(bs);
		},
		b -> {
			int length = b.getShort() & 0xFFFF;
			byte[] bs = new byte[length];
			b.get(bs);
			return new String(bs, StandardCharsets.UTF_8);
		});

	BufferCodec<UUID> UUID = ofTuple(
		LONG, java.util.UUID::getMostSignificantBits,
		LONG, java.util.UUID::getLeastSignificantBits,
		java.util.UUID::new);

	static <T extends Enum<T>> BufferCodec<T> ofEnum(T[] values) {
		Map<T, Integer> e2i = new HashMap<>();
		for (int i = 0; i < values.length; i++) e2i.put(values[i], i);
		if (values.length < 256) return BYTE.map(v -> values[v & 0xFF], v -> e2i.get(v).byteValue());
		if (values.length < 65536) return SHORT.map(v -> values[v & 0xFFFF], v -> e2i.get(v).shortValue());
		return INT.map(v -> values[v], v -> e2i.get(v));
	}

	static <T, P1> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, Tuple1<T, P1> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
			},
			b -> factory.apply(p1.read(b)));
	}

	static <T, P1, P2> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, BufferCodec<P2> p2, Function<T, P2> g2, Tuple2<T, P1, P2> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
				p2.write(g2.apply(v), b);
			},
			b -> factory.apply(p1.read(b), p2.read(b)));
	}

	static <T, P1, P2, P3> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, BufferCodec<P2> p2, Function<T, P2> g2, BufferCodec<P3> p3, Function<T, P3> g3, Tuple3<T, P1, P2, P3> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
				p2.write(g2.apply(v), b);
				p3.write(g3.apply(v), b);
			},
			b -> factory.apply(p1.read(b), p2.read(b), p3.read(b)));
	}

	static <T, P1, P2, P3, P4> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, BufferCodec<P2> p2, Function<T, P2> g2, BufferCodec<P3> p3, Function<T, P3> g3, BufferCodec<P4> p4, Function<T, P4> g4, Tuple4<T, P1, P2, P3, P4> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
				p2.write(g2.apply(v), b);
				p3.write(g3.apply(v), b);
				p4.write(g4.apply(v), b);
			},
			b -> factory.apply(p1.read(b), p2.read(b), p3.read(b), p4.read(b)));
	}

	static <T, P1, P2, P3, P4, P5> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, BufferCodec<P2> p2, Function<T, P2> g2, BufferCodec<P3> p3, Function<T, P3> g3, BufferCodec<P4> p4, Function<T, P4> g4, BufferCodec<P5> p5, Function<T, P5> g5, Tuple5<T, P1, P2, P3, P4, P5> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
				p2.write(g2.apply(v), b);
				p3.write(g3.apply(v), b);
				p4.write(g4.apply(v), b);
				p5.write(g5.apply(v), b);
			},
			b -> factory.apply(p1.read(b), p2.read(b), p3.read(b), p4.read(b), p5.read(b)));
	}

	static <T, P1, P2, P3, P4, P5, P6> BufferCodec<T> ofTuple(BufferCodec<P1> p1, Function<T, P1> g1, BufferCodec<P2> p2, Function<T, P2> g2, BufferCodec<P3> p3, Function<T, P3> g3, BufferCodec<P4> p4, Function<T, P4> g4, BufferCodec<P5> p5, Function<T, P5> g5, BufferCodec<P6> p6, Function<T, P6> g6, Tuple6<T, P1, P2, P3, P4, P5, P6> factory) {
		return of(
			(v, b) -> {
				p1.write(g1.apply(v), b);
				p2.write(g2.apply(v), b);
				p3.write(g3.apply(v), b);
				p4.write(g4.apply(v), b);
				p5.write(g5.apply(v), b);
				p6.write(g6.apply(v), b);
			},
			b -> factory.apply(p1.read(b), p2.read(b), p3.read(b), p4.read(b), p5.read(b), p6.read(b)));
	}

	@FunctionalInterface
	interface Tuple1<T, P1> {
		T apply(P1 p1);
	}

	@FunctionalInterface
	interface Tuple2<T, P1, P2> {
		T apply(P1 p1, P2 p2);
	}

	@FunctionalInterface
	interface Tuple3<T, P1, P2, P3> {
		T apply(P1 p1, P2 p2, P3 p3);
	}

	@FunctionalInterface
	interface Tuple4<T, P1, P2, P3, P4> {
		T apply(P1 p1, P2 p2, P3 p3, P4 p4);
	}

	@FunctionalInterface
	interface Tuple5<T, P1, P2, P3, P4, P5> {
		T apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
	}

	@FunctionalInterface
	interface Tuple6<T, P1, P2, P3, P4, P5, P6> {
		T apply(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
	}
}
