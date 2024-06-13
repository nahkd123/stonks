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

public interface DynamicFactory {
	public DynamicPrimitive createPrimitive(Number value);

	public DynamicPrimitive createPrimitive(String value);

	public DynamicPrimitive createPrimitive(boolean value);

	public DynamicMap createMap();

	public DynamicList createList();

	/**
	 * <p>
	 * Deep clone (a.k.a recursive clone) the dynamic. The cloned dynamic will have
	 * its factory bounds to this factory.
	 * </p>
	 * 
	 * @param another A dynamic.
	 * @return A cloned dynamic.
	 */
	default Dynamic cloneDynamic(Dynamic another) {
		if (another instanceof DynamicPrimitive prim) {
			if (prim.isString()) return createPrimitive(prim.asString());
			if (prim.isNumber()) return createPrimitive(prim.asNumber());
			if (prim.isBoolean()) return createPrimitive(prim.asBoolean());
			throw new IllegalArgumentException("Unknown primitive type for " + prim);
		}

		if (another instanceof DynamicMap map) {
			var out = createMap();
			map.forEach(entry -> map.put(entry.getKey(), cloneDynamic(entry.getValue())));
			return out;
		}

		if (another instanceof DynamicList list) {
			var out = createList();
			list.forEach(val -> out.add(val));
			return out;
		}

		throw new IllegalArgumentException("Unknown type for " + another);
	}
}
