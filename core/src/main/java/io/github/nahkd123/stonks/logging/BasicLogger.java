package io.github.nahkd123.stonks.logging;

import java.util.Map;

public class BasicLogger implements Logger {
	private static final Map<Integer, String> LEVELS = Map.of(
		Logger.LEVEL_VERBOSE, "Verbose",
		Logger.LEVEL_INFO, "Info",
		Logger.LEVEL_WARNING, "WARNING",
		Logger.LEVEL_ERROR, "ERROR",
		Logger.LEVEL_CRITICAL, "CRITICAL ERROR");

	@Override
	public void log(int level, String message) {
		System.out.println("[%s]: %s".formatted(
			LEVELS.getOrDefault(level, "level=%d".formatted(level)),
			message));
	}
}
