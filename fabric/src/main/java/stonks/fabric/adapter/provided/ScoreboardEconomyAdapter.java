/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.fabric.adapter.provided;

import net.minecraft.scoreboard.ReadableScoreboardScore;
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
		var objective = scoreboard.getNullableObjective(objectiveName);

		if (objective == null) {
			return scoreboard.addObjective(
				objectiveName,
				ScoreboardCriterion.DUMMY,
				Text.literal(objectiveName),
				RenderType.INTEGER,
				false,
				null);
		} else {
			return objective;
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
		ReadableScoreboardScore scoreEntry = scoreboard.getScore(player, getObjective());
		return scoreToMoney(scoreEntry != null ? scoreEntry.getScore() : 0);
	}

	@Override
	public boolean accountDeposit(ServerPlayerEntity player, double money) {
		var score = scoreboard.getOrCreateScore(player, getObjective());
		var bal = scoreToMoney(score.getScore());
		bal += money;
		score.setScore(moneyToScore(bal));
		return true;
	}

	@Override
	public boolean accountWithdraw(ServerPlayerEntity player, double money) {
		var score = scoreboard.getOrCreateScore(player, getObjective());
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
