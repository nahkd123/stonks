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
package io.github.nahkd123.stonks.minecraft.fabric.gui.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import io.github.nahkd123.stonks.minecraft.fabric.gui.ContainerGuiFrontend;
import io.github.nahkd123.stonks.minecraft.fabric.gui.Fill;
import io.github.nahkd123.stonks.minecraft.text.FriendlyParser;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import nahara.common.configurations.Config;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabric;

public class GuiTemplate {
	public Function<ContainerGuiFrontend, TextComponent> title;
	public ScreenHandlerType<?> type;
	public final List<ButtonTemplate> buttons = new ArrayList<>();

	public GuiTemplate configure(Config config) {
		config.firstChild("title").flatMap(Config::getValue).ifPresent(title -> {
			this.title = frontend -> {
				TextFactory textFactory = frontend.getBackend().getTextFactory();
				return FriendlyParser.parse(textFactory, title, null); // TODO placeholder here
			};
		});

		config.firstChild("type").flatMap(Config::getValue).ifPresent(type -> {
			this.type = Registries.SCREEN_HANDLER.get(new Identifier(type));
		});

		config.children("button").forEach(buttonConfig -> {
			Optional<Identifier> idOpt = buttonConfig.getValue(Identifier::tryParse);
			if (idOpt.isEmpty()) {
				StonksFabric.LOGGER.warn("Invaild button type ID: {}",
					buttonConfig.getValue().orElse("<not specified>"));
				return;
			}

			Identifier id = idOpt.get();
			String nativeButtonId = buttonConfig.firstChild("nativeButton").flatMap(Config::getValue).orElse(null);
			List<Fill> fillRects = buttonConfig.children("fill")
				.map(Config::getValue)
				.flatMap(Optional::stream)
				.map(Fill::fromString)
				.filter(v -> v != null)
				.toList();

			buttons.add(ButtonTemplate.fromConfig(id, buttonConfig, fillRects, nativeButtonId));
		});

		return this;
	}
}
