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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface.ClickCallback;
import eu.pb4.sgui.api.gui.GuiInterface;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.fabric.gui.ContainerGuiFrontend;
import io.github.nahkd123.stonks.minecraft.fabric.gui.Fill;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.LazyLoadedNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.NativeButton;
import io.github.nahkd123.stonks.utils.lazy.LoadState;
import nahara.common.configurations.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabricPlatform;

public class LazyButtonTemplate implements ButtonTemplate {
	private List<Fill> fillRects;
	private String nativeButtonId;
	public ButtonTemplate onLoading, onLoaded, onPresent, onAbsent, onError;

	public LazyButtonTemplate(List<Fill> fillRects, String nativeButtonId) {
		this.fillRects = fillRects;
		this.nativeButtonId = nativeButtonId;
	}

	@Override
	public List<Fill> getFillRects() { return fillRects; }

	@Override
	public String getNativeButtonId() { return nativeButtonId; }

	@Override
	public GuiElementInterface createGuiElement(ContainerGuiFrontend gui, int ordinal) {
		NativeButton nativeButton = gui.getBackend().getNative(nativeButtonId, ordinal);
		ClickCallback callback = (index, type, action, $) -> {
			if (nativeButton != null) {
				Player player = ((StonksFabricPlatform) gui.getPlayer().getServer())
					.getModernWrapper()
					.getPlayerByUUID(gui.getPlayer().getUuid());
				ClickType clickType = ContainerGuiFrontend.toCommonClickType(type);
				nativeButton.onClick(gui.getBackend(), player, clickType);
			}
		};

		if (nativeButton == null || !(nativeButton instanceof LazyLoadedNativeButton llnb)) {
			return new GuiElementBuilder(Items.BARRIER)
				.setName(Text.literal("Failback Button / Error").styled(s -> s.withColor(Formatting.RED)))
				.addLoreLine(Text.literal("The native button can't be lazy loaded!")
					.styled(s -> s.withColor(Formatting.GRAY)))
				.setCallback(callback)
				.build();
		}

		GuiElementInterface onLoading = this.onLoading != null ? this.onLoading.createGuiElement(gui, ordinal) : null;
		GuiElementInterface onLoaded = this.onLoaded != null ? this.onLoaded.createGuiElement(gui, ordinal) : null;
		GuiElementInterface onPresent = this.onPresent != null ? this.onPresent.createGuiElement(gui, ordinal) : null;
		GuiElementInterface onAbsent = this.onAbsent != null ? this.onAbsent.createGuiElement(gui, ordinal) : null;
		GuiElementInterface onError = this.onError != null ? this.onError.createGuiElement(gui, ordinal) : null;

		return new GuiElementInterface() {
			private GuiElementInterface pick() {
				LoadState state = llnb.getLoader().load();
				if (state != LoadState.SUCCESS) return switch (state) {
				case LOADING -> onLoading;
				case FAILED -> onError;
				default -> onError;
				};

				if (onLoaded != null) return onLoaded;
				if (llnb.getLoader().get() == null) return onAbsent;
				return onPresent;
			}

			@Override
			public ItemStack getItemStack() {
				GuiElementInterface picked = pick();
				return picked != null ? picked.getItemStack() : ItemStack.EMPTY;
			}

			@Override
			public ItemStack getItemStackForDisplay(GuiInterface gui) {
				GuiElementInterface picked = pick();
				return picked != null ? picked.getItemStackForDisplay(gui) : ItemStack.EMPTY;
			}

			@Override
			public ClickCallback getGuiCallback() { return callback; }
		};
	}

	private void applyConfig(Optional<Config> config, Consumer<ButtonTemplate> setter) {
		if (config.isEmpty()) return;
		Config cfg = config.get();
		Identifier templateType = cfg.getValue().map(Identifier::new).orElseGet(() -> new Identifier("missingno"));
		ButtonTemplate childTemplate = ButtonTemplate.fromConfig(templateType, cfg, fillRects, nativeButtonId);
		setter.accept(childTemplate);
	}

	public static final ButtonTemplateFactory FACTORY = new ButtonTemplateFactory() {
		@Override
		public ButtonTemplate createFromConfig(Config config, List<Fill> fillRects, String nativeButtonId) {
			LazyButtonTemplate template = new LazyButtonTemplate(fillRects, nativeButtonId);
			template.applyConfig(config.firstChild("onLoading"), t -> template.onLoading = t);
			template.applyConfig(config.firstChild("onLoaded"), t -> template.onLoaded = t);
			template.applyConfig(config.firstChild("onPresent"), t -> template.onPresent = t);
			template.applyConfig(config.firstChild("onAbsent"), t -> template.onAbsent = t);
			template.applyConfig(config.firstChild("onError"), t -> template.onError = t);
			return template;
		}
	};
}
