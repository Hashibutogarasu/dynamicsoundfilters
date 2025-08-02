/*
 * Copyright (c) 2023 Andre Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.dynamicsf.filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.openal.EXTEfx;

import me.andre111.dynamicsf.api.AbstractReverbFilter;
import me.andre111.dynamicsf.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

/**
 * New Reverb Filter implementation.
 */
public class ReverbFilter extends AbstractReverbFilter {

	private int tickCount = 0;
	private float prevDecayFactor = 0.0f;
	private float prevRoomFactor = 0.0f;
	private float prevSkyFactor = 0.0f;

	// Reverb parameters
	private float density = 0.2f;
	private float diffusion = 0.6f;
	private float gain = 0.15f;
	private float gainHF = 0.8f;
	private float decayTime = 0.1f;
	private float decayHFRatio = 0.7f;
	private float reflectionsGain = 0.0f;
	private float reflectionsDelay = 0.0f;
	private float lateReverbGain = 0.0f;
	private float lateReverbDelay = 0.0f;
	private float airAbsorptionGainHF = 0.99f;
	private int decayHFLimit = 1;

	public ReverbFilter() {
		super("Reverb");
	}

	@Override
	protected void update(MinecraftClient client) {
		enabled = Config.getData().reverbFilter.enabled;

		if (!enabled) {
			return;
		}

		if (effectId == -1) {
			reinit();
		}

		// Update reverb parameters
		if (tickCount++ == 20) {
			tickCount = 0;
			updateReverbParameters(client);
			applyReverbSettings();
		}
	}

	@Override
	protected boolean applyFilter(SoundInstance soundInstance) {
		if (reflectionsDelay <= 0 && lateReverbDelay <= 0) {
			return false;
		}
		if (effectId <= 0 || effectSlot <= 0) {
			return false;
		}
		return true;
	}

	@Override
	protected void resetReverbState() {
		density = Config.getData().reverbFilter.density;
		diffusion = Config.getData().reverbFilter.diffusion;
		gain = Config.getData().reverbFilter.gain;
		gainHF = Config.getData().reverbFilter.gainHF;
		decayTime = Config.getData().reverbFilter.minDecayTime;
		decayHFRatio = Config.getData().reverbFilter.decayHFRatio;
		reflectionsGain = 0;
		reflectionsDelay = 0;
		lateReverbGain = 0;
		lateReverbDelay = 0;
		airAbsorptionGainHF = Config.getData().reverbFilter.airAbsorptionGainHF;

		// Clamping values
		clampReverbValues();
	}

	/**
	 * Update reverb parameters based on the current environment.
	 */
	private void updateReverbParameters(MinecraftClient client) {
		int maxBlocks = Config.getData().reverbFilter.maxBlocks;
		boolean checkSky = Config.getData().reverbFilter.checkSky;
		float reverbPercent = Config.getData().reverbFilter.reverbPercent;
		float minDecayTime = Config.getData().reverbFilter.minDecayTime;

		// Get the base reverb value for the current dimension
		Identifier dimension = client.world.getRegistryKey().getValue();
		float baseReverb = Config.getData().reverbFilter.getDimensionBaseReverb(dimension);

		BlockPos playerPos = getPlayerEyePosition(client);

		// Scan the surroundings for blocks
		ScanResult scanResult = scanSurroundings(client, playerPos, maxBlocks);

		// Calculate decay factor, room factor, and sky factor
		float decayFactor = calculateDecayFactor(baseReverb, scanResult.blocksFound);
		float roomFactor = (float) scanResult.visited.size() / maxBlocks;
		float skyFactor = calculateSkyFactor(client.world, playerPos, checkSky, scanResult.visited.size(), maxBlocks);

		// Interpolate factors
		decayFactor = (decayFactor + prevDecayFactor) / 2.0f;
		roomFactor = (roomFactor + prevRoomFactor) / 2.0f;
		skyFactor = (skyFactor + prevSkyFactor) / 2.0f;

		// Save previous values
		prevDecayFactor = decayFactor;
		prevRoomFactor = roomFactor;
		prevSkyFactor = skyFactor;

		// Update reverb parameters
		updateReverbValues(reverbPercent, minDecayTime, decayFactor, roomFactor, skyFactor);
	}

	/**
	 * Get the player's eye position
	 */
	private BlockPos getPlayerEyePosition(MinecraftClient client) {
		return new BlockPos(
				MathHelper.floor(client.player.getPos().getX()),
				MathHelper.floor(client.player.getPos().getY() + client.player.getEyeHeight(client.player.getPose())),
				MathHelper.floor(client.player.getPos().getZ()));
	}

	/**
	 * Store the results of a scan
	 */
	private static class ScanResult {
		final Set<BlockPos> visited;
		final List<Identifier> blocksFound;

		ScanResult(Set<BlockPos> visited, List<Identifier> blocksFound) {
			this.visited = visited;
			this.blocksFound = blocksFound;
		}
	}

