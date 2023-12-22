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
package io.github.nahkd123.stonks.minecraft.fabric.text;

import io.github.nahkd123.stonks.minecraft.text.LegacyColor;
import io.github.nahkd123.stonks.minecraft.text.TextColor;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MinecraftTextFactory implements TextFactory {
	@Override
	public TextComponent empty() {
		return new TextProxy(Text.empty());
	}

	@Override
	public TextComponent literal(String text) {
		return new TextProxy(Text.literal(text));
	}

	@Override
	public TextComponent translate(String key, Object... args) {
		return new TextProxy(Text.translatable(key, args));
	}

	@Override
	public TextComponent translateFailback(String key, String failback, Object... args) {
		return new TextProxy(Text.translatableWithFallback(key, failback, args));
	}

	@Override
	public TextColor hexColor(int r, int g, int b) {
		return new MinecraftTextColor(net.minecraft.text.TextColor.fromRgb((r << 16) | (g << 8) | b));
	}

	@Override
	public TextColor namedColor(String name) {
		return new MinecraftTextColor(net.minecraft.text.TextColor.parse(name).result()
			.orElse(net.minecraft.text.TextColor.fromFormatting(Formatting.RESET)));
	}

	@Override
	public TextColor legacyColor(LegacyColor color) {
		return new MinecraftTextColor(net.minecraft.text.TextColor.fromFormatting(switch (color) {
		case BLACK -> Formatting.BLACK;
		case DARK_BLUE -> Formatting.DARK_BLUE;
		case DARK_GREEN -> Formatting.DARK_GREEN;
		case DARK_AQUA -> Formatting.DARK_AQUA;
		case DARK_RED -> Formatting.DARK_RED;
		case DARK_PURPLE -> Formatting.DARK_PURPLE;
		case GOLD -> Formatting.GOLD;
		case GRAY -> Formatting.GRAY;
		case DARK_GRAY -> Formatting.DARK_GRAY;
		case BLUE -> Formatting.BLUE;
		case GREEN -> Formatting.GREEN;
		case AQUA -> Formatting.AQUA;
		case RED -> Formatting.RED;
		case LIGHT_PURPLE -> Formatting.LIGHT_PURPLE;
		case YELLOW -> Formatting.YELLOW;
		case WHITE -> Formatting.WHITE;
		default -> Formatting.RESET;
		}));
	}
}
