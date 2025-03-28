package io.github.nahkd123.stonks.impl.service.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	public static final TableInfo<ProductRecord> PRODUCTS = new TableInfo<>("Products", ProductRecord.RECORD);
	public static final TableInfo<OfferRecord> OFFERS = new TableInfo<>("Offers", OfferRecord.RECORD);

	private Logger logger;
	private Queue<Runnable> transactionQueue = new ConcurrentLinkedQueue<>();
	private CompletableFuture<Void> serviceStartTask = null;
	EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();
	Connection sql;
	ServiceConfig config = new ServiceConfig(false, 5);

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
	}

	private void init() throws SQLException {
		PRODUCTS.migrate(sql);
		OFFERS.migrate(sql);
	}

	public <T> CompletableFuture<T> queueTransaction(Supplier<T> callback) {
		CompletableFuture<T> task = new CompletableFuture<>();

		if (!transactionQueue.offer(() -> {
			try {
				task.complete(callback.get());
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
			try (var s = sql.createStatement();
				var set = s.executeQuery("select * from %s".formatted(PRODUCTS.name()))) {
				List<ProductRecord> list = new ArrayList<>();
				while (set.next()) list.add(ProductRecord.RECORD.getFrom(set));
				return list.stream()
					.map(ref -> new SqlProduct(this, ref))
					.collect(Collectors.toUnmodifiableSet());
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<Set<? extends Offer>> queryUserOffers(UUID uuid) {
		return queueTransaction(() -> {
			try (var s = sql.prepareStatement("select * from %s where Owner=?".formatted(OFFERS.name()))) {
				s.setString(1, uuid.toString());
				List<SqlOffer> offers = new ArrayList<>();

				try (var set = s.executeQuery()) {
					while (set.next()) {
						OfferRecord rec = OfferRecord.RECORD.getFrom(set);
						offers.add(new SqlOffer(this, rec));
					}
				}

				return Set.copyOf(offers);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<? extends Product> createProduct(String id) {
		return queueTransaction(() -> {
			try (var s = sql.prepareStatement("select * from %s where Id=?".formatted(PRODUCTS.name()))) {
				s.setString(1, id);

				try (var set = s.executeQuery()) {
					if (set.next()) throw new ServiceException("Product with ID %s already exists".formatted(id));
				}

				ProductRecord rec = new ProductRecord(id);
				PRODUCTS.insert(sql, rec);
				return new SqlProduct(this, rec);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<Void> deleteProduct(Product product) {
		return queueTransaction(() -> {
			try (var s = sql.prepareStatement("select * from %s where Id=?".formatted(PRODUCTS.name()))) {
				s.setString(1, product.getId());

				try (var set = s.executeQuery()) {
					if (!set.next()) throw new ServiceException("Product with ID %s does not exists"
						.formatted(product.getId()));
				}

				// Delete all offers
				try (var del = sql.prepareStatement("delete from %s where ProductId=?".formatted(OFFERS.name()))) {
					del.setString(1, product.getId());
					del.execute();
				}

				// Delete product
				ProductRecord rec = new ProductRecord(product.getId());
				PRODUCTS.delete(sql, rec);
				return null;
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<ServiceConfig> queryConfig() {
		return CompletableFuture.completedFuture(config);
	}

	@Override
	public CompletableFuture<Void> useConfig(ServiceConfig config) {
		if (config == null) throw new IllegalArgumentException("config must not be null");
		this.config = config;
		return CompletableFuture.completedFuture(null);
	}
}
