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
package me.andre111.dynamicsf;

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import me.andre111.dynamicsf.config.Config;
import me.andre111.dynamicsf.filter.ObstructionFilter;
import me.andre111.dynamicsf.filter.ReverbFilter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;

@Environment(EnvType.CLIENT)
public class FilterManager {
	private boolean disabled = false;
	private final ReverbFilter reverbFilter = new ReverbFilter();
	private final ObstructionFilter obstructionFilter = new ObstructionFilter();

	public void updateGlobal(MinecraftClient client) {
		if (disabled)
			return;

		reverbFilter.updateGlobal(client);
		obstructionFilter.updateGlobal(client);
	}

	public void updateSoundInstance(SoundInstance soundInstance, int sourceID) {
		if (disabled)
			return;
		if (Config.getData().general.isIgnoredSoundEvent(soundInstance.getId()))
			return;

		boolean includeReverb = reverbFilter.updateSoundInstance(soundInstance);
		boolean includeLowPass = obstructionFilter.updateSoundInstance(soundInstance);

		AL11.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, includeLowPass ? obstructionFilter.getFilterId() : 0);
		AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, includeReverb ? reverbFilter.getEffectSlot() : 0, 0,
				includeLowPass ? obstructionFilter.getFilterId() : 0);

		// retry once on error
		int error = AL11.alGetError();
		if (error != AL11.AL_NO_ERROR) {
			DynamicSoundFilters.getLogger().warn("OpenAL error when applying sound filters: " + error);
			DynamicSoundFilters.getLogger().warn("Retrying...");

			reverbFilter.reinit();
			obstructionFilter.reinit();

			AL11.alSourcei(sourceID, EXTEfx.AL_DIRECT_FILTER, includeLowPass ? obstructionFilter.getFilterId() : 0);
			AL11.alSource3i(sourceID, EXTEfx.AL_AUXILIARY_SEND_FILTER, includeReverb ? reverbFilter.getEffectSlot() : 0, 0,
					includeLowPass ? obstructionFilter.getFilterId() : 0);
		}

		// report further errors
		error = AL11.alGetError();
		if (error != AL11.AL_NO_ERROR) {
			DynamicSoundFilters.getLogger().error("OpenAL error when applying sound filters: " + error);
			DynamicSoundFilters.getLogger().error("Cannot apply filters - disabling");
			disabled = true;
		}
	}
}
