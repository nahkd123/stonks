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

import stonks.core.exec.InstantOfferExecuteResult;

public class MessageS2CInstantOffer implements Message {
	public static final String ID = "s2c_instantOffer";
	private long responseId;
	private String errorMessage;
	private int units;
	private double balance;

	public MessageS2CInstantOffer(long responseId, int units, double balance) {
		this.responseId = responseId;
		this.units = units;
		this.balance = balance;
	}

	public MessageS2CInstantOffer(long responseId, String errorMessage) {
		this.responseId = responseId;
		this.errorMessage = errorMessage;
	}

	public MessageS2CInstantOffer(DataInput input) throws IOException {
		this.responseId = input.readLong();

		if (!input.readBoolean()) {
			this.errorMessage = input.readUTF();
			return;
		}

		this.units = input.readInt();
		this.balance = input.readDouble();
	}

	@Override
	public String getMessageId() { return ID; }

	public long getResponseId() { return responseId; }

	public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

	public InstantOfferExecuteResult getResult() { return new InstantOfferExecuteResult(units, balance); }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeLong(responseId);

		if (errorMessage != null) {
			output.writeBoolean(false);
			output.writeUTF(errorMessage);
			return;
		}

		output.writeBoolean(true);
		output.writeInt(units);
		output.writeDouble(balance);
	}
}
