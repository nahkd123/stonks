package stonks.fabric.adapter.provided;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import stonks.core.product.Product;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.provider.StonksProvidersRegistry;

public class ScoreboardUnitAdapter implements StonksFabricAdapter {
	private static final String PREFIX = "scoreboard-objective";
	private Scoreboard scoreboard;

	public ScoreboardUnitAdapter(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public Scoreboard getScoreboard() { return scoreboard; }

	public ScoreboardObjective getObjective(Product product) {
		var str = product.getProductConstructionData();
		if (!str.startsWith(PREFIX)) return null;

		var objectiveName = str.substring(PREFIX.length());
		if (scoreboard.containsObjective(objectiveName)) return scoreboard.getObjective(objectiveName);
		return scoreboard.addObjective(objectiveName,
			ScoreboardCriterion.DUMMY, Text.literal(objectiveName),
			RenderType.INTEGER);
	}

	@Override
	public int getUnits(ServerPlayerEntity player, Product product) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.getUnits(player, product);
		return scoreboard.getPlayerScore(player.getEntityName(), obj).getScore();
	}

	@Override
	public boolean addUnitsTo(ServerPlayerEntity player, Product product, int amount) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.addUnitsTo(player, product, amount);
		scoreboard.getPlayerScore(player.getEntityName(), obj).incrementScore(amount);
		return true;
	}

	@Override
	public boolean removeUnitsFrom(ServerPlayerEntity player, Product product, int amount) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.removeUnitsFrom(player, product, amount);
		scoreboard.getPlayerScore(player.getEntityName(), obj).incrementScore(-amount);
		return true;
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(ScoreboardUnitAdapter.class,
			(server, config) -> new ScoreboardUnitAdapter(server.getScoreboard()));
	}
}
