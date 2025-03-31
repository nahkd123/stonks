package io.github.nahkd123.stonks.service.provided.remote.message;

import java.nio.ByteBuffer;

import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public enum SimpleQueryMessage implements Message {
	CATALOG;

	public static final BufferCodec<SimpleQueryMessage> CODEC = BufferCodec.ofEnum(values());

	public static SimpleQueryMessage read(ByteBuffer buffer) {
		return SimpleQueryMessage.values()[buffer.get() & 0xFF];
	}

	public void write(ByteBuffer buffer) {
		buffer.put((byte) ordinal());
	}
}
