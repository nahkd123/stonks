package io.github.nahkd123.stonks.utils.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.utils.net.Message.Response;

public abstract class MessageConnection extends Connection {
	private AtomicLong requestIdCounter = new AtomicLong(0L);
	private Map<Long, CompletableFuture<Message.Response>> unfulfilledRequests = new ConcurrentHashMap<>();
	private Map<Integer, Message.Type<?>> idToType = new HashMap<>();
	private Map<Class<?>, Message.Type<?>> classToType = new HashMap<>();

	protected <T extends Message> void registerMessageType(int messageId, Class<T> clazz, BufferCodec<T> codec) {
		Message.Type<T> type = new Message.Type<>(messageId, codec);
		idToType.put(messageId, type);
		classToType.put(clazz, type);
	}

	@Override
	protected void onFrameReceive(short kind, int messageId, long requestId, ByteBuffer buffer) {
		Message.Type<?> type = idToType.get(messageId);

		switch (kind) {
		case KIND_REQUEST:
			handleRequest(new Message.Request(type.codec().read(buffer), requestId));
			break;
		case KIND_RESPONSE_SUCCEED:
			handleResponse(new Message.Response(type.codec().read(buffer), requestId, null));
			break;
		case KIND_RESPONSE_FAILED:
			byte[] bs = new byte[buffer.remaining()];
			buffer.get(bs);
			handleResponse(new Message.Response(null, requestId, new String(bs, StandardCharsets.UTF_8)));
			break;
		case KIND_NOTIFY:
			handleNotification(type.codec().read(buffer));
			break;
		}
	}

	protected abstract void handleRequest(Message.Request request);

	protected void handleResponse(Message.Response response) {
		CompletableFuture<Response> future = unfulfilledRequests.remove(response.requestId());
		if (future != null) future.complete(response);
	}

	protected abstract void handleNotification(Message message);

	@SuppressWarnings("unchecked")
	public <T extends Message> CompletableFuture<Message.Response> request(T message) {
		if (!isRunning() || isCloseRequested())
			return CompletableFuture.failedFuture(new ServiceException("Connection is closed or being closed"));

		CompletableFuture<Message.Response> future = new CompletableFuture<>();
		long requestId = requestIdCounter.getAndIncrement();
		unfulfilledRequests.put(requestId, future);
		Message.Type<T> type = (Message.Type<T>) classToType.get(message.getClass());
		writeFrame(KIND_REQUEST, type.messageId(), requestId, buffer -> type.codec().write(message, buffer));
		return future;
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> void responseSucceed(Message.Request request, T message) {
		Message.Type<T> type = (Message.Type<T>) classToType.get(message.getClass());
		writeFrame(
			KIND_RESPONSE_SUCCEED,
			type.messageId(),
			request.requestId(),
			buffer -> type.codec().write(message, buffer));
	}

	public <T extends Message> void responseFailed(Message.Request request, String message) {
		writeFrame(
			KIND_RESPONSE_FAILED, 0,
			request.requestId(),
			buffer -> buffer.put(message.getBytes(StandardCharsets.UTF_8)));
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> void notify(T message) {
		if (!isRunning() || isCloseRequested())
			throw new ServiceException("Connection is closed or being closed");

		Message.Type<T> type = (Message.Type<T>) classToType.get(message.getClass());
		writeFrame(KIND_NOTIFY, type.messageId(), 0L, buffer -> type.codec().write(message, buffer));
	}
}
