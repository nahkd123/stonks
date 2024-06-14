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
package stonks.fabric;

import java.util.List;
import java.util.concurrent.Future;

import stonks.core.caching.StonksCache;
import stonks.core.caching.StonksServiceCache;
import stonks.core.platform.StonksPlatform;
import stonks.core.product.Product;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.economy.Economy;
import stonks.fabric.economy.LegacyEconomy;
import stonks.fabric.misc.StonksSounds;
import stonks.fabric.misc.TasksHandler;
import stonks.fabric.service.StonksServiceProvider;

public interface StonksFabricPlatform extends StonksPlatform {
	/**
	 * <p>
	 * Obtain the legacy Stonks service cache.
	 * </p>
	 * 
	 * @return The cache.
	 * @deprecated use {@link #getStonksCache()}. This method is there to help you
	 *             transitioning to APIs that uses {@link Future}.
	 */
	@Deprecated(forRemoval = true)
	public StonksServiceCache getLegacyStonksCache();

	/**
	 * <p>
	 * Obtain the Stonks service cache. The cache performs caching automatically and
	 * you should use this instead of your own caching solution, as this will be
	 * shared across all mods.
	 * </p>
	 * 
	 * @return The cache.
	 */
	public StonksCache getStonksCache();

	/**
	 * <p>
	 * Obtain the adapter which can be used to convert {@link Product} to whatever
	 * objects in Fabric platform.
	 * </p>
	 * 
	 * @return The adapter.
	 */
	public StonksFabricAdapter getStonksAdapter();

	/**
	 * <p>
	 * Get currently active economy system. This will always return
	 * {@link LegacyEconomy} for now.
	 * </p>
	 * 
	 * @return The currently active economy system.
	 */
	public Economy getEconomySystem();

	/**
	 * <p>
	 * Obtain the tasks handler, which can be used to handle tasks that might throw
	 * {@link Throwable}.
	 * </p>
	 * 
	 * @return The tasks handler.
	 */
	public TasksHandler getTasksHandler();

	/**
	 * <p>
	 * Platform-specific configurations. These configurations are not shared through
	 * services, unlike {@code useService}.
	 * </p>
	 * 
	 * @return Platform configurations.
	 */
	public PlatformConfig getPlatformConfig();

	/**
	 * <p>
	 * Obtain the sounds handler to play and schedule sounds.
	 * </p>
	 * 
	 * @return The sounds handler.
	 */
	public StonksSounds getSounds();

	public void startStonks(PlatformConfig config, StonksServiceProvider service, List<StonksFabricAdapterProvider> adapters);
}
