package io.github.nahkd123.stonks.mc.fabric.gui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.mc.fabric.gui.templated.GuiTemplate;

/**
 * <p>
 * I've decided to pause development on GUI templating system for now. We will
 * come back to it in distant future.
 * </p>
 */
public record StonksGuis(GuiTemplate market) {
	public static final MapCodec<StonksGuis> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		GuiTemplate.CODEC.fieldOf("market").forGetter(StonksGuis::market))
		.apply(i, StonksGuis::new));
}
