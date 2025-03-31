package io.github.nahkd123.stonks.service.provided.remote.message.product;

import java.util.Optional;

import io.github.nahkd123.stonks.service.provided.remote.message.offer.SlippageMessageData;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record InstantSellMessage(String productId, long units, Optional<SlippageMessageData> slippage) implements Message {
	public static final BufferCodec<InstantSellMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.ID, InstantSellMessage::productId,
		BufferCodec.LONG, InstantSellMessage::units,
		SlippageMessageData.CODEC.asOptional(), InstantSellMessage::slippage,
		InstantSellMessage::new);
}
