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

import nahara.common.configurations.Config;
import stonks.fabric.menu.product.input.OfferCustomPriceInput;

/**
 * <p>
 * Platform-specific configurations.
 * </p>
 */
public class PlatformConfig {
	public int decimals = 2;
	public double tax = 0d;
	public double topOfferPriceDelta = 0.1d;

	public void from(Config config) {
		config.firstChild("decimals").flatMap(v -> v.getValue(Integer::parseInt))
			.ifPresent(v -> decimals = v);
		config.firstChild("tax").flatMap(v -> v.getValue(Double::parseDouble))
			.ifPresent(v -> tax = v);
		config.firstChild("topOfferPriceDelta").flatMap(v -> v.getValue(Double::parseDouble))
			.ifPresent(v -> topOfferPriceDelta = v);
	}

	// Processing methods
	/**
	 * <p>
	 * Force input value to have maximum {@link #decimals} decimal points. Used in
	 * {@link OfferCustomPriceInput} to ensure player can't attempt to type an
	 * extremely small number, like {@code 0.00000000001} for example.
	 * </p>
	 * <p>
	 * You should make value equals to economy adapter's decimal points.
	 * </p>
	 * 
	 * @param in Input value.
	 * @return Processed value.
	 */
	public double processCurrency(double in) {
		if (decimals <= 0) return Math.floor(in);
		var mul = Math.pow(10, decimals);
		return Math.floor(in * mul) / mul;
	}

	public double applyTax(double in) {
		if (tax <= 0d) return in;
		var taxed = in * tax;
		return in - taxed;
	}
}
