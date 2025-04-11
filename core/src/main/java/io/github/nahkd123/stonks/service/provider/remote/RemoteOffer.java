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
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.CancelOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.ClaimOfferRequest;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.QueryOfferStatus;
import io.github.nahkd123.stonks.service.provider.remote.packet.offer.RemoteOfferData;

record RemoteOffer(RemoteServiceClient client, RemoteOfferData data) implements Offer {
	@Override
	public UUID id() {
		return data.id();
	}

	@Override
	public UUID owner() {
		return data.owner();
	}

	@Override
	public OfferType type() {
		return data.type();
	}

	@Override
	public Product product() {
		return new RemoteProduct(client, data.productId());
	}

	@Override
	public long price() {
		return data.price();
	}

	@Override
	public long totalUnits() {
		return data.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		return client.request(new QueryOfferStatus(data.id()));
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		return client.request(new ClaimOfferRequest(data.id()));
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		return client.request(new CancelOfferRequest(data.id()));
	}
}
