package io.github.nahkd123.stonks.mc.fabric.gui.templated;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record DisplayTemplate(Optional<Either<Slotted, Text>> name, List<Either<Slotted, Text>> lore) {
	public static record Slotted(String name) {
		public static final MapCodec<Slotted> MAP_CODEC = Codec.STRING.fieldOf("$slot").xmap(
			Slotted::new,
			Slotted::name);
		public static final Codec<Slotted> CODEC = MAP_CODEC.codec();
	}

	public static final Codec<Either<Slotted, Text>> SLOT_OR_TEXT = Codec.either(Slotted.CODEC, TextCodecs.CODEC);
	public static final MapCodec<DisplayTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		SLOT_OR_TEXT.optionalFieldOf("name").forGetter(DisplayTemplate::name),
		SLOT_OR_TEXT.listOf().optionalFieldOf("lore", List.of()).forGetter(DisplayTemplate::lore))
		.apply(i, DisplayTemplate::new));
	public static final Codec<DisplayTemplate> CODEC = MAP_CODEC.codec();

	public ItemStack apply(ItemStack stack, Function<String, List<Text>> source, boolean clone) {
		if (clone) stack = stack.copy();
		if (name.isPresent()) stack.set(DataComponentTypes.ITEM_NAME, unpack(name.get(), source).get(0));
		if (!lore.isEmpty()) {
			List<Text> content = lore.stream().flatMap(p -> unpack(p, source).stream()).toList();
			stack.set(DataComponentTypes.LORE, new LoreComponent(content));
		}
		return stack;
	}

	public static List<Text> unpack(Either<Slotted, Text> either, Function<String, List<Text>> source) {
		if (either.left().isPresent()) {
			return source.apply(either.left().get().name);
		} else {
			return List.of(either.right().get());
		}
	}
}
