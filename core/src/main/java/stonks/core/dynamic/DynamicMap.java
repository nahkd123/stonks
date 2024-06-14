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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface DynamicMap extends Dynamic, Iterable<Map.Entry<String, ? extends Dynamic>> {
	public Set<String> keys();

	public Dynamic getOrNull(String key);

	public Dynamic put(String key, Dynamic value);

	public boolean remove(String key);

	default void clear() {
		for (String key : keys()) remove(key);
	}

	@Override
	default Iterator<Entry<String, ? extends Dynamic>> iterator() {
		return keys().stream().<Map.Entry<String, ? extends Dynamic>>map(k -> Map.entry(k, getOrNull(k))).iterator();
	}

	default Collection<Map.Entry<String, ? extends Dynamic>> entries() {
		var out = new ArrayList<Map.Entry<String, ? extends Dynamic>>();
		var iter = iterator();
		while (iter.hasNext()) out.add(iter.next());
		return out;
	}
}
