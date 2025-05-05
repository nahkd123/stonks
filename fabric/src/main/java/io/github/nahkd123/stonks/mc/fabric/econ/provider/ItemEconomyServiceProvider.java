package io.github.nahkd123.stonks.mc.fabric.econ.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.econ.Transaction;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.ItemEconomyServiceProvider.Config;
import io.github.nahkd123.stonks.mc.fabric.econ.provider.ItemEconomyServiceProvider.Config.ItemEntry;
import io.github.nahkd123.stonks.service.ServiceException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemEconomyServiceProvider implements EconomyServiceProvider<Config> {
	@Override
	public String getProviderName() { return "item"; }

	@Override
	public MapCodec<Config> getConfigCodec() { return Config.MAP_CODEC; }

	record Config(List<ItemEntry> items) {
		record ItemEntry(ItemStack stack, List<String> name, int worth) {
			static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
				ItemStack.MAP_CODEC.forGetter(ItemEntry::stack),
				Codec.STRING.listOf().optionalFieldOf("name", List.of()).forGetter(ItemEntry::name),
				Codec.INT.optionalFieldOf("worth", 1).forGetter(ItemEntry::worth))
				.apply(i, ItemEntry::new));
		}

		static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ItemEntry.CODEC.listOf().fieldOf("items").forGetter(Config::items))
			.apply(i, Config::new));
	}

	@Override
	public EconomyService createService(MinecraftServer server, Config config) {
		TreeMap<Integer, ItemEntry> map = new TreeMap<>((a, b) -> b.compareTo(a));
		config.items.forEach(e -> map.put(e.worth, e));
		return new ServiceImpl(server, map);
	}

	private class ServiceImpl implements EconomyService {
		private MinecraftServer server;
		private TreeMap<Integer, ItemEntry> entries;
		private Map<String, Long> nameToValue = new HashMap<>();

		public ServiceImpl(MinecraftServer server, TreeMap<Integer, ItemEntry> entries) {
			this.server = server;
			this.entries = entries;

			for (Entry<Integer, ItemEntry> entry : entries.entrySet()) {
				for (String name : entry.getValue().name) nameToValue.put(name, entry.getKey().longValue());
			}
		}

		@Override
		public String formatCurrency(long amount) {
			List<String> result = new ArrayList<>();

			for (Entry<Integer, ItemEntry> entry : entries.entrySet()) {
				long items = amount / entry.getKey();
				amount %= entry.getKey();
				if (items > 0L) result.add(items + " " + entry.getValue().name().get(0));
			}

			if (result.size() == 0) return "0 " + entries.lastEntry().getValue().name().get(0);
			return String.join(", ", result);
		}

		@Override
		public long parseCurrency(String formatted) throws IllegalArgumentException {
			long sum = 0L;
			long input = 0L;
			char[] cs = formatted.toCharArray();
			int mode = 0; // 0: reading first digit - 1: reading next digits - 1: reading unit
			String unit = null;

			charIterator: for (int i = 0; i < cs.length; i++) {
				char ch = cs[i];

				switch (mode) {
				case 0, 1:
					if (Character.isWhitespace(ch) || ch == '.' || ch == ',' || ch == '_') continue;

					if (ch >= '0' && ch <= '9') {
						input *= 10;
						input += ch - '0';
						mode = 1;
						continue;
					}

					for (String name : nameToValue.keySet()) {
						if (name.charAt(0) == ch) {
							if (mode == 0) input = 1L;
							mode = 2;
							unit = String.valueOf(ch);
							continue charIterator;
						}
					}

					throw new IllegalArgumentException("Unexpected character: '%s'".formatted(ch));
				case 2:
					if (Character.isWhitespace(ch) || ch == ',' || (ch >= '0' && ch <= '9')) {
						Long worth = nameToValue.get(unit);
						if (worth == null)
							throw new IllegalArgumentException("Unexpected unit name: '%s'".formatted(unit));

						sum += input * worth;
						unit = null;

						if (ch >= '0' && ch <= '9') {
							input = ch - '0';
							mode = 1;
						} else {
							input = 0L;
							mode = 0;
						}
					}

					unit += ch;
					continue;
				}
			}

			if (mode == 0) throw new IllegalArgumentException("Input is blank");
			if (mode == 1) sum += input;

			if (mode == 2) {
				Long worth = nameToValue.get(unit);
				if (worth == null)
					throw new IllegalArgumentException("Unexpected unit name: '%s'".formatted(unit));

				sum += input * worth;
			}

			return sum;
		}

		@Override
		public CompletableFuture<Long> queryBalance(GameProfile player) {
			ServerPlayerEntity p = server.getPlayerManager().getPlayer(player.getId());
			long balance = 0L;

			for (ItemStack stack : p.getInventory()) {
				for (ItemEntry e : entries.values()) {
					if (ItemStack.areItemsAndComponentsEqual(stack, e.stack)) {
						balance += stack.getCount() * e.worth;
						break;
					}
				}
			}

			return CompletableFuture.completedFuture(balance);
		}

		@Override
		public CompletableFuture<Transaction> withdrawFrom(GameProfile player, long amount) {
			// TODO Auto-generated method stub
			return CompletableFuture.failedFuture(new ServiceException("Not implemented"));
		}

		@Override
		public CompletableFuture<Transaction> depositTo(GameProfile player, long amount) {
			// TODO Auto-generated method stub
			return CompletableFuture.failedFuture(new ServiceException("Not implemented"));
		}

		@Override
		public CompletableFuture<Void> rollback(Transaction txn) {
			// TODO Auto-generated method stub
			return CompletableFuture.failedFuture(new ServiceException("Not implemented"));
		}
	}
}
