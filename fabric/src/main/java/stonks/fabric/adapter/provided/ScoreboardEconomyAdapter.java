package stonks.fabric.adapter.provided;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import stonks.fabric.adapter.StonksFabricAdapter;
import stonks.fabric.provider.StonksProvidersRegistry;

public class ScoreboardEconomyAdapter implements StonksFabricAdapter {
	private Scoreboard scoreboard;
	private String objectiveName;
	private int decimals;

	public ScoreboardEconomyAdapter(Scoreboard scoreboard, String objectiveName, int decimals) {
		this.scoreboard = scoreboard;
		this.objectiveName = objectiveName;
		this.decimals = decimals;
	}

	public Scoreboard getScoreboard() { return scoreboard; }

	public ScoreboardObjective getObjective() {
		if (!scoreboard.containsObjective(objectiveName)) {
			return scoreboard.addObjective(
				objectiveName,
				ScoreboardCriterion.DUMMY,
				Text.literal(objectiveName),
				RenderType.INTEGER);
		} else {
			return scoreboard.getObjective(objectiveName);
		}
	}

	public double scoreToMoney(int score) {
		return score / Math.pow(10, decimals);
	}

	public int moneyToScore(double money) {
		return (int) Math.round(money * Math.pow(10, decimals));
	}

	@Override
	public double accountBalance(ServerPlayerEntity player) {
		return scoreToMoney(scoreboard.getPlayerScore(player.getEntityName(), getObjective()).getScore());
	}

	@Override
	public boolean accountDeposit(ServerPlayerEntity player, double money) {
		var score = scoreboard.getPlayerScore(player.getEntityName(), getObjective());
		var bal = scoreToMoney(score.getScore());
		bal += money;
		score.setScore(moneyToScore(bal));
		return true;
	}

	@Override
	public boolean accountWithdraw(ServerPlayerEntity player, double money) {
		var score = scoreboard.getPlayerScore(player.getEntityName(), getObjective());
		var bal = scoreToMoney(score.getScore());
		bal = Math.max(bal - money, 0d);
		score.setScore(moneyToScore(bal));
		return true;
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(ScoreboardEconomyAdapter.class, (server, config) -> {
			var objectiveName = config.firstChild("objective").flatMap(v -> v.getValue()).orElse("balance");
			var decimals = config.firstChild("decimals").flatMap(v -> v.getValue(Integer::parseInt)).orElse(2);
			return new ScoreboardEconomyAdapter(server.getScoreboard(), objectiveName, decimals);
		});
	}
}
