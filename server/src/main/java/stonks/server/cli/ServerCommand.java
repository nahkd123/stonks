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

import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import stonks.core.service.LocalStonksService;
import stonks.core.service.remote.StonksRemoteServer;
import stonks.server.Main;
import stonks.server.cli.console.ConsoleInstance;
import stonks.server.cli.converter.SocketAddressConverter;

@Command(name = "server", description = "Start a new Stonks Server.")
public class ServerCommand implements Callable<Integer> {
	@ParentCommand
	private MainCommand parent;

	@Option(names = { "--address", "-A" },
		converter = SocketAddressConverter.class,
		description = {
			"Specify a socket address to bind server.",
			"Use inet:<address[:port]> for regular TCP address.",
			"Use socket:<path/to/socket> for Unix socket.",
			"You can specify more than 1 address."
		})
	public List<SocketAddress> addresses;

	private List<Map.Entry<StonksRemoteServer, Thread>> servers = new ArrayList<>();

	@Override
	public Integer call() throws Exception {
		Main.LOGGER.info("Starting service...");
		var service = parent.provider.configure(parent.configuration);

		if (service instanceof LocalStonksService local) {
			Main.LOGGER.info("Local service found, loading data...");
			local.loadServiceData();
		}

		for (var addr : addresses) {
			Main.LOGGER.info("Starting networking thread for address " + addr + "...");

			var serverChannel = addr instanceof UnixDomainSocketAddress
				? ServerSocketChannel.open(StandardProtocolFamily.UNIX)
				: ServerSocketChannel.open();
			if (addr instanceof UnixDomainSocketAddress unix) Files.deleteIfExists(unix.getPath());
			serverChannel.bind(addr);
			var server = new StonksRemoteServer(service, serverChannel);
			var thread = server.getServer().createThread();
			thread.start();
			servers.add(Map.entry(server, thread));
		}

		Main.LOGGER.info("Service ready!");

		// while (true) Thread.sleep(1000); // TODO No console mode
		// Console will be in main thread
		var console = new ConsoleInstance(service);
		console.startInCurrentThread();
		Main.LOGGER.info("Console exited, stopping networking threads...");

		for (var server : servers) {
			var addr = server.getKey().getServer().getServer().getLocalAddress();
			server.getKey().getServer().stop();
			while (server.getValue().isAlive()) Thread.sleep(1);
			server.getKey().getServer().getServer().close();
			Main.LOGGER.info("Stopped networking thread for address {}", addr);
		}

		// TODO stop service
		if (service instanceof LocalStonksService local) {
			Main.LOGGER.info("Saving service data...");
			local.saveServiceData();
		}

		Main.LOGGER.info("Goodbye!");
		return 0;
	}
}
