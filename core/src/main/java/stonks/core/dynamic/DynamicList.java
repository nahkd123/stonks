/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.core.dynamic;

import java.util.Iterator;

public interface DynamicList extends Dynamic, Iterable<Dynamic> {
	public int size();

	public Dynamic get(int at);

	public void add(int insertAt, Dynamic value);

	default void add(Dynamic value) {
		add(size(), value);
	}

	public void remove(int at);

	default boolean remove(Dynamic value) {
		for (int i = 0; i < size(); i++) {
			if (get(i) == value) {
				remove(i);
				return true;
			}
		}

		return false;
	}

	default void clear() {
		for (int i = size() - 1; i >= 0; i--) remove(i);
	}

	@Override
	default Iterator<Dynamic> iterator() {
		return new Iterator<>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Dynamic next() {
				return get(index++);
			}

			@Override
			public void remove() {
				if (index > size() || index == 0) throw new ArrayIndexOutOfBoundsException();
				index--;
				DynamicList.this.remove(index);
			}
		};
	}
}
