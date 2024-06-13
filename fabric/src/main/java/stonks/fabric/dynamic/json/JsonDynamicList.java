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
package stonks.fabric.dynamic.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import stonks.core.dynamic.Dynamic;
import stonks.core.dynamic.DynamicList;

public class JsonDynamicList implements JsonDynamic, DynamicList {
	private JsonArray json;

	public JsonDynamicList(JsonArray json) {
		this.json = json;
	}

	@Override
	public JsonElement getJson() { return json; }

	@Override
	public int size() {
		return json.size();
	}

	@Override
	public Dynamic get(int at) {
		return getFactory().wrap(json.get(at));
	}

	@Override
	public void add(int insertAt, Dynamic value) {
		if (value == null) {
			json.add(JsonNull.INSTANCE);
			return;
		}

		if (!(value instanceof JsonDynamic jsonDyn)) throw new IllegalArgumentException("Value must be JsonDyn");
		json.asList().add(insertAt, jsonDyn.getJson());
	}

	@Override
	public void remove(int at) {
		json.remove(at);
	}

	@Override
	public boolean remove(Dynamic value) {
		if (!(value instanceof JsonDynamic jsonDyn)) throw new IllegalArgumentException("Value must be JsonDyn");
		return json.remove(jsonDyn.getJson());
	}
}
