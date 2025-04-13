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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Dynamic objects for aid with reading configuration. Dynamic objects are
 * obtained from configuration formats like JSON or YAML. It is recommened that
 * you use {@link Dynamic} with {@code switch} pattern matching from Java 21 if
 * you wish to add case for each type.
 * </p>
 * {@snippet :
 * switch (config) {
 * case Dynamic.Primitive p:
 * 	url = parseUrl(p.asString());
 * 	break;
 * case Dynamic.Compound c:
 * 	String hostname = c.get("host", "hostname").asString();
 * 	int port = c.get("port").asNumber().intValue();
 * 	url = fromHostnameAndPort(hostname, port);
 * 	break;
 * default:
 * 	break;
 * }
 * }
 */
public interface Dynamic {
	/**
	 * <p>
	 * Get the path to this dynamic object. Root dynamic object is always {@code /}.
	 * </p>
	 * 
	 * @return The path to dynamic object
	 */
	String path();

	default Primitive asPrimitive() throws IllegalArgumentException {
		if (!(this instanceof Primitive p)) throw new IllegalArgumentException("%s: Not a primitive".formatted(path()));
		return p;
	}

	default Compound asCompound() throws IllegalArgumentException {
		if (!(this instanceof Compound c)) throw new IllegalArgumentException("%s: Not a compound".formatted(path()));
		return c;
	}

	default List asList() throws IllegalArgumentException {
		if (!(this instanceof List l)) throw new IllegalArgumentException("%s: Not a list".formatted(path()));
		return l;
	}

	interface Primitive extends Dynamic {
		/**
		 * <p>
		 * Check the primitive type of this primitive dynamic. Possible values are class
		 * of {@link Number} and class of {@link String}.
		 * </p>
		 * 
		 * @return The primitive type.
		 */
		Class<?> primitiveType();

		String asString();

		default Number asNumber() throws IllegalArgumentException {
			try {
				return Double.parseDouble(asString());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("%s: Not a valid number".formatted(path()), e);
			}
		}

		default int asInt() throws IllegalArgumentException {
			return asNumber().intValue();
		}

		default double asDouble() throws IllegalArgumentException {
			return asNumber().doubleValue();
		}

		default boolean asBool() {
			return Boolean.parseBoolean(asString());
		}
	}

	interface Compound extends Dynamic {
		Set<String> keys();

		Dynamic get(String... aliases) throws NoSuchElementException;

		default Optional<Dynamic> getOptional(String... aliases) {
			Set<String> keys = keys();
			for (String alias : aliases) if (keys.contains(alias)) return Optional.of(get(alias));
			return Optional.empty();
		}
	}

	interface List extends Dynamic {
		int size();

		Dynamic get(int index) throws IndexOutOfBoundsException;
	}
}
