package stonks.core.service.remote.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public class MessageC2SGetOffers implements Message {
	public static final String ID = "c2s_getOffers";
	private UUID offerer;

	public MessageC2SGetOffers(UUID offerer) {
		this.offerer = offerer;
	}

	public MessageC2SGetOffers(DataInput input) throws IOException {
		var msb = input.readLong();
		var lsb = input.readLong();
		this.offerer = new UUID(msb, lsb);
	}

	@Override
	public String getMessageId() { return ID; }

	public UUID getOfferer() { return offerer; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {
		output.writeLong(offerer.getMostSignificantBits());
		output.writeLong(offerer.getLeastSignificantBits());
	}
}
