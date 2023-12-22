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
package io.github.nahkd123.stonks.minecraft.commodity;

import io.github.nahkd123.stonks.market.MarketService;
import io.github.nahkd123.stonks.minecraft.economy.EconomyService;
import stonks.core.service.StonksService;

/**
 * <p>
 * A service for managing commodities. Unlike {@link StonksService} and
 * {@link EconomyService}, there can be more than 1 {@link CommoditiesService}
 * active on a single server.
 * </p>
 * <p>
 * Commodities can be anything, ranging from items in player's inventory to
 * hypothetical stuffs like player's souls or mana from Botania (if that mod
 * ever supports Fabric)
 * </p>
 * <p>
 * All operations are <b>synchronous</b>: it might blocks main thread while
 * performing operations like removing items from player's inventory or adding
 * items.
 * </p>
 * <p>
 * Unlike other services, all commodities services that has been registered by
 * mods/plugins will be loaded when server starts.</p.
 */
public interface CommoditiesService {
	// TODO should we take in namespace?
	// maybe not now because the legacy configuration does not have namespace
	// the legacy looks like this: "item minecraft:diamond{CustomModelData:1}"
	// or this: "scoreboard myObjective"
	/**
	 * <p>
	 * Convert the string to {@link Commodity}.
	 * </p>
	 * 
	 * @param str The string representation of commodity/product type. Usually
	 *            obtained from {@link MarketService}.
	 * @return The commodity handler, or {@code null} if the string can't be
	 *         converted by this service (which will forwards to other services).
	 */
	public Commodity typeFromString(String str);
}
