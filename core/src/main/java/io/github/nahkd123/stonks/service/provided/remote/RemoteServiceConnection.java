package io.github.nahkd123.stonks.service.provided.remote;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ProductOverview;
import io.github.nahkd123.stonks.service.provided.remote.packet.QueryCatalog;
import io.github.nahkd123.stonks.service.provided.remote.packet.StonksBufferCodecs;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.CancelOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.ClaimOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOffer;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryOfferStatus;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.QueryUserOffers;
import io.github.nahkd123.stonks.service.provided.remote.packet.offer.RemoteOfferData;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.InstantBuyRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.InstantSellRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.PlaceOfferRequest;
import io.github.nahkd123.stonks.service.provided.remote.packet.product.QueryProductOverview;
import io.github.nahkd123.transporter.TransporterConnection;

abstract class RemoteServiceConnection extends TransporterConnection {
	private ByteChannel channel;

	public RemoteServiceConnection(ByteChannel channel) {
		this.channel = channel;
		registerPacket(0x0000, QueryCatalog.class, QueryCatalog.CODEC);
		registerPacket(0x0001, QueryCatalog.Response.class, QueryCatalog.Response.CODEC);

		registerPacket(0x0100, QueryProductOverview.class, QueryProductOverview.CODEC);
		registerPacket(0x0101, ProductOverview.class, QueryProductOverview.RESPONSE);
		registerPacket(0x0102, InstantBuyRequest.class, InstantBuyRequest.CODEC);
		registerPacket(0x0103, Product.InstantBuyResult.class, InstantBuyRequest.RESPONSE);
		registerPacket(0x0104, InstantSellRequest.class, InstantSellRequest.CODEC);
		registerPacket(0x0105, Product.InstantSellResult.class, InstantSellRequest.RESPONSE);
		registerPacket(0x0106, PlaceOfferRequest.class, PlaceOfferRequest.CODEC);
		registerPacket(0x0107, PlaceOfferRequest.Response.class, PlaceOfferRequest.Response.CODEC);

		registerPacket(0x0200, QueryOffer.class, QueryOffer.CODEC);
		registerPacket(0x0201, RemoteOfferData.class, RemoteOfferData.CODEC);
		registerPacket(0x0203, QueryUserOffers.class, QueryUserOffers.CODEC);
		registerPacket(0x0204, QueryUserOffers.Response.class, QueryUserOffers.Response.CODEC);
		registerPacket(0x0205, QueryOfferStatus.class, QueryOfferStatus.CODEC);
		registerPacket(0x0206, Offer.Status.class, StonksBufferCodecs.OFFER_STATUS);
		registerPacket(0x0207, ClaimOfferRequest.class, ClaimOfferRequest.CODEC);
		registerPacket(0x0208, CancelOfferRequest.class, CancelOfferRequest.CODEC);
		registerPacket(0x0209, Offer.ClaimResult.class, StonksBufferCodecs.OFFER_CLAIM);
	}

	@Override
	protected ByteBuffer createConnectionBuffer() {
		return ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	protected void onClose(boolean remote, Throwable error) {
		super.onClose(remote, error);

		try {
			if (!remote) channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ByteChannel getChannel() { return channel; }
}
