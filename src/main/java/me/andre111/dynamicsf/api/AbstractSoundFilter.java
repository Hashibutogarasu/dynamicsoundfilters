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

import me.andre111.dynamicsf.DynamicSoundFilters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;

/**
 * Abstract class providing common filter implementation.
 */
public abstract class AbstractSoundFilter implements ISoundFilter {

  protected boolean enabled = false;
  private final String name;

  protected AbstractSoundFilter(String name) {
    this.name = name;
  }

  @Override
  public final void updateGlobal(MinecraftClient client) {
    if (client.world != null && client.player != null && client.isRunning()) {
      update(client);
    } else {
      reset();
    }
  }

  @Override
  public final boolean updateSoundInstance(SoundInstance soundInstance) {
    if (!enabled) {
      return false;
    }

    return applyFilter(soundInstance);
  }

  @Override
  public final boolean isEnabled() {
    return enabled;
  }

  @Override
  public final String getName() {
    return name;
  }

  /**
   * Filter's specific update process (to be implemented by subclasses)
   * 
   * @param client MinecraftClient instance
   */
  protected abstract void update(MinecraftClient client);

  /**
   * Filter's reset process (to be implemented by subclasses)
   */
  protected abstract void reset();

  /**
   * Filter's specific application process (to be implemented by subclasses)
   * 
   * @param soundInstance Target sound instance
   * @return true if the filter was applied
   */
  protected abstract boolean applyFilter(SoundInstance soundInstance);

  /**
   * Initialize OpenAL resources with error handling
   * 
   * @param resourceInitializer Resource initialization process
   * @param resourceName        Resource name (for logging)
   */
  protected final void safeInitializeResource(Runnable resourceInitializer, String resourceName) {
    try {
      resourceInitializer.run();
    } catch (Exception e) {
      DynamicSoundFilters.getLogger().error("Failed to initialize {} for filter {}: {}",
          resourceName, name, e.getMessage());
      enabled = false;
    }
  }
}
