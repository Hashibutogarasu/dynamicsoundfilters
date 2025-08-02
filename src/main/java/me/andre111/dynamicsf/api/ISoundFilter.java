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
package me.andre111.dynamicsf.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;

/**
 * Interface for sound filters in Dynamic Sound Filters.
 */
public interface ISoundFilter {

  /**
   * Update the filter state globally.
   * 
   * @param client MinecraftClient instance
   */
  void updateGlobal(MinecraftClient client);

  /**
   * Determine whether to apply the filter to a specific sound instance and apply
   * it if necessary.
   * 
   * @param soundInstance Target sound instance
   * @return true if the filter was applied
   */
  boolean updateSoundInstance(SoundInstance soundInstance);

  /**
   * Reinitialize the filter's OpenAL resources.
   */
  void reinit();

  /**
   * Return whether the filter is enabled.
   * 
   * @return true if enabled
   */
  boolean isEnabled();

  /**
   * Return the filter's name (for debugging purposes).
   * 
   * @return Filter name
   */
  String getName();
}
