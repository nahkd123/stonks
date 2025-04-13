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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class JsonDynamicWriterTest {
	@Test
	void testWriteObject() throws IOException {
		StringWriter writer = new StringWriter();
		JsonDynamicWriter json = new JsonDynamicWriter(writer);
		json.beginObject();
		json.nextObjectKey("player");
		json.nextString("nahkd123");
		json.nextObjectKey("balance");
		json.nextNumber(12345);
		json.endObject();
		assertEquals("{\"player\":\"nahkd123\",\"balance\":12345}", writer.toString());
	}

	@Test
	void testWriteArray() throws IOException {
		StringWriter writer = new StringWriter();
		JsonDynamicWriter json = new JsonDynamicWriter(writer);
		json.beginArray();
		json.nextString("nahkd123");
		json.nextNumber(12345);
		json.endArray();
		assertEquals("[\"nahkd123\",12345]", writer.toString());
	}
}
