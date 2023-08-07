package stonks.core.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

// TODO move this to Nahara's Toolkit (if you think this configuration format is
// a banger)
public class Config extends ConfigElement {
	public Config() {
		super(-1, "//root", null);
	}

	@Override
	public String exportAsString() {
		if (getChildren().size() == 0) return "";
		var str = getChildren().get(0).exportAsString();
		for (int i = 1; i < getChildren().size(); i++) str += "\n" + getChildren().get(i).exportAsString();
		return str;
	}

	public static Config fromPath(Path path) {
		if (path == null || Files.notExists(path)) return new Config();

		try {
			var configScanner = new ConfigParser(Files.readAllLines(path, StandardCharsets.UTF_8).iterator());
			var root = new Config();

			var stack = new Stack<ConfigElement>();
			stack.add(root);

			ConfigElement e;
			while ((e = configScanner.nextElement()) != null) {
				if (stack.lastElement().getIndentLevel() < e.getIndentLevel()) {
					stack.lastElement().getChildren().add(e);
				} else if (stack.lastElement().getIndentLevel() == e.getIndentLevel()) {
					stack.pop();
					stack.lastElement().getChildren().add(e);
				} else {
					while (stack.lastElement().getIndentLevel() >= e.getIndentLevel()) stack.pop();
					stack.lastElement().getChildren().add(e);
				}

				stack.push(e);
			}

			return root;
		} catch (IOException e) {
			e.printStackTrace();
			return new Config();
		}
	}
}
