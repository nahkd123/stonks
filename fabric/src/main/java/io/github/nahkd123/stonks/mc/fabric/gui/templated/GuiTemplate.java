package io.github.nahkd123.stonks.mc.fabric.gui.templated;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record GuiTemplate(Text title, ScreenHandlerType<?> type, Optional<Integer> width, Optional<Integer> height, List<Button> buttons) {
	private static final Codec<ScreenHandlerType<?>> TYPE_CODEC = Codec.lazyInitialized(
		Registries.SCREEN_HANDLER::getCodec);
	public static final MapCodec<GuiTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		TextCodecs.CODEC.fieldOf("title").forGetter(GuiTemplate::title),
		TYPE_CODEC.fieldOf("type").forGetter(GuiTemplate::type),
		Codec.INT.optionalFieldOf("width").forGetter(GuiTemplate::width),
		Codec.INT.optionalFieldOf("height").forGetter(GuiTemplate::height),
		Button.CODEC.listOf().optionalFieldOf("buttons", List.of()).forGetter(GuiTemplate::buttons))
		.apply(i, GuiTemplate::new));
	public static final Codec<GuiTemplate> CODEC = MAP_CODEC.codec();
}
