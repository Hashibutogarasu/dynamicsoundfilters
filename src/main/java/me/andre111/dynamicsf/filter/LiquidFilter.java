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

import me.andre111.dynamicsf.api.AbstractSoundFilter;
import me.andre111.dynamicsf.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * New liquid filter implementation based on the new architecture.
 */
public class LiquidFilter extends AbstractSoundFilter {

	private float lowPassGain = 1.0f;
	private float lowPassGainHF = 1.0f;
	private float targetLowPassGain = 1.0f;
	private float targetLowPassGainHF = 1.0f;

	public LiquidFilter() {
		super("Liquid");
	}

	@Override
	public void reinit() {
		// Liquid filter does not hold OpenAL resources directly
	}

	@Override
	protected void update(MinecraftClient client) {
		enabled = Config.getData().liquidFilter.enabled;

		if (!enabled) {
			return;
		}

		BlockPos playerPos = getPlayerEyePosition(client);

		// Update target values
		updateTargetValues(client, playerPos);

		// Interpolate values
		interpolateValues();

		// Clamp values
		clampValues();
	}

	@Override
	protected boolean applyFilter(SoundInstance soundInstance) {
		return lowPassGain < 1.0f || lowPassGainHF < 1.0f;
	}

	@Override
	protected void reset() {
		enabled = false;
		lowPassGain = 1.0f;
		lowPassGainHF = 1.0f;
		targetLowPassGain = 1.0f;
		targetLowPassGainHF = 1.0f;
	}

	/**
	 * Get player eye position
	 */
	private BlockPos getPlayerEyePosition(MinecraftClient client) {
		return new BlockPos(
				MathHelper.floor(client.player.getPos().getX()),
				MathHelper.floor(client.player.getPos().getY() + client.player.getEyeHeight(client.player.getPose())),
				MathHelper.floor(client.player.getPos().getZ()));
	}

	/**
	 * Update target values
	 */
	private void updateTargetValues(MinecraftClient client, BlockPos playerPos) {
		FluidState fluidState = client.world.getFluidState(playerPos);

		if (fluidState.isIn(FluidTags.WATER)) {
			targetLowPassGain = Config.getData().liquidFilter.waterGain;
			targetLowPassGainHF = Config.getData().liquidFilter.waterGainHF;
		} else if (fluidState.isIn(FluidTags.LAVA)) {
			targetLowPassGain = Config.getData().liquidFilter.lavaGain;
			targetLowPassGainHF = Config.getData().liquidFilter.lavaGainHF;
		} else {
			targetLowPassGain = 1.0f;
			targetLowPassGainHF = 1.0f;
		}
	}

	/**
	 * Interpolate values
	 */
	private void interpolateValues() {
		lowPassGain = (targetLowPassGain + lowPassGain) / 2.0f;
		lowPassGainHF = (targetLowPassGainHF + lowPassGainHF) / 2.0f;
	}

	/**
	 * Clamp values
	 */
	private void clampValues() {
		lowPassGain = Math.max(0, Math.min(lowPassGain, 1));
		lowPassGainHF = Math.max(0, Math.min(lowPassGainHF, 1));
	}

	/**
	 * Get low pass gain
	 */
	public float getLowPassGain() {
		return lowPassGain;
	}

	/**
	 * Get low pass gain high frequency
	 */
	public float getLowPassGainHF() {
		return lowPassGainHF;
	}
}
