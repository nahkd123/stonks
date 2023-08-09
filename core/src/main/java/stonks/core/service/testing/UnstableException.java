package stonks.core.service.testing;

import java.io.Serial;

public class UnstableException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = -1316143815081683052L;

	public UnstableException(String message) {
		super(message);
	}
}