	/**
	 * Scan the surrounding blocks
	 */
	private ScanResult scanSurroundings(MinecraftClient client, BlockPos playerPos, int maxBlocks) {
		Random random = new Random();
		Set<BlockPos> visited = new TreeSet<>();
		List<Identifier> blocksFound = new ArrayList<>();
		List<BlockPos> toVisit = new LinkedList<>();
		toVisit.add(playerPos);

		for (int i = 0; i < maxBlocks && !toVisit.isEmpty(); ++i) {
			BlockPos current = toVisit.remove(random.nextInt(toVisit.size()));
			visited.add(current);

			for (Direction direction : Direction.values()) {
				BlockPos pos = current.offset(direction);
				BlockState blockState = client.world.getBlockState(pos);
				Identifier blockID = Registries.BLOCK.getId(blockState.getBlock());

				if (!blockState.isSolidBlock(client.world, pos)) {
					if (!visited.contains(pos) && !toVisit.contains(pos)) {
						toVisit.add(pos);
					}
					if (!blockState.isAir() && blockState.getFluidState().isEmpty()) {
						blocksFound.add(blockID);
					}
				} else {
					blocksFound.add(blockID);
				}
			}
		}

		return new ScanResult(visited, blocksFound);
	}

	/**
	 * Calculate the decay factor
	 */
	private float calculateDecayFactor(float baseReverb, List<Identifier> blocksFound) {
		double highReverb = 0.0;
		double midReverb = 0.0;
		double lowReverb = 0.0;

		for (Identifier blockID : blocksFound) {
			if (Config.getData().reverbFilter.lowReverbBlocks.contains(blockID)) {
				lowReverb += 1.0;
			} else if (Config.getData().reverbFilter.highReverbBlocks.contains(blockID)) {
				highReverb += 1.0;
			} else {
				midReverb += 1.0;
			}
		}

		float decayFactor = baseReverb;
		if (highReverb + midReverb + lowReverb > 0.0) {
			decayFactor += (highReverb - lowReverb) / (highReverb + midReverb + lowReverb);
		}

		return Math.max(0, Math.min(decayFactor, 1));
	}

	/**
	 * Calculate the sky factor
	 */
	private float calculateSkyFactor(ClientWorld world, BlockPos playerPos, boolean checkSky, int roomSize,
			int maxBlocks) {
		float skyFactor = 0;

		if (checkSky && roomSize == maxBlocks) {
			Random random = new Random();
			if (hasSkyAbove(world, playerPos))
				skyFactor += 1;

			Direction[] directions = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
			for (Direction direction : directions) {
				if (hasSkyAbove(world, playerPos.offset(direction, random.nextInt(5) + 5))) {
					skyFactor += 1;
				}
				if (hasSkyAbove(world, playerPos.offset(direction, random.nextInt(5) + 5).offset(Direction.UP, 5))) {
					skyFactor += 1;
				}
			}
		}

		skyFactor = 1.0f - skyFactor / 9.0f;
		return skyFactor * skyFactor;
	}

	/**
	 * Update the reverb values
	 */
	private void updateReverbValues(float reverbPercent, float minDecayTime, float decayFactor, float roomFactor,
			float skyFactor) {
		// Calculate the reverb time
		decayTime = reverbPercent * 6.0f * decayFactor * roomFactor * skyFactor;
		if (decayTime < minDecayTime) {
			decayTime = minDecayTime;
		}

		// Calculate other parameters
		float reflectionGainBase = Config.getData().reverbFilter.reflectionsGainBase;
		float reflectionGainMultiplier = Config.getData().reverbFilter.reflectionsGainMultiplier;
		float reflectionDelayMultiplier = Config.getData().reverbFilter.reflectionsDelayMultiplier;
		float lateReverbGainBase = Config.getData().reverbFilter.lateReverbGainBase;
		float lateReverbGainMultiplier = Config.getData().reverbFilter.lateReverbGainMultiplier;
		float lateReverbDelayMultiplier = Config.getData().reverbFilter.lateReverbDelayMultiplier;

		reflectionsGain = reverbPercent * (reflectionGainBase + reflectionGainMultiplier * roomFactor);
		reflectionsDelay = reflectionDelayMultiplier * roomFactor;
		lateReverbGain = reverbPercent * (lateReverbGainBase + lateReverbGainMultiplier * roomFactor);
		lateReverbDelay = lateReverbDelayMultiplier * roomFactor;

		// Clamp the reverb values
		clampReverbValues();
	}

