/*
 * Copyright (c) 2023 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.nahkd123.stonks.minecraft.fabric;

import io.github.nahkd123.stonks.minecraft.fabric.command.DevelopmentCommand;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.ButtonTemplateFactory;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.DynamicButtonTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.EmptyButtonTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.GuiTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.LazyButtonTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.StaticButtonTemplate;
import nahara.common.configurations.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabric;

public class ModernStonksFabric {
	private static FabricPlatform platform;

	public static void initialize() {
		platform = new FabricPlatform();

		StonksFabric.LOGGER.info("Initializing modern Stonks API...");
		StonksFabric.LOGGER.info("The modern Stonks API is experimental!");
		initializeGuiSystem();
		initializeDevelopmentEnvironment();
	}

	public static FabricPlatform getPlatform() { return platform; }

	private static void initializeGuiSystem() {
		// Register button template factories here!
		ButtonTemplateFactory.register(new Identifier(StonksFabric.MODID, "empty"), EmptyButtonTemplate.FACTORY);
		ButtonTemplateFactory.register(new Identifier(StonksFabric.MODID, "static"), StaticButtonTemplate.FACTORY);
		ButtonTemplateFactory.register(new Identifier(StonksFabric.MODID, "dynamic"), DynamicButtonTemplate.FACTORY);
		ButtonTemplateFactory.register(new Identifier(StonksFabric.MODID, "lazy_loaded"), LazyButtonTemplate.FACTORY);

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			FabricServer wrapper = platform.wrap(server);

			for (Config guiConfig : StonksFabric.getOrCreateConfig("gui.txt").getChildren()) {
				StonksFabric.LOGGER.info("Loading configured GUI: {}...", guiConfig.getKey());
				wrapper.getGuiTemplates().put(guiConfig.getKey(), new GuiTemplate().configure(guiConfig));
			}
		});

		StonksFabric.LOGGER.info("Initialized GUI system!");
	}

	private static void initializeDevelopmentEnvironment() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			StonksFabric.LOGGER.info("Development environment detected! Loading some funny debugging stuffs...");

			CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
				dispatcher.register(DevelopmentCommand.ROOT);
			});
		}
	}
}
