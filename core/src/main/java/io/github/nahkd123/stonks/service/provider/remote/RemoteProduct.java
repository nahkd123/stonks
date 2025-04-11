/*
 * Copyright (c) 2023-2025 nahkd
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
package io.github.nahkd123.stonks.service.provider.remote;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.InstantBuyRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.InstantSellRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.PlaceOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.product.QueryProductOverview;

record RemoteProduct(RemoteServiceClient client, String id) implements Product {
	@Override
	public String getId() { return id; }

	@Override
	public CompletableFuture<ProductOverview> queryOverview() {
		return client.request(new QueryProductOverview(id));
	}

	@Override
	public CompletableFuture<InstantBuyResult> instantBuy(long balance, long units, SlippageOption slippage) {
		return client.request(new InstantBuyRequest(id, balance, units, slippage));
	}

	@Override
	public CompletableFuture<InstantSellResult> instantSell(long units, SlippageOption slippage) {
		return client.request(new InstantSellRequest(id, units, slippage));
	}

	@Override
	public CompletableFuture<? extends Offer> placeOffer(UUID owner, OfferType type, long price, long units) {
		return client.<PlaceOfferRequest.Response>request(new PlaceOfferRequest(id, owner, type, price, units))
			.thenApply(response -> new RemoteOffer(client, response.offer()));
	}

	@Override
	public final String toString() {
		return "RemoteProduct(%s)".formatted(id);
	}
}
