package stonks.core.service.remote.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MessageC2SQueryProducts implements Message {
	public static final String ID = "c2s_queryProducts";

	public MessageC2SQueryProducts(DataInput input) throws IOException {}

	public MessageC2SQueryProducts() {}

	@Override
	public String getMessageId() { return ID; }

	@Override
	public void onMessageSerialize(DataOutput output) throws IOException {}
}
