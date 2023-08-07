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
