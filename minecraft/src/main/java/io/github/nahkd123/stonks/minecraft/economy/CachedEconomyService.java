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
package io.github.nahkd123.stonks.minecraft.economy;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import io.github.nahkd123.stonks.utils.lazy.LazyLoader;

/**
 * <p>
 * The cached version of {@link EconomyService}, using {@link WeakHashMap} as
 * cache. The cached version will try to return the player's balance
 * immediately, and it will only request balance query if the cached value does
 * not exists or it has been garbage collected.
 * </p>
 */
public class CachedEconomyService implements EconomyService {
	private EconomyService service;
	private Map<UUID, Long> cachedBalance = new WeakHashMap<>();

	public CachedEconomyService(EconomyService service) {
		this.service = service;
	}

	public EconomyService getService() { return service; }

	@Override
	public TextComponent getDisplayName() { return service.getDisplayName(); }

	@Override
	public Currency getCurrency() { return service.getCurrency(); }

	@Override
	public CompletableFuture<Long> getBalance(Player player) {
		return service.getBalance(player).thenApply(v -> {
			cachedBalance.put(player.getUuid(), v);
			return v;
		});
	}

	public LazyLoader<Long> getCachedBalance(Player player) {
		Long balance = cachedBalance.get(player.getUuid());
		if (balance != null) return LazyLoader.ofFinished(balance);
		return LazyLoader.wrap(getBalance(player));
	}

	@Override
	public CompletableFuture<Transaction> depositTo(Player player, long amount) {
		return service.depositTo(player, amount).thenApply(v -> {
			cachedBalance.put(player.getUuid(), v.getNewBalance());
			return v;
		});
	}

	@Override
	public CompletableFuture<Transaction> withdrawFrom(Player player, long amount) {
		return service.withdrawFrom(player, amount).thenApply(v -> {
			cachedBalance.put(player.getUuid(), v.getNewBalance());
			return v;
		});
	}
}
