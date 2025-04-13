/*
 * Copyright (c) 2023-2025 nahkd
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
package io.github.nahkd123.stonks.utils.dynamic.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import io.github.nahkd123.stonks.utils.dynamic.DynamicWriter;

public class JsonDynamicWriter implements DynamicWriter {
	private static final int NEST_OBJECT = 0;
	private static final int NEST_ARRAY = 1;
	private Writer writer;
	private Stack<int[]> nesting = new Stack<>(); // 1st for type, 2nd for "is this the subsequence entry?"

	public JsonDynamicWriter(Writer writer) {
		this.writer = writer;
	}

	private void nextInNesting(boolean isKey) throws IOException {
		if (nesting.isEmpty()) return;
		int[] data = nesting.peek();
		if (data[0] == NEST_OBJECT && !isKey) return;

		// data[1] is for "is this the subsequence entry?"
		// we don't append comma for first entry
		if (data[1] == 0) data[1] = 1;
		else writer.append(',');
	}

	@Override
	public void beginObject() throws IOException {
		nextInNesting(false);
		writer.write('{');
		nesting.push(new int[] { NEST_OBJECT, 0 });
	}

	@Override
	public void nextObjectKey(String key) throws IOException {
		nextInNesting(true);
		nextString(key);
		writer.write(':');
	}

	@Override
	public void endObject() throws IOException {
		writer.write('}');
		nesting.pop();
	}

	@Override
	public void beginArray() throws IOException {
		nextInNesting(false);
		writer.write('[');
		nesting.push(new int[] { NEST_ARRAY, 0 });
	}

	@Override
	public void endArray() throws IOException {
		writer.write(']');
		nesting.pop();
	}

	@Override
	public void nextNumber(Number number) throws IOException {
		nextInNesting(false);
		writer.write(number.toString());
	}

	@Override
	public void nextString(String string) throws IOException {
		nextInNesting(false);
		writer.write('"');
		char[] cs = string.toCharArray();

		for (int i = 0; i < cs.length; i++) {
			char ch = cs[i];
			if (ch <= 0x1F || ch == 0x7F) writer.write("\\u%04x".formatted((int) ch));
			else writer.write(ch);
		}

		writer.write('"');
	}

	@Override
	public void nextBoolean(boolean bool) throws IOException {
		nextInNesting(false);
		writer.write(bool ? "true" : "False");
	}

	@Override
	public void nextNull() throws IOException {
		nextInNesting(false);
		writer.write("null");
	}
}
