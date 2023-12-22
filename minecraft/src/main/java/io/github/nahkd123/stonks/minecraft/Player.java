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
package io.github.nahkd123.stonks.minecraft;

import java.util.UUID;

import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;

public interface Player {
	public MinecraftServer getServer();

	default MinecraftPlatform getPlatform() { return getServer().getPlatform(); }

	/**
	 * <p>
	 * Get player's UUID. If you want to have player to object mapping, consider use
	 * {@link UUID} instead of {@link Player} as map's key.
	 * </p>
	 * 
	 * @return Player's unique identifier.
	 */
	public UUID getUuid();

	/**
	 * <p>
	 * Get the player's username. While it is okay to use username for player to
	 * object mapping when you are trying to calculate something, it is better to
	 * use {@link #getUUID()} as map's key.
	 * </p>
	 * 
	 * @return Player's username.
	 */
	public String getUsername();

	public TextComponent getDisplayName();

	/**
	 * <p>
	 * Send the message as system message to player's chat box. System messages will
	 * have "system message" indicator in player's chat HUD.
	 * </p>
	 * 
	 * @param message The message to send.
	 */
	public void sendSystemMessage(TextComponent message);

	/**
	 * <p>
	 * Send the message as action bar message that will be displayed above player's
	 * hotbar.
	 * </p>
	 * 
	 * @param message The message to send.
	 */
	public void sendActionBarMessage(TextComponent message);

	public void openGui(ContainerGui gui);
}
