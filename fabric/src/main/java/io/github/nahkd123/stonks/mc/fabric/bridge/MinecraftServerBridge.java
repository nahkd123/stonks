package io.github.nahkd123.stonks.mc.fabric.bridge;

import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;

public interface MinecraftServerBridge {
	void attachStonks(StonksMcInstance instance);

	StonksMcInstance getStonks();
}
