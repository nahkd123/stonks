package io.github.nahkd123.stonks.service.provided.remote.packet.product;

import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.transporter.serialize.BufferCodec;

public record InstantBuyRequest(String productId, long balance, long units, SlippageOption slippage) {
	public static final BufferCodec<InstantBuyRequest> CODEC = BufferCodec.tupleOf(
		BufferCodec.UTF8, InstantBuyRequest::productId,
		BufferCodec.I64, InstantBuyRequest::balance,
		BufferCodec.I64, InstantBuyRequest::units,
		StonksBufferCodecs.SLIPPAGE, InstantBuyRequest::slippage,
		InstantBuyRequest::new);

	public static final BufferCodec<Product.InstantBuyResult> RESPONSE = BufferCodec.tupleOf(
		BufferCodec.I64, Product.InstantBuyResult::bought,
		BufferCodec.I64, Product.InstantBuyResult::leftoverBalance,
		Product.InstantBuyResult::new);
}
