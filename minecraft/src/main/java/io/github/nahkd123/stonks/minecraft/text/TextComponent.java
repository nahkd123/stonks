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
package io.github.nahkd123.stonks.minecraft.text;

/**
 * <p>
 * The text component that will be used by Stonks. This interface will sits on
 * top of existing text component system implementation (either being a proxy or
 * through Mixins interface injection).
 * </p>
 */
public interface TextComponent {
	public TextComponent withBold(Boolean bold);

	public TextComponent withItalic(Boolean italic);

	public TextComponent withUnderline(Boolean underline);

	public TextComponent withStrikethrough(Boolean strike);

	public TextComponent withObfuscated(Boolean obfuscated);

	public TextComponent withFont(String font);

	public TextComponent withColor(TextColor color);

	/**
	 * <p>
	 * When player click on this text component, it will replaces the player's chat
	 * content with insertion content in this component.
	 * </p>
	 * 
	 * @param text The insertion text, or {@code null} to unset.
	 * @return {@code this} for chaining.
	 */
	public TextComponent withInsertion(String text);

	public TextComponent withClickEvent(ClickEvent event);

	public TextComponent withHover(TextComponent hover);

	public TextComponent withExtras(TextComponent... children);

	public Boolean getBold();

	public Boolean getItalic();

	public Boolean getUnderline();

	public Boolean getStrikethrough();

	public Boolean getObfuscated();

	public String getFont();

	public TextColor getColor();

	public String getInsertion();

	public ClickEvent getClickEvent();

	public TextComponent getHover();

	public TextComponent[] getExtras();
}
