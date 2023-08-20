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
package stonks.fabric.adapter.provided;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import stonks.fabric.adapter.AdapterResponse;
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
	public AdapterResponse<Double> accountBalance(ServerPlayerEntity player) {
		return AdapterResponse
			.success(scoreToMoney(scoreboard.getPlayerScore(player.getEntityName(), getObjective()).getScore()));
	}

	@Override
	public AdapterResponse<Void> accountDeposit(ServerPlayerEntity player, double money) {
		var score = scoreboard.getPlayerScore(player.getEntityName(), getObjective());
		var bal = scoreToMoney(score.getScore());
		bal += money;
		score.setScore(moneyToScore(bal));
		return AdapterResponse.success(null);
	}

	@Override
	public AdapterResponse<Void> accountWithdraw(ServerPlayerEntity player, double money) {
		var score = scoreboard.getPlayerScore(player.getEntityName(), getObjective());
		var bal = scoreToMoney(score.getScore());
		bal = Math.max(bal - money, 0d);
		score.setScore(moneyToScore(bal));
		return AdapterResponse.success(null);
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(ScoreboardEconomyAdapter.class, (server, config) -> {
			var objectiveName = config.firstChild("objective").flatMap(v -> v.getValue()).orElse("balance");
			var decimals = config.firstChild("decimals").flatMap(v -> v.getValue(Integer::parseInt)).orElse(2);
			return new ScoreboardEconomyAdapter(server.getScoreboard(), objectiveName, decimals);
		});
	}
}
