package stonks.fabric.provider;

import nahara.common.configurations.Config;
import net.minecraft.server.MinecraftServer;

@FunctionalInterface
public interface ConfigurableProvider<T> {
	public T configure(MinecraftServer server, Config config);
}
