package stonks.fabric.menu.handling;

import java.util.stream.Stream;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import nahara.common.tasks.Task;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class WaitableGuiElement<T> implements GuiElementInterface {
	protected static final ItemStack[] LOADING = Stream
		.of(":..", ".:.", "..:", ".:.")
		.map(v -> new GuiElementBuilder(Items.CLOCK)
			.setName(Text.literal("Please wait " + v).styled(s -> s.withColor(Formatting.GRAY)))
			.asStack())
		.toArray(ItemStack[]::new);
	public static final AnimatedGuiElement ANIMATED_LOADING = new AnimatedGuiElement(LOADING, 5, false, EMPTY_CALLBACK);

	private Task<T> task;
	private int timeTicked;
	private ItemStack stack;

	public WaitableGuiElement(Task<T> task) {
		this.task = task;
	}

	@Override
	public ItemStack getItemStackForDisplay(GuiInterface gui) {
		var now = task.get();
		if (now.isEmpty()) { return LOADING[(timeTicked++ / 5) % LOADING.length].copy(); }

		if (stack == null) stack = createStackWhenLoaded(
			task.get().get().getSuccess(),
			task.get().get().getFailure());
		return stack.copy();
	}

	public abstract ItemStack createStackWhenLoaded(T success, Throwable error);

	public abstract void onSlotClick(int index, ClickType type, SlotActionType action, SlotGuiInterface gui, T success, Throwable error);

	@Override
	public ItemStack getItemStack() { return stack; }

	@Override
	public ClickCallback getGuiCallback() {
		var now = task.get();
		if (now.isEmpty()) return GuiElementInterface.super.getGuiCallback();
		return (index, type, action, gui) -> onSlotClick(index, type, action, gui, now.get().getSuccess(),
			now.get().getFailure());
	}
}
