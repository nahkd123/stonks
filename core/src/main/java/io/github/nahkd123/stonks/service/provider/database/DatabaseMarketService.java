package io.github.nahkd123.stonks.service.provider.database;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.nahkd123.stonks.service.ManagableMarketService;
import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceConfig;
import io.github.nahkd123.stonks.service.ServiceException;
import io.github.nahkd123.stonks.service.ServiceNotificationListener;
import io.github.nahkd123.stonks.utils.EmitHandler;
import io.github.nahkd123.tableschema.Database;
import io.github.nahkd123.tableschema.Table;
import io.github.nahkd123.tableschema.query.Filter;

public class DatabaseMarketService extends Thread implements ManagableMarketService {
	private Callable<Database> db;
	private List<DatabaseServiceOption> options;

	private CompletableFuture<Void> startTask = null;
	private Queue<Request<?>> requestQueue;
	private Map<String, DatabaseProduct> productCache;

	// Shared
	EmitHandler<ServiceNotificationListener> listeners = new EmitHandler<>();
	ServiceConfig config;
	Table<String, ProductData> products;
	Table<UUID, OfferData> offers;

	public DatabaseMarketService(Callable<Database> db, ServiceConfig config, DatabaseServiceOption... options) {
		this.db = db;
		this.config = config;
		this.options = List.of(options);
	}

	public CompletableFuture<Void> startServiceThread() {
		if (requestQueue != null) throw new ServiceException("Service is already running on another thread");
		startTask = new CompletableFuture<>();
		start();
		return startTask;
	}

	@Override
	public void run() {
		if (startTask == null) throw new IllegalStateException("Must be started with startServiceThread()");
		if (requestQueue != null) throw new ServiceException("Service is already running on another thread");
		Database db;

		try {
			db = this.db.call();
		} catch (Exception e) {
			throw new ServiceException("Unable to create database interface");
		}

		boolean makeBackup = options.contains(DatabaseServiceOption.Standard.BACKUP_ON_MIGRATE);
		products = db.table("Products", ProductData.SCHEMA);
		products.migrate(makeBackup);
		offers = db.table("Offers", OfferData.SCHEMA);
		offers.migrate(makeBackup);
		requestQueue = new ConcurrentLinkedQueue<>();
		productCache = new WeakHashMap<>();
		startTask.complete(null);

		while (!Thread.interrupted()) {
			Request<?> request = requestQueue.poll();

			if (request != null) {
				if (config.lockdown()) request.task.completeExceptionally(new ServiceException("Lockdown mode"));
				else request.performRequest();
			}

			else LockSupport.park();
		}

		while (!requestQueue.isEmpty()) {
			requestQueue.poll().task.completeExceptionally(new ServiceException("Service is shutting down"));
		}

		products = null;
		offers = null;
		requestQueue = null;
		productCache = null;
		db.close();
	}

	private record Request<T>(CompletableFuture<T> task, Callable<T> callback) {
		public void performRequest() {
			try {
				task.complete(callback.call());
			} catch (Throwable t) {
				task.completeExceptionally(t);
			}
		}
	}

	<T> CompletableFuture<T> queueTask(Callable<T> callable, boolean bypassLockdown) {
		CompletableFuture<T> task = new CompletableFuture<>();

		if (requestQueue == null) {
			task.completeExceptionally(new ServiceException("Service is not started"));
			return task;
		}

		if (config.lockdown() && !bypassLockdown) {
			task.completeExceptionally(new ServiceException("Lockdown mode"));
			return task;
		}

		if (!requestQueue.offer(new Request<>(task, callable))) {
			task.completeExceptionally(new ServiceException("Service is overloaded"));
			return task;
		}

		LockSupport.unpark(this);
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

	private Set<DatabaseProduct> queryCatalogSync() {
		Set<DatabaseProduct> catalog = products.query(null, null)
			.asList().stream()
			.map(d -> new DatabaseProduct(this, d))
			.collect(Collectors.toUnmodifiableSet());
		productCache.clear();
		productCache.putAll(catalog.stream().collect(Collectors.toMap(
			DatabaseProduct::getId,
			Function.identity())));
		return catalog;
	}

	private DatabaseProduct getProductByIdSync(String id) {
		DatabaseProduct product = productCache.get(id);

		if (product == null) {
			Set<DatabaseProduct> set = queryCatalogSync();
			product = set.stream().filter(p -> p.getId().equals(id)).findAny().orElse(null);
			if (product == null) throw new ServiceException("No product with ID %s".formatted(id));
		}

		return product;
	}

	@Override
	public CompletableFuture<Set<? extends Product>> queryCatalog() {
		return queueTask(this::queryCatalogSync, false);
	}

	@Override
	public CompletableFuture<List<? extends Offer>> queryUserOffers(UUID uuid) {
		return queueTask(() -> offers.query(Filter.eq(OfferData.OWNER, uuid), null)
			.asList().stream()
			.map(o -> new DatabaseOffer(this, o, getProductByIdSync(o.productId())))
			.toList(), false);
	}

	@Override
	public CompletableFuture<? extends Offer> queryOffer(UUID id) {
		return queueTask(() -> {
			OfferData data = offers.query(id).first();
			if (data == null) throw new ServiceException("Offer with ID %s not found".formatted(id));
			return new DatabaseOffer(this, data, getProductByIdSync(data.productId()));
		}, false);
	}

	@Override
	public CompletableFuture<? extends Product> createProduct(String id) {
		return queueTask(() -> {
			ProductData data = new ProductData(id);

			if (products.insert(data)) {
				Set<DatabaseProduct> catalog = queryCatalogSync();
				listeners.beginEmit(l -> l.onCatalogUpdate(this, catalog));
				return new DatabaseProduct(this, data);
			}

			throw new ServiceException("Failed to create product with ID %s".formatted(id));
		}, true);
	}

	@Override
	public CompletableFuture<Void> deleteProduct(Product product) {
		return queueTask(() -> {
			offers.delete(Filter.eq(OfferData.PRODUCT_ID, product.getId()));

			if (products.delete(product.getId())) {
				Set<DatabaseProduct> catalog = queryCatalogSync();
				listeners.beginEmit(l -> l.onCatalogUpdate(this, catalog));
			}

			return null;
		}, true);
	}

	@Override
	public CompletableFuture<ServiceConfig> queryConfig() {
		return CompletableFuture.completedFuture(config);
	}

	@Override
	public CompletableFuture<Void> useConfig(ServiceConfig config) {
		if (config == null) return CompletableFuture.failedFuture(new ServiceException("Config must not be null"));
		this.config = config;
		return CompletableFuture.completedFuture(null);
	}
}
