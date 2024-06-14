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
package stonks.fabric.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * <p>
 * Handle sound feedbacks when player perform a certain action.
 * </p>
 */
public class StonksSounds {
	private static record Entry(ServerPlayerEntity player, SoundEvent sound, int atTime, float volume, float pitch) {
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
				e.player.playSoundToPlayer(e.sound, SoundCategory.PLAYERS, e.volume, e.pitch);
				iter.remove();
			}
		}

		soundTime++;
	}

	public void play(ServerPlayerEntity player, SoundEvent sound, int ticks, float volume, float pitch) {
		if (ticks <= 0) player.playSoundToPlayer(sound, SoundCategory.PLAYERS, volume, pitch);
		else entries.add(new Entry(player, sound, soundTime + ticks, volume, pitch));
	}

	public void play(ServerPlayerEntity player, SoundEvent sound, float volume, float pitch) {
		play(player, sound, 0, volume, pitch);
	}

	public void playClaimedSound(ServerPlayerEntity player) {
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0, 1f, 0.3f);
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 3, 1f, 0.6f);
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 6, 1f, 0.8f);
	}

	public void playInstantOfferSound(ServerPlayerEntity player) {
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0, 1f, 0.3f);
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 3, 1f, 0.6f);
	}

	public void playOfferPlacedSound(ServerPlayerEntity player) {
		play(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0, 1f, 1f);
	}

	public void playCancelledSound(ServerPlayerEntity player) {
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
	}

	public void playErrorSound(ServerPlayerEntity player) {
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 3, 1f, 1f);
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 6, 1f, 1f);
	}

	public void playFailedSound(ServerPlayerEntity player) {
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0, 1f, 1f);
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 3, 1f, 1f);
	}

	public void playOfferFilledSound(ServerPlayerEntity player) {
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0, 1f, 1f);
		play(player, SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 3, 1f, 1f);
	}
}
