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
package io.github.nahkd123.stonks.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlTestUtils {
	public static final Logger LOGGER = LoggerFactory.getLogger("SqlTestUtils");

	public static void printResultSet(ResultSet set) throws SQLException {
		List<String[]> rows = new ArrayList<>();
		int cols = set.getMetaData().getColumnCount();

		String[] names = new String[cols];
		int[] width = new int[cols];
		for (int i = 0; i < cols; i++) {
			names[i] = set.getMetaData().getColumnName(i + 1);
			if (names[i].length() > width[i]) width[i] = names[i].length();
		}

		while (set.next()) {
			String[] rec = new String[cols];

			for (int i = 0; i < cols; i++) {
				rec[i] = set.getString(i + 1);
				if (rec[i].length() > width[i]) width[i] = rec[i].length();
			}

			rows.add(rec);
		}

		printSeparator(width);
		printRow(names, width);
		printSeparator(width);
		for (String[] row : rows) printRow(row, width);
		printSeparator(width);
	}

	public static void printSeparator(int[] width) {
		String s = String.join("-+-", IntStream.of(width).mapToObj(v -> {
			String w = "";
			while (w.length() < v) w += "-";
			return w;
		}).toArray(String[]::new));
		LOGGER.info("+-" + s + "-+");
	}

	public static void printRow(String[] row, int[] width) {
		String placeholders = String.join(" | ", IntStream.of(width)
			.mapToObj(v -> "%-" + v + "s")
			.toArray(String[]::new));
		LOGGER.info("| " + String.format(placeholders, (Object[]) row) + " |");
	}
}
