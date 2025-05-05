package io.github.nahkd123.stonks.mc.fabric.product.provider;

import com.mojang.serialization.MapCodec;

import io.github.nahkd123.stonks.mc.fabric.product.ProductInfo;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ItemProductInfoProvider.ProductInfoImpl;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemProductInfoProvider implements ProductInfoProvider<ProductInfoImpl> {
	@Override
	public String getTypeName() { return "item"; }

	@Override
	public Class<ProductInfoImpl> getInfoClass() { return ProductInfoImpl.class; }

	@Override
	public MapCodec<ProductInfoImpl> getCodec() { return ProductInfoImpl.MAP_CODEC; }

	record ProductInfoImpl(ItemStack stack) implements ProductInfo {
		public static final MapCodec<ProductInfoImpl> MAP_CODEC = ItemStack
			.createOptionalCodec("item")
			.xmap(ProductInfoImpl::new, ProductInfoImpl::stack);

		@Override
		public ItemStack display() {
			return stack;
		}

		@Override
		public long getInventory(ServerPlayerEntity player) {
			long count = 0L;
			for (ItemStack stack : player.getInventory())
				if (ItemStack.areItemsAndComponentsEqual(stack, this.stack)) count += stack.getCount();
			return count;
		}

		@Override
		public void giveTo(ServerPlayerEntity player, long amount) {
			PlayerInventory inventory = player.getInventory();

			for (int i = 0; i < inventory.size(); i++) {
				ItemStack stack = inventory.getStack(i);

				if (stack.isEmpty()) {
					stack = this.stack.copy();
					stack.setCount(Math.min(stack.getMaxCount(), (int) amount));
					amount -= stack.getCount();
					inventory.setStack(i, stack);
					continue;
				}

				if (ItemStack.areItemsAndComponentsEqual(stack, this.stack)) {
					int toAdd = Math.min(stack.getMaxCount() - stack.getCount(), (int) amount);
					stack.setCount(stack.getCount() + toAdd);
					amount -= toAdd;
					inventory.setStack(i, stack);
				}
			}
		}

		@Override
		public void takeFrom(ServerPlayerEntity player, long amount) {
			Inventories.remove(
				player.getInventory(),
				s -> ItemStack.areItemsAndComponentsEqual(s, stack),
				(int) amount, false);
		}
	}
}
