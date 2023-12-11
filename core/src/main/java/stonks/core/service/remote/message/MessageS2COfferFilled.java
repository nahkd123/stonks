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
import java.util.function.Function;

import stonks.core.market.Offer;
import stonks.core.product.Product;

public class MessageS2COfferFilled implements Message {
	public static final String ID = "s2c_offerFilled";
	private Offer offer;

	public MessageS2COfferFilled(Offer offer) {
		this.offer = offer;
	}

	public MessageS2COfferFilled(Function<String, Product> products, DataInput input) throws IOException {
		this.offer = MessageS2COffersList.deserializeOffer(products, input);
	}

	public static MessageDeserializer createDeserializer(Function<String, Product> products) {
		return input -> new MessageS2COfferFilled(products, input);
	}

	@Override
	public String getMessageId() { return ID; }

	public Offer getOffer() { return offer; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		MessageS2COffersList.serializeOffer(offer, output);
	}
}
