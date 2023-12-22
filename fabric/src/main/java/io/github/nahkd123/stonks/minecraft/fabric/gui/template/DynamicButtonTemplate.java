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

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface.ClickCallback;
import eu.pb4.sgui.api.gui.GuiInterface;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.impl.legacy.LegacyProductWrapper;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.commodity.CommoditiesService;
import io.github.nahkd123.stonks.minecraft.commodity.Commodity;
import io.github.nahkd123.stonks.minecraft.fabric.FabricServer;
import io.github.nahkd123.stonks.minecraft.fabric.commodity.LegacyCommoditiesService;
import io.github.nahkd123.stonks.minecraft.fabric.gui.ContainerGuiFrontend;
import io.github.nahkd123.stonks.minecraft.fabric.gui.Fill;
import io.github.nahkd123.stonks.minecraft.fabric.text.TextProxy;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.LazyLoadedNativeButton;
import io.github.nahkd123.stonks.minecraft.gui.NativeButton;
import io.github.nahkd123.stonks.minecraft.text.FriendlyParser;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import io.github.nahkd123.stonks.minecraft.text.placeholder.MergedPlaceholders;
import io.github.nahkd123.stonks.minecraft.text.placeholder.Placeholders;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;
import io.github.nahkd123.stonks.utils.lazy.LoadState;
import nahara.common.configurations.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabricPlatform;

public class DynamicButtonTemplate implements ButtonTemplate {
	private boolean typeFromNative = false;
	private Item type;
	private List<Fill> fillRects;
	private String nativeButtonId;

	public int amount = 1;
	public String name;
	public List<String> lore;

	public DynamicButtonTemplate(Item type, List<Fill> fillRects, String nativeButtonId) {
		this.type = type;
		this.typeFromNative = type == null;
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
		TextFactory textFactory = gui.getBackend().getServer().getPlatform().getTextFactory();
		Placeholders placeholders = new MergedPlaceholders(gui.getBackend(), nativeButton);

		ClickCallback callback = (index, type, action, $) -> {
			if (nativeButton != null) {
				Player player = ((StonksFabricPlatform) gui.getPlayer().getServer())
					.getModernWrapper()
					.getPlayerByUUID(gui.getPlayer().getUuid());
				ClickType clickType = ContainerGuiFrontend.toCommonClickType(type);
				nativeButton.onClick(gui.getBackend(), player, clickType);
			}
		};

		return new GuiElementInterface() {
			@Override
			public ItemStack getItemStack() {
				ItemStack base = typeFromNative
					? deriveStackFromObject(gui, nativeButton)
					: new ItemStack(type, amount);
				if (base == ItemStack.EMPTY) return base;

				GuiElementBuilder builder = GuiElementBuilder.from(base);
				if (name != null) builder.setName(unwrap(FriendlyParser.parse(textFactory, name, placeholders)));
				if (lore.size() > 0) builder.setLore(lore.stream()
					.map(line -> unwrap(FriendlyParser.parse(textFactory, line, placeholders)))
					.toList());

				return builder.asStack();
			}

			@Override
			public ItemStack getItemStackForDisplay(GuiInterface gui) {
				return getItemStack();
			}

			@Override
			public ClickCallback getGuiCallback() { return callback; }
		};
	}

	private static Text unwrap(TextComponent wrapped) {
		return ((TextProxy) wrapped).getUnderlying();
	}

	private static ItemStack deriveStackFromObject(ContainerGuiFrontend gui, Object obj) {
		if (obj instanceof LazyLoadedNativeButton llnb) return deriveStackFromObject(gui, llnb.getLoader());

		if (obj instanceof LazyLoader ll) {
			if (ll.load() == LoadState.SUCCESS) return deriveStackFromObject(gui, ll.get());
			return ItemStack.EMPTY;
		}

		if (obj instanceof Product product) {
			String commodityString = product.getCommodityString();
			List<CommoditiesService> commoditiesServices = gui.getBackend().getServer().getCommoditiesServices();

			for (CommoditiesService s : commoditiesServices) {
				// Dumb legacy hack please ignore
				if (s instanceof LegacyCommoditiesService ls && product instanceof LegacyProductWrapper lp)
					return ls.getLegacyAdapter().createDisplayStack(lp.getLegacy());

				Commodity commodity = s.typeFromString(commodityString);
				if (commodity != null) {
					String itemString = commodity.getDisplayItemString();
					return ((FabricServer) gui.getBackend().getServer()).getStacksDeriver().fromString(itemString, 1);
				}
			}

			return ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	public static final ButtonTemplateFactory FACTORY = new ButtonTemplateFactory() {
		@Override
		public ButtonTemplate createFromConfig(Config config, List<Fill> fillRects, String nativeButtonId) {
			Item type = config.firstChild("type")
				.flatMap(Config::getValue)
				.map(v -> v.equals("native") ? null : Registries.ITEM.get(new Identifier(v)))
				.orElse(null);
			DynamicButtonTemplate template = new DynamicButtonTemplate(type, fillRects, nativeButtonId);

			config.firstChild("amount").flatMap(s -> s.getValue(Integer::parseInt)).ifPresent(v -> template.amount = v);
			config.firstChild("name").flatMap(Config::getValue).ifPresent(v -> template.name = v);
			template.lore = config.children("lore").flatMap(c -> c.getValue().stream()).toList();
			// TODO more fun stuffs here!
			return template;
		}
	};
}
