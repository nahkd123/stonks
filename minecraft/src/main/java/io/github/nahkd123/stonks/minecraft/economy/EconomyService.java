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
package io.github.nahkd123.stonks.minecraft.economy;

import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;

/**
 * <p>
 * The economy service. In Stonks, there can be at most 1 economy service. This
 * is because a single stocks market can't have more than 1 economy system
 * (otherwise it would be chaos).
 * </p>
 * <p>
 * All operations are <b>asynchronous</b>: they can be executed without blocking
 * main thread, but the implementation must have requests queue to prevent race
 * conditions.
 * </p>
 */
public interface EconomyService {
	public TextComponent getDisplayName();

	public Currency getCurrency();

	/**
	 * <p>
	 * Get player's main account balance.
	 * </p>
	 * <p>
	 * Some plugins/mods may allows player to have more than 1 active account. In
	 * this case, the primary account will be picked. If there is no primary/main
	 * accounts, the first account will be picked. If there is no accounts, this
	 * will returns zero value.
	 * </p>
	 * <p>
	 * The task only completed exceptionally when an error occurred on the service
	 * adapter side (such as {@link NullPointerException}). Things like connection
	 * drops or insufficient balance should be presented on {@link Transaction}.
	 * </p>
	 * 
	 * @param player The player.
	 * @return The player's balance.
	 */
	public CompletableFuture<Long> getBalance(Player player);

	/**
	 * <p>
	 * Attempt to deposit money to player's main account.
	 * </p>
	 * <p>
	 * The task only completed exceptionally when an error occurred on the service
	 * adapter side (such as {@link NullPointerException}). Things like connection
	 * drops or insufficient balance should be presented on {@link Transaction}.
	 * </p>
	 * 
	 * @param player The player.
	 * @param amount Amount of money to deposit.
	 * @return Transaction result.
	 */
	public CompletableFuture<Transaction> depositTo(Player player, long amount);

	/**
	 * <p>
	 * Attempt to withdraw money from player's main account. Will fails if player
	 * doesn't have sufficient balance.
	 * </p>
	 * <p>
	 * The task only completed exceptionally when an error occurred on the service
	 * adapter side (such as {@link NullPointerException}). Things like connection
	 * drops or insufficient balance should be presented on {@link Transaction}.
	 * </p>
	 * 
	 * @param player The player.
	 * @param amount Amount of money to withdraw.
	 * @return Transaction result.
	 */
	public CompletableFuture<Transaction> withdrawFrom(Player player, long amount);
}
