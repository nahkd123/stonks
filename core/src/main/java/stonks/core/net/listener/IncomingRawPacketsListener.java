package stonks.core.net.listener;

import java.nio.ByteBuffer;

import stonks.core.net.Connection;

@FunctionalInterface
public interface IncomingRawPacketsListener {
	public void onIncomingRawPacket(Connection connection, ByteBuffer raw);
}
