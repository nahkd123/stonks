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
import java.io.Reader;
import java.util.Stack;

import io.github.nahkd123.stonks.utils.dynamic.DynamicReader;

public class JsonDynamicReader implements DynamicReader {
	private enum ReadMode {
		NUMBER_NEG,
		NUMBER_NUM,
		NUMBER_ZERO,
		NUMBER_FRAC,
		NUMBER_EXP,
		OBJECT_KEY,
		OBJECT_VALUE,
		BOOLEAN_TRUE,
		BOOLEAN_FALSE;
	}

	private enum NestStackElement {
		OBJECT,
		ARRAY;
	}

	private Reader reader;
	private ReadMode mode;
	private int lastToken = -2;
	private char firstChar = '\0';
	private Stack<NestStackElement> nesting = new Stack<>();

	public JsonDynamicReader(Reader reader) {
		this.reader = reader;
	}

	@Override
	public int nextToken() throws IOException {
		if (lastToken != -2) {
			int token = lastToken;
			lastToken = -2;
			return token;
		}

		return nextToken0(reader.read());
	}

	private int readSkipWs() throws IOException {
		int ch;
		do ch = reader.read(); while (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t');
		return ch;
	}

	private int nextToken0(int ch) throws IOException {
		do {
			switch (ch) {

			// Object
			case '{':
				nesting.add(NestStackElement.OBJECT);
				mode = ReadMode.OBJECT_KEY;
				return TYPE_OBJECT_HEAD;
			case '}':
				if (nesting.pop() != NestStackElement.OBJECT) throw new IOException("Not in object but found }");
				return TYPE_OBJECT_TAIL;

			// Array
			case '[':
				nesting.add(NestStackElement.ARRAY);
				return TYPE_ARRAY_HEAD;
			case ']':
				if (nesting.pop() != NestStackElement.ARRAY) throw new IOException("Not in array but found ]");
				return TYPE_ARRAY_TAIL;

			// Array or object
			case ',':
				if (nesting.isEmpty()) throw new IOException("Not in object or array but found ,");
				switch (nesting.peek()) {
				case ARRAY:
					ch = reader.read();
					continue;
				case OBJECT:
					ch = reader.read();
					mode = ReadMode.OBJECT_KEY;
					continue;
				}
				break;

			// Number
			case '-':
				mode = ReadMode.NUMBER_NEG;
				firstChar = '-';
				return TYPE_NUMBER;
			case '1', '2', '3', '4', '5', '6', '7', '8', '9':
				mode = ReadMode.NUMBER_NUM;
				firstChar = (char) ch;
				return TYPE_NUMBER;
			case '0':
				mode = ReadMode.NUMBER_ZERO;
				firstChar = '0';
				return TYPE_NUMBER;

			// String
			case '"':
				return mode == ReadMode.OBJECT_KEY ? TYPE_OBJECT_KEY : TYPE_STRING;

			// Boolean
			case 't':
				nextLiteral("rue");
				mode = ReadMode.BOOLEAN_TRUE;
				return TYPE_BOOLEAN;
			case 'f':
				nextLiteral("alse");
				mode = ReadMode.BOOLEAN_FALSE;
				return TYPE_BOOLEAN;

			// Whitespaces
			case ' ', '\r', '\n', '\t':
				ch = reader.read();
				continue;

			// Misc
			case 'n':
				nextLiteral("ull");
				return TYPE_NULL;
			case -1:
				return TYPE_EOS;
			default:
				throw new IOException("Unknown character: %s".formatted((char) ch));

			}
		} while (true);
	}

	@Override
	public void undoNextToken(int token) throws IOException {
		lastToken = token;
	}

	public String nextNumberRaw() throws IOException {
		String collector = String.valueOf(firstChar);
		int ch;

		if (mode == ReadMode.NUMBER_NEG) {
			ch = reader.read();

			if (ch >= '1' && ch <= '9') {
				collector += (char) ch;
				mode = ReadMode.NUMBER_NUM;
			} else if (ch == '0') {
				collector += (char) ch;
				mode = ReadMode.NUMBER_ZERO;
			} else if (ch == -1) {
				throw new IOException("Expecting [0-9] but found end of stream");
			} else {
				throw new IOException("Expecting [0-9] but found %s".formatted((char) ch));
			}
		}

		while (mode == ReadMode.NUMBER_NUM) {
			ch = reader.read();

			if (ch >= '0' && ch <= '9') {
				collector += (char) ch;
			} else if (ch == '.') {
				collector += (char) ch;
				mode = ReadMode.NUMBER_FRAC;
			} else {
				lastToken = nextToken0(ch);
				return collector;
			}
		}

		if (mode == ReadMode.NUMBER_ZERO) {
			ch = reader.read();

			if (ch == '.') {
				collector += (char) ch;
				ch = reader.read();

				if (ch >= '0' && ch <= '9') {
					collector += (char) ch;
				} else if (ch == -1) {
					throw new IOException("Expecting [0-9] but found end of stream");
				} else {
					throw new IOException("Expecting [0-9] but found %s".formatted((char) ch));
				}

				mode = ReadMode.NUMBER_FRAC;
			} else if (ch == 'e' || ch == 'E') {
				collector += (char) ch;
				ch = reader.read();

				if (ch == '-') {
					collector += (char) ch;
					ch = reader.read();
				}

				if (ch >= '0' && ch <= '9') {
					collector += (char) ch;
				} else if (ch == -1) {
					throw new IOException("Expecting [0-9] but found end of stream");
				} else {
					throw new IOException("Expecting [0-9] but found %s".formatted((char) ch));
				}

				mode = ReadMode.NUMBER_EXP;
			} else {
				lastToken = nextToken0(ch);
				return collector;
			}
		}

		while (mode == ReadMode.NUMBER_FRAC) {
			ch = reader.read();

			if (ch >= '0' && ch <= '9') {
				collector += (char) ch;
			} else if (ch == 'e' || ch == 'E') {
				collector += (char) ch;
				ch = reader.read();

				if (ch == '-') {
					collector += (char) ch;
					ch = reader.read();
				}

				if (ch >= '0' && ch <= '9') {
					collector += (char) ch;
				} else if (ch == -1) {
					throw new IOException("Expecting [0-9] but found end of stream");
				} else {
					throw new IOException("Expecting [0-9] but found %s".formatted((char) ch));
				}

				mode = ReadMode.NUMBER_EXP;
			} else {
				lastToken = nextToken0(ch);
				return collector;
			}
		}

		while (mode == ReadMode.NUMBER_EXP) {
			ch = reader.read();

			if (ch >= '0' && ch <= '9') {
				collector += (char) ch;
			} else {
				lastToken = nextToken0(ch);
				return collector;
			}
		}

		return collector;
	}

	@Override
	public Number nextNumber() throws IOException {
		return Double.parseDouble(nextNumberRaw());
	}

	@Override
	public String nextString() throws IOException {
		CharBuffer buf = new CharBuffer(32);

		while (true) {
			int ch = reader.read();
			switch (ch) {
			case '"':
				return buf.toString();
			case '\\':
				ch = reader.read();
				buf.push(switch (ch) {
				case '"', '\\', '/':
					yield (char) ch;
				case 'b':
					yield '\b';
				case 'f':
					yield '\f';
				case 'n':
					yield '\n';
				case 'r':
					yield '\r';
				case 't':
					yield '\t';
				case 'u':
					int cp = 0;

					for (int i = 0; i < 4; i++) {
						ch = reader.read();
						if (ch >= '0' && ch <= '9') cp |= (ch - '0') << ((3 - i) * 4);
						else if (ch >= 'a' && ch <= 'f') cp |= (ch - 'a' + 10) << ((3 - i) * 4);
						else if (ch >= 'A' && ch <= 'F') cp |= (ch - 'A' + 10) << ((3 - i) * 4);
						else throw new IOException("Expecting [0-9a-fA-F] but found %s".formatted((char) ch));
					}

					yield (char) cp;
				case -1:
					throw new IOException("Expecting \", \\, /, b, f, n, r, t, u but found end of stream");
				default:
					throw new IOException("Expecting \", \\, /, b, f, n, r, t, u but found %s".formatted((char) ch));
				});
				break;
			default:
				if (ch >= 0x00 && ch <= 0x1F) throw new IOException("Control characters must be escaped");
				if (ch == -1) throw new IOException("Expecting valid codepoint or \" but found end of stream");
				buf.push((char) ch);
				continue;
			}
		}
	}

	@Override
	public String nextObjectKey() throws IOException {
		String s = nextString();
		int ch = readSkipWs();
		if (ch == -1) throw new IOException("Expecting : but found end of stream");
		if (ch != ':') throw new IOException("Expecting : but found %s".formatted((char) ch));
		mode = ReadMode.OBJECT_VALUE;
		return s;
	}

	@Override
	public boolean nextBoolean() throws IOException {
		return mode == ReadMode.BOOLEAN_TRUE;
	}

	private void nextLiteral(String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			char src = s.charAt(i);
			int ch = reader.read();
			if (ch == -1) throw new IOException("Expecting %s but found end of stream".formatted(src));
			if (ch != src) throw new IOException("Expecting %s but found %s".formatted(src, (char) ch));
		}
	}

	private class CharBuffer {
		private char[] cs;
		private int size;

		public CharBuffer(int initialCapacity) {
			this.cs = new char[initialCapacity];
			this.size = 0;
		}

		public void push(char ch) {
			if (size >= cs.length) {
				char[] newCs = new char[cs.length * 2];
				System.arraycopy(cs, 0, newCs, 0, cs.length);
				cs = newCs;
			}

			cs[size++] = ch;
		}

		@Override
		public String toString() {
			return String.copyValueOf(cs, 0, size);
		}
	}
}
