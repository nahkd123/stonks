package io.github.nahkd123.stonks.service.provided.remote.packet.product;

import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record InstantSellRequest(String productId, long units, SlippageOption slippage) {
	public static final BufferCodec<InstantSellRequest> CODEC = BufferCodec.tupleOf(
		BufferCodec.UTF8, InstantSellRequest::productId,
		BufferCodec.I64, InstantSellRequest::units,
		StonksBufferCodecs.SLIPPAGE, InstantSellRequest::slippage,
		InstantSellRequest::new);

	public static final BufferCodec<Product.InstantSellResult> RESPONSE = BufferCodec.tupleOf(
		BufferCodec.I64, Product.InstantSellResult::earning,
		BufferCodec.I64, Product.InstantSellResult::leftoverUnits,
		Product.InstantSellResult::new);
}
