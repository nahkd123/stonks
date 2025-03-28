package io.github.nahkd123.stonks.service;

import java.io.Serial;

public class ServiceException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 9100075709189889806L;

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}
}
