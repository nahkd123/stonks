package io.github.nahkd123.stonks.mc.fabric.gui.templated;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public record Button(Slot slot, Optional<ItemStack> item, Optional<String> special, Optional<DisplayTemplate> template) {

	public static final MapCodec<Button> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Slot.CODEC.fieldOf("slot").forGetter(Button::slot),
		ItemStack.CODEC.optionalFieldOf("item").forGetter(Button::item),
		Codec.STRING.optionalFieldOf("special").forGetter(Button::special),
		DisplayTemplate.CODEC.optionalFieldOf("template").forGetter(Button::template))
		.apply(i, Button::new));
	public static final Codec<Button> CODEC = MAP_CODEC.codec();

	public ItemStack apply(ItemStack stack, Function<String, List<Text>> source, boolean clone) {
		if (item.isPresent()) stack = item.get().copy();
		else if (clone) stack = stack.copy();
		if (template.isPresent()) stack = template.get().apply(stack, source, clone);
		return stack;
	}
}
