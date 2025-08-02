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

import org.lwjgl.openal.EXTEfx;

import me.andre111.dynamicsf.DynamicSoundFilters;

/**
 * リバーブフィルター用の抽象クラス：エフェクトスロットとエフェクトの管理を統一
 */
public abstract class AbstractReverbFilter extends AbstractSoundFilter {
    
    protected int effectId = -1;
    protected int effectSlot = -1;
    
    protected AbstractReverbFilter(String name) {
        super(name);
    }
    
    @Override
    public final void reinit() {
        // 既存のリソースを削除
        cleanupResources();
        
        // 新しいリソースを初期化
        safeInitializeResource(this::initializeReverbResources, "Reverb resources");
    }
    
    /**
     * リバーブリソースの初期化
     */
    private void initializeReverbResources() {
        effectId = EXTEfx.alGenEffects();
        effectSlot = EXTEfx.alGenAuxiliaryEffectSlots();
        
        if (effectId <= 0 || effectSlot <= 0) {
            throw new RuntimeException("Failed to generate OpenAL reverb resources");
        }
        
        // エフェクトタイプを設定
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);
    }
    
    /**
     * リソースのクリーンアップ
     */
    protected void cleanupResources() {
        if (effectId > 0) {
            try {
                EXTEfx.alDeleteEffects(effectId);
            } catch (Exception e) {
                DynamicSoundFilters.getLogger().warn("Failed to delete effect {} for {}: {}", 
                    effectId, getName(), e.getMessage());
            }
            effectId = -1;
        }
        
        if (effectSlot > 0) {
            try {
                EXTEfx.alDeleteAuxiliaryEffectSlots(effectSlot);
            } catch (Exception e) {
                DynamicSoundFilters.getLogger().warn("Failed to delete effect slot {} for {}: {}", 
                    effectSlot, getName(), e.getMessage());
            }
            effectSlot = -1;
        }
    }
    
    /**
     * エフェクトスロットを取得
     * @return エフェクトスロット
     */
    public final int getEffectSlot() {
        return effectSlot;
    }
    
    /**
     * リバーブパラメータを安全に設定
     * @param parameter パラメータ
     * @param value 値
     */
    protected final void safeSetReverbParameter(int parameter, float value) {
        try {
            EXTEfx.alEffectf(effectId, parameter, value);
        } catch (Exception e) {
            DynamicSoundFilters.getLogger().warn("Failed to set reverb parameter {} to {} for {}: {}", 
                parameter, value, getName(), e.getMessage());
        }
    }
    
    /**
     * エフェクトをスロットに適用
     */
    protected final void applyEffectToSlot() {
        try {
            EXTEfx.alAuxiliaryEffectSloti(effectSlot, EXTEfx.AL_EFFECTSLOT_EFFECT, effectId);
            EXTEfx.alAuxiliaryEffectSlotf(effectSlot, EXTEfx.AL_EFFECTSLOT_GAIN, 1.0f);
        } catch (Exception e) {
            DynamicSoundFilters.getLogger().warn("Failed to apply effect to slot for {}: {}", 
                getName(), e.getMessage());
        }
    }
    
    @Override
    protected final void reset() {
        enabled = false;
        resetReverbState();
    }
    
    /**
     * リバーブ固有の状態をリセット（サブクラスで実装）
     */
    protected abstract void resetReverbState();
}
