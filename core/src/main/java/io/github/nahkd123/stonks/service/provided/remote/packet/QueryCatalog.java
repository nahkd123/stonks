package io.github.nahkd123.stonks.service.provided.remote.packet;

import java.util.List;

import io.github.nahkd123.transporter.serialize.BufferCodec;

public record QueryCatalog() {
	public static final BufferCodec<QueryCatalog> CODEC = BufferCodec.of((v, b) -> {}, b -> new QueryCatalog());

	public static record Response(List<String> productIds) {
		public static final BufferCodec<Response> CODEC = BufferCodec.UTF8
			.asVarSequence(BufferCodec.VUINT)
			.map(Response::new, Response::productIds);
	}
}
