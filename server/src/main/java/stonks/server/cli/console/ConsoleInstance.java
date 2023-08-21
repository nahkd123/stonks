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
package stonks.server.cli.console;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import stonks.core.service.StonksService;

@Command(name = "<console>",
	subcommands = {
		ExitCommand.class,
		ListProductsCommand.class,
		ProductOverviewCommand.class,
		ListOffersCommand.class,
	})
public class ConsoleInstance {
	public static final Logger LOGGER = LoggerFactory.getLogger("Console");
	public StonksService service;
	public boolean shouldStopConsole = false;

	public ConsoleInstance(StonksService service) {
		this.service = service;
	}

	public void startInCurrentThread() {
		var console = System.console();

		if (console == null) {
			LOGGER.warn("System.console() returns null - Unsupported TTY/Missing console.");
			LOGGER.warn("Scanner will be used to read data from standard input.");

			var scanner = new Scanner(System.in);
			LOGGER.info("Console ready!");
			while (!shouldStopConsole) sendCommand(scanner.nextLine());
			scanner.close();
		} else {
			LOGGER.info("Console ready!");
			while (!shouldStopConsole) console.readLine();
		}
	}

	private void sendCommand(String raw) {
		if (shouldStopConsole) return;
		new CommandLine(this).execute(raw.split(" "));
	}
}
