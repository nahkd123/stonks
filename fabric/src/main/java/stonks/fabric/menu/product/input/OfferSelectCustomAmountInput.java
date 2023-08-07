package stonks.fabric.menu.product.input;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.fabric.StonksFabric;
import stonks.fabric.menu.product.OfferPriceConfigureMenu;
import stonks.fabric.menu.product.OfferAmountConfigureMenu;

public class OfferSelectCustomAmountInput extends SignGui {
	private OfferAmountConfigureMenu menu;

	public OfferSelectCustomAmountInput(ServerPlayerEntity player, OfferAmountConfigureMenu menu) {
		super(player);
		this.menu = menu;

		setSignType(Blocks.DARK_OAK_SIGN);
		setColor(DyeColor.WHITE);
		setLine(0, Text.empty());
		setLine(1, Text.literal("--------"));
		setLine(2, Text.literal("Specify how much you want to"));
		setLine(3, Text.literal(
			menu.getOfferType().toString().toLowerCase() + " " + menu.getProduct().getProductName()));
	}

	public OfferAmountConfigureMenu getMenu() { return menu; }

	@Override
	public void onClose() {
		var input = getLine(0).getString().trim().toLowerCase();
		if (input.isEmpty()) {
			getMenu().open();
			return;
		}

		var mul = 1;
		if (input.endsWith("k")) {
			mul = 1000;
			input = input.substring(0, input.length() - 1);
		}

		try {
			var base = Integer.parseInt(input);
			var amount = base * mul;

			if (amount <= 0) {
				getPlayer().sendMessage(Text.literal("You must specify at least 1")
					.styled(s -> s.withColor(Formatting.RED)), true);
				return;
			}

			var currentAmount = StonksFabric.getServiceProvider(getPlayer())
				.getStonksAdapter()
				.getUnits(getPlayer(), getMenu().getProduct());
			if (menu.getOfferType() == OfferType.SELL && amount > currentAmount) {
				getPlayer().sendMessage(Text.literal("Not enough items! (" + amount + "/" + currentAmount + ")")
					.styled(s -> s.withColor(Formatting.RED)), true);
				return;
			}

			var type = getMenu().getOfferType();
			new OfferPriceConfigureMenu(getMenu(), getPlayer(), type, amount, getMenu().getOverview()).open();
		} catch (NumberFormatException e) {
			getPlayer().sendMessage(
				Text.literal("Invaild input: " + input).styled(s -> s.withColor(Formatting.RED)),
				true);
		}
	}
}
