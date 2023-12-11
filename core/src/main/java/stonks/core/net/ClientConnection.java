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
import java.nio.channels.SocketChannel;

import stonks.core.net.listener.NewConnectionsListener;

public class ClientConnection extends Connection {
	private SocketChannel client;
	private NewConnectionsListener handler;

	public ClientConnection(SocketChannel client, NewConnectionsListener handler) throws IOException {
		this.client = client;
		this.handler = handler;
		this.client.configureBlocking(false);
	}

	public void startOnCurrentThread() throws IOException {
		while (!client.finishConnect()) { if (isCloseNeeded()) return; }
		handler.onNewConnection(this);

		do {
			handleRead(client);
			handleWrite(client);

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Interrupted while sleeping
				break;
			}

			if (Thread.currentThread().isInterrupted()) break;
		} while (!isCloseNeeded());

		setClosed(true);
		emitClose();
		client.close();
	}

	public Thread createThread() {
		var thread = new Thread(() -> {
			try {
				startOnCurrentThread();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		thread.setName("Client Networking Thread");
		return thread;
	}
}
