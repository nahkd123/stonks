package io.github.nahkd123.stonks.utils.net;

public interface Message {
	record Request(Message message, long requestId) {
	}

	record Response(Message message, long requestId, String error) {
	}

	record Type<T extends Message>(int messageId, BufferCodec<T> codec) {
	}
}
