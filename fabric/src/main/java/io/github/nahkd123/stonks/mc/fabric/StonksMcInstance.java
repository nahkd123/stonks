package io.github.nahkd123.stonks.mc.fabric;

import java.util.List;

import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.product.Category;
import io.github.nahkd123.stonks.service.ManageableMarketService;
import io.github.nahkd123.stonks.service.MarketService;

/**
 * <p>
 * Represent a running instance of Stonks under Minecraft server.
 * </p>
 * <p>
 * To obtain the instance, attach your callback in
 * {@link StonksMcCallbacks#INSTANCE_INIT} or obtain with
 * {@link StonksMod#getInstance(net.minecraft.server.MinecraftServer)} static
 * method.
 * </p>
 * 
 * @see StonksMcCallbacks#INSTANCE_INIT
 * @see StonksMod#getInstance(net.minecraft.server.MinecraftServer)
 */
public interface StonksMcInstance {
	/**
	 * <p>
	 * Get the market service instance that is running in this Stonks instance.
	 * Every Stonks instance must have market service present.
	 * </p>
	 * 
	 * @return Market service.
	 */
	MarketService getMarketService();

	/**
	 * <p>
	 * Get the economy service for handling payments (when player place offers for
	 * example). Economy service is <em>optional</em>, but without it, all players
	 * will be considered as having no money in balance/purse.
	 * </p>
	 * 
	 * @return Economy service, can be {@code null}.
	 */
	EconomyService getEconomyService();

	/**
	 * <p>
	 * Get the configured catalog. The catalog may not reflect the actual catalog
	 * from market service, but it contains the important bits for categorizing
	 * products, as well as providing product information for interacting with
	 * player's inventory.
	 * </p>
	 * <p>
	 * If {@link #getMarketService()} returns {@link ManageableMarketService}, the
	 * catalog in service will sync with configured catalog, unless sync is
	 * disabled. Note that deleting a product from configuration will also delete
	 * the product on service, which indirectly delete all offers referring to that
	 * product. Beware of potential data loss before you continue.
	 * </p>
	 * 
	 * @return The configured catalog.
	 */
	List<Category> getCatalog();

	/**
	 * <p>
	 * Get the tax rate, which is the fraction of product price to be taken when
	 * claiming sell offers or performing instant sell. Tax rate cannot be negative
	 * or greater than {@code 1.00}. If the tax rate is {@code 1.00}, all incomes
	 * are taken as tax (and user earn nothing from selling products).
	 * </p>
	 * 
	 * @return The configured tax rate.
	 */
	double getTax();

	/**
	 * <p>
	 * Calculate how much money to take from income.
	 * </p>
	 * 
	 * @param income The income/earned money.
	 * @return The tax money.
	 */
	default long calculateTax(long income) {
		return (long) (income * getTax());
	}
}
