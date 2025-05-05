package io.github.nahkd123.stonks.mc.fabric.config;

import java.nio.file.Path;

public record ConfigPaths(Path mainConfig, Path guiConfig) {
	public ConfigPaths(Path root) {
		this(root.resolve("stonks.config.json"), root.resolve("stonks.gui.json"));
	}
}
