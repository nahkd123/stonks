package io.github.nahkd123.stonks.mc.fabric.product;

import java.util.List;

import net.minecraft.item.ItemStack;

public record Category(ItemStack icon, List<CategoryProduct> products) {
}
