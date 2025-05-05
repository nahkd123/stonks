package io.github.nahkd123.stonks.mc.fabric.product;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * <p>
 * Product info, which may acts more or less like good ol' reconstruction data
 * from Stonks v2. Product info is configured entirely on instance side for now.
 * </p>
 */
public interface ProductInfo {
	/**
	 * <p>
	 * Get the display item. If product is an item, this may be the same as target
	 * item, otherwise it is the representation of this product for display in GUIs.
	 * </p>
	 * 
	 * @return The display item.
	 */
	ItemStack display();

	/**
	 * <p>
	 * Count how much units of this product is in player's inventory. Inventory
	 * doesn't have to store items.
	 * </p>
	 * 
	 * @param player The player to query.
	 * @return Number of units.
	 */
	long getInventory(ServerPlayerEntity player);

	void giveTo(ServerPlayerEntity player, long amount);

	void takeFrom(ServerPlayerEntity player, long amount);
}
