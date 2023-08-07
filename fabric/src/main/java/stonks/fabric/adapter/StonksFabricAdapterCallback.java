package stonks.fabric.adapter;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public class StonksFabricAdapterCallback {
	@FunctionalInterface
	public static interface AdapterCallback {
		public void registerAdaptersTo(MinecraftServer server, AdaptersContainer container);
	}

	/**
	 * <p>
	 * Event fired when Stonks service is starting.
	 * </p>
	 */
	public static final Event<AdapterCallback> EVENT = EventFactory.createArrayBacked(AdapterCallback.class,
		callbacks -> (server, container) -> {
			for (var cb : callbacks) cb.registerAdaptersTo(server, container);
		});
}
