package stonks.fabric.provider;

import java.util.List;

import stonks.core.caching.StonksServiceCache;
import stonks.core.product.Product;
import stonks.core.service.StonksService;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
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

	public void startStonks(StonksServiceProvider service, List<StonksFabricAdapterProvider> adapters);
}
