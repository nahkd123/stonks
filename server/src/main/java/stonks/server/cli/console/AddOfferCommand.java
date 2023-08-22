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
import stonks.core.market.OfferType;
import stonks.core.service.remote.RemoteServiceException;
import stonks.server.Main;

@Command(name = "add-offer", description = "Add new offer to Stonks.")
public class AddOfferCommand implements AsyncConsoleCommand<Offer> {
	@ParentCommand
	public ConsoleInstance parent;

	@Option(names = { "--uuid", "-U" }, description = "User's unique ID. Leave blank for default UUID.")
	public UUID uuid = Main.DEFAULT_USER_UUID;

	@Parameters(paramLabel = "ID", description = "Product ID.", index = "0")
	public String productId;

	@Parameters(paramLabel = "Type", description = "Offer type.", index = "1")
	public OfferType type;

	@Parameters(paramLabel = "Units", description = "Number of units.", index = "2")
	public int units;

	@Parameters(paramLabel = "Price", description = "Price per unit.", index = "3")
	public double pricePerUnit;

	@Override
	public Task<Offer> getTask() throws Exception {
		return parent.service.queryAllCategories()
			.andThen(categories -> categories.stream()
				.flatMap(category -> category.getProducts().stream())
				.filter(product -> product.getProductId().equals(productId))
				.findAny()
				.map(product -> parent.service.listOffer(uuid, product, type, units, pricePerUnit))
				.orElse(Task.failed(new RemoteServiceException("Product with ID " + productId + " does not exists!"))));
	}

	@Override
	public void onSuccess(Offer obj) throws Exception {
		ConsoleInstance.LOGGER.info("New offer listed: {} {}x {} at ${}/ea",
			obj.getType(),
			obj.getTotalUnits(),
			obj.getProduct().getProductId(),
			obj.getPricePerUnit());
	}

	@Override
	public void onFailure(Throwable t) throws Exception {
		if (!(t instanceof RemoteServiceException)) t.printStackTrace();
		ConsoleInstance.LOGGER.error("Failed to query add offer: " + t.getMessage());
	}
}