package stonks.fabric;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffer;

public class StonksFabricUtils {
	public static Text progressBar(int width, Formatting background, double[] progress, Formatting[] colors) {
		var p = new int[progress.length + 1];
		p[p.length - 1] = width;
		for (int i = 0; i < progress.length; i++) { p[i] = (int) Math.round(progress[i] * width); }
		var bars = new MutableText[p.length];

		for (int i = 0; i < p.length; i++) {
			var prevWidth = i == 0 ? 0 : p[i - 1];
			var thisWidth = p[i] - prevWidth;
			var i2 = i;
			bars[i] = spaces(thisWidth)
				.styled(s -> s.withStrikethrough(true).withColor(i2 < colors.length ? colors[i2] : background));
		}

		var ret = bars[0];
		for (int i = 1; i < bars.length; i++) ret = ret.append(bars[i]);
		return ret;
	}

	public static MutableText spaces(int width) {
		var s = "";
		while (s.length() < width) s += " ";
		return Text.literal(s);
	}

	public static String createStringFor(ItemStack stack, MinecraftServer server) {
		var reg = server.getRegistryManager().getOptional(RegistryKeys.ITEM);
		if (reg.isEmpty()) return null;
		var key = reg.get().getKey(stack.getItem()).map(v -> v.getValue().toString());
		if (key.isEmpty()) return null;

		var out = key.get();
		if (stack.hasNbt()) out += stack.getNbt().asString();
		return out;
	}

	public static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("$#,##0.##");

	public static Text currencyText(Optional<Double> v, boolean fullNotAvailable) {
		return v
			.map(d -> Text.literal(CURRENCY_FORMATTER.format(d)).styled(s -> s.withColor(Formatting.YELLOW)))
			.orElse(Text.literal(fullNotAvailable ? "Not Available" : "n/a").styled(s -> s.withColor(Formatting.RED)));
	}

	public static Text offerText(OfferType type, OverviewOffer offer) {
		var typeText = Text.literal(type.toString()).styled(s -> s
			.withBold(true)
			.withColor(switch (type) {
			case BUY -> Formatting.GREEN;
			case SELL -> Formatting.YELLOW;
			}));

		return Text.empty()
			.styled(s -> s.withColor(Formatting.GRAY))
			.append(typeText)
			.append(" ")
			.append(Text.literal(Integer.toString(offer.totalAvailableUnits()))
				.styled(s -> s.withColor(Formatting.AQUA)))
			.append("x from ")
			.append(Text.literal(Integer.toString(offer.offers()))
				.styled(s -> s.withColor(Formatting.AQUA)))
			.append(" offers for ")
			.append(currencyText(Optional.of(offer.pricePerUnit()), false))
			.append("/ea"); // BUY 128x from 64 offers for $1,525/ea
	}

	public static boolean compareStack(ItemStack a, ItemStack b) {
		if (a.isEmpty()) return b.isEmpty();
		if (b.isEmpty()) return a.isEmpty();
		if (!a.isOf(b.getItem())) return false;
		var nbtA = a.getNbt();
		if (nbtA != null && nbtA.isEmpty()) nbtA = null;
		var nbtB = b.getNbt();
		if (nbtB != null && nbtB.isEmpty()) nbtB = null;
		return Objects.equals(nbtA, nbtB);
	}
}
