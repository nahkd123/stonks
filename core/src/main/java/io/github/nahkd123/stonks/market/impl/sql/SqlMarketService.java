/*
 * Copyright (c) 2023 nahkd
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
package io.github.nahkd123.stonks.market.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.market.MarketService;
import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.catalogue.Product;
import io.github.nahkd123.stonks.market.catalogue.ProductsCatalogue;
import io.github.nahkd123.stonks.market.helper.InstantOrdersProcessor;
import io.github.nahkd123.stonks.market.helper.MutableOrder;
import io.github.nahkd123.stonks.market.helper.OrdersIterator;
import io.github.nahkd123.stonks.market.helper.ProductSummaryGenerator;
import io.github.nahkd123.stonks.market.impl.queue.QueuedMarketService;
import io.github.nahkd123.stonks.market.result.ClaimResult;
import io.github.nahkd123.stonks.market.result.InstantBuyResult;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.result.SimpleClaimResult;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.utils.Response;
import io.github.nahkd123.stonks.utils.ServiceException;
import io.github.nahkd123.stonks.utils.UncheckedException;
import stonks.core.service.Emittable;

/**
 * <p>
 * A market service that uses SQL database for storing orders data.
 * </p>
 * <p>
 * <b>The operations are synchronous and can block current thread!</b> Although
 * all operations returns {@link CompletableFuture}, it is actually synchronous.
 * If you are looking for asynchronous operations, consider wrapping this
 * service in {@link QueuedMarketService}. {@link QueuedMarketService} will
 * still performs your operations sequentially, but operations are performed on
 * a separate thread that the {@link Executor} belongs to.
 * </p>
 */
public class SqlMarketService implements MarketService {
	private Emittable<OrderInfo> onOrderFilled = new Emittable<>();
	private ProductsCatalogue catalogue;
	private Connection connection;
	private Table<MutableOrder> ordersTable;
	private Map<String, ProductSummaryGenerator> summaries = new WeakHashMap<>();

	public SqlMarketService(ProductsCatalogue catalogue, Connection connection) throws SQLException {
		if (catalogue == null) throw new NullPointerException("catalogue can't be null");
		if (connection == null) throw new NullPointerException("connection can't be null (seriously?)");
		this.catalogue = catalogue;
		this.connection = connection;
		initializeDatabase();
	}

	private void initializeDatabase() throws SQLException {
		ordersTable = new Table<MutableOrder>("orders", MutableOrder::new)
			.setPrimary("id")
			.addColumn("id", ColumnType.UUID, MutableOrder::getOrderId, MutableOrder::setOrderId)
			.addColumn("owner", ColumnType.UUID, MutableOrder::getOwnerUuid, MutableOrder::setOwnerUuid)
			.addColumn("product", ColumnType.varchar(50), MutableOrder::getProductId, MutableOrder::setProductId)
			.addColumn("isBuy", ColumnType.BOOL, MutableOrder::isBuyOffer, MutableOrder::setBuyOffer)
			.addColumn("units", ColumnType.BIGINT, MutableOrder::getTotalUnits, MutableOrder::setTotalUnits)
			.addColumn("pricePerUnit", ColumnType.BIGINT, MutableOrder::getPricePerUnit, MutableOrder::setPricePerUnits)
			.addColumn("filled", ColumnType.BIGINT, MutableOrder::getFilledUnits, MutableOrder::setFilledUnits)
			.addColumn("claimed", ColumnType.BIGINT, MutableOrder::getClaimedUnits, MutableOrder::setClaimedUnits)
			.initializeTable(connection);
	}

	/**
	 * <p>
	 * Update a specific order in the database with new data. If the order is fully
	 * filled and claimed, it will be removed from database.
	 * </p>
	 * <p>
	 * If the order ID does not exists in the database, it will creates a new
	 * record, otherwise it will attempts to update existing record.
	 * </p>
	 * 
	 * @param order The order.
	 * @throws SQLException
	 */
	public void updateOrder(MutableOrder order) throws SQLException {
		if (order.getTotalUnits() == order.getClaimedUnits() && order.getTotalUnits() == order.getFilledUnits()) {
			ordersTable.delete(connection, order);
			return;
		}

		ordersTable.update(connection, order);
		if (order.getFilledUnits() == order.getTotalUnits()) onOrderFilled.emit(order);
	}

	/**
	 * <p>
	 * Filter only orders from specific product and order type. The result set is
	 * sorted in descending order for buy orders and ascending order for sell
	 * orders.
	 * </p>
	 * 
	 * @param product The product.
	 * @param isBuy   true if you want to get all buy orders, otherwise false for
	 *                sell orders.
	 * @return A mapped {@link ResultSet} that maps each record into
	 *         {@link MutableOrder}. Any changes in {@link MutableOrder} will NOT
	 *         reflects back to the database; you'll have to use
	 *         {@link #updateOrder(MutableOrder)} if you want.
	 * @throws SQLException
	 */
	public MappedResultSet<MutableOrder> filterOrders(Product product, boolean isBuy) throws SQLException {
		PreparedStatement s = connection
			.prepareStatement("SELECT * FROM " + ordersTable.getTableName()
				+ " WHERE product=? AND isBuy=? ORDER BY pricePerUnit " + (isBuy ? "DESC" : "ASC"));
		s.setString(1, product.getId());
		s.setBoolean(2, isBuy);
		s.setFetchSize(10);
		return ordersTable.mapResults(s.executeQuery());
	}

