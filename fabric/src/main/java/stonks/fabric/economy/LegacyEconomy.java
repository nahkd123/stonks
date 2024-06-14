/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.fabric.economy;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.OptionalLong;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import stonks.fabric.PlatformConfig;
import stonks.fabric.StonksFabric;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.translation.Translations;

@Deprecated
public class LegacyEconomy implements Economy {
	private StonksFabricAdapter adapter;
	private PlatformConfig config;
	private DecimalFormat formatter;

	public LegacyEconomy(StonksFabricAdapter adapter, PlatformConfig config) {
		this.adapter = adapter;
		this.config = config;

		var template = "#,##0.";
		for (int i = 0; i < config.decimals; i++) template += '#';
		this.formatter = new DecimalFormat(template);
	}

	@Override
	public Identifier getEconomyId() { return Identifier.of(StonksFabric.MODID, "legacy_economy_adapter"); }

	public long doubleToRaw(double value) {
		return (long) (value * config.decimals);
	}

	public double rawToDouble(long raw) {
		return raw / (double) config.decimals;
	}

	@Override
	public String formatCurrency(long raw) {
		return "$" + formatter.format(rawToDouble(raw));
	}

	@Override
	public Text formatAsDisplay(long raw) {
		return Translations.currency(rawToDouble(raw));
	}

	@Override
	public OptionalLong tryParse(String formatted) {
		try {
			formatted = formatted.trim();
			if (formatted.startsWith("$")) formatted = formatted.substring(1).trim();
			return OptionalLong.of(doubleToRaw(formatter.parse(formatted).doubleValue()));
		} catch (ParseException e) {
			return OptionalLong.empty();
		}
	}

	@Override
	public long balanceOf(ServerPlayerEntity player) {
		return doubleToRaw(adapter.accountBalance(player));
	}

	@Override
	public boolean withdrawFrom(ServerPlayerEntity player, long raw) {
		return adapter.accountWithdraw(player, rawToDouble(raw));
	}

	@Override
	public boolean depositTo(ServerPlayerEntity player, long raw) {
		return adapter.accountDeposit(player, rawToDouble(raw));
	}
}
