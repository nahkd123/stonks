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
import java.util.UUID;

public class MessageC2SOfferOption implements Message {
	public static enum Type {
		CLAIM,
		CANCEL;
	}

	public static final String ID = "c2s_offerOption";
	private long responseId;
	private UUID userId;
	private UUID offerId;
	private Type type;

	public MessageC2SOfferOption(long responseId, UUID userId, UUID offerId, Type type) {
		this.responseId = responseId;
		this.userId = userId;
		this.offerId = offerId;
		this.type = type;
	}

	public MessageC2SOfferOption(DataInput input) throws IOException {
		this.responseId = input.readLong();
		long userMsb = input.readLong(), userLsb = input.readLong();
		long offerMsb = input.readLong(), offerLsb = input.readLong();
		this.userId = new UUID(userMsb, userLsb);
		this.offerId = new UUID(offerMsb, offerLsb);
		this.type = Type.valueOf(input.readUTF().toUpperCase());
	}

	@Override
	public String getMessageId() { return ID; }

	public long getResponseId() { return responseId; }

	public UUID getUserId() { return userId; }

	public UUID getOfferId() { return offerId; }

	public Type getType() { return type; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeLong(responseId);
		output.writeLong(userId.getMostSignificantBits());
		output.writeLong(userId.getLeastSignificantBits());
		output.writeLong(offerId.getMostSignificantBits());
		output.writeLong(offerId.getLeastSignificantBits());
		output.writeUTF(type.toString());
	}
}