	/**
	 * <p>
	 * Create orders iterator that you can iterate the orders in sorted order and
	 * update them if needed.
	 * </p>
	 * 
	 * @param product The product.
	 * @param isBuy   true if you want to get all buy orders, otherwise false for
	 *                sell orders.
	 * @return An iterator.
	 * @throws SQLException
	 */
	public OrdersIterator createIterator(Product product, boolean isBuy) throws SQLException {
		MappedResultSet<MutableOrder> orders = filterOrders(product, isBuy);
		return new OrdersIterator() {
			MutableOrder current = null;
			boolean ended = false;

			@Override
			public void update(OrderInfo newInfo) {
				try {
					updateOrder(newInfo.asMutable());
				} catch (SQLException e) {
					throw new UncheckedException(e);
				}
			}

			@Override
			public OrderInfo next() {
				if (!hasNext()) throw new NoSuchElementException();
				MutableOrder temp = current;
				current = null;
				return temp;
			}

			@Override
			public boolean hasNext() {
				try {
					if (ended) return false;
					if (current != null) return true;

					if (!orders.next()) {
						ended = true;
						return false;
					}

					current = orders.getCurrent();
					return true;
				} catch (SQLException e) {
					throw new UncheckedException(e);
				}
			}

			@Override
			public Product getProduct() { return product; }
		};
	}

	public ProductsCatalogue getCatalogue() { return catalogue; }

	public void setCatalogue(ProductsCatalogue catalogue) {
		if (catalogue == null) throw new NullPointerException("catalogue can't be null");
		this.catalogue = catalogue;
	}

	@Override
	public CompletableFuture<Response<ProductsCatalogue>> queryCatalogue() {
		return CompletableFuture.completedFuture(new Response<>(getCatalogue()));
	}

	@Override
	public CompletableFuture<Response<ProductSummary>> querySummary(Product product) {
		ProductSummaryGenerator generator = summaries.get(product.getId());
		if (generator == null) {
			Supplier<OrdersIterator> buy = () -> {
				try {
					return createIterator(product, true);
				} catch (SQLException e) {
					e.printStackTrace();
					return OrdersIterator.empty(product);
				}
			};
			Supplier<OrdersIterator> sell = () -> {
				try {
					return createIterator(product, false);
				} catch (SQLException e) {
					e.printStackTrace();
					return OrdersIterator.empty(product);
				}
			};

			summaries.put(product.getId(), generator = new ProductSummaryGenerator(product, 1000L, buy, sell));
		}

		return CompletableFuture.completedFuture(new Response<>(generator.generate(5, 5))); // TODO customizable
	}

	@Override
	public CompletableFuture<Response<List<OrderInfo>>> queryOrders(UUID user) {
		try {
			PreparedStatement s = connection
				.prepareStatement("SELECT * FROM " + ordersTable.getTableName() + " WHERE owner=?");
			s.setString(1, user.toString());
			List<MutableOrder> orderInfo = ordersTable.mapResults(s.executeQuery()).collect(Collectors.toList());
			return CompletableFuture.completedFuture(new Response<>(orderInfo.stream()
				.map(v -> (OrderInfo) v)
				.toList()));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> queryOrder(UUID orderId) {
		try {
			PreparedStatement s = connection
				.prepareStatement("SELECT * FROM " + ordersTable.getTableName() + " WHERE id=?");
			s.setString(1, orderId.toString());
			MutableOrder orderInfo = ordersTable.mapResults(s.executeQuery()).first().orElse(null);
			return CompletableFuture.completedFuture(new Response<>(orderInfo));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeBuyOrder(UUID user, Product product, long amount, long pricePerUnit) {
		try {
			MutableOrder order = new MutableOrder(id -> getCatalogue().getProductById(id).orElse(null), UUID
				.randomUUID(), user, product.getId(), true, amount, pricePerUnit, 0L, 0L);
			updateOrder(order);
			return CompletableFuture.completedFuture(new Response<>(order));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Response<OrderInfo>> makeSellOrder(UUID user, Product product, long amount, long pricePerUnit) {
		try {
			MutableOrder order = new MutableOrder(id -> getCatalogue().getProductById(id).orElse(null), UUID
				.randomUUID(), user, product.getId(), false, amount, pricePerUnit, 0L, 0L);
			updateOrder(order);
			return CompletableFuture.completedFuture(new Response<>(order));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Response<ClaimResult>> claimOrder(UUID orderId) {
		return queryOrder(orderId)
			.thenCompose(res -> {
				if (res.data() == null)
					return CompletableFuture.failedStage(new ServiceException("No order with given UUID found"));

				MutableOrder info = res.data().asMutable();
				long newlyClaimed = info.getUnclaimedUnits();
				info.setClaimedUnits(info.getFilledUnits());

				try {
					updateOrder(info);
				} catch (SQLException e) {
					return CompletableFuture.failedStage(e);
				}

				SimpleClaimResult result = new SimpleClaimResult(info.getOwnerUuid(), info
					.getProduct(), orderId, newlyClaimed, newlyClaimed
						* info.getPricePerUnit(), info.getClaimedUnits() == info.getTotalUnits());
				return CompletableFuture.completedStage(new Response<>(result));
			});
	}

	@Override
	public CompletableFuture<Response<InstantBuyResult>> instantBuy(UUID user, Product product, long amount, long balance) {
		try {
			OrdersIterator sellIter = createIterator(product, false);
			return CompletableFuture
				.completedFuture(new Response<>(InstantOrdersProcessor.instantBuy(sellIter, user, amount, balance)));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public CompletableFuture<Response<InstantSellResult>> instantSell(UUID user, Product product, long amount) {
		try {
			OrdersIterator buyIter = createIterator(product, true);
			return CompletableFuture
				.completedFuture(new Response<>(InstantOrdersProcessor.instantSell(buyIter, user, amount)));
		} catch (Exception e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	@Override
	public Emittable<OrderInfo> onOrderFilled() {
		return onOrderFilled;
	}
}
