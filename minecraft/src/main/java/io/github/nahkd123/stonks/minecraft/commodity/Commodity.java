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
package io.github.nahkd123.stonks.minecraft.commodity;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;

public interface Commodity {
	/**
	 * <p>
	 * Get the display name of this commodity.
	 * </p>
	 * 
	 * @return The display name of this commodity.
	 */
	public TextComponent getDisplayName();

	/**
	 * <p>
	 * Get the display item as item string. The string representation is
	 * "[namespace:]&lt;id&gt;{NBT data...}". For example:
	 * "minecraft:apple{display:{Name:'{"text": "Red Apple"}'}}"
	 * </p>
	 * 
	 * @return The item string that follows the format.
	 */
	public String getDisplayItemString();

	/**
	 * <p>
	 * Count how much of this items the player currently have.
	 * </p>
	 * 
	 * @param player The player.
	 * @return Amount of items.
	 */
	public long getAmountAvailable(Player player);

	/**
	 * <p>
	 * Take from target player.
	 * </p>
	 * 
	 * @param player Player to take items.
	 * @param amount Amount of items.
	 * @return Amount of leftover items that couldn't be taken (either not enough
	 *         items or something else).
	 */
	public long takeAmount(Player player, long amount);

	/**
	 * <p>
	 * Give to target player.
	 * </p>
	 * 
	 * @param player Player to get items.
	 * @param amount Amount of items.
	 * @return Amount of leftover items that couldn't be added to player.
	 */
	public long giveAmount(Player player, long amount);
}
