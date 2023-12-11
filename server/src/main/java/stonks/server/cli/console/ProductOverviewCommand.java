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

import nahara.common.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import stonks.core.market.OverviewOffersList;
import stonks.core.market.ProductMarketOverview;
import stonks.core.service.ServiceException;
import stonks.core.service.remote.RemoteServiceException;

@Command(name = "product", description = "View product overview.")
public class ProductOverviewCommand implements AsyncConsoleCommand<ProductMarketOverview> {
	@ParentCommand
	public ConsoleInstance parent;

	@Parameters(paramLabel = "ID", description = "Product ID.")
	public String productId;

	@Override
	public Task<ProductMarketOverview> getTask() throws Exception {
		return parent.service
			.queryAllCategories()
			.andThen(categories -> categories.stream()
				.flatMap(category -> category.getProducts().stream())
				.filter(product -> product.getProductId().equals(productId))
				.findAny()
				.map(product -> parent.service.queryProductMarketOverview(product))
				.orElse(Task.failed(new RemoteServiceException("Product with ID " + productId + " does not exists!"))));
	}

	@Override
	public void onSuccess(ProductMarketOverview obj) throws Exception {
		ConsoleInstance.LOGGER.info("Product Overview for {}", obj.getProduct().getProductId());
		ConsoleInstance.LOGGER.info("* Product name: {}", obj.getProduct().getProductName());
		ConsoleInstance.LOGGER.info("* Construction data: {}", obj.getProduct().getProductConstructionData());
		ConsoleInstance.LOGGER.info("* Buy offers: {} offers", obj.getBuyOffers().getEntries().size());
		printOffersList(obj.getBuyOffers());
		ConsoleInstance.LOGGER.info("* Sell offers: {} offers", obj.getSellOffers().getEntries().size());
		printOffersList(obj.getSellOffers());
	}

	private void printOffersList(OverviewOffersList list) {
		if (list.getEntries().size() == 0) {
			ConsoleInstance.LOGGER.info("  * No offers!");
			return;
		}

		var computed = list.compute();
		if (computed.isEmpty()) {
			ConsoleInstance.LOGGER.info("  * Failed to compute pricing");
		} else {
			ConsoleInstance.LOGGER.info("  * Avg. {} / Max. {} / Min. {}",
				computed.get().average(), computed.get().max(), computed.get().min());
		}

		for (var e : list.getEntries()) {
			ConsoleInstance.LOGGER.info("  * {} {}x units for ${} in {} offers",
				list.getType(), e.totalAvailableUnits(), e.pricePerUnit(), e.offers());
		}
	}

	@Override
	public void onFailure(Throwable t) throws Exception {
		if (!(t instanceof ServiceException)) t.printStackTrace();
		ConsoleInstance.LOGGER.error("Failed to query product overview: " + t.getMessage());
	}
}
