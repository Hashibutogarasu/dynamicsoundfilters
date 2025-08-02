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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.openal.EXTEfx;

import me.andre111.dynamicsf.api.AbstractOpenALFilter;
import me.andre111.dynamicsf.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstance.AttenuationType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * New obstruction filter implementation based on the new architecture.
 */
public class ObstructionFilter extends AbstractOpenALFilter {

	private final Map<SoundInstance, Float> obstructions = new HashMap<>();
	private final Queue<SoundInstance> toScan = new ConcurrentLinkedQueue<>();
	private final LiquidFilter liquidFilter;

	public ObstructionFilter() {
		super("Obstruction");
		this.liquidFilter = new LiquidFilter();
	}

	@Override
	protected void initializeOpenALResources() {
		filterId = EXTEfx.alGenFilters();
		if (filterId <= 0) {
			throw new RuntimeException("Failed to generate OpenAL lowpass filter");
		}

		// Set fixed parameters
		EXTEfx.alFilteri(filterId, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
	}

	@Override
	protected void update(MinecraftClient client) {
		// Update liquid filter
		liquidFilter.updateGlobal(client);

		enabled = Config.getData().obstructionFilter.enabled;
		if (!enabled) {
			return;
		}

		if (filterId == -1) {
			reinit();
		}

		// Remove sound sources that have finished playing
		obstructions.entrySet().removeIf(entry -> !client.getSoundManager().isPlaying(entry.getKey()));

		// Add new sound sources
		SoundInstance newSoundInstance;
		while ((newSoundInstance = toScan.poll()) != null) {
			obstructions.put(newSoundInstance, 0.0f);
		}

		// Update obstruction amounts
		updateObstructions(client);
	}

	@Override
	protected boolean applyFilter(SoundInstance soundInstance) {
		// Get liquid filter values
		float lowPassGain = 1.0f;
		float lowPassGainHF = 1.0f;

		if (liquidFilter.updateSoundInstance(soundInstance)) {
			lowPassGain = liquidFilter.getLowPassGain();
			lowPassGainHF = liquidFilter.getLowPassGainHF();
		}

		// Use obstruction filter
		if (soundInstance.getAttenuationType() == AttenuationType.LINEAR) {
			Float obstructionAmount = obstructions.get(soundInstance);
			if (obstructionAmount == null) {
				toScan.add(soundInstance);
				obstructionAmount = 0.0f;
			}

			if (obstructionAmount > 0.01) {
				lowPassGain = lowPassGain * (1.0f - obstructionAmount);
				lowPassGainHF = lowPassGainHF * (1.0f - MathHelper.sqrt(obstructionAmount));
			}
		}

		// Check if filter is needed
		if (lowPassGain >= 1.0f && lowPassGainHF >= 1.0f) {
			return false;
		}

		// Apply clamping
		lowPassGain = MathHelper.clamp(lowPassGain, EXTEfx.AL_LOWPASS_MIN_GAIN, EXTEfx.AL_LOWPASS_MAX_GAIN);
		lowPassGainHF = MathHelper.clamp(lowPassGainHF, EXTEfx.AL_LOWPASS_MIN_GAINHF, EXTEfx.AL_LOWPASS_MAX_GAINHF);

		EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAIN, lowPassGain);
		EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAINHF, lowPassGainHF);

