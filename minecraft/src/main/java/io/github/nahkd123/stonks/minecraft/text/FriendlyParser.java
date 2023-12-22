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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.nahkd123.stonks.minecraft.text.placeholder.Placeholders;

public class FriendlyParser {
	private static final class CharStream {
		private CharSequence input;
		private int index = 0;

		public CharStream(CharSequence input) {
			this.input = input;
		}

		public char get() {
			return input.charAt(index);
		}

		public void advance(int by) {
			index += by;
		}

		public boolean hasNext() {
			return index < input.length();
		}
	}

	public static TextComponent parse(TextFactory factory, String input, Placeholders placeholders) {
		if (input == null || input.isEmpty()) return factory.empty();
		return parseInternal(factory, new CharStream(input), placeholders);
	}

	private static String nextEnclosure(CharStream input, char start, char end) {
		if (!input.hasNext() || input.get() != start) return null;
		String tagName = "";
		boolean escape = false;

		input.advance(1);
		while (input.hasNext()) {
			if (escape) {
				escape = false;
				tagName += input.get();
				input.advance(1);
				continue;
			}

			if (input.get() == '\\') {
				escape = true;
				input.advance(1);
				continue;
			}

			if (input.get() == end) {
				input.advance(1);
				return tagName;
			}

			tagName += input.get();
			input.advance(1);
		}

		input.advance(-tagName.length() - 1);
		return null;
	}

	private static TextComponent parseInternal(TextFactory factory, CharStream input, Placeholders placeholders) {
		// When we reach the end OR [/], end the parsing
		String content = "";
		String tagName;
		boolean escape = false;
		boolean styledInBetween = false;

		String parent = "";
		List<TextComponent> extras = new ArrayList<>();

		while (input.hasNext()) {
			if (escape) {
				escape = false;
				content += input.get();
				input.advance(1);
				continue;
			}

			if (input.get() == '\\') {
				escape = true;
				input.advance(1);
				continue;
			}

			if ((tagName = nextEnclosure(input, '[', ']')) != null) {
				if (tagName.equals("/")) break;

				// TODO duplicated code
				if (!content.isEmpty()) {
					if (!styledInBetween) {
						parent = content;
					} else {
						if (!parent.isEmpty()) {
							extras.add(0, factory.literal(parent));
							parent = "";
						}

						extras.add(factory.literal(content));
					}

					content = "";
				}

				TextComponent next = parseInternal(factory, input, placeholders);
				applyStyling(tagName, factory, next);
				extras.add(next);
				styledInBetween = true;
				continue;
			}

			if (placeholders != null && (tagName = nextEnclosure(input, '{', '}')) != null) {
				// TODO duplicated code
				if (!content.isEmpty()) {
					if (!styledInBetween) {
						parent = content;
					} else {
						if (!parent.isEmpty()) {
							extras.add(0, factory.literal(parent));
							parent = "";
						}

						extras.add(factory.literal(content));
					}

					content = "";
				}

				TextComponent placeholderContent;
				if ((placeholderContent = placeholders.replacePlaceholder(factory, tagName)) != null) {
					extras.add(placeholderContent);
					styledInBetween = true;
				}

				continue;
			}

			content += input.get();
			input.advance(1);
		}

		if (!content.isEmpty()) {
			if (styledInBetween) {
				extras.add(factory.literal(content));
			} else {
				parent = content;
			}
		}

		return (parent.isEmpty() ? factory.empty() : factory.literal(parent))
			.withExtras(extras.toArray(TextComponent[]::new));
	}

	private static final Pattern HEX_COLOR_PATTERN = Pattern
		.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

	private static record TextModifier(String shortName, String longName, Consumer<TextComponent> positive, Consumer<TextComponent> negative, Consumer<TextComponent> absent) {
	}

	private static final List<TextModifier> MODIFIERS = Arrays.asList(
		new TextModifier("b", "bold", t -> t.withBold(true), t -> t.withBold(false), t -> t.withBold(null)),
		new TextModifier("i", "italic", t -> t.withItalic(true), t -> t.withItalic(false), t -> t.withItalic(null)),
		new TextModifier("u", "underline", t -> t.withUnderline(true), t -> t.withUnderline(false), t -> t
			.withUnderline(null)),
		new TextModifier("s", "strike", t -> t.withStrikethrough(true), t -> t.withStrikethrough(false), t -> t
			.withStrikethrough(null)),
		new TextModifier("o", "obfuscate", t -> t.withObfuscated(true), t -> t.withObfuscated(false), t -> t
			.withObfuscated(null)));

	private static void applyStyling(String tag, TextFactory factory, TextComponent text) {
		if (tag.startsWith("font:")) {
			text.withFont(tag.substring("font:".length()));
			return;
		}

		if (tag.startsWith("color:")) {
			text.withColor(factory.namedColor(tag.substring("color:".length())));
			return;
		}

		if (tag.startsWith("#")) {
			Matcher matcher = HEX_COLOR_PATTERN.matcher(tag);
			if (!matcher.matches()) return;

			int r = Integer.parseInt(matcher.group(1), 16);
			int g = Integer.parseInt(matcher.group(2), 16);
			int b = Integer.parseInt(matcher.group(3), 16);
			text.withColor(factory.hexColor(r, g, b));
			return;
		}

		for (TextModifier mod : MODIFIERS) {
			if (tag.equalsIgnoreCase(mod.shortName) || tag.equalsIgnoreCase(mod.longName)) {
				mod.positive.accept(text);
				return;
			}

			if (tag.equalsIgnoreCase("!" + mod.shortName) || tag.equalsIgnoreCase("not-" + mod.longName)) {
				mod.negative.accept(text);
				return;
			}

			if (tag.equalsIgnoreCase("~" + mod.shortName) || tag.equalsIgnoreCase("no-" + mod.longName)) {
				mod.absent.accept(text);
				return;
			}
		}
	}
}
