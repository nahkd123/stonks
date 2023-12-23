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
package io.github.nahkd123.stonks.minecraft.economy;

import java.text.DecimalFormat;

import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;

public class DoubleBackedCurrency implements Currency {
	private TextFactory textFactory;
	private int decimals;
	private long wholeDollar;
	private DecimalFormat format = new DecimalFormat("#,##0.##");

	public DoubleBackedCurrency(TextFactory textFactory, int decimals) {
		this.textFactory = textFactory;
		this.decimals = decimals;
		this.wholeDollar = (long) Math.pow(10L, decimals);
	}

	public double toDouble(long value) {
		long dollars = value / wholeDollar;
		long cents = value % wholeDollar;
		return dollars + (cents / (double) wholeDollar);
	}

	public long fromDouble(double value) {
		long dollars = (long) Math.floor(value);
		long cents = Math.round(wholeDollar * (value - dollars));
		return dollars * wholeDollar + cents;
	}

	public int getDecimals() { return decimals; }

	@Override
	public TextComponent getDisplay(long underlying) {
		return textFactory.literal(format.format(decimals));
	}

	@Override
	public String convertToString(long underlying) {
		return Double.toString(toDouble(underlying));
	}

	@Override
	public long convertFromString(String str) {
		return fromDouble(Double.parseDouble(str));
	}
}
