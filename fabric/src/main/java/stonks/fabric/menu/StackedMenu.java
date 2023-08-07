package stonks.fabric.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class StackedMenu extends SimpleGui {
	private StackedMenu previous;
	private int ticks = 0;

	public StackedMenu(StackedMenu previous, ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots) {
		super(type, player, manipulatePlayerSlots);
		this.previous = previous;

		placeBorder();
		placeButtons();
	}

	public StackedMenu getPrevious() { return previous; }

	protected void placeBorder() {
		for (int x = 0; x < getWidth(); x++) setSlot(x, MenuIcons.BORDER);
	}

	protected void placeButtons() {
		if (previous != null) { setSlot(1, MenuIcons.BACK); }
	}

	@Override
	public void onTick() {
		ticks++;
	}

	public int getTicks() { return ticks; }

	public void placeLoadingSpinner(int slot) {
		var spinner = new String[] { ":..", ".:.", "..:", ".:." };
		var i = getTicks() / 5;

		setSlot(slot, new GuiElementBuilder(Items.CLOCK)
			.setName(Text.literal("Please wait " + spinner[i % spinner.length])
				.styled(s -> s.withColor(Formatting.GRAY))));
	}
}
