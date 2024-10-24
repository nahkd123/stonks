/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.core.market;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import stonks.core.product.Product;

public class Offer {
	private UUID offerId;
	private UUID offerer;
	private Product product;
	private OfferType type;
	private int totalUnits;
	private int claimedUnits;
	private int filledUnits;
	private double pricePerUnit;

	public Offer(UUID offerId, UUID offerer, Product product, OfferType type, int totalUnits, int claimedUnits, int filledUnits, double pricePerUnit) {
		this.offerId = offerId;
		this.offerer = offerer;
		this.product = product;
		this.type = type;
		this.totalUnits = totalUnits;
		this.claimedUnits = claimedUnits;
		this.filledUnits = filledUnits;
		this.pricePerUnit = pricePerUnit;
	}

	public Offer createCopy() {
		return new Offer(offerId, offerer, product, type, totalUnits, claimedUnits, filledUnits, pricePerUnit);
	}

	public UUID getOfferId() { return offerId; }

	public UUID getOffererId() { return offerer; }

	public Product getProduct() { return product; }

	public OfferType getType() { return type; }

	public double getPricePerUnit() { return pricePerUnit; }

	public int getTotalUnits() { return totalUnits; }

	public int getClaimedUnits() { return claimedUnits; }

	public void setClaimedUnits(int claimedUnits) { this.claimedUnits = claimedUnits; }

	public int getFilledUnits() { return filledUnits; }

	public void setFilledUnits(int filledUnits) { this.filledUnits = filledUnits; }

	public int getAvailableUnits() { return getTotalUnits() - getFilledUnits(); }

	public int getAvailableToClaim() { return getFilledUnits() - getClaimedUnits(); }

	public boolean canClaim() {
		return getAvailableToClaim() > 0;
	}

	public boolean isFilled() { return getAvailableUnits() == 0; }

	public boolean isFullyClaimed() { return getClaimedUnits() == getTotalUnits(); }

	public int fillOffer(int units) {
		var total = getTotalUnits();
		var filled = getFilledUnits();
		var toFill = Math.min(total - filled, units);
		setFilledUnits(filled + toFill);
		return units - toFill;
	}

	public int claimOffer() {
		var toClaim = getAvailableToClaim();
		setClaimedUnits(getFilledUnits());
		return toClaim;
	}

	public static final int SERIALIZE_VERSION_NULL = 0;
	public static final int SERIALIZE_VERSION_1 = 1;

	public static void serializeV1(Offer offer, OutputStream stream) throws IOException {
		var data = new DataOutputStream(stream);
		if (offer == null) {
			data.writeInt(SERIALIZE_VERSION_NULL);
			return;
		}

		data.writeInt(SERIALIZE_VERSION_1);
		writeUUID(offer.getOfferId(), data);
		writeUUID(offer.getOffererId(), data);
		data.writeUTF(offer.getProduct().getProductId());
		data.writeUTF(offer.getType().toString());
		data.writeInt(offer.getTotalUnits());
		data.writeInt(offer.getClaimedUnits());
		data.writeInt(offer.getFilledUnits());
		data.writeDouble(offer.getPricePerUnit());
	}

	public static Offer deserialize(Function<String, Optional<Product>> productGetter, InputStream stream) throws IOException {
		var data = new DataInputStream(stream);
		var header = data.readInt();
		if (header == SERIALIZE_VERSION_NULL) return null;
		if (header == SERIALIZE_VERSION_1) return deserializeV1(productGetter, stream, false);
		return null;
	}

	public static Offer deserializeV1(Function<String, Optional<Product>> productGetter, InputStream stream, boolean readHeader) throws IOException {
		var data = new DataInputStream(stream);
		if (readHeader && data.readInt() != SERIALIZE_VERSION_1) return null;
		var offerId = readUUID(data);
		var offerer = readUUID(data);
		var productId = data.readUTF();
		var typeStr = data.readUTF().toUpperCase();
		var totalUnits = data.readInt();
		var claimedUnits = data.readInt();
		var filledUnits = data.readInt();
		var pricePerUnit = data.readDouble();

		var type = OfferType.valueOf(typeStr);
		if (type == null) return null;
		var product = productGetter.apply(productId);
		if (product.isEmpty()) return null;

		return new Offer(offerId, offerer, product.get(), type, totalUnits, claimedUnits, filledUnits, pricePerUnit);
	}

	private static void writeUUID(UUID uuid, DataOutput out) throws IOException {
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
	}

	private static UUID readUUID(DataInput in) throws IOException {
		return new UUID(in.readLong(), in.readLong());
	}

	@Override
	public String toString() {
		return "Offer["
			+ "id=" + offerId + " by " + offerer + ", "
			+ "x" + totalUnits + " " + product + " @ " + pricePerUnit + "/ea, "
			+ "filled=" + filledUnits + ", "
			+ "claimed=" + claimedUnits + "]";
	}
}
