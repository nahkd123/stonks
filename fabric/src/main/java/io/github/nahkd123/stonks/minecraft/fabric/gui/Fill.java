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
package io.github.nahkd123.stonks.minecraft.fabric.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Fill(int xMin, int yMin, int xMax, int yMax, boolean reversedX, boolean reversedY) {

	public static final Pattern PATTERN_FULL = Pattern.compile(
		"\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*(to|--?>|==?>)\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)(\\s*reversed-x)?(\\s*reversed-y)?");
	public static final Pattern PATTERN_MINIMAL = Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");

	public Fill {
		if (xMin > xMax) throw new IllegalArgumentException("Min x is larger than max x (" + xMin + " > " + xMax + ")");
		if (yMin > yMax) throw new IllegalArgumentException("Min y is larger than max y (" + yMin + " > " + yMax + ")");
	}

	public Fill(int x, int y) {
		this(x, y, x, y, false, false);
	}

	public static Fill fromString(String s) {
		s = s.trim();
		Matcher matcher;

		if ((matcher = PATTERN_FULL.matcher(s)).matches()) {
			int x1 = Integer.parseInt(matcher.group(1));
			int y1 = Integer.parseInt(matcher.group(2));
			int x2 = Integer.parseInt(matcher.group(4));
			int y2 = Integer.parseInt(matcher.group(5));
			int xMin = Math.min(x1, x2);
			int yMin = Math.min(y1, y2);
			int xMax = Math.max(x1, x2);
			int yMax = Math.max(y1, y2);
			boolean reversedX = matcher.group(6) != null;
			boolean reversedY = matcher.group(7) != null;
			return new Fill(xMin, yMin, xMax, yMax, reversedX, reversedY);
		}

		if ((matcher = PATTERN_MINIMAL.matcher(s)).matches()) {
			int x = Integer.parseInt(matcher.group(1));
			int y = Integer.parseInt(matcher.group(2));
			return new Fill(x, y);
		}

		return null;
	}

	public int width() {
		return xMax - xMin + 1;
	}

	public int height() {
		return yMax - yMin + 1;
	}

	public int area() {
		return width() * height();
	}

	public void forEach(FillConsumer callback) {
		int xStart = reversedX ? xMax : xMin,
			xEnd = reversedX ? xMin : xMax,
			xStep = reversedX ? -1 : 1;
		int yStart = reversedY ? yMax : yMin,
			yEnd = reversedY ? yMin : yMax,
			yStep = reversedY ? -1 : 1;
		int ordinal = 0;

		for (int y = yStart; reversedY ? y >= yEnd : y <= yEnd; y += yStep) {
			for (int x = xStart; reversedX ? x >= xEnd : x <= xEnd; x += xStep) {
				callback.accept(x, y, ordinal);
				ordinal++;
			}
		}
	}

	@Override
	public String toString() {
		if (xMin == xMax && yMin == yMax) return "(" + xMin + ", " + yMin + ")";
		return "(" + xMin + ", " + yMin + ") to (" + xMax + ", " + yMax + ")" +
			(reversedX ? " reversed-x" : "") +
			(reversedY ? " reversed-y" : "");
	}
}
