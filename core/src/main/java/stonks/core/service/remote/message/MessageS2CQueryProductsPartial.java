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
import java.util.Optional;

import stonks.core.product.Product;

/**
 * <p>
 * This packet uses "partial thing" when sending product declarations to
 * platforms/front-ends.
 * </p>
 * <p>
 * Basically, a single raw packet can only have up to 32768 bytes. If we put all
 * products into a single packet, it would be filled up pretty quickly (assuming
 * you are not using other adapters to save spaces).
 * </p>
 */
public class MessageS2CQueryProductsPartial implements Message {
	public static final String ID = "s2c_queryProductsPartial";
	private String errorMessage;
	private String categoryId;
	private String categoryName;
	private String productId;
	private String productName;
	private String constructionData;
	private boolean isFinished;

	public MessageS2CQueryProductsPartial(String categoryId, String categoryName, String productId, String productName, String constructionData, boolean isFinished) {
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.productId = productId;
		this.productName = productName;
		this.constructionData = constructionData;
		this.isFinished = isFinished;
	}

	public MessageS2CQueryProductsPartial(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public MessageS2CQueryProductsPartial(Product product, boolean isFinished) {
		this.categoryId = product.getCategory().getCategoryId();
		this.categoryName = product.getCategory().getCategoryName();
		this.productId = product.getProductId();
		this.productName = product.getProductName();
		this.constructionData = product.getProductConstructionData();
		this.isFinished = isFinished;
	}

	public MessageS2CQueryProductsPartial(DataInput input) throws IOException {
		if (!input.readBoolean()) {
			this.errorMessage = input.readUTF();
			return;
		}

		this.categoryId = input.readUTF();
		this.categoryName = input.readUTF();
		this.productId = input.readUTF();
		this.productName = input.readUTF();
		this.constructionData = input.readUTF();
		this.isFinished = input.readBoolean();
	}

	public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

	public String getCategoryId() { return categoryId; }

	public String getCategoryName() { return categoryName; }

	public String getProductId() { return productId; }

	public String getProductName() { return productName; }

	public String getConstructionData() { return constructionData; }

	public boolean isFinished() { return isFinished; }

	@Override
	public String getMessageId() { return ID; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		if (errorMessage != null) {
			output.writeBoolean(false);
			output.writeUTF(errorMessage);
			return;
		}

		output.writeBoolean(true);
		output.writeUTF(categoryId);
		output.writeUTF(categoryName);
		output.writeUTF(productId);
		output.writeUTF(productName);
		output.writeUTF(constructionData);
		output.writeBoolean(isFinished);
	}
}
