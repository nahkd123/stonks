/*
 * Copyright (c) 2023-2025 nahkd
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
package io.github.nahkd123.stonks.mc.fabric.econ;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;

import net.minecraft.text.Text;

/**
 * <p>
 * Economy service manages the economy system of Minecraft server. By default,
 * the service uses gold ingot and nugget as currency (where 1 ingot = 9
 * nugget).
 * </p>
 * <p>
 * The economy service is <em>optional</em>, but player won't be able to place
 * any offer or make any instant buy requests, because their balance is observed
 * as zero.
 * </p>
 * 
 * @see #withdrawFrom(UUID, long)
 * @see #depositTo(UUID, long)
 * @see #rollback(Transaction)
 */
public interface EconomyService {
	/**
	 * <p>
	 * Format currency value for displaying in GUI.
	 * </p>
	 * 
	 * @param amount The amount of currency.
	 * @return The amount of currency formatted as string.
	 */
	String formatCurrency(long amount);

	default Text formatDisplayCurrency(long amount) {
		return Text.literal(formatCurrency(amount));
	}

	/**
	 * <p>
	 * Parse currency from formatted string, usually from configuration.
	 * </p>
	 * 
	 * @param formatted The formatted string.
	 * @return The amount of currency.
	 * @throws IllegalArgumentException if input string is not valid.
	 */
	long parseCurrency(String formatted) throws IllegalArgumentException;

	// TODO rollback to UUID for universal Minecraft platform abstraction layer
	CompletableFuture<Long> queryBalance(GameProfile player);

	CompletableFuture<Transaction> withdrawFrom(GameProfile player, long amount);

	CompletableFuture<Transaction> depositTo(GameProfile player, long amount);

	/**
	 * <p>
	 * Rollback a certain transaction. This async method must never fail.
	 * Transactions are only rolled back if service encounters an error that is
	 * recoverable.
	 * </p>
	 * 
	 * @param txn The transaction to rollback, usually obtained from
	 *            {@link #withdrawFrom(UUID, long)} or
	 *            {@link #depositTo(UUID, long)}.
	 * @return The task that will be completed once rollback finished.
	 */
	CompletableFuture<Void> rollback(Transaction txn);
}
