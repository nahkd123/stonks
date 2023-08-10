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
package stonks.fabric.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import nahara.modkit.annotations.v1.AutoMixin;
import net.minecraft.server.MinecraftServer;
import stonks.core.caching.StonksServiceCache;
import stonks.core.service.LocalStonksService;
import stonks.core.service.StonksService;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.AdaptersContainer;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterCallback;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.misc.TasksHandler;
import stonks.fabric.provider.StonksProvider;
import stonks.fabric.service.StonksServiceProvider;

@AutoMixin
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements StonksProvider {
	@Unique
	private StonksService stonks$service;
	@Unique
	private StonksServiceCache stonks$cache;
	@Unique
	private AdaptersContainer stonks$adapters;
	@Unique
	private TasksHandler stonks$tasksHandler;

	@Override
	public StonksService getStonksService() { return stonks$service; }

	@Override
	public StonksServiceCache getStonksCache() { return stonks$cache; }

	@Override
	public StonksFabricAdapter getStonksAdapter() { return stonks$adapters; }

	@Override
	public TasksHandler getTasksHandler() { return stonks$tasksHandler; }

	@Override
	public void startStonks(StonksServiceProvider service, List<StonksFabricAdapterProvider> adapters) {
		stonks$service = service.createService((MinecraftServer) (Object) this);
		stonks$cache = new StonksServiceCache(stonks$service);
		stonks$tasksHandler = new TasksHandler();

		stonks$adapters = new AdaptersContainer();
		for (var p : adapters) stonks$adapters.add(p.createAdapter((MinecraftServer) (Object) this));

		StonksFabricAdapterCallback.EVENT
			.invoker()
			.registerAdaptersTo((MinecraftServer) (Object) this, stonks$adapters);

		StonksFabric.LOGGER.info("Now using {} service", stonks$service.getClass().getCanonicalName());
		StonksFabric.LOGGER.info("{} adapters found", stonks$adapters.getAdapters().size());

		if (stonks$service instanceof LocalStonksService localService) {
			StonksFabric.LOGGER.info("Local service found! Loading data...");
			localService.loadServiceData();
		}
	}
}
