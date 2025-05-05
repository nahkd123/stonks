package io.github.nahkd123.stonks.mc.fabric.product.provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.mc.fabric.product.ProductInfo;
import io.github.nahkd123.stonks.mc.fabric.product.provider.ScoreProductInfoProvider.ProductInfoImpl;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ScoreProductInfoProvider implements ProductInfoProvider<ProductInfoImpl> {
	@Override
	public String getTypeName() { return "score"; }

	@Override
	public Class<ProductInfoImpl> getInfoClass() { return ProductInfoImpl.class; }

	@Override
	public MapCodec<ProductInfoImpl> getCodec() { return ProductInfoImpl.MAP_CODEC; }

	record ProductInfoImpl(String objective, ItemStack display) implements ProductInfo {
		public static final MapCodec<ProductInfoImpl> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("objective").forGetter(ProductInfoImpl::objective),
			ItemStack.CODEC.optionalFieldOf("display", ItemStack.EMPTY).forGetter(ProductInfoImpl::display))
			.apply(i, ProductInfoImpl::new));

		public ProductInfoImpl {
			if (display.isEmpty()) display = createDisplay(objective);
		}

		private static ItemStack createDisplay(String objective) {
			ItemStack stack = new ItemStack(Items.SLIME_BALL);
			stack.set(DataComponentTypes.ITEM_NAME, Text.literal(objective).formatted(Formatting.GREEN));
			return stack;
		}

		@Override
		public long getInventory(ServerPlayerEntity player) {
			Scoreboard sb = player.getScoreboard();
			ScoreboardObjective o = sb.getNullableObjective(objective);
			return o != null ? sb.getScore(player, o).getScore() : 0;
		}

		private ScoreboardObjective getOrCreateObjective(Scoreboard sb) {
			ScoreboardObjective o = sb.getNullableObjective(objective);
			if (o == null) o = sb.addObjective(
				objective,
				ScoreboardCriterion.DUMMY,
				Text.literal(objective),
				RenderType.INTEGER,
				false,
				null);
			return o;
		}

		@Override
		public void giveTo(ServerPlayerEntity player, long amount) {
			Scoreboard sb = player.getScoreboard();
			ScoreboardObjective o = getOrCreateObjective(sb);
			sb.getOrCreateScore(player, o).incrementScore((int) amount);
		}

		@Override
		public void takeFrom(ServerPlayerEntity player, long amount) {
			Scoreboard sb = player.getScoreboard();
			ScoreboardObjective o = getOrCreateObjective(sb);
			sb.getOrCreateScore(player, o).incrementScore((int) -amount);
		}
	}
}
