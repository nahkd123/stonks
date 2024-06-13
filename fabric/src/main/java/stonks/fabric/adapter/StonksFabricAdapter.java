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
package stonks.fabric.adapter;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.core.product.Product;
import stonks.fabric.economy.Economy;

/**
 * <p>
 * Implement this interface to convert from {@link Product} to its equivalents
 * and vice versa.
 * </p>
 */
public interface StonksFabricAdapter {
	/**
	 * <p>
	 * Get how many units this player have.
	 * </p>
	 * 
	 * @param player  Player to check.
	 * @param product Product type.
	 * @return Number of units. Negative value if this adapter does not support.
	 */
	default int getUnits(ServerPlayerEntity player, Product product) {
		return -1;
	}

	/**
	 * <p>
	 * Attempt to add units to player.
	 * </p>
	 * 
	 * @param player  Player that will receive the product.
	 * @param product Product type.
	 * @param amount  How many units to add.
	 * @return true if this adapter managed to put {@link Product} to player.
	 * @implNote You can reconstruct product data by reading value from
	 *           {@link Product#getProductConstructionData()}.
	 */
	default boolean addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
		return false;
	}

	/**
	 * <p>
	 * Attempt to remove units from player.
	 * </p>
	 * 
	 * @param player  Player that will have their products taken away.
	 * @param product Product type.
	 * @param amount  How many units to take.
	 * @return true if this adapter managed to take out {@link Product} from player.
	 */
	default boolean removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
		return false;
	}

	default ItemStack createDisplayStack(Product product) {
		return null;
	}

	/**
	 * <p>
	 * Attempt to get account balance in player's primary account.
	 * </p>
	 * 
	 * @param player Player to obtain their balance in primary account.
	 * @return Negative value if this adapter failed to obtain balance, positive
	 *         value (including zero) if this adapter managed to obtain balance.
	 * @deprecated use {@link Economy#balanceOf(ServerPlayerEntity)}
	 */
	@Deprecated
	default double accountBalance(ServerPlayerEntity player) {
		return -1d;
	}

	/**
	 * <p>
	 * Attempt to deposit money to player's primary account.
	 * </p>
	 * 
	 * @param player Player that will receive money to their primary account.
	 * @param money  Amount of money to receive.
	 * @return true if deposit successful. false will allow other adapters to
	 *         deposit.
	 * @deprecated use {@link Economy#depositTo(ServerPlayerEntity, long)}
	 */
	@Deprecated
	default boolean accountDeposit(ServerPlayerEntity player, double money) {
		return false;
	}

	/**
	 * <p>
	 * Attempt to withdraw money from player's primary account.
	 * </p>
	 * 
	 * @param player Player that will have their primary account withdrawn.
	 * @param money  Amount of money to withdraw.
	 * @return true if withdraw successful. false will allow other adapters to
	 *         withdraw.
	 * @deprecated use {@link Economy#withdrawFrom(ServerPlayerEntity, long)}
	 */
	@Deprecated
	default boolean accountWithdraw(ServerPlayerEntity player, double money) {
		return false;
	}
}
