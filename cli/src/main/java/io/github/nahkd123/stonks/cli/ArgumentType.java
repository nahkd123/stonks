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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface ArgumentType<T> {
	T parse(Arguments args);

	default <U> ArgumentType<U> map(Function<T, U> mapper) {
		return args -> mapper.apply(parse(args));
	}

	ArgumentType<Void> VOID = args -> null;

	ArgumentType<String> STRING = args -> {
		if (!args.hasNext()) return "";
		String first = args.next();
		String out = first;
		char quote = '\0';
		if (first.charAt(0) == '\'' || first.charAt(0) == '"') quote = first.charAt(0);

		while (quote != '\0') {
			if (!args.hasNext()) return out;
			String word = args.next();
			out += " " + word;
			if (word.charAt(word.length() - 1) == quote) break;
		}

		if (quote != '\0') out = out.substring(1, out.length() - 1);
		return out;
	};

	ArgumentType<Path> PATH = STRING.map(Path::of);

	class Arguments {
		private String[] args;
		private int i = 0;

		public Arguments(String[] args) {
			this.args = args;
		}

		public String next() {
			return args[i++];
		}

		public boolean hasNext() {
			return i < args.length;
		}

		public void accept(Collection<Option<?>> options) {
			Map<String, Option<?>> collected = options.stream()
				.flatMap(a -> Stream.of(a.aliases()).map(b -> Map.entry(b, a)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			while (hasNext()) {
				String sw = next();
				Option<?> arg = collected.get(sw);

				if (arg == null) {
					System.err.println("Unknown argument: %s".formatted(sw));
					continue;
				}

				arg.accept(this);
			}
		}
	}
}
