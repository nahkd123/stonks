package stonks.fabric.menu;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.fabric.misc.TasksHandler;
import stonks.fabric.provider.StonksProvider;

public abstract class StackedMenu extends SimpleGui {
	private StackedMenu previous;
	private TasksHandler guiTasksHandler;

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
		getGuiTasksHandler().tick();
	}

	/**
	 * <p>
	 * A local {@link TasksHandler} that's bound to this menu. Does not call
	 * callbacks if the menu is closed. If you want to handle tasks outside the
	 * bound of menus, consider using {@link StonksProvider#getTasksHandler()}.
	 * </p>
	 * 
	 * @return GUI tasks handler.
	 */
	public TasksHandler getGuiTasksHandler() {
		if (guiTasksHandler == null) guiTasksHandler = new TasksHandler();
		return guiTasksHandler;
	}
}
