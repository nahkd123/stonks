package stonks.fabric.adapter.provided;

import java.util.WeakHashMap;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import stonks.core.product.Product;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.provider.StonksProvidersRegistry;

public class ItemsAdapter implements StonksFabricAdapter {
	private static final String PREFIX = "item ";
	private WeakHashMap<Product, ItemStack> conversionCache = new WeakHashMap<>();
	private CommandRegistryAccess registry;

	public ItemsAdapter(MinecraftServer server) {
		registry = CommandManager.createRegistryAccess(server.getRegistryManager());
	}

	protected ItemStack convert(Product product) {
		var stack = conversionCache.get(product);
		if (stack != null) return stack;

		var str = product.getProductConstructionData();
		if (str == null) return null;

		// String format: "item [namespace:]<id>[{<nbt>}]"
		if (str.startsWith(PREFIX)) {
			str = str.substring(PREFIX.length());

			try {
				var parsed = ItemStackArgumentType.itemStack(registry).parse(new StringReader(str));
				stack = parsed.createStack(1, false);
				conversionCache.put(product, stack);
				return stack;
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
				StonksFabric.LOGGER.warn("Failed to parse '{}' (from product construction data with ID = {})",
					str, product.getProductId());
			}
		}

		return null;
	}

	@Override
	public ItemStack createDisplayStack(Product product) {
		return convert(product);
	}

	@Override
	public int getUnits(ServerPlayerEntity player, Product product) {
		var refStack = convert(product);
		if (refStack == null) return StonksFabricAdapter.super.getUnits(player, product);

		var inv = player.getInventory();
		var count = 0;

		for (int i = 0; i < inv.size(); i++) {
			var stack = inv.getStack(i);
			if (stack.isEmpty() || !StonksFabricUtils.compareStack(refStack, stack)) continue;
			count += stack.getCount();
		}

		return count;
	}

	@Override
	public boolean addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
		var refStack = convert(product);
		if (refStack == null) return StonksFabricAdapter.super.addUnitsTo(player, product, amount);

		var inv = player.getInventory();
		var giveStack = refStack.copyWithCount(amount);
		if (!giveStack.hasNbt() || giveStack.getNbt().isEmpty()) giveStack.setNbt(null); // Fix stacking issue

		if (!inv.insertStack(giveStack)) {
			var e = player.dropItem(giveStack, false);
			e.resetPickupDelay();
			e.setOwner(player.getUuid());
		}

		return true;
	}

	@Override
	public boolean removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
		var refStack = convert(product);
		if (refStack == null) return StonksFabricAdapter.super.removeUnitsFrom(player, product, amount);

		var inv = player.getInventory();

		for (int i = 0; i < inv.size(); i++) {
			var stack = inv.getStack(i);
			if (stack.isEmpty() || !StonksFabricUtils.compareStack(refStack, stack)) continue;

			var toTake = Math.min(amount, stack.getCount());
			stack.decrement(toTake);
			amount -= toTake;
			if (amount == 0) return true;
		}

		return true;
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(ItemsAdapter.class, (server, config) -> new ItemsAdapter(server));
	}
}
