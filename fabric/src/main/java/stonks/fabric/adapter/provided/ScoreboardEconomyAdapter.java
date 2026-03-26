/*
 * Copyright (c) 2023-2026 nahkd
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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
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

	public Objective getObjective() {
		var objective = scoreboard.getObjective(objectiveName);

		if (objective == null) {
			return scoreboard.addObjective(
				objectiveName,
				ObjectiveCriteria.DUMMY,
				Component.literal(objectiveName),
				ObjectiveCriteria.RenderType.INTEGER,
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
	public double accountBalance(ServerPlayer player) {
		var score = scoreboard.getPlayerScoreInfo(player, getObjective());
		return scoreToMoney(score != null ? score.value() : 0);
	}

	@Override
	public boolean accountDeposit(ServerPlayer player, double money) {
		var score = scoreboard.getOrCreatePlayerScore(player, getObjective());
		var bal = scoreToMoney(score.get());
		bal += money;
		score.set(moneyToScore(bal));
		return true;
	}

	@Override
	public boolean accountWithdraw(ServerPlayer player, double money) {
		var score = scoreboard.getOrCreatePlayerScore(player, getObjective());
		var bal = scoreToMoney(score.get());
		bal = Math.max(bal - money, 0d);
		score.set(moneyToScore(bal));
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
