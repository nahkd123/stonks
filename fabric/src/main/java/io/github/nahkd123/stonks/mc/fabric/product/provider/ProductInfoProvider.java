package io.github.nahkd123.stonks.mc.fabric.product.provider;

import com.mojang.serialization.MapCodec;

import io.github.nahkd123.stonks.mc.fabric.product.ProductInfo;

public interface ProductInfoProvider<I extends ProductInfo> {
	String getTypeName();

	Class<I> getInfoClass();

	MapCodec<I> getCodec();
}
