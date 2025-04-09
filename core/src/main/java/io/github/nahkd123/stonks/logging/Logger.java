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
package io.github.nahkd123.stonks.logging;

public interface Logger {
	int LEVEL_VERBOSE = 0;
	int LEVEL_INFO = 100;
	int LEVEL_WARNING = 200;
	int LEVEL_ERROR = 300;
	int LEVEL_CRITICAL = 400;

	void log(int level, String message);

	default void verbose(String message) {
		log(LEVEL_VERBOSE, message);
	}

	default void info(String message) {
		log(LEVEL_INFO, message);
	}

	default void warning(String message) {
		log(LEVEL_WARNING, message);
	}

	default void error(String message) {
		log(LEVEL_ERROR, message);
	}

	default void critical(String message) {
		log(LEVEL_CRITICAL, message);
	}
}
