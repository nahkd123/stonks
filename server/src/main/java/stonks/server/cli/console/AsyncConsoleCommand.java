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

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import nahara.common.tasks.AsyncException;
import nahara.common.tasks.Task;

public interface AsyncConsoleCommand<T> extends Callable<Integer> {
	public Task<T> getTask() throws Exception;

	public void onSuccess(T obj) throws Exception;

	public void onFailure(Throwable t) throws Exception;

	public static final DecimalFormat MS_FORMATTER = new DecimalFormat("#,##0.##");

	@Override
	default Integer call() throws Exception {
		long start = System.nanoTime();

		try {
			var obj = getTask().await();
			onSuccess(obj);
		} catch (AsyncException e) {
			onFailure(e.getCause());
		}

		long duration = System.nanoTime() - start;
		ConsoleInstance.LOGGER.info("Task completed in {}ms", MS_FORMATTER.format(duration / 1_000_000d));
		return 0;
	}
}
