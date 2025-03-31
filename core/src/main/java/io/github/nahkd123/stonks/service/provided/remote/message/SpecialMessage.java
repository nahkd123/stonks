package io.github.nahkd123.stonks.service.provided.remote.message;

import java.nio.ByteBuffer;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public enum SpecialMessage implements Message {
	BYE,
	PING,
	PONG;

	public static final BufferCodec<SpecialMessage> CODEC = BufferCodec.ofEnum(values());

	public static SpecialMessage read(ByteBuffer buffer) {
		return SpecialMessage.values()[buffer.get() & 0xFF];
	}

	public void write(ByteBuffer buffer) {
		buffer.put((byte) ordinal());
	}
}
