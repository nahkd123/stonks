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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import stonks.core.market.Offer;
import stonks.core.market.OfferType;
import stonks.core.product.Product;

public class MessageS2COffersList implements Message {
	public static final String ID = "s2c_offersList";
	private UUID offerer;
	private String errorMessage;
	private List<Offer> offers;

	public MessageS2COffersList(UUID offerer, List<Offer> offers) {
		this.offerer = offerer;
		this.offers = offers;
	}

	public MessageS2COffersList(UUID offerer, String errorMessage) {
		this.offerer = offerer;
		this.errorMessage = errorMessage;
	}

	public MessageS2COffersList(Function<String, Product> products, DataInput input) throws IOException {
		var msb = input.readLong();
		var lsb = input.readLong();
		this.offerer = new UUID(msb, lsb);

		if (!input.readBoolean()) {
			this.errorMessage = input.readUTF();
			return;
		}

		this.offers = new ArrayList<>();
		var count = input.readInt();

		for (int i = 0; i < count; i++) {
			var e = deserializeOffer(products, input);
			if (e == null) {
				this.errorMessage = "Local: One or more product IDs are not populated";
				return;
			}

			this.offers.add(e);
		}
	}

	public static MessageDeserializer createDeserializer(Function<String, Product> products) {
		return input -> new MessageS2COffersList(products, input);
	}

	public UUID getOfferer() { return offerer; }

	@Override
	public String getMessageId() { return ID; }

	public List<Offer> getOffers() { return offers; }

	public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeLong(offerer.getMostSignificantBits());
		output.writeLong(offerer.getLeastSignificantBits());

		if (errorMessage != null) {
			output.writeBoolean(false);
			output.writeUTF(errorMessage);
			return;
		}

		output.writeBoolean(true);
		output.writeInt(offers.size());
		for (var e : offers) serializeOffer(e, output);
	}

	public static void serializeOffer(Offer offer, DataOutput output) throws IOException {
		output.writeLong(offer.getOfferId().getMostSignificantBits());
		output.writeLong(offer.getOfferId().getLeastSignificantBits());
		output.writeLong(offer.getOffererId().getMostSignificantBits());
		output.writeLong(offer.getOffererId().getLeastSignificantBits());
		output.writeUTF(offer.getProduct().getProductId());
		output.writeUTF(offer.getType().toString());
		output.writeInt(offer.getTotalUnits());
		output.writeInt(offer.getClaimedUnits());
		output.writeInt(offer.getFilledUnits());
		output.writeDouble(offer.getPricePerUnit());
	}

	public static Offer deserializeOffer(Function<String, Product> products, DataInput input) throws IOException {
		var offerIdMsb = input.readLong();
		var offerIdLsb = input.readLong();
		var offerId = new UUID(offerIdMsb, offerIdLsb);
		var offererMsb = input.readLong();
		var offererLsb = input.readLong();
		var offerer = new UUID(offererMsb, offererLsb);
		var product = products.apply(input.readUTF());
		if (product == null) return null;
		var type = OfferType.valueOf(input.readUTF().toUpperCase());
		var total = input.readInt();
		var claimed = input.readInt();
		var filled = input.readInt();
		var price = input.readDouble();
		return new Offer(offerId, offerer, product, type, total, claimed, filled, price);
	}
}
