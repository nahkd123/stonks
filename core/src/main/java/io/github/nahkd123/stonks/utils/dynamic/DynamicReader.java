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

import java.io.IOException;

public interface DynamicReader {
	int TYPE_EOS = -1;
	int TYPE_STRING = 0;
	int TYPE_NUMBER = 1;
	int TYPE_OBJECT_HEAD = 2;
	int TYPE_OBJECT_KEY = 3;
	int TYPE_OBJECT_TAIL = 4;
	int TYPE_ARRAY_HEAD = 5;
	int TYPE_ARRAY_TAIL = 6;
	int TYPE_BOOLEAN = 7;
	int TYPE_NULL = 8;

	int nextToken() throws IOException;

	void undoNextToken(int token) throws IOException;

	Number nextNumber() throws IOException;

	String nextString() throws IOException;

	boolean nextBoolean() throws IOException;

	String nextObjectKey() throws IOException;
}