	/**
	 * Clamp the reverb values
	 */
	private void clampReverbValues() {
		density = MathHelper.clamp(density, EXTEfx.AL_REVERB_MIN_DENSITY, EXTEfx.AL_REVERB_MAX_DENSITY);
		diffusion = MathHelper.clamp(diffusion, EXTEfx.AL_REVERB_MIN_DIFFUSION, EXTEfx.AL_REVERB_MAX_DIFFUSION);
		gain = MathHelper.clamp(gain, EXTEfx.AL_REVERB_MIN_GAIN, EXTEfx.AL_REVERB_MAX_GAIN);
		gainHF = MathHelper.clamp(gainHF, EXTEfx.AL_REVERB_MIN_GAINHF, EXTEfx.AL_REVERB_MAX_GAINHF);
		decayTime = MathHelper.clamp(decayTime, EXTEfx.AL_REVERB_MIN_DECAY_TIME, EXTEfx.AL_REVERB_MAX_DECAY_TIME);
		decayHFRatio = MathHelper.clamp(decayHFRatio, EXTEfx.AL_REVERB_MIN_DECAY_HFRATIO,
				EXTEfx.AL_REVERB_MAX_DECAY_HFRATIO);
		reflectionsGain = MathHelper.clamp(reflectionsGain, EXTEfx.AL_REVERB_MIN_REFLECTIONS_GAIN,
				EXTEfx.AL_REVERB_MAX_REFLECTIONS_GAIN);
		reflectionsDelay = MathHelper.clamp(reflectionsDelay, EXTEfx.AL_REVERB_MIN_REFLECTIONS_DELAY,
				EXTEfx.AL_REVERB_MAX_REFLECTIONS_DELAY);
		lateReverbGain = MathHelper.clamp(lateReverbGain, EXTEfx.AL_REVERB_MIN_LATE_REVERB_GAIN,
				EXTEfx.AL_REVERB_MAX_LATE_REVERB_GAIN);
		lateReverbDelay = MathHelper.clamp(lateReverbDelay, EXTEfx.AL_REVERB_MIN_LATE_REVERB_DELAY,
				EXTEfx.AL_REVERB_MAX_LATE_REVERB_DELAY);
		airAbsorptionGainHF = MathHelper.clamp(airAbsorptionGainHF, EXTEfx.AL_REVERB_MIN_AIR_ABSORPTION_GAINHF,
				EXTEfx.AL_REVERB_MAX_AIR_ABSORPTION_GAINHF);
		decayHFLimit = MathHelper.clamp(decayHFLimit, EXTEfx.AL_REVERB_MIN_DECAY_HFLIMIT,
				EXTEfx.AL_REVERB_MAX_DECAY_HFLIMIT);
	}

	/**
	 * Apply the reverb settings to OpenAL
	 */
	private void applyReverbSettings() {
		// Temporarily set the gain to 0
		EXTEfx.alAuxiliaryEffectSlotf(effectSlot, EXTEfx.AL_EFFECTSLOT_GAIN, 0);

		// リバーブパラメータを設定
		safeSetReverbParameter(EXTEfx.AL_REVERB_DENSITY, density);
		safeSetReverbParameter(EXTEfx.AL_REVERB_DIFFUSION, diffusion);
		safeSetReverbParameter(EXTEfx.AL_REVERB_GAIN, gain);
		safeSetReverbParameter(EXTEfx.AL_REVERB_GAINHF, gainHF);
		safeSetReverbParameter(EXTEfx.AL_REVERB_DECAY_TIME, decayTime);
		safeSetReverbParameter(EXTEfx.AL_REVERB_DECAY_HFRATIO, decayHFRatio);
		safeSetReverbParameter(EXTEfx.AL_REVERB_REFLECTIONS_GAIN, reflectionsGain);
		safeSetReverbParameter(EXTEfx.AL_REVERB_REFLECTIONS_DELAY, reflectionsDelay);
		safeSetReverbParameter(EXTEfx.AL_REVERB_LATE_REVERB_GAIN, lateReverbGain);
		safeSetReverbParameter(EXTEfx.AL_REVERB_LATE_REVERB_DELAY, lateReverbDelay);
		safeSetReverbParameter(EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, airAbsorptionGainHF);
		safeSetReverbParameter(EXTEfx.AL_REVERB_ROOM_ROLLOFF_FACTOR, 0.0f);

		try {
			EXTEfx.alEffecti(effectId, EXTEfx.AL_REVERB_DECAY_HFLIMIT, decayHFLimit);
		} catch (Exception e) {
			// Log is handled by the parent class
		}

		// Apply the effect to the slot
		applyEffectToSlot();
	}

	/**
	 * Check if there is sky above
	 */
	private boolean hasSkyAbove(ClientWorld world, BlockPos pos) {
		if (world.getDimension().hasCeiling()) {
			return false;
		}

		Chunk chunk = world.getChunk(pos);
		Heightmap heightMap = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
		int x = pos.getX() - chunk.getPos().getStartX();
		int z = pos.getZ() - chunk.getPos().getStartZ();
		x = Math.max(0, Math.min(x, 15));
		z = Math.max(0, Math.min(z, 15));
		return heightMap != null && heightMap.get(x, z) <= pos.getY();
	}
}
