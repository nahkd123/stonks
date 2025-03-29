package io.github.nahkd123.stonks.impl.service.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import io.github.nahkd123.stonks.impl.orm.Ordering;
import io.github.nahkd123.stonks.impl.orm.Query;
import io.github.nahkd123.stonks.impl.orm.TableIndex;
import io.github.nahkd123.stonks.impl.orm.TableInfo;
import io.github.nahkd123.stonks.impl.utils.EmitHandler;
import io.github.nahkd123.stonks.logging.Logger;
import io.github.nahkd123.stonks.service.ManagableMarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceConfig;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;

/**
 * <p>
 * An implementation of {@link ManagableMarketService} that uses SQL connection
 * as back-end. You must use {@link #startService()} in order to start the
 * market service; this will spawn a new <em>platform thread</em> that
 * continuously wait for "transactions" (which are requests). Interrupt the
 * thread to stop the service.
 * </p>
 */
public class SqlMarketService extends Thread implements ManagableMarketService {
	// @formatter:off
	public static final TableInfo<ProductRecord> PRODUCTS = new TableInfo<>(
		"Products",
		ProductRecord.RECORD,
		List.of());
	public static final TableInfo<OfferRecord> OFFERS = new TableInfo<>(
		"Offers",
		OfferRecord.RECORD,
		List.of(
			new TableIndex("Offers::Price::Asc", "Price", Ordering.ASCENDING),
			new TableIndex("Offers::Price::Desc", "Price", Ordering.DESCENDING)));
	// @formatter:on

	private Logger logger;
	private Queue<Runnable> transactionQueue = new ConcurrentLinkedQueue<>();
	private CompletableFuture<Void> serviceStartTask = null;
	private Connection sql;
	EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();
	ServiceConfig config = new ServiceConfig(false, 5);

	TableInfo.Select<ProductRecord> selectProductById;
	TableInfo.Select<ProductRecord> selectAllProducts;
	TableInfo.Insert<ProductRecord> insertProduct;
	TableInfo.Update<ProductRecord> deleteProduct;

	TableInfo.Select<OfferRecord> selectOfferById;
	TableInfo.Select<OfferRecord> selectOfferByOwner;
	TableInfo.Select<OfferRecord> selectBuyOffers;
	TableInfo.Select<OfferRecord> selectSellOffers;
	TableInfo.Select<OfferRecord> selectTopBuyOffers;
	TableInfo.Select<OfferRecord> selectTopSellOffers;
	TableInfo.Insert<OfferRecord> insertOffer;
	TableInfo.Update<OfferRecord> updateOffer;
	TableInfo.Update<OfferRecord> deleteOffer;

	public SqlMarketService(Connection sql, Logger logger) {
		this.sql = sql;
		this.logger = logger;
	}

	/**
	 * <p>
	 * Start the service.
	 * </p>
	 * 
	 * @return Async task that will be resolved when the service is started.
	 */
	public CompletableFuture<Void> startService() {
		if (serviceStartTask != null) throw new IllegalStateException("Service already started or is starting");
		serviceStartTask = new CompletableFuture<>();
		start();
		return serviceStartTask;
	}

