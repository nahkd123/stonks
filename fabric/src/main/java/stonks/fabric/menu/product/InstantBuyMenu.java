package stonks.fabric.menu.product;

import java.util.Optional;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricHelper;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.menu.StackedMenu;
import stonks.fabric.menu.product.input.OfferInstantBuyAmountInput;

public class InstantBuyMenu extends StackedMenu {
	private Product product;
	private double originalPricePerUnit;
	private double instantPricePerUnit;

	public InstantBuyMenu(StackedMenu previous, ServerPlayerEntity player, Product product, double originalPricePerUnit, double instantPricePerUnit) {
		super(previous, ScreenHandlerType.GENERIC_9X4, player, false);
		this.product = product;
		this.originalPricePerUnit = originalPricePerUnit;
		this.instantPricePerUnit = instantPricePerUnit;

		setTitle(Text.literal("Market > " + product.getProductName() + " > Buy offer"));
		placeBuyButtons();
	}

	public Product getProduct() { return product; }

	public double getOriginalPricePerUnit() { return originalPricePerUnit; }

	public double getInstantPricePerUnit() { return instantPricePerUnit; }

	public void placeBuyButtons() {
		var balance = StonksFabric.getServiceProvider(player).getStonksAdapter().accountBalance(player);
		setSlot(19, createInstantBuyButton(balance, 1, Items.GOLD_NUGGET));
		setSlot(20, createInstantBuyButton(balance, 16, Items.GOLD_INGOT));
		setSlot(21, createInstantBuyButton(balance, 64, Items.GOLD_INGOT));
		setSlot(22, createInstantBuyButton(balance, 256, Items.GOLD_INGOT));
		setSlot(23, createInstantBuyButton(balance, 1024, Items.GOLD_BLOCK));

		setSlot(25, new GuiElementBuilder(Items.DARK_OAK_SIGN)
			.setName(Text.literal("Custom amount").styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.literal(product.getProductName()).styled(s -> s.withColor(Formatting.GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Click to specify amount").styled(s -> s.withColor(Formatting.GRAY)))
			.setCallback((index, type, action, gui) -> new OfferInstantBuyAmountInput(player, this).open()));
	}

	public void blockBuyButtons() {
		var blockIcon = new GuiElementBuilder(Items.BARRIER)
			.setName(Text.literal("Can't buy now").styled(s -> s.withColor(Formatting.RED)))
			.addLoreLine(Text.literal("Buying product...").styled(s -> s.withColor(Formatting.GRAY)));
		setSlot(19, blockIcon);
		setSlot(20, blockIcon);
		setSlot(21, blockIcon);
		setSlot(22, blockIcon);
		setSlot(23, blockIcon);
		setSlot(25, blockIcon);
	}

	public GuiElementBuilder createInstantBuyButton(double balance, int amount, Item icon) {
		var moneyToSpend = amount * instantPricePerUnit;
		var canBuy = balance >= moneyToSpend;

		return new GuiElementBuilder(canBuy ? icon : Items.BARRIER, Math.min(Math.max(amount / 64, 1), 64))
			.setName(Text.literal("Instant buy x" + amount)
				.styled(s -> s.withColor(Formatting.YELLOW)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal("Average price: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(amount * originalPricePerUnit), true)))
			.addLoreLine(Text.literal("Minimum balance: ")
				.styled(s -> s.withColor(Formatting.GRAY))
				.append(StonksFabricUtils.currencyText(Optional.of(moneyToSpend), true)))
			.addLoreLine(Text.literal("Having minimum balance is required to")
				.styled(s -> s.withColor(Formatting.DARK_GRAY)))
			.addLoreLine(Text.literal("avoid your buy request from failing.")
				.styled(s -> s.withColor(Formatting.DARK_GRAY)))
			.addLoreLine(Text.literal("Hold Shift to keep this menu opened")
				.styled(s -> s.withColor(Formatting.DARK_GRAY)))
			.addLoreLine(Text.empty())
			.addLoreLine(Text.literal(canBuy
				? "Click to instantly buy"
				: "Can't instant buy")
				.styled(s -> s.withColor(canBuy ? Formatting.GRAY : Formatting.RED)))
			.setCallback((index, type, action, gui) -> {
				var provider = StonksFabric.getServiceProvider(getPlayer());
				var adapter = provider.getStonksAdapter();

				if (adapter.accountBalance(getPlayer()) < moneyToSpend) {
					getPlayer().sendMessage(Text.literal("You don't have ").styled(s -> s.withColor(Formatting.RED))
						.append(StonksFabricUtils.currencyText(Optional.of(moneyToSpend), true))
						.append(" to buy!"), true);
					close();
					return;
				}

				var task = StonksFabricHelper.instantOffer(getPlayer(), product, OfferType.BUY, amount, moneyToSpend);
				if (!type.shift) close();
				else {
					blockBuyButtons();
					getGuiTasksHandler().handle(task, ($void, error) -> {
						if (error != null) {
							close();
							error.printStackTrace();
							return;
						}

						placeBuyButtons();
					});
				}
			});
	}
}
