package io.github.nahkd123.stonks.mc.fabric.utils;

import static net.minecraft.text.Text.literal;
import static net.minecraft.util.Formatting.BOLD;
import static net.minecraft.util.Formatting.GOLD;
import static net.minecraft.util.Formatting.GREEN;
import static net.minecraft.util.Formatting.WHITE;
import static net.minecraft.util.Formatting.YELLOW;

import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.product.CategoryProduct;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferOverviewEntry;
import io.github.nahkd123.stonks.service.OfferType;
import net.minecraft.text.Text;

public class StonksTextUtils {
	public static Text currencyOf(long amount, EconomyService econ) {
		if (econ == null)
			return literal("[").formatted(GOLD)
				.append(literal("" + amount).formatted(YELLOW))
				.append("]");
		return econ.formatDisplayCurrency(amount);
	}

	public static Text currencyOf(long amount, StonksMcInstance instance) {
		return currencyOf(amount, instance.getEconomyService());
	}

	public static Text offerOf(Offer offer, CategoryProduct product, EconomyService econ) {
		Text productName = product.info().display().getFormattedName();
		return (offer.type() == OfferType.BUY
			? literal("Buying").formatted(GREEN, BOLD)
			: literal("Selling").formatted(YELLOW, BOLD))
			.append(literal(" " + offer.totalUnits() + "x ").formatted(WHITE))
			.append(productName)
			.append(" for ")
			.append(currencyOf(offer.price(), econ))
			.append("/ea");
	}

	public static Text offerOf(Offer offer, CategoryProduct product, StonksMcInstance instance) {
		return offerOf(offer, product, instance.getEconomyService());
	}

	public static Text topOfferOf(OfferType type, OfferOverviewEntry offer, EconomyService econ) {
		return (type == OfferType.BUY
			? literal("Buying").formatted(GREEN, BOLD)
			: literal("Selling").formatted(YELLOW, BOLD))
			.append(literal(" " + offer.units() + " units ").formatted(WHITE))
			.append(" for ")
			.append(currencyOf(offer.price(), econ))
			.append("/ea");
	}
}
