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
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.fabric.gui.ContainerGuiFrontend;
import io.github.nahkd123.stonks.minecraft.fabric.gui.Fill;
import io.github.nahkd123.stonks.minecraft.gui.ClickType;
import io.github.nahkd123.stonks.minecraft.gui.NativeButton;
import nahara.common.configurations.Config;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import stonks.fabric.StonksFabricPlatform;

public class FailbackButtonTemplate implements ButtonTemplate {
	private Identifier typeId;
	private List<Fill> fillRects;
	private String nativeButtonId;

	public FailbackButtonTemplate(Identifier typeId, List<Fill> fillRects, String nativeButtonId) {
		this.typeId = typeId;
		this.fillRects = fillRects;
		this.nativeButtonId = nativeButtonId;
	}

	@Override
	public List<Fill> getFillRects() { return fillRects; }

	@Override
	public String getNativeButtonId() { return nativeButtonId; }

	@Override
	public GuiElementInterface createGuiElement(ContainerGuiFrontend gui, int ordinal) {
		return new GuiElementBuilder(Items.BARRIER)
			.setName(Text.literal("Failback Button / Error").styled(s -> s.withColor(Formatting.RED)))
			.addLoreLine(Text.literal("Unknown type: " + typeId + " / Ordinal " + ordinal)
				.styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, $) -> {
				NativeButton nativeButton = gui.getBackend().getNative(nativeButtonId, ordinal);

				if (nativeButton != null) {
					Player player = ((StonksFabricPlatform) gui.getPlayer().getServer())
						.getModernWrapper()
						.getPlayerByUUID(gui.getPlayer().getUuid());
					ClickType clickType = ContainerGuiFrontend.toCommonClickType(type);
					nativeButton.onClick(gui.getBackend(), player, clickType);
				}
			})
			.build();
	}

	public static final ButtonTemplateFactory FACTORY = new ButtonTemplateFactory() {
		@Override
		public ButtonTemplate createFromConfig(Config config, List<Fill> fillRects, String nativeButtonId) {
			return new FailbackButtonTemplate(new Identifier("minecraft", "missingno"), fillRects, nativeButtonId);
		}
	};
}