	@Override
	public void run() {
		logger.info("SQL Market Service thread is starting");

		try {
			init();
			serviceStartTask.complete(null);
		} catch (SQLException e) {
			serviceStartTask.completeExceptionally(e);
			logger.error("Failed to initialize SQL Market Service");
			return;
		}

		while (!Thread.interrupted()) {
			logger.verbose("Processing transactions... (Queue size = %d)".formatted(transactionQueue.size()));

			while (!transactionQueue.isEmpty()) {
				Runnable transaction = transactionQueue.poll();
				transaction.run();
			}

			LockSupport.park();
		}

		logger.info("SQL Market Service is shutting down");
		try {
			onShutdown();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void init() throws SQLException {
		PRODUCTS.migrate(sql);
		selectProductById = PRODUCTS.select(sql);
		selectAllProducts = PRODUCTS.select(sql, null);
		insertProduct = PRODUCTS.insert(sql);
		deleteProduct = PRODUCTS.delete(sql);

		OFFERS.migrate(sql);
		selectOfferById = OFFERS.select(sql);
		selectOfferByOwner = OFFERS.select(sql, Query.ofCondition("Owner=?"));
		selectBuyOffers = OFFERS.select(sql, Query.ofCondition("Type='BUY'").withSorted("Price", Ordering.DESCENDING));
		selectSellOffers = OFFERS.select(sql, Query.ofCondition("Type='SELL'").withSorted("Price", Ordering.ASCENDING));
		selectTopBuyOffers = OFFERS.select(sql, Query
			.ofCondition("Type='BUY'")
			.withSorted("Price", Ordering.DESCENDING)
			.withLimit(config.overviewSamples()));
		selectTopSellOffers = OFFERS.select(sql, Query
			.ofCondition("Type='SELL'")
			.withSorted("Price", Ordering.ASCENDING)
			.withLimit(config.overviewSamples()));
		insertOffer = OFFERS.insert(sql);
		updateOffer = OFFERS.update(sql);
		deleteOffer = OFFERS.delete(sql);
	}

	public void onShutdown() throws SQLException {
		selectProductById.close();
		selectAllProducts.close();
		insertProduct.close();
		deleteProduct.close();

		selectOfferById.close();
		selectOfferByOwner.close();
		selectBuyOffers.close();
		selectSellOffers.close();
		selectTopBuyOffers.close();
		selectTopSellOffers.close();
		insertOffer.close();
		updateOffer.close();
		deleteOffer.close();
	}

	public <T> CompletableFuture<T> queueTransaction(SqlTransaction<T> callback) {
		CompletableFuture<T> task = new CompletableFuture<>();

		if (!transactionQueue.offer(() -> {
			try {
				task.complete(callback.executeTransaction());
			} catch (Throwable t) {
				task.completeExceptionally(t);
			}
		})) {
			task.completeExceptionally(new ServiceException("Transaction queue is full"));
		} else {
			LockSupport.unpark(this);
		}

		return task;
	}

	public CompletableFuture<Void> queueTransaction(SqlTransactionVoid callback) {
		return queueTransaction(() -> {
			callback.executeTransaction();
			return null;
		});
	}

	@Override
	public void addNotificationListener(ServiceNotificationListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeNotificationListener(ServiceNotificationListener listener) {
		listeners.removeListener(listener);
	}

	@Override
	public CompletableFuture<Set<? extends Product>> queryCatalog() {
		return queueTransaction(() -> {
			try (var set = selectAllProducts.query()) {
				Set<SqlProduct> catalog = new HashSet<>();
				for (ProductRecord rec : set) catalog.add(new SqlProduct(this, rec));
				return catalog;
			}
		});
	}

	@Override
	public CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid) {
		return queueTransaction(() -> {
			selectOfferByOwner.statement().setString(1, uuid.toString());

			try (var set = selectOfferByOwner.query()) {
				Set<SqlOffer> offers = new HashSet<>();
				for (OfferRecord rec : set) offers.add(new SqlOffer(this, rec));
				return offers;
			}
		});
	}

	@Override
	public CompletableFuture<? extends Product> createProduct(String id) {
		return queueTransaction(() -> {
			ProductRecord rec = new ProductRecord(id);

			try (var result = selectProductById.query(rec)) {
				if (result.hasNext()) throw new ServiceException("Product with ID %s already exists".formatted(id));
			}

			insertProduct.insert(rec);
			return new SqlProduct(this, rec);
		});
	}

	@Override
	public CompletableFuture<Void> deleteProduct(Product product) {
		if (!(product instanceof SqlProduct(SqlMarketService sv, ProductRecord rec)))
			throw new ServiceException("Not a valid product object obtained from SqlMarketService");
		return queueTransaction(() -> {
			try (var s = sql.prepareStatement(OFFERS.sqlDelete("ProductId=?"))) {
				s.setString(1, rec.id());
				s.executeUpdate();
			}

			deleteProduct.update(rec);
		});
	}

	@Override
	public CompletableFuture<ServiceConfig> queryConfig() {
		return CompletableFuture.completedFuture(config);
	}

	@Override
	public CompletableFuture<Void> useConfig(ServiceConfig config) {
		if (config == null) throw new IllegalArgumentException("config must not be null");

		if (!this.config.equals(config)) try {
			this.config = config;

			selectTopBuyOffers.close();
			selectTopBuyOffers = OFFERS.select(sql, Query
				.ofCondition("Type='BUY'")
				.withSorted("Price", Ordering.DESCENDING)
				.withLimit(config.overviewSamples()));

			selectTopSellOffers.close();
			selectTopSellOffers = OFFERS.select(sql, Query
				.ofCondition("Type='SELL'")
				.withSorted("Price", Ordering.ASCENDING)
				.withLimit(config.overviewSamples()));
		} catch (SQLException e) {
			return CompletableFuture.failedFuture(e);
		}

		return CompletableFuture.completedFuture(null);
	}

	@FunctionalInterface
	public static interface SqlTransaction<T> {
		T executeTransaction() throws SQLException;
	}

	@FunctionalInterface
	public static interface SqlTransactionVoid {
		void executeTransaction() throws SQLException;
	}
}
