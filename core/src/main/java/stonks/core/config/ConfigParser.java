package stonks.core.config;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
	private Iterator<String> lines;

	public ConfigParser(Iterator<String> lines) {
		this.lines = lines;
	}

	private static final Pattern COMMENT = Pattern.compile("^(\\s*(\\/\\/.+\\r?\\n?|\\/\\*(.|\\r|\\n)*?\\*/))*");
	private static final Pattern ELEMENT_HEAD = Pattern.compile("^(\\s*)([\\w\\.]+)\\s+(.+)\\r?\\n?");
	private static final Pattern ELEMENT_HEAD_VALUELESS = Pattern.compile("^(\\s*)([\\w\\.]+)\\r?\\n?");

	public ConfigElement nextElement() {
		while (lines.hasNext()) {
			var line = lines.next();
			Matcher matcher;

			if ((matcher = COMMENT.matcher(line)).matches()) { continue; }
			if ((matcher = ELEMENT_HEAD.matcher(line)).matches()) {
				var indent = matcher.group(1).length();
				var key = matcher.group(2);
				var value = matcher.group(3);

				if (value.endsWith(" \\")) {
					value = value.substring(0, value.length() - 2);
					while (lines.hasNext() && (line = lines.next()).endsWith(" \\")) {
						value += line.substring(0, line.length() - 2);
					}

					value += line;
				}

				return new ConfigElement(indent, key, value);
			}
			if ((matcher = ELEMENT_HEAD_VALUELESS.matcher(line)).matches()) {
				var indent = matcher.group(1).length();
				var key = matcher.group(2);
				return new ConfigElement(indent, key, null);
			}
		}

		return null;
	}
}
