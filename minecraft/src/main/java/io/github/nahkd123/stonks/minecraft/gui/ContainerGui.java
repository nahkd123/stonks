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
package io.github.nahkd123.stonks.minecraft.gui;

import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.minecraft.text.placeholder.Placeholders;

public interface ContainerGui extends Placeholders {
	public MinecraftServer getServer();

	public String getId();

	default TextFactory getTextFactory() { return getServer().getPlatform().getTextFactory(); }

	/**
	 * <p>
	 * Get the native button from Stonks native button ID. The native button is
	 * normally obtained from global Stonks for Minecraft native buttons registry,
	 * which means you can display the products category button inside buy menu!.
	 * </p>
	 * 
	 * @param id      Native button ID.
	 * @param ordinal The ordinal of the button. This will be used in configurations
	 *                to determine the index of "list" that the button is trying to
	 *                refers to. For example, {@code CategoryButton} takes in
	 *                ordinal to display category to the GUI.
	 * @return Native button.
	 */
	public NativeButton getNative(String id, int ordinal);
}