		return true;
	}

	@Override
	protected void resetFilterState() {
		obstructions.clear();
	}

	/**
	 * Update obstruction amounts
	 */
	private void updateObstructions(MinecraftClient client) {
		for (Map.Entry<SoundInstance, Float> entry : obstructions.entrySet()) {
			float currentAmount = entry.getValue();
			float nextAmount = calculateObstructionAmount(client, entry.getKey());
			// スムーズに変化させる
			entry.setValue((currentAmount * 3.0f + nextAmount) / 4.0f);
		}
	}

	/**
	 * Calculate obstruction amount for a sound instance
	 */
	private float calculateObstructionAmount(MinecraftClient client, SoundInstance soundInstance) {
		float obstructionStep = Config.getData().obstructionFilter.obstructionStep;
		float obstructionMax = Config.getData().obstructionFilter.obstructionMax;

		// Get player and sound positions
		Vec3d playerPos = getPlayerEyePosition(client);
		Vec3d soundPos = new Vec3d(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());

		if (!isValidPosition(playerPos) || !isValidPosition(soundPos)) {
			return 0;
		}

		return calculateLinearObstruction(client, playerPos, soundPos, obstructionStep, obstructionMax);
	}

	/**
	 * Get player eye position
	 */
	private Vec3d getPlayerEyePosition(MinecraftClient client) {
		return client.player.getPos().add(0, client.player.getEyeHeight(client.player.getPose()), 0);
	}

	/**
	 * Check if position is valid
	 */
	private boolean isValidPosition(Vec3d pos) {
		return !Double.isNaN(pos.x) && !Double.isNaN(pos.y) && !Double.isNaN(pos.z);
	}

	/**
	 * Calculate linear obstruction
	 */
	private float calculateLinearObstruction(MinecraftClient client, Vec3d playerPos, Vec3d soundPos,
			float obstructionStep, float obstructionMax) {
		BlockPos playerBlockPos = BlockPos.ofFloored(playerPos);
		BlockPos.Mutable currentPos = new BlockPos.Mutable(soundPos.x, soundPos.y, soundPos.z);
		Vec3d currentPosD = soundPos;

		float obstruction = 0.0f;

		// Max 100 steps
		for (int i = 0; i < 100; i++) {
			Vec3d prevPosD = currentPosD;

			// Check if player has reached the current block
			if (playerBlockPos.equals(currentPos)) {
				return obstruction;
			}

			// Calculate next block position
			StepResult stepResult = calculateNextStep(playerBlockPos, currentPos, playerPos, soundPos);
			currentPosD = stepResult.nextPosition;
			currentPos.set(currentPosD.x, currentPosD.y, currentPosD.z);

			// Check for obstruction
			BlockState blockState = client.world.getBlockState(currentPos);
			if (blockState.isFullCube(client.world, currentPos)) {
				Identifier blockID = Registries.BLOCK.getId(blockState.getBlock());
				float blockObstruction = getBlockObstruction(blockID, obstructionStep);
				obstruction += blockObstruction * currentPosD.distanceTo(prevPosD);

				if (obstruction > obstructionMax) {
					return obstructionMax;
				}
			}
		}

		return obstruction;
	}

	/**
	 * Store step result
	 */
	private static class StepResult {
		final Vec3d nextPosition;

		StepResult(Vec3d nextPosition) {
			this.nextPosition = nextPosition;
		}
	}

	/**
	 * Calculate next step position
	 */
	private StepResult calculateNextStep(BlockPos playerPos, BlockPos.Mutable currentPos,
			Vec3d playerPosD, Vec3d soundPosD) {
		// Check for axis changes
		boolean changeX = currentPos.getX() != playerPos.getX();
		boolean changeY = currentPos.getY() != playerPos.getY();
		boolean changeZ = currentPos.getZ() != playerPos.getZ();

		// Calculate next block position
		int nextBlockX = currentPos.getX() + (changeX ? (currentPos.getX() < playerPos.getX() ? 1 : -1) : 0);
		int nextBlockY = currentPos.getY() + (changeY ? (currentPos.getY() < playerPos.getY() ? 1 : -1) : 0);
		int nextBlockZ = currentPos.getZ() + (changeZ ? (currentPos.getZ() < playerPos.getZ() ? 1 : -1) : 0);

		// Calculate progress along the line to the player
		double progressX = changeX ? Math.abs((nextBlockX - soundPosD.x) / (playerPosD.x - soundPosD.x))
				: Double.POSITIVE_INFINITY;
		double progressY = changeY ? Math.abs((nextBlockY - soundPosD.y) / (playerPosD.y - soundPosD.y))
				: Double.POSITIVE_INFINITY;
		double progressZ = changeZ ? Math.abs((nextBlockZ - soundPosD.z) / (playerPosD.z - soundPosD.z))
				: Double.POSITIVE_INFINITY;

		double progress = Math.min(Math.min(progressX, progressY), progressZ);

		// Calculate next position based on progress
		Vec3d nextPos = new Vec3d(
				soundPosD.x + (playerPosD.x - soundPosD.x) * progress,
				soundPosD.y + (playerPosD.y - soundPosD.y) * progress,
				soundPosD.z + (playerPosD.z - soundPosD.z) * progress);

		return new StepResult(nextPos);
	}

	/**
	 * Get the obstruction value for a block
	 */
	private float getBlockObstruction(Identifier blockID, float obstructionStep) {
		if (Config.getData().obstructionFilter.highObstructionBlocks.contains(blockID)) {
			return obstructionStep * 2.0f;
		}
		return obstructionStep;
	}

	/**
	 * Get the liquid filter (for compatibility)
	 */
	public LiquidFilter getLiquidFilter() {
		return liquidFilter;
	}
}
