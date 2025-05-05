package io.github.nahkd123.stonks.mc.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface StonksMcCallbacks {
	/**
	 * <p>
	 * Universe initialization callbacks will be called during the Stonks
	 * initialization phase. You can register platform-specific providers here.
	 * </p>
	 */
	static Event<OnUniverseInit> UNIVERSE_INIT = EventFactory.createArrayBacked(OnUniverseInit.class,
		listeners -> universe -> {
			for (OnUniverseInit listener : listeners) listener.onUniverseInit(universe);
		});

	@FunctionalInterface
	interface OnUniverseInit {
		void onUniverseInit(StonksMcUniverse universe);
	}

	/**
	 * <p>
	 * Instance initialization callbacks will be called during the instance
	 * initialization phase (which is during server starting phase).
	 * </p>
	 */
	static Event<OnInstanceInit> INSTANCE_INIT = EventFactory.createArrayBacked(OnInstanceInit.class,
		listeners -> instance -> {
			for (OnInstanceInit listener : listeners) listener.onInstanceInit(instance);
		});

	@FunctionalInterface
	interface OnInstanceInit {
		void onInstanceInit(StonksMcInstance instance);
	}
}
