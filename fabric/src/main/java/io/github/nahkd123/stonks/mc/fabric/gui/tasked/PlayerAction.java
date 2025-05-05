package io.github.nahkd123.stonks.mc.fabric.gui.tasked;

import java.io.Serial;

public class PlayerAction extends Throwable {
	@Serial
	private static final long serialVersionUID = -2138972874798123789L;

	public PlayerAction(String message, Throwable cause) {
		super(message, cause);
	}

	public PlayerAction(String message) {
		super(message);
	}

	public PlayerAction() {
		super();
	}
}
