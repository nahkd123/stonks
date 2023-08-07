package stonks.fabric.provider;

import net.minecraft.server.MinecraftServer;
import stonks.core.config.ConfigElement;

@FunctionalInterface
public interface ConfigurableProvider<T> {
	public T configure(MinecraftServer server, ConfigElement config);
}
