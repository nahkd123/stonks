package io.github.nahkd123.stonks.logging;

public interface Logger {
	int LEVEL_VERBOSE = 0;
	int LEVEL_INFO = 100;
	int LEVEL_WARNING = 200;
	int LEVEL_ERROR = 300;
	int LEVEL_CRITICAL = 400;

	void log(int level, String message);

	default void verbose(String message) {
		log(LEVEL_VERBOSE, message);
	}

	default void info(String message) {
		log(LEVEL_INFO, message);
	}

	default void warning(String message) {
		log(LEVEL_WARNING, message);
	}

	default void error(String message) {
		log(LEVEL_ERROR, message);
	}

	default void critical(String message) {
		log(LEVEL_CRITICAL, message);
	}
}
