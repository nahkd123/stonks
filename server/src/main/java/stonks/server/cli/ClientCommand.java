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
package stonks.server.cli;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import stonks.core.net.ClientConnection;
import stonks.core.service.remote.StonksRemoteService;
import stonks.server.Main;
import stonks.server.cli.console.ConsoleInstance;
import stonks.server.cli.converter.SocketAddressConverter;

@Command(name = "client", description = "Connect to Stonks Server.")
public class ClientCommand implements Callable<Integer> {
	@ParentCommand
	private MainCommand parent;

	@Option(names = { "--address", "-A" },
		converter = SocketAddressConverter.class,
		description = {
			"Specify a socket address to connect to Stonks Server.",
			"Use inet:<address[:port]> for regular TCP address.",
			"Use socket:<path/to/socket> for Unix socket."
		})
	public SocketAddress address = new InetSocketAddress(36636);

	private ConsoleInstance console;

	@Override
	public Integer call() throws Exception {
		Main.LOGGER.info("Connecting to {}...", address);
		var channel = address instanceof UnixDomainSocketAddress
			? SocketChannel.open(StandardProtocolFamily.UNIX)
			: SocketChannel.open();
		channel.connect(address);

		var conn = new ClientConnection(channel, connection -> {
			Main.LOGGER.info("Connected!");
			connection.listenClose($ -> {
				console.shouldStopConsole = true;
				Main.LOGGER.info("Disconnected! Press Enter to close console.");
			});
		});

		var service = new StonksRemoteService(conn);
		console = new ConsoleInstance(service);
		var thread = conn.createThread();
		thread.start();

		while (channel.isConnectionPending()) Thread.sleep(1);
		console.startInCurrentThread();
		Main.LOGGER.info("Console exited, stopping networking thread...");
		conn.requestClose();
		while (!conn.isClosed()) Thread.sleep(1);
		Main.LOGGER.info("Goodbye!");
		return 0;
	}
}
