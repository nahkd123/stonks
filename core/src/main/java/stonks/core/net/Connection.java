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
package stonks.core.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import stonks.core.net.listener.ConnectionClosedListener;
import stonks.core.net.listener.IncomingRawPacketsListener;

public class Connection {
	private Queue<ByteBuffer> outgoingQueue = new ConcurrentLinkedQueue<>();
	private List<IncomingRawPacketsListener> rawPacketsListeners = new ArrayList<>();
	private List<ConnectionClosedListener> closeListeners = new ArrayList<>();

	// Packet specification
	// 2 bytes: Packet length (always uncompressed). Maximum length of 32767.
	// [length] bytes: Packet content
	private ByteBuffer header;
	private ByteBuffer content;
	private int contentLength = -1;
	private boolean closeNeeded = false, isClosed = false;

	protected Connection() {
		header = ByteBuffer.allocateDirect(2);
		content = ByteBuffer.allocateDirect(32767);
	}

	protected void handleRead(SocketChannel socket) throws IOException {
		if (!socket.isOpen()) {
			requestClose();
			return;
		}

		if (isCloseNeeded()) return;

		if (contentLength == -1) {
			var byteRead = socket.read(header);
			if (byteRead == -1) {
				requestClose();
				return;
			}

			if (!header.hasRemaining()) {
				header.flip();
				var length = header.getShort();
				if (length < 0) {
					// Likely over 32767, because Java doesn't have unsigned short
					requestClose();
					return;
				}

				content.clear();
				content.limit(contentLength = length);

				if (length == 0) {
					content.flip();
					emitIncomingRawPacket(content);
					header.clear();
					contentLength = -1;
					if (isCloseNeeded()) return;
				}
			} else {
				// We might have 1 or 2 more bytes to read
				return;
			}
		} else {
			var byteRead = socket.read(content);
			if (byteRead == -1) {
				requestClose();
				return;
			}

			if (!content.hasRemaining()) {
				content.flip();
				emitIncomingRawPacket(content);

				// Ready to read next packet
				header.clear();
				contentLength = -1;
				if (isCloseNeeded()) return;
			}
		}
	}

	protected void handleWrite(SocketChannel socket) throws IOException {
		while (!outgoingQueue.isEmpty()) {
			var content = outgoingQueue.poll();
			header.clear();
			header.putShort((short) content.limit());
			header.flip();
			socket.write(header);

			// Sometimes you forgot to flip the buffer
			// Yep, a single method had drove me insane for 2 hours - nahkd123
			if (content.position() != 0) content.flip();
			socket.write(content);
		}

		// Prevent client from reading itself
		header.clear();
	}

	protected boolean isCloseNeeded() { return closeNeeded; }

	/**
	 * <p>
	 * Listen for incoming raw packets.
	 * </p>
	 * 
	 * @param listener
	 */
	public void listenRawPackets(IncomingRawPacketsListener listener) {
		rawPacketsListeners.add(listener);
	}

	/**
	 * <p>
	 * Listen for close events.
	 * </p>
	 * 
	 * @param listener
	 */
	public void listenClose(ConnectionClosedListener listener) {
		closeListeners.add(listener);
	}

	protected void emitIncomingRawPacket(ByteBuffer raw) {
		for (var l : rawPacketsListeners) l.onIncomingRawPacket(this, raw);
	}

	protected void emitClose() {
		for (var l : closeListeners) l.onClosed(this);
	}

	/**
	 * <p>
	 * Request {@link ServerHandler} to close this connection.
	 * </p>
	 */
	public void requestClose() {
		closeNeeded = true;
	}

	public boolean isClosed() { return isClosed; }

	protected void setClosed(boolean isClosed) { this.isClosed = isClosed; }

	public void sendRawPacket(ByteBuffer buffer) {
		outgoingQueue.add(buffer);
	}

	protected void closeAttachment() {
		// Attempt to dereference as much buffers as possible
		// I know this is not needed, but like, who knows, maybe selection key still
		// holds reference to this attachment object.
		header = null;
		content = null;
	}
}
