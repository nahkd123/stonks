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

import io.github.nahkd123.stonks.minecraft.text.ClickEvent;
import io.github.nahkd123.stonks.minecraft.text.TextColor;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class TextProxy implements TextComponent {
	private MutableText underlying;

	public TextProxy(MutableText underlying) {
		this.underlying = underlying;
	}

	public MutableText getUnderlying() { return underlying; }

	@Override
	public TextComponent withBold(Boolean bold) {
		underlying = underlying.styled(s -> s.withBold(bold));
		return this;
	}

	@Override
	public TextComponent withItalic(Boolean italic) {
		underlying = underlying.styled(s -> s.withItalic(italic));
		return this;
	}

	@Override
	public TextComponent withUnderline(Boolean underline) {
		underlying = underlying.styled(s -> s.withUnderline(underline));
		return this;
	}

	@Override
	public TextComponent withStrikethrough(Boolean strike) {
		underlying = underlying.styled(s -> s.withStrikethrough(strike));
		return this;
	}

	@Override
	public TextComponent withObfuscated(Boolean obfuscated) {
		underlying = underlying.styled(s -> s.withObfuscated(obfuscated));
		return this;
	}

	@Override
	public TextComponent withFont(String font) {
		underlying = underlying.styled(s -> s.withFont(font != null ? new Identifier(font) : null));
		return null;
	}

	@Override
	public TextComponent withColor(TextColor color) {
		underlying = underlying
			.styled(s -> color == null
				? null
				: s.withColor(color instanceof MinecraftTextColor mtc
					? mtc.getUnderlying()
					: net.minecraft.text.TextColor.parse(color.asJsonColor()).result().orElse(null)));
		return null;
	}

	@Override
	public TextComponent withInsertion(String text) {
		underlying = underlying.styled(s -> s.withInsertion(text));
		return this;
	}

	@Override
	public TextComponent withClickEvent(ClickEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent withHover(TextComponent hover) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TextComponent withExtras(TextComponent... children) {
		for (TextComponent child : children) {
			if (child instanceof TextProxy proxied) underlying.append(proxied.underlying);
		}

		return this;
	}

	@Override
	public Boolean getBold() { return underlying.getStyle().isBold(); }

	@Override
	public Boolean getItalic() { return underlying.getStyle().isItalic(); }

	@Override
	public Boolean getUnderline() { return underlying.getStyle().isUnderlined(); }

	@Override
	public Boolean getStrikethrough() { return underlying.getStyle().isStrikethrough(); }

	@Override
	public Boolean getObfuscated() { return underlying.getStyle().isObfuscated(); }

	@Override
	public String getFont() {
		Identifier id = underlying.getStyle().getFont();
		return id != null ? id.toString() : null;
	}

	@Override
	public TextColor getColor() { return new MinecraftTextColor(underlying.getStyle().getColor()); }

	@Override
	public String getInsertion() { return underlying.getStyle().getInsertion(); }

	@Override
	public ClickEvent getClickEvent() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getHover() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent[] getExtras() {
		return underlying.getSiblings().stream()
			.map(v -> v instanceof MutableText m ? m : null) // TODO not rely on MutableText
			.filter(v -> v != null)
			.map(v -> new TextProxy(v))
			.toArray(TextProxy[]::new);
	}
}
