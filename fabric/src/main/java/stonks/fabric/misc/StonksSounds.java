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
package stonks.fabric.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * <p>
 * Handle sound feedbacks when player perform a certain action.
 * </p>
 */
public class StonksSounds {
	private static record Entry(ServerPlayer player, SoundEvent sound, int atTime, float volume, float pitch) {
	}

	private int soundTime = 0;
	private List<Entry> entries = new ArrayList<>();

	/**
	 * <p>
	 * Get sound time in ticks
	 * </p>
	 * 
	 * @return Sound time in ticks.
	 */
	public int getSoundTime() { return soundTime; }

	/**
	 * <p>
	 * Called in server ticking loop
	 * </p>
	 */
	public void tick() {
		var iter = entries.iterator();
		while (iter.hasNext()) {
			var e = iter.next();

			if (e.atTime <= soundTime) {
				e.player.playSound(e.sound, e.volume, e.pitch);
				iter.remove();
			}
		}

		soundTime++;
	}

	public void play(ServerPlayer player, SoundEvent sound, int ticks, float volume, float pitch) {
		if (ticks <= 0) {
			var registry = Holder.direct(sound);
			double x = player.getX(), y = player.getY(), z = player.getZ();
			var packet = new ClientboundSoundPacket(registry, SoundSource.PLAYERS, x, y, z, volume, pitch, 0);
			player.connection.send(packet);
		} else {
			entries.add(new Entry(player, sound, soundTime + ticks, volume, pitch));
		}
	}

	public void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
		play(player, sound, 0, volume, pitch);
	}

	public void playClaimedSound(ServerPlayer player) {
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0, 1f, 0.3f);
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 3, 1f, 0.6f);
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 6, 1f, 0.8f);
	}

	public void playInstantOfferSound(ServerPlayer player) {
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0, 1f, 0.3f);
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 3, 1f, 0.6f);
	}

	public void playOfferPlacedSound(ServerPlayer player) {
		play(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0, 1f, 1f);
	}

	public void playCancelledSound(ServerPlayer player) {
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
	}

	public void playErrorSound(ServerPlayer player) {
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 3, 1f, 1f);
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 6, 1f, 1f);
	}

	public void playFailedSound(ServerPlayer player) {
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
		play(player, SoundEvents.NOTE_BLOCK_PLING.value(), 3, 1f, 1f);
	}

	public void playOfferFilledSound(ServerPlayer player) {
		play(player, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 0, 1f, 1f);
		play(player, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 3, 1f, 1f);
	}
}
