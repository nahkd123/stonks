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
package stonks.core.service.remote.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import stonks.core.product.Product;

public class MessageC2SQueryProductOverview implements Message {
	public static final String ID = "c2s_queryProductOverview";
	private String productId;

	public MessageC2SQueryProductOverview(String productId) {
		this.productId = productId;
	}

	public MessageC2SQueryProductOverview(Product product) {
		this.productId = product.getProductId();
	}

	public MessageC2SQueryProductOverview(DataInput input) throws IOException {
		this.productId = input.readUTF();
	}

	@Override
	public String getMessageId() { return ID; }

	public String getProductId() { return productId; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeUTF(productId);
	}
}
