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
package io.github.nahkd123.stonks.cli;

import java.io.IOException;

import io.github.nahkd123.stonks.utils.dynamic.DynamicReader;

public class StringOnlyDynamicReader implements DynamicReader {
	private String content;

	public StringOnlyDynamicReader(String content) {
		this.content = content;
	}

	@Override
	public int nextToken() throws IOException {
		return TYPE_STRING;
	}

	@Override
	public void undoNextToken(int token) throws IOException {}

	@Override
	public Number nextNumber() throws IOException {
		throw new IOException("Only support string (have you checked nextToken() yet?)");
	}

	@Override
	public String nextString() throws IOException {
		return content;
	}

	@Override
	public boolean nextBoolean() throws IOException {
		throw new IOException("Only support string (have you checked nextToken() yet?)");
	}

	@Override
	public String nextObjectKey() throws IOException {
		throw new IOException("Only support string (have you checked nextToken() yet?)");
	}
}
