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

import nahara.common.configurations.Config;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import stonks.server.cli.converter.ConfigConverter;
import stonks.server.cli.converter.ConfigurableProviderConverter;
import stonks.server.service.ConfigurableProvider;
import stonks.server.service.ServiceProviders;

@Command(name = "stonks-server", subcommands = {
	ServerCommand.class,
	ClientCommand.class,
})
public class MainCommand {
	@Option(names = { "--provider", "-P" },
		converter = ConfigurableProviderConverter.class,
		description = "Specify a service provider to use. Default is \"stonks:memory\".")
	public ConfigurableProvider provider = ServiceProviders.MEMORY;

	@Option(names = { "--config", "-C" },
		converter = ConfigConverter.class,
		description = "Specify configuration file for service provider.")
	public Config configuration = new Config();
}
