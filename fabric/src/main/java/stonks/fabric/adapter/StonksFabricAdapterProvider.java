package stonks.fabric.adapter;

import net.minecraft.server.MinecraftServer;

public interface StonksFabricAdapterProvider {
	public StonksFabricAdapter createAdapter(MinecraftServer server);
}
