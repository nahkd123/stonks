/*
 * Copyright (c) 2023 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package stonks.core.service.remote.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import stonks.core.net.Connection;
import stonks.core.service.Emittable;

public class MessagesHandler {
	private Map<String, Message.MessageDeserializer> deserializers = new HashMap<>();
	private Emittable<ConnectionMessage<?>> messagesEmitter = new Emittable<>();

	// Response queue
	private static record WaitingResponse<T extends Message>(Class<T> clazz, Predicate<T> isValid, Consumer<ConnectionMessage<T>> callback) {
	}

	private Queue<WaitingResponse<?>> waiting = new ConcurrentLinkedQueue<>();

	public void registerDeserializer(String id, Message.MessageDeserializer deserializer) {
		deserializers.put(id, deserializer);
	}

	public Message deserialize(ByteBuffer buffer) {
		if (buffer.position() != 0) buffer.flip();
		byte[] bs = new byte[buffer.limit()];
		buffer.get(bs);
		var input = new DataInputStream(new ByteArrayInputStream(bs));

		try {
			var id = input.readUTF();
			var deserializer = deserializers.get(id);
			if (deserializer == null) return null;
			return deserializer.deserialize(input);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> void listenForMessage(Class<T> type, Consumer<ConnectionMessage<T>> consumer) {
		messagesEmitter.listen(msg -> {
			if (msg.message().getClass() == type) consumer.accept((ConnectionMessage<T>) msg);
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleConnection(Connection conn) {
		conn.listenRawPackets((connection, raw) -> {
			var message = this.deserialize(raw);
			if (message == null) return;

			var wrapped = new ConnectionMessage<>(connection, message);
			messagesEmitter.emit(wrapped);
			this.waiting.removeIf(waiting -> {
				if (waiting.clazz == message.getClass() && ((Predicate) waiting.isValid).test(message)) {
					waiting.callback.accept((ConnectionMessage) wrapped);
					return true;
				}

				return false;
			});
		});
	}

	public <T extends Message> void waitForMessage(Class<T> type, Predicate<T> filter, Consumer<ConnectionMessage<T>> cb) {
		waiting.add(new WaitingResponse<>(type, filter, cb));
	}
}
