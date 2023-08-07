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

	@Override
	public StonksService getStonksService() { return stonks$service; }

	@Override
	public StonksServiceCache getStonksCache() { return stonks$cache; }

	@Override
	public StonksFabricAdapter getStonksAdapter() { return stonks$adapters; }

	@Override
	public void startStonks(StonksServiceProvider service, List<StonksFabricAdapterProvider> adapters) {
		stonks$service = service.createService((MinecraftServer) (Object) this);
		stonks$cache = new StonksServiceCache(stonks$service);

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
