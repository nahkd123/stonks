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
package stonks.fabric.adapter;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.core.product.Product;

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
	 * @return Number of units.
	 */
	default AdapterResponse<Integer> getUnits(ServerPlayerEntity player, Product product) {
		return AdapterResponse.pass();
	}

	/**
	 * <p>
	 * Attempt to add units to player.
	 * </p>
	 * 
	 * @param player  Player that will receive the product.
	 * @param product Product type.
	 * @param amount  How many units to add.
	 * @return Nothing.
	 * @implNote You can reconstruct product data by reading value from
	 *           {@link Product#getProductConstructionData()}.
	 */
	default AdapterResponse<Void> addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
		return AdapterResponse.pass();
	}

	/**
	 * <p>
	 * Attempt to remove units from player.
	 * </p>
	 * 
	 * @param player  Player that will have their products taken away.
	 * @param product Product type.
	 * @param amount  How many units to take.
	 * @return Nothing.
	 */
	default AdapterResponse<Void> removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
		return AdapterResponse.pass();
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
	 * @return Player's balance in primary account.
	 */
	default AdapterResponse<Double> accountBalance(ServerPlayerEntity player) {
		return AdapterResponse.pass();
	}

	/**
	 * <p>
	 * Attempt to deposit money to player's primary account.
	 * </p>
	 * 
	 * @param player Player that will receive money to their primary account.
	 * @param money  Amount of money to receive.
	 * @return Nothing.
	 */
	default AdapterResponse<Void> accountDeposit(ServerPlayerEntity player, double money) {
		return AdapterResponse.pass();
	}

	/**
	 * <p>
	 * Attempt to withdraw money from player's primary account.
	 * </p>
	 * 
	 * @param player Player that will have their primary account withdrawn.
	 * @param money  Amount of money to withdraw.
	 * @return Nothing.
	 */
	default AdapterResponse<Void> accountWithdraw(ServerPlayerEntity player, double money) {
		return AdapterResponse.pass();
	}
}
