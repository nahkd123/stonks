/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.fabric.economy;

import java.util.OptionalLong;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * <p>
 * An interface to access the economy system that is installed on the server.
 * There can be only 1 economy system that will be used by Stonks at a time.
 * </p>
 * <p>
 * I want to renew the economy adapter because I used double-precision floating
 * point number, plus the old legacy economy system is kinda bad anyways.
 * </p>
 */
public interface Economy {
	// TODO: To improve the sysadmin's experience when setting up Stonks, we might
	// have to employ automatic economy system detection. If there are more than 2
	// mods providing economy, we will pick the first one based on natural order of
	// the economy ID.

	/**
	 * <p>
	 * Get the economy ID.
	 * </p>
	 * 
	 * @return The economy ID.
	 */
	public Identifier getEconomyId();

	/**
	 * <p>
	 * Format the currency as string for display, usually as fixed point number. The
	 * returned value of this method must be reversible (a.k.a you can pass the
	 * returned value to {@link #tryParse(String)} and it would returns the same raw
	 * value).
	 * </p>
	 * 
	 * @param raw The raw value.
	 * @return The formatted string.
	 */
	public String formatCurrency(long raw);

	default Text formatAsDisplay(long raw) {
		return Text.literal(formatCurrency(raw)).styled(s -> s.withColor(Formatting.YELLOW));
	}

	/**
	 * <p>
	 * Try parsing the formatted currency string back to raw value.
	 * </p>
	 * 
	 * @param formatted The formatted string.
	 * @return The raw value.
	 */
	public OptionalLong tryParse(String formatted);

	/**
	 * <p>
	 * Obtain the balance of player as raw value.
	 * </p>
	 * 
	 * @param player The player.
	 * @return The raw value.
	 */
	public long balanceOf(ServerPlayerEntity player);

	/**
	 * <p>
	 * Withdraw some amount of money from player's purse.
	 * </p>
	 * 
	 * @param player The player.
	 * @param raw    The raw value.
	 * @return true if player have sufficient balance and transaction succeed.
	 */
	public boolean withdrawFrom(ServerPlayerEntity player, long raw);

	/**
	 * <p>
	 * Deposit some amount of money to player's purse.
	 * </p>
	 * 
	 * @param player The player.
	 * @param raw    The raw value.
	 * @return true if player have enough room in their purse and transaction
	 *         succeed.
	 */
	public boolean depositTo(ServerPlayerEntity player, long raw);
}
