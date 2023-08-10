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
package stonks.fabric.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.fabric.menu.player.ViewOffersMenu;

public class MenuIcons {
	public static final GuiElementBuilder BORDER = new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE)
		.setName(Text.literal(" "));

	public static final GuiElementBuilder BACK = new GuiElementBuilder(Items.ARROW)
		.setName(Text.literal("<--")
			.styled(s -> s.withColor(Formatting.GRAY))
			.append(Text.literal("Back").styled(s -> s.withColor(Formatting.GREEN))))
		.setCallback((index, type, action, gui) -> {
			if (gui instanceof StackedMenu stacked) { stacked.getPrevious().open(); }
		});

	public static final GuiElementBuilder MAIN_MENU = new GuiElementBuilder(Items.GOLD_BLOCK)
		.setName(Text.literal("Market menu").styled(s -> s.withColor(Formatting.YELLOW)))
		.addLoreLine(Text.literal("Click to go back to main menu").styled(s -> s.withColor(Formatting.GRAY)))
		.setCallback((index, type, action, gui) -> {
			var previous = gui instanceof StackedMenu stacked ? stacked : null;
			if (previous != null && previous.getPrevious() instanceof MarketMainMenu) {
				previous.getPrevious().open();
				return;
			}

			var main = new MarketMainMenu(previous, gui.getPlayer());
			main.open();
		});

	public static final GuiElementBuilder VIEW_SELF_OFFERS = new GuiElementBuilder(Items.CHEST)
		.setName(Text.literal("View offers").styled(s -> s.withColor(Formatting.YELLOW)))
		.addLoreLine(Text.literal("Click to view your offers").styled(s -> s.withColor(Formatting.GRAY)))
		.setCallback((index, type, action, gui) -> {
			var previous = gui instanceof StackedMenu stacked ? stacked : null;
			if (previous != null && previous.getPrevious() instanceof ViewOffersMenu) {
				previous.getPrevious().open();
				return;
			}

			var menu = new ViewOffersMenu(previous, gui.getPlayer());
			menu.open();
		});
}