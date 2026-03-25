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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
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

	public Objective getObjective(Product product) {
		var str = product.getProductConstructionData();
		if (!str.startsWith(PREFIX)) return null;

		var objectiveName = str.substring(PREFIX.length());
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

	@Override
	public int getUnits(ServerPlayer player, Product product) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.getUnits(player, product);
		return scoreboard.getOrCreatePlayerScore(player, obj).get();
	}

	@Override
	public boolean addUnitsTo(ServerPlayer player, Product product, int amount) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.addUnitsTo(player, product, amount);
		scoreboard.getOrCreatePlayerScore(player, obj).add(amount);
		return true;
	}

	@Override
	public boolean removeUnitsFrom(ServerPlayer player, Product product, int amount) {
		var obj = getObjective(product);
		if (obj == null) return StonksFabricAdapter.super.removeUnitsFrom(player, product, amount);
		scoreboard.getOrCreatePlayerScore(player, obj).add(-amount);
		return true;
	}

	public static void register() {
		StonksProvidersRegistry.registerAdapter(ScoreboardUnitAdapter.class,
			(server, config) -> new ScoreboardUnitAdapter(server.getScoreboard()));
	}
}
