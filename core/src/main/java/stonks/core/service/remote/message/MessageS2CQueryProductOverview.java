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

import stonks.core.market.OverviewOffer;
import stonks.core.market.ProductMarketOverview;

public class MessageS2CQueryProductOverview implements Message {
	public static final String ID = "s2c_queryProductOverview";
	private String errorMessage;
	private String productId;
	private List<OverviewOffer> buyOffers;
	private List<OverviewOffer> sellOffers;

	public MessageS2CQueryProductOverview(ProductMarketOverview overview) {
		this.productId = overview.getProduct().getProductId();
		this.buyOffers = overview.getBuyOffers().getEntries();
		this.sellOffers = overview.getSellOffers().getEntries();
	}

	public MessageS2CQueryProductOverview(String productId, String errorMessage) {
		this.productId = productId;
		this.errorMessage = errorMessage;
	}

	public MessageS2CQueryProductOverview(DataInput input) throws IOException {
		this.productId = input.readUTF();

		if (!input.readBoolean()) {
			this.errorMessage = input.readUTF();
			return;
		}

		this.buyOffers = new ArrayList<>();
		var buyOffersCount = input.readInt();
		for (int i = 0; i < buyOffersCount; i++) buyOffers.add(deserializeOfferOverview(input));

		this.sellOffers = new ArrayList<>();
		var sellOffersCount = input.readInt();
		for (int i = 0; i < sellOffersCount; i++) sellOffers.add(deserializeOfferOverview(input));
	}

	@Override
	public String getMessageId() { return ID; }

	public String getProductId() { return productId; }

	public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

	public List<OverviewOffer> getBuyOffers() { return buyOffers; }

	public List<OverviewOffer> getSellOffers() { return sellOffers; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeUTF(productId);

		if (errorMessage != null) {
			output.writeBoolean(false);
			output.writeUTF(errorMessage);
			return;
		}

		output.writeBoolean(true);
		output.writeInt(buyOffers.size());
		for (var e : buyOffers) serializeOfferOverview(e, output);
		output.writeInt(sellOffers.size());
		for (var e : sellOffers) serializeOfferOverview(e, output);
	}

	private static void serializeOfferOverview(OverviewOffer entry, DataOutput output) throws IOException {
		output.writeInt(entry.offers());
		output.writeInt(entry.totalAvailableUnits());
		output.writeDouble(entry.pricePerUnit());
	}

	private static OverviewOffer deserializeOfferOverview(DataInput input) throws IOException {
		var offers = input.readInt();
		var totalAvailableUnits = input.readInt();
		var pricePerUnit = input.readDouble();
		return new OverviewOffer(offers, totalAvailableUnits, pricePerUnit);
	}
}
