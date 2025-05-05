package io.github.nahkd123.stonks.mc.fabric.gui.templated;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface Slot {
	IntStream stream(int width, int height);

	Codec<Slot> CODEC = Codec.recursive("Slot", self -> Codec.either(
		self.listOf().xmap(Multiple::new, Multiple::children),
		Codec.either(Area.CODEC, Codec.either(Indexed.CODEC, Coords.CODEC)
			.xmap(
				e -> e.left().<Slot>map(v -> v).or(e::right).get(),
				v -> v instanceof Indexed i ? Either.left(i) : Either.right((Coords) v)))
			.xmap(
				e -> e.right().or(e::left).get(),
				v -> v instanceof Area a ? Either.left(a) : Either.right(v)))
		.xmap(
			e -> e.right().or(e::left).get(),
			v -> v instanceof Multiple m ? Either.left(m) : Either.right(v)));

	record Indexed(int index) implements Slot {
		public static final Codec<Indexed> CODEC = Codec.INT.xmap(Indexed::new, Indexed::index);

		@Override
		public IntStream stream(int width, int height) {
			return IntStream.of(index);
		}
	}

	record Coords(int x, int y) implements Slot {
		public static final MapCodec<Coords> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("x").forGetter(Coords::x),
			Codec.INT.fieldOf("y").forGetter(Coords::y))
			.apply(i, Coords::new));
		public static final Codec<Coords> CODEC = MAP_CODEC.codec();

		@Override
		public IntStream stream(int width, int height) {
			return IntStream.of(y * width + x);
		}
	}

	record Range(int from, int to) implements Slot {
		public static final MapCodec<Range> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("from").forGetter(Range::from),
			Codec.INT.fieldOf("to").forGetter(Range::to))
			.apply(i, Range::new));
		public static final Codec<Range> CODEC = MAP_CODEC.codec();

		@Override
		public IntStream stream(int width, int height) {
			return IntStream.range(from, to);
		}
	}

	record Area(Coords from, Coords to) implements Slot {
		public static final MapCodec<Area> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Coords.CODEC.fieldOf("from").forGetter(Area::from),
			Coords.CODEC.fieldOf("to").forGetter(Area::to))
			.apply(i, Area::new));
		public static final Codec<Area> CODEC = MAP_CODEC.codec();

		@Override
		public IntStream stream(int width, int height) {
			int cw = to.x - from.x;
			int ch = to.y - from.y;
			return IntStream.range(0, cw * ch).map(i -> (i / cw + from.y) * width + (i % cw + from.x));
		}
	}

	record Multiple(List<Slot> children) implements Slot {
		@Override
		public IntStream stream(int width, int height) {
			return children.stream().flatMapToInt(s -> s.stream(width, height));
		}
	}
}
