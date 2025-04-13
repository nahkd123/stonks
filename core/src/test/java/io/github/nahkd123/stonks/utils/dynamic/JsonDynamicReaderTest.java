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
package io.github.nahkd123.stonks.utils.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import io.github.nahkd123.stonks.utils.dynamic.json.JsonDynamicReader;

class JsonDynamicReaderTest {
	@Test
	void testParseObject() throws IOException {
		String c = "  { \"value\"    :   12.34E0567 ,  \"text\"  :  \"Hello world\" } ";
		JsonDynamicReader reader = new JsonDynamicReader(new StringReader(c));

		assertEquals(DynamicReader.TYPE_OBJECT_HEAD, reader.nextToken());
		assertEquals(DynamicReader.TYPE_OBJECT_KEY, reader.nextToken());
		assertEquals("value", reader.nextObjectKey());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("12.34E0567", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_OBJECT_KEY, reader.nextToken());
		assertEquals("text", reader.nextObjectKey());
		assertEquals(DynamicReader.TYPE_STRING, reader.nextToken());
		assertEquals("Hello world", reader.nextString());
		assertEquals(DynamicReader.TYPE_OBJECT_TAIL, reader.nextToken());
	}

	@Test
	void testParseArray() throws IOException {
		String c = "  [  1   ,  0  , 0.1  , -1.23E123  , \"Hello world\" ] ";
		JsonDynamicReader reader = new JsonDynamicReader(new StringReader(c));

		assertEquals(DynamicReader.TYPE_ARRAY_HEAD, reader.nextToken());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("1", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("0", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("0.1", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("-1.23E123", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_STRING, reader.nextToken());
		assertEquals("Hello world", reader.nextString());
		assertEquals(DynamicReader.TYPE_ARRAY_TAIL, reader.nextToken());
	}

	@Test
	void testParseNested() throws IOException {
		String c = "[1, {\"value\": \"Text\"}, {}, 0.1, -1.23E123, \"Hello world\"]";
		JsonDynamicReader reader = new JsonDynamicReader(new StringReader(c));

		assertEquals(DynamicReader.TYPE_ARRAY_HEAD, reader.nextToken());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("1", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_OBJECT_HEAD, reader.nextToken());
		assertEquals(DynamicReader.TYPE_OBJECT_KEY, reader.nextToken());
		assertEquals("value", reader.nextObjectKey());
		assertEquals(DynamicReader.TYPE_STRING, reader.nextToken());
		assertEquals("Text", reader.nextString());
		assertEquals(DynamicReader.TYPE_OBJECT_TAIL, reader.nextToken());
		assertEquals(DynamicReader.TYPE_OBJECT_HEAD, reader.nextToken());
		assertEquals(DynamicReader.TYPE_OBJECT_TAIL, reader.nextToken());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("0.1", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_NUMBER, reader.nextToken());
		assertEquals("-1.23E123", reader.nextNumberRaw());
		assertEquals(DynamicReader.TYPE_STRING, reader.nextToken());
		assertEquals("Hello world", reader.nextString());
		assertEquals(DynamicReader.TYPE_ARRAY_TAIL, reader.nextToken());
	}
}
