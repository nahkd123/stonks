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

import stonks.core.market.OfferType;

public class MessageC2SInstantOffer implements Message {
	public static final String ID = "c2s_instantOffer";
	private long responseId;
	private String productId;
	private OfferType type;
	private int units;
	private double balance;

	public MessageC2SInstantOffer(long responseId, String productId, OfferType type, int units, double balance) {
		this.responseId = responseId;
		this.productId = productId;
		this.type = type;
		this.units = units;
		this.balance = balance;
	}

	public MessageC2SInstantOffer(DataInput input) throws IOException {
		this.responseId = input.readLong();
		this.productId = input.readUTF();
		this.type = OfferType.valueOf(input.readUTF().toUpperCase());
		this.units = input.readInt();
		this.balance = input.readDouble();
	}

	@Override
	public String getMessageId() { return ID; }

	public long getResponseId() { return responseId; }

	public String getProductId() { return productId; }

	public OfferType getType() { return type; }

	public int getUnits() { return units; }

	public double getBalance() { return balance; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeLong(responseId);
		output.writeUTF(productId);
		output.writeUTF(type.toString());
		output.writeInt(units);
		output.writeDouble(balance);
	}

}
