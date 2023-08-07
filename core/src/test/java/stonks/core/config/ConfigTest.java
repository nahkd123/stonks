package stonks.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ConfigTest {
	@Test
	void test() throws Exception {
		var res = getClass().getClassLoader().getResource("config");
		var path = Path.of(res.toURI());
		var config = Config.fromPath(path);
		System.out.println(config.exportAsString());
	}
}
