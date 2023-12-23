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
package io.github.nahkd123.stonks.minecraft.utils;

import io.github.nahkd123.stonks.minecraft.text.LegacyColor;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;
import io.github.nahkd123.stonks.utils.lazy.LoadState;

public class LazyTexts {
	/**
	 * <p>
	 * Get the text component depending on the current loading state. If the loading
	 * state is {@link LoadState#SUCCESS}, it will returns the value. Returns
	 * "Loading..." if the state is {@link LoadState#LOADING} and "Error!" if the
	 * state is {@link LoadState#FAILED}.
	 * </p>
	 * 
	 * @param loader  The loader.
	 * @param factory The text components factory.
	 * @return The text component.
	 */
	public static TextComponent loading(LazyLoader<TextComponent> loader, TextFactory factory) {
		return loader.getTriState(
			() -> factory.literal("Loading...").withColor(factory.legacyColor(LegacyColor.GRAY)),
			t -> factory.literal("Error!").withColor(factory.legacyColor(LegacyColor.RED)));
	}
}
