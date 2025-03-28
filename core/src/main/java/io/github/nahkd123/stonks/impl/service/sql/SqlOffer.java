package io.github.nahkd123.stonks.impl.service.sql;

import static io.github.nahkd123.stonks.impl.service.sql.SqlMarketService.OFFERS;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.service.Offer;
import io.github.nahkd123.stonks.service.OfferType;
import io.github.nahkd123.stonks.service.Product;
import io.github.nahkd123.stonks.service.ServiceException;

class SqlOffer implements Offer {
	private SqlMarketService service;
	private OfferRecord rec;
	private long lastFilled;
	private long lastClaimed;
	private boolean removed = false;

	public SqlOffer(SqlMarketService service, OfferRecord rec) {
		this.service = service;
		this.rec = rec;
		this.lastFilled = rec.filledUnits();
		this.lastClaimed = rec.claimedUnits();
	}

	@Override
	public UUID id() {
		return rec.id();
	}

	@Override
	public UUID owner() {
		return rec.owner();
	}

	@Override
	public OfferType type() {
		return rec.type();
	}

	@Override
	public Product product() {
		return new SqlProduct(service, new ProductRecord(rec.productId()));
	}

	@Override
	public long price() {
		return rec.price();
	}

	@Override
	public long totalUnits() {
		return rec.totalUnits();
	}

	@Override
	public CompletableFuture<Status> queryStatus() {
		if (removed) return CompletableFuture.completedFuture(new Offer.Status(lastFilled, lastClaimed, true));

		return service.queueTransaction(() -> {
			try (var s = service.sql.prepareStatement("select * from %s where Id=?".formatted(OFFERS.name()))) {
				s.setString(1, id().toString());

				try (var set = s.executeQuery()) {
					if (!set.next()) {
						removed = true;
						return new Offer.Status(lastFilled, lastClaimed, true);
					}

					rec = OfferRecord.RECORD.getFrom(set);
					lastFilled = rec.filledUnits();
					lastClaimed = rec.claimedUnits();
					return new Offer.Status(lastFilled, lastClaimed, false);
				}
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<ClaimResult> claimOffer() {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		return service.queueTransaction(() -> {
			try (var s = service.sql.prepareStatement("select * from %s where Id=?".formatted(OFFERS.name()))) {
				s.setString(1, id().toString());

				try (var set = s.executeQuery()) {
					if (!set.next()) {
						removed = true;
						return new Offer.ClaimResult(0L, true);
					}

					rec = OfferRecord.RECORD.getFrom(set);
					lastFilled = rec.filledUnits();
					lastClaimed = rec.claimedUnits();
				}

				long toClaim = lastFilled - lastClaimed;
				rec = rec.withClaimedUnits(lastFilled);

				if (rec.claimedUnits() >= rec.totalUnits()) {
					removeOffer();
					return new Offer.ClaimResult(toClaim, true);
				} else {
					String sqlCode = "update %s set ClaimedUnits=? where Id=?".formatted(OFFERS.name());

					try (var upd = service.sql.prepareStatement(sqlCode)) {
						upd.setLong(1, rec.claimedUnits());
						upd.setString(2, rec.id().toString());
						upd.execute();
					}

					return new Offer.ClaimResult(toClaim, false);
				}
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	@Override
	public CompletableFuture<ClaimResult> cancelOffer() {
		if (service.config.lockdown()) return CompletableFuture.failedFuture(new ServiceException("Lockdown"));
		if (removed) return CompletableFuture.failedFuture(new ServiceException("Offer no longer exist"));
		return service.queueTransaction(() -> {
			try (var s = service.sql.prepareStatement("select * from %s where Id=?".formatted(OFFERS.name()))) {
				s.setString(1, id().toString());

				try (var set = s.executeQuery()) {
					if (!set.next()) {
						removed = true;
						return new Offer.ClaimResult(0L, true);
					}

					rec = OfferRecord.RECORD.getFrom(set);
					lastFilled = rec.filledUnits();
					lastClaimed = rec.claimedUnits();
				}

				long toClaim = lastFilled - lastClaimed;
				rec = rec.withClaimedUnits(lastFilled);
				removeOffer();
				return new Offer.ClaimResult(toClaim, true);
			} catch (SQLException e) {
				throw new ServiceException("Internal error", e);
			}
		});
	}

	private void removeOffer() {
		String sqlCode = "delete from %s where Id=?".formatted(SqlMarketService.OFFERS.name());
		try (var s = service.sql.prepareStatement(sqlCode)) {
			s.setString(1, rec.id().toString());
			s.execute();
			removed = true;
		} catch (SQLException e) {
			throw new ServiceException("Internal error", e);
		}
	}
}
