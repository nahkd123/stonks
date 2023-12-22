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
package io.github.nahkd123.stonks.minecraft.text.placeholder;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.economy.CachedEconomyService;
import io.github.nahkd123.stonks.minecraft.text.LegacyColor;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;

public class PlayerPlaceholders implements Placeholders {
	private Player player;

	public PlayerPlaceholders(Player player) {
		this.player = player;
	}

	public Player getPlayer() { return player; }

	@Override
	public TextComponent replacePlaceholder(TextFactory text, String name) {
		CachedEconomyService economy = player.getServer().getEconomyService();

		return switch (name) {
		case "player.name" -> player.getDisplayName();
		case "player.balance" -> economy.getCachedBalance(player)
			.map(v -> economy.getCurrency().getDisplay(v))
			.getTriState(
				() -> text.literal("...").withColor(text.legacyColor(LegacyColor.GRAY)),
				err -> text.literal("Error!").withColor(text.legacyColor(LegacyColor.RED)));
		default -> null;
		};
	}
}
