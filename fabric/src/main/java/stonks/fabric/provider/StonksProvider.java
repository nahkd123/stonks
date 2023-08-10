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
package stonks.fabric.provider;

import java.util.List;

import stonks.core.caching.StonksServiceCache;
import stonks.core.product.Product;
import stonks.core.service.StonksService;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.misc.TasksHandler;
import stonks.fabric.service.StonksServiceProvider;

public interface StonksProvider {
	/**
	 * <p>
	 * The service interface. You can make direct service calls with this service
	 * interface.
	 * </p>
	 * 
	 * @return The service interface.
	 */
	public StonksService getStonksService();

	/**
	 * <p>
	 * Obtain the Stonks service cache. You should use this cache instead of
	 * {@link #getStonksService()} for performance.
	 * </p>
	 * 
	 * @return The cache.
	 */
	public StonksServiceCache getStonksCache();

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
	 * Obtain the tasks handler, which can be used to handle tasks that might throw
	 * {@link Throwable}.
	 * </p>
	 * 
	 * @return The tasks handler.
	 */
	public TasksHandler getTasksHandler();

	public void startStonks(StonksServiceProvider service, List<StonksFabricAdapterProvider> adapters);
}
