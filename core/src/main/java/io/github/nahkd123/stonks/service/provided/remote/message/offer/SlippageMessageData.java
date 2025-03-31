package io.github.nahkd123.stonks.service.provided.remote.message.offer;

import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.Product.SlippageOption;
import io.github.nahkd123.stonks.utils.net.BufferCodec;

public record SlippageMessageData(long targetPrice, double maxRate) {
	public static final BufferCodec<SlippageMessageData> CODEC = BufferCodec.ofTuple(
		BufferCodec.LONG, SlippageMessageData::targetPrice,
		BufferCodec.DOUBLE, SlippageMessageData::maxRate,
		SlippageMessageData::new);

	public SlippageMessageData(Product.SlippageOption option) {
		this(option.targetPrice(), option.maxRate());
	}

	public Product.SlippageOption option() {
		return new SlippageOption(targetPrice, maxRate);
	}
}
