package stonks.fabric.provider;

import java.util.HashMap;
import java.util.Map;

import nahara.common.configurations.Config;
import stonks.core.service.StonksService;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.adapter.StonksFabricAdapterProvider;
import stonks.fabric.service.StonksServiceProvider;

public class StonksProvidersRegistry {
	private static final String NOT_SPECIFIED = "<Not specified>";
	private static Map<String, ConfigurableProvider<StonksService>> services = new HashMap<>();
	private static Map<String, ConfigurableProvider<StonksFabricAdapter>> adapters = new HashMap<>();

	public static StonksServiceProvider getServiceProvider(Config config) {
		var provider = config.getValue().map(v -> services.get(v.trim())).orElse(null);
		if (provider == null) {
			StonksFabric.LOGGER.warn("Unknown service provider: {}", config.getValue().orElse(NOT_SPECIFIED));
			return null;
		}

		return server -> provider.configure(server, config);
	}

	public static StonksFabricAdapterProvider getAdapterProvider(Config config) {
		var provider = config.getValue().map(v -> adapters.get(v.trim())).orElse(null);
		if (provider == null) {
			StonksFabric.LOGGER.warn("Unknown adapter provider: {}", config.getValue().orElse(NOT_SPECIFIED));
			return null;
		}

		return server -> provider.configure(server, config);
	}

	@SuppressWarnings("unchecked")
	public static <T extends StonksService> void registerService(Class<T> serviceClass, ConfigurableProvider<T> provider) {
		services.put(serviceClass.getCanonicalName(), (ConfigurableProvider<StonksService>) provider);
	}

	@SuppressWarnings("unchecked")
	public static <T extends StonksFabricAdapter> void registerAdapter(Class<T> serviceClass, ConfigurableProvider<T> provider) {
		adapters.put(serviceClass.getCanonicalName(), (ConfigurableProvider<StonksFabricAdapter>) provider);
	}
}
