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
package me.andre111.dynamicsf.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.lwjgl.openal.EXTEfx;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ConfigScreen implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create();
			builder.setParentScreen(parent);
			builder.setTitle(Text.translatable("dynamicsoundfilters.config.title"));
			ConfigEntryBuilder entryBuilder = builder.entryBuilder();

			builder.setSavingRunnable(Config::saveData);

			ConfigCategory generalCat = builder.getOrCreateCategory(Text.translatable("dynamicsoundfilters.config.general"));
			{
				generalCat.addEntry(entryBuilder
						.startStrList(Text.translatable("dynamicsoundfilters.config.general.ignored"),
								Config.getData().general.ignoredSoundEvents)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.general.ignored.tooltip"))
						.setDefaultValue(ConfigDataGeneral.DEFAULT_IGNORED_SOUND_EVENTS)
						.setSaveConsumer(l -> {
							Config.getData().general.ignoredSoundEvents = l;
							Config.getData().general.recalculateCache();
						})
						.build());
			}

			ConfigCategory liquidFilterCat = builder
					.getOrCreateCategory(Text.translatable("dynamicsoundfilters.config.liquid"));
			{
				liquidFilterCat.addEntry(entryBuilder
						.startBooleanToggle(Text.translatable("dynamicsoundfilters.config.liquid.enable"),
								Config.getData().liquidFilter.enabled)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.liquid.enable.tooltip"))
						.setDefaultValue(true)
						.setSaveConsumer(b -> Config.getData().liquidFilter.enabled = b)
						.build());

				SubCategoryBuilder waterCat = entryBuilder
						.startSubCategory(Text.translatable("dynamicsoundfilters.config.liquid.water"));
				{
					waterCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.liquid.gain"),
									Config.getData().liquidFilter.waterGain)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.liquid.gain.tooltip"))
							.setDefaultValue(0.8f)
							.setMin(EXTEfx.AL_LOWPASS_MIN_GAIN)
							.setMax(EXTEfx.AL_LOWPASS_MAX_GAIN)
							.setSaveConsumer(f -> Config.getData().liquidFilter.waterGain = f)
							.build());
					waterCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.liquid.gainhf"),
									Config.getData().liquidFilter.waterGainHF)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.liquid.gainhf.tooltip"))
							.setDefaultValue(0.3f)
							.setMin(EXTEfx.AL_LOWPASS_MIN_GAINHF)
							.setMax(EXTEfx.AL_LOWPASS_MAX_GAINHF)
							.setSaveConsumer(f -> Config.getData().liquidFilter.waterGainHF = f)
							.build());
				}
				liquidFilterCat.addEntry(waterCat.build());

				SubCategoryBuilder lavaCat = entryBuilder
						.startSubCategory(Text.translatable("dynamicsoundfilters.config.liquid.lava"));
				{
					lavaCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.liquid.gain"),
									Config.getData().liquidFilter.lavaGain)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.liquid.gain.tooltip"))
							.setDefaultValue(0.5f)
							.setMin(EXTEfx.AL_LOWPASS_MIN_GAIN)
							.setMax(EXTEfx.AL_LOWPASS_MAX_GAIN)
							.setSaveConsumer(f -> Config.getData().liquidFilter.lavaGain = f)
							.build());
					lavaCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.liquid.gainhf"),
									Config.getData().liquidFilter.lavaGainHF)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.liquid.gainhf.tooltip"))
							.setDefaultValue(0.1f)
							.setMin(EXTEfx.AL_LOWPASS_MIN_GAINHF)
							.setMax(EXTEfx.AL_LOWPASS_MAX_GAINHF)
							.setSaveConsumer(f -> Config.getData().liquidFilter.lavaGainHF = f)
							.build());
				}
				liquidFilterCat.addEntry(lavaCat.build());
			}

			ConfigCategory reverbFilterCat = builder
					.getOrCreateCategory(Text.translatable("dynamicsoundfilters.config.reverb"));
			{
				reverbFilterCat.addEntry(entryBuilder
						.startBooleanToggle(Text.translatable("dynamicsoundfilters.config.reverb.enable"),
								Config.getData().reverbFilter.enabled)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.enable.tooltip"))
						.setDefaultValue(true)
						.setSaveConsumer(b -> Config.getData().reverbFilter.enabled = b)
						.build());

				reverbFilterCat.addEntry(entryBuilder
						.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.percent"),
								Config.getData().reverbFilter.reverbPercent)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.percent.tooltip"))
						.setDefaultValue(1.0f)
						.setMin(0.0f)
						.setMax(2.0f)
						.setSaveConsumer(f -> Config.getData().reverbFilter.reverbPercent = f)
						.build());

				reverbFilterCat.addEntry(entryBuilder
						.startStrList(Text.translatable("dynamicsoundfilters.config.reverb.dimensions"),
								Config.getData().reverbFilter.dimensionBaseReverb)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.dimensions.tooltip1"),
								Text.translatable("dynamicsoundfilters.config.reverb.dimensions.tooltip2"),
								Text.translatable("dynamicsoundfilters.config.reverb.dimensions.tooltip3"),
								Text.translatable("dynamicsoundfilters.config.reverb.dimensions.tooltip4"))
						.setDefaultValue(Arrays.asList("minecraft:the_nether;1.0"))
						.setSaveConsumer(l -> {
							Config.getData().reverbFilter.dimensionBaseReverb = l;
							Config.getData().reverbFilter.recalculateCache();
						})
						.build());

				SubCategoryBuilder scannerCat = entryBuilder
						.startSubCategory(Text.translatable("dynamicsoundfilters.config.reverb.scanner"));
				scannerCat.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.scanner.tooltip"));
				{
					scannerCat.add(entryBuilder
							.startIntField(Text.translatable("dynamicsoundfilters.config.reverb.scanner.blocks"),
									Config.getData().reverbFilter.maxBlocks)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.scanner.blocks.tooltip"))
							.setDefaultValue(1024)
							.setMin(0)
							.setMax(Integer.MAX_VALUE)
							.setSaveConsumer(i -> Config.getData().reverbFilter.maxBlocks = i)
							.build());

					scannerCat.add(entryBuilder
							.startBooleanToggle(Text.translatable("dynamicsoundfilters.config.reverb.scanner.checksky"),
									Config.getData().reverbFilter.checkSky)
							.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.scanner.checksky.tooltip"))
							.setDefaultValue(true)
							.setSaveConsumer(b -> Config.getData().reverbFilter.checkSky = b)
							.build());

					scannerCat.add(entryBuilder
							.startStrList(Text.translatable("dynamicsoundfilters.config.reverb.blocks.low"),
									Config.getData().reverbFilter.lowReverbBlocks.stream().map(Identifier::toString)
											.collect(Collectors.toList()))
							.setDefaultValue(ConfigDataReverbFilter.DEFAULT_LOW_REVERB_BLOCKS.stream().map(Identifier::toString)
									.collect(Collectors.toList()))
							.setSaveConsumer(l -> Config.getData().reverbFilter.lowReverbBlocks = l.stream().map(Identifier::of)
									.collect(Collectors.toList()))
							.build());

					scannerCat.add(entryBuilder
							.startStrList(Text.translatable("dynamicsoundfilters.config.reverb.blocks.high"),
									Config.getData().reverbFilter.highReverbBlocks.stream().map(Identifier::toString)
											.collect(Collectors.toList()))
							.setDefaultValue(ConfigDataReverbFilter.DEFAULT_HIGH_REVERB_BLOCKS.stream().map(Identifier::toString)
									.collect(Collectors.toList()))
							.setSaveConsumer(l -> Config.getData().reverbFilter.highReverbBlocks = l.stream().map(Identifier::of)
									.collect(Collectors.toList()))
							.build());
				}
				reverbFilterCat.addEntry(scannerCat.build());

				SubCategoryBuilder advancedCat = entryBuilder
						.startSubCategory(Text.translatable("dynamicsoundfilters.config.reverb.advanced"));
				advancedCat.setTooltip(Text.translatable("dynamicsoundfilters.config.reverb.advanced.tooltip"));
				{
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.density"),
									Config.getData().reverbFilter.density)
							.setDefaultValue(0.2f)
							.setMin(EXTEfx.AL_REVERB_MIN_DENSITY)
							.setMax(EXTEfx.AL_REVERB_MAX_DENSITY)
							.setSaveConsumer(f -> Config.getData().reverbFilter.density = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.diffusion"),
									Config.getData().reverbFilter.diffusion)
							.setDefaultValue(0.6f)
							.setMin(EXTEfx.AL_REVERB_MIN_DIFFUSION)
							.setMax(EXTEfx.AL_REVERB_MAX_DIFFUSION)
							.setSaveConsumer(f -> Config.getData().reverbFilter.diffusion = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.gain"),
									Config.getData().reverbFilter.gain)
							.setDefaultValue(0.15f)
							.setMin(EXTEfx.AL_REVERB_MIN_GAIN)
							.setMax(EXTEfx.AL_REVERB_MAX_GAIN)
							.setSaveConsumer(f -> Config.getData().reverbFilter.gain = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.gainhf"),
									Config.getData().reverbFilter.gainHF)
							.setDefaultValue(0.8f)
							.setMin(EXTEfx.AL_REVERB_MIN_GAINHF)
							.setMax(EXTEfx.AL_REVERB_MAX_GAINHF)
							.setSaveConsumer(f -> Config.getData().reverbFilter.gainHF = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.mindecaytime"),
									Config.getData().reverbFilter.minDecayTime)
							.setDefaultValue(0.1f)
							.setMin(EXTEfx.AL_REVERB_MIN_DECAY_TIME)
							.setMax(EXTEfx.AL_REVERB_MAX_DECAY_TIME)
							.setSaveConsumer(f -> Config.getData().reverbFilter.minDecayTime = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.decayhfratio"),
									Config.getData().reverbFilter.decayHFRatio)
							.setDefaultValue(0.7f)
							.setMin(EXTEfx.AL_REVERB_MIN_DECAY_HFRATIO)
							.setMax(EXTEfx.AL_REVERB_MAX_DECAY_HFRATIO)
							.setSaveConsumer(f -> Config.getData().reverbFilter.decayHFRatio = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.airabsorptiongainhf"),
									Config.getData().reverbFilter.airAbsorptionGainHF)
							.setDefaultValue(0.99f)
							.setMin(EXTEfx.AL_REVERB_MIN_AIR_ABSORPTION_GAINHF)
							.setMax(EXTEfx.AL_REVERB_MAX_AIR_ABSORPTION_GAINHF)
							.setSaveConsumer(f -> Config.getData().reverbFilter.airAbsorptionGainHF = f)
							.build());

					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.reflectionsgainbase"),
									Config.getData().reverbFilter.reflectionsGainBase)
							.setDefaultValue(0.05f)
							.setMin(0.0f)
							.setMax(1.58f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.reflectionsGainBase = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(
									Text.translatable("dynamicsoundfilters.config.reverb.advanced.reflectionsgainmultiplier"),
									Config.getData().reverbFilter.reflectionsGainMultiplier)
							.setDefaultValue(0.05f)
							.setMin(0.0f)
							.setMax(1.58f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.reflectionsGainMultiplier = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(
									Text.translatable("dynamicsoundfilters.config.reverb.advanced.reflectionsdelaymultiplier"),
									Config.getData().reverbFilter.reflectionsDelayMultiplier)
							.setDefaultValue(0.025f)
							.setMin(0.0f)
							.setMax(0.3f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.reflectionsDelayMultiplier = f)
							.build());

					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.latereverbgainbase"),
									Config.getData().reverbFilter.lateReverbGainBase)
							.setDefaultValue(1.26f)
							.setMin(0.0f)
							.setMax(5.0f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.lateReverbGainBase = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(Text.translatable("dynamicsoundfilters.config.reverb.advanced.latereverbgainmultiplier"),
									Config.getData().reverbFilter.lateReverbGainMultiplier)
							.setDefaultValue(0.1f)
							.setMin(0.0f)
							.setMax(5.0f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.lateReverbGainMultiplier = f)
							.build());
					advancedCat.add(entryBuilder
							.startFloatField(
									Text.translatable("dynamicsoundfilters.config.reverb.advanced.latereverbdelaymultiplier"),
									Config.getData().reverbFilter.lateReverbDelayMultiplier)
							.setDefaultValue(0.01f)
							.setMin(0.0f)
							.setMax(0.1f)
							.setSaveConsumer(f -> Config.getData().reverbFilter.lateReverbDelayMultiplier = f)
							.build());
				}
				reverbFilterCat.addEntry(advancedCat.build());
			}

			ConfigCategory obstructionFilterCat = builder
					.getOrCreateCategory(Text.translatable("dynamicsoundfilters.config.obstruction"));
			{
				obstructionFilterCat.addEntry(entryBuilder
						.startBooleanToggle(Text.translatable("dynamicsoundfilters.config.obstruction.enable"),
								Config.getData().obstructionFilter.enabled)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.obstruction.enable.tooltip"))
						.setDefaultValue(true)
						.setSaveConsumer(b -> Config.getData().obstructionFilter.enabled = b)
						.build());

				obstructionFilterCat.addEntry(entryBuilder
						.startFloatField(Text.translatable("dynamicsoundfilters.config.obstruction.step"),
								Config.getData().obstructionFilter.obstructionStep)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.obstruction.step.tooltip"))
						.setDefaultValue(0.1f)
						.setMin(0.0f)
						.setMax(1.0f)
						.setSaveConsumer(f -> Config.getData().obstructionFilter.obstructionStep = f)
						.build());
				obstructionFilterCat.addEntry(entryBuilder
						.startFloatField(Text.translatable("dynamicsoundfilters.config.obstruction.max"),
								Config.getData().obstructionFilter.obstructionMax)
						.setTooltip(Text.translatable("dynamicsoundfilters.config.obstruction.max.tooltip"))
						.setDefaultValue(0.98f)
						.setMin(0.0f)
						.setMax(1.0f)
						.setSaveConsumer(f -> Config.getData().obstructionFilter.obstructionMax = f)
						.build());

				obstructionFilterCat.addEntry(entryBuilder
						.startStrList(Text.translatable("dynamicsoundfilters.config.obstruction.blocks"),
								Config.getData().obstructionFilter.highObstructionBlocks.stream().map(Identifier::toString)
										.collect(Collectors.toList()))
						.setDefaultValue(ConfigDataObstructionFilter.DEFAULT_HIGH_OBSTRUCTION_BLOCKS.stream()
								.map(Identifier::toString).collect(Collectors.toList()))
						.setSaveConsumer(l -> Config.getData().obstructionFilter.highObstructionBlocks = l.stream()
								.map(Identifier::of).collect(Collectors.toList()))
						.build());
			}

			return builder.build();
		};
	}
}
