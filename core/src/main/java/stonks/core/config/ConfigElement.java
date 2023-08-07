package stonks.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigElement {
	private int indentLevel;
	private String key;
	private String value;
	private List<ConfigElement> children = new ArrayList<>();

	public ConfigElement(int indentLevel, String key, String value) {
		this.indentLevel = indentLevel;
		this.key = key;
		this.value = value;
	}

	public int getIndentLevel() { return indentLevel; }

	public String getKey() { return key; }

	public String getValue() { return value; }

	public List<ConfigElement> getChildren() { return children; }

	public String exportAsString(String indent) {
		var str = indent + key;
		if (value != null) str += " " + value;
		for (var child : children) str += "\n" + child.exportAsString(indent + "  ");
		return str;
	}

	public String exportAsString() {
		return exportAsString("");
	}

	public Optional<ConfigElement> firstChild(String key) {
		return children.stream().filter(v -> v.getKey().equals(key)).findFirst();
	}
}
