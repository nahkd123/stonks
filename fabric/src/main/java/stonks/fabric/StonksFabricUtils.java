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
package stonks.fabric;

import java.text.DecimalFormat;
import java.util.Optional;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.core.market.OfferType;
import stonks.core.market.OverviewOffer;
import stonks.fabric.translation.Translations;

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

	public static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,##0.##");
	public static final DecimalFormat TAX_FORMATTER = new DecimalFormat("#,##0.##%");

	public static Text currencyText(Optional<Double> v, boolean fullNotAvailable) {
		if (v.isEmpty()) return fullNotAvailable
			? Translations.Messages.NotAvailable
			: Translations.Messages.NotAvailableShort;
		return Translations.Messages.Currency(v.get());
	}

	public static Optional<Text> taxText(double tax) {
		if (tax <= 0d) return Optional.empty();
		return Optional.of(Text.literal(TAX_FORMATTER.format(tax)).styled(s -> s.withColor(Formatting.YELLOW)));
	}

	public static Text offerText(OfferType type, OverviewOffer offer) {
		var typeText = type == OfferType.BUY
			? Translations.Messages.OfferInfoText$Buy
			: Translations.Messages.OfferInfoText$Sell;
		return Translations.Messages.OfferInfoText(typeText, offer.totalAvailableUnits(), offer.offers(),
			offer.pricePerUnit());
	}
}
