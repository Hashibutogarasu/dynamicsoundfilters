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

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

import me.andre111.dynamicsf.DynamicSoundFilters;

/**
 * Abstract class for OpenAL filters.
 */
public abstract class AbstractOpenALFilter extends AbstractSoundFilter {

  protected int filterId = -1;

  protected AbstractOpenALFilter(String name) {
    super(name);
  }

  @Override
  public final void reinit() {
    // Cleanup existing resources
    cleanupResources();

    // Initialize new resources
    safeInitializeResource(this::initializeOpenALResources, "OpenAL resources");
  }

  /**
   * Initialize OpenAL resources (to be implemented by subclasses)
   */
  protected abstract void initializeOpenALResources();

  /**
   * Cleanup resources
   */
  protected void cleanupResources() {
    if (filterId > 0) {
      try {
        EXTEfx.alDeleteFilters(filterId);
      } catch (Exception e) {
        DynamicSoundFilters.getLogger().warn("Failed to delete filter {} for {}: {}",
            filterId, getName(), e.getMessage());
      }
      filterId = -1;
    }
  }

  /**
   * Get the filter ID
   * 
   * @return Filter ID
   */
  public final int getFilterId() {
    return filterId;
  }

  /**
   * Check for OpenAL errors and log them
   * 
   * @param operation Name of the operation performed
   * @return true if there was an error
   */
  protected final boolean checkOpenALError(String operation) {
    int error = AL11.alGetError();
    if (error != AL11.AL_NO_ERROR) {
      DynamicSoundFilters.getLogger().warn("OpenAL error in {} filter during {}: {}",
          getName(), operation, error);
      return true;
    }
    return false;
  }

  @Override
  protected final void reset() {
    enabled = false;
    resetFilterState();
  }

  /**
   * Reset the filter's specific state (to be implemented by subclasses)
   */
  protected abstract void resetFilterState();
}
