package stonks.core.service.remote.message;

import stonks.core.net.Connection;

public record ConnectionMessage<T extends Message>(Connection connection, T message) {
}
