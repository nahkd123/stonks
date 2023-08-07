package stonks.fabric.service;

import net.minecraft.server.MinecraftServer;
import stonks.core.service.StonksService;

@FunctionalInterface
public interface StonksServiceProvider {
	public StonksService createService(MinecraftServer server);
}
