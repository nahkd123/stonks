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

import java.util.UUID;

import nahara.common.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import stonks.core.market.Offer;
import stonks.core.service.ServiceException;
import stonks.server.Main;

@Command(name = "cancel-offer", description = "Cancel offer.")
public class CancelOfferCommand implements AsyncConsoleCommand<Offer> {
	@ParentCommand
	public ConsoleInstance parent;

	@Option(names = { "--uuid", "-U" }, description = "User's unique ID. Leave blank for default UUID.")
	public UUID uuid = Main.DEFAULT_USER_UUID;

	@Parameters(paramLabel = "ID", description = "ID of the offer to cancel.")
	public UUID offerId;

	@Override
	public Task<Offer> getTask() throws Exception { return parent.service.cancelOffer(uuid, offerId); }

	@Override
	public void onSuccess(Offer obj) throws Exception {
		ConsoleInstance.LOGGER.info("Canceled offer successfully!");
	}

	@Override
	public void onFailure(Throwable t) throws Exception {
		if (!(t instanceof ServiceException)) t.printStackTrace();
		ConsoleInstance.LOGGER.error("Failed to cancel offer: " + t.getMessage());
	}
}
