package io.github.nahkd123.stonks.mc.fabric.gui.player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.gui.StonksGuiElements;
import io.github.nahkd123.stonks.mc.fabric.gui.tasked.TaskedMenu;
import io.github.nahkd123.stonks.mc.fabric.utils.PaginationUtils;
import io.github.nahkd123.stonks.service.Offer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PlayerOffersMenu extends TaskedMenu<Void> {
	private StonksMcInstance instance;
	private int offersPage = 0;

	private CompletableFuture<List<? extends Offer>> offers;

	public PlayerOffersMenu(ServerPlayerEntity player, StonksMcInstance instance) {
		super(ScreenHandlerType.GENERIC_9X6, player, false);
		this.instance = instance;
		setTitle(Text.literal("Market > ").append(player.getDisplayName().getString()).append("'s offers"));

		offers = markDirtyOnFinish(instance.getMarketService().queryUserOffers(player.getUuid()));

		onUpdate();
	}

	@Override
	protected void onUpdate() {
		updateFrame();
		updateOffers();
	}

	private void updateFrame() {
		for (int i = 0; i < 9; i++) setSlot(i, StonksGuiElements.FRAME);

		setSlot(1, GuiElementBuilder.from(StonksGuiElements.BACK.getItemStack())
			.setCallback((index, type, action, gui) -> resolve(null))
			.build());
	}

	private void updateOffers() {
		if (offers.isDone()) {
			if (offers.isCompletedExceptionally()) {
				for (int i = 9; i < 54; i++) clearSlot(i);
				setSlot(31, StonksGuiElements.internalError(offers.exceptionNow()));
				return;
			}

			List<? extends Offer> page = PaginationUtils.paginate(offersPage, 9 * 5, offers.getNow(List.of()));

			for (int i = 0; i < page.size(); i++) {
				Offer offer = page.get(i);
				if (offer == null) clearSlot(9 + i);
				setSlot(9 + i, GuiElementBuilder.from(new ItemStack(Items.STONE)));
			}
		} else {
			for (int i = 9; i < 54; i++) clearSlot(i);
			setSlot(31, StonksGuiElements.LOADING);
		}
	}
}
