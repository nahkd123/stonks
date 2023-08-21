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

import java.util.List;

import nahara.common.tasks.Task;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import stonks.core.product.Category;
import stonks.core.service.remote.RemoteServiceException;

@Command(name = "list-products", description = "List all products from service.")
public class ListProductsCommand implements AsyncConsoleCommand<List<Category>> {
	@ParentCommand
	public ConsoleInstance parent;

	@Override
	public Task<List<Category>> getTask() throws Exception { return parent.service.queryAllCategories(); }

	@Override
	public void onSuccess(List<Category> obj) throws Exception {
		ConsoleInstance.LOGGER.info("Found {} categories:", obj.size());

		for (var category : obj) {
			ConsoleInstance.LOGGER.info("* Category: {} (Name = {})", category.getCategoryId(),
				category.getCategoryName());
			for (var product : category.getProducts()) {
				ConsoleInstance.LOGGER.info("  * Product: {} (Name = {})", product.getProductId(),
					product.getProductName());
			}
		}
	}

	@Override
	public void onFailure(Throwable t) throws Exception {
		if (!(t instanceof RemoteServiceException)) t.printStackTrace();
		ConsoleInstance.LOGGER.error("Failed to query categories: " + t.getMessage());
	}
}
