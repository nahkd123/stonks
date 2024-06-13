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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import stonks.core.dynamic.DynamicFactory;
import stonks.core.dynamic.DynamicList;
import stonks.core.dynamic.DynamicMap;

public final class JsonDynamicFactory implements DynamicFactory {
	public static final JsonDynamicFactory FACTORY = new JsonDynamicFactory();

	private JsonDynamicFactory() {}

	public JsonDynamic wrap(JsonElement json) {
		if (json == null || json.isJsonNull()) return null;
		if (json.isJsonPrimitive()) return new JsonDynamicPrimitive(json.getAsJsonPrimitive());
		if (json.isJsonObject()) return new JsonDynamicMap(json.getAsJsonObject());
		if (json.isJsonArray()) return new JsonDynamicList(json.getAsJsonArray());
		throw new IllegalArgumentException("Unknown JSON type: " + json);
	}

	@Override
	public JsonDynamicPrimitive createPrimitive(Number value) {
		return new JsonDynamicPrimitive(new JsonPrimitive(value));
	}

	@Override
	public JsonDynamicPrimitive createPrimitive(String value) {
		return new JsonDynamicPrimitive(new JsonPrimitive(value));
	}

	@Override
	public JsonDynamicPrimitive createPrimitive(boolean value) {
		return new JsonDynamicPrimitive(new JsonPrimitive(value));
	}

	@Override
	public DynamicMap createMap() {
		return new JsonDynamicMap(new JsonObject());
	}

	@Override
	public DynamicList createList() {
		return new JsonDynamicList(new JsonArray());
	}
}
