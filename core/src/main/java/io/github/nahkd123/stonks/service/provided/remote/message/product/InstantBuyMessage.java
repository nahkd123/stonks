package io.github.nahkd123.stonks.service.provided.remote.message.product;

import java.util.Optional;

import io.github.nahkd123.stonks.service.provided.remote.message.offer.SlippageMessageData;
import io.github.nahkd123.stonks.utils.net.BufferCodec;
import io.github.nahkd123.stonks.utils.net.Message;

public record InstantBuyMessage(String productId, long balance, long units, Optional<SlippageMessageData> slippage) implements Message {
	public static final BufferCodec<InstantBuyMessage> CODEC = BufferCodec.ofTuple(
		BufferCodec.ID, InstantBuyMessage::productId,
		BufferCodec.LONG, InstantBuyMessage::balance,
		BufferCodec.LONG, InstantBuyMessage::units,
		SlippageMessageData.CODEC.asOptional(), InstantBuyMessage::slippage,
		InstantBuyMessage::new);
}
