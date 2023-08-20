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
package stonks.fabric.adapter;

import net.minecraft.text.Text;

public record AdapterResponse<T>(AdapterResponseType type, T result, Text message) {
	public AdapterResponse {
		if (type == null) throw new IllegalArgumentException("type can't be null");
		if (type == AdapterResponseType.FAILED && message == null)
			throw new IllegalArgumentException("message can't be null");
	}

	public static <T> AdapterResponse<T> success(T result) {
		return new AdapterResponse<T>(AdapterResponseType.SUCCESS, result, null);
	}

	public static <T> AdapterResponse<T> failed(Text message) {
		return new AdapterResponse<T>(AdapterResponseType.FAILED, null, message);
	}

	public static <T> AdapterResponse<T> pass() {
		return new AdapterResponse<T>(AdapterResponseType.PASS, null, null);
	}

	public boolean isPass() { return type == AdapterResponseType.PASS; }

	public boolean isSuccess() { return type == AdapterResponseType.SUCCESS; }

	public boolean isFailed() { return type == AdapterResponseType.FAILED; }

	public T or(T value) {
		return isSuccess() ? result() : value;
	}
}
