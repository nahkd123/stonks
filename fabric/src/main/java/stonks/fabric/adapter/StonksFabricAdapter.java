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
	 */
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
	 */
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
	 */
	default boolean accountWithdraw(ServerPlayerEntity player, double money) {
		return false;
	}
}
