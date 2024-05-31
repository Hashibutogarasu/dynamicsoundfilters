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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;

public class ConfigDataReverbFilter {
	public static final List<Identifier> DEFAULT_LOW_REVERB_BLOCKS = new ArrayList<>();
	public static final List<Identifier> DEFAULT_HIGH_REVERB_BLOCKS = new ArrayList<>();
	static {
		for(Block block : Registries.BLOCK) {
			Identifier blockID = Registries.BLOCK.getId(block);
			BlockState state = block.getDefaultState();
			
			// special overrides
			if(state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent()).isEmpty()) {
				continue;
			}
			if(block instanceof FlowerPotBlock) {
				DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
				continue;
			}
			
			BlockSoundGroup soundGroup = state.getSoundGroup();
			// Groups in comments are skipped -> mid reverb
			// INTENTIONALLY_EMPTY
			if(soundGroup == BlockSoundGroup.WOOD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// GRAVEL
			if(soundGroup == BlockSoundGroup.GRASS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.LILY_PAD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.STONE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.METAL) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.GLASS) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.WOOL) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// SAND
			if(soundGroup == BlockSoundGroup.SNOW) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.POWDER_SNOW) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// LADDER, ANVIL
			if(soundGroup == BlockSoundGroup.SLIME) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.HONEY) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.WET_GRASS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// CORAL, BAMBOO, BAMBOO_SAPLING, SCAFFOLDING
			if(soundGroup == BlockSoundGroup.SWEET_BERRY_BUSH) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.CROP) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.STEM) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.VINE) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.NETHER_WART) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// LANTERN, NETHER_STEM, NYLIUM, FUNGUS
			if(soundGroup == BlockSoundGroup.ROOTS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SHROOMLIGHT) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.WEEPING_VINES) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.WEEPING_VINES_LOW_PITCH) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// SOUL_SAND, SOUL_SOIL
			if(soundGroup == BlockSoundGroup.BASALT) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// WART_BLOCK, NETHERRACK
			if(soundGroup == BlockSoundGroup.NETHER_BRICKS) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// NETHER_SPROUTS, NETHER_ORE
			if(soundGroup == BlockSoundGroup.BONE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.NETHERITE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.ANCIENT_DEBRIS) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.LODESTONE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// CHAIN, NETHER_GOLD_ORE
			if(soundGroup == BlockSoundGroup.GILDED_BLACKSTONE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// CANDLE
			if(soundGroup == BlockSoundGroup.AMETHYST_BLOCK) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// AMETHYST_CLUSTER, SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD
			if(soundGroup == BlockSoundGroup.TUFF) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.CALCITE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.DRIPSTONE_BLOCK) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.POINTED_DRIPSTONE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.COPPER) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.CAVE_VINES) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SPORE_BLOSSOM) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.AZALEA) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.FLOWERING_AZALEA) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.MOSS_CARPET) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.PINK_PETALS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.MOSS_BLOCK) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.BIG_DRIPLEAF) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SMALL_DRIPLEAF) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// ROOTED_DIRT, HANGING_ROOTS
			if(soundGroup == BlockSoundGroup.AZALEA_LEAVES) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SCULK_SENSOR) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SCULK_CATALYST) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SCULK) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SCULK_VEIN) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.SCULK_SHRIEKER) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// GLOW_LICHEN
			if(soundGroup == BlockSoundGroup.DEEPSLATE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.DEEPSLATE_BRICKS) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.DEEPSLATE_TILES) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.POLISHED_DEEPSLATE) DEFAULT_HIGH_REVERB_BLOCKS.add(blockID);
			// FROGLIGHT, FROGSPAWN
			if(soundGroup == BlockSoundGroup.MANGROVE_ROOTS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.MUDDY_MANGROVE_ROOTS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.MUD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.MUD_BRICKS) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.PACKED_MUD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// HANGING_SIGN, NETHER_WOOD_HANGING_SIGN, BAMBOO_WOOD_HANGING_SIGN
			if(soundGroup == BlockSoundGroup.BAMBOO_WOOD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.NETHER_WOOD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.CHERRY_WOOD) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// CHERRY_SAPLING
			if(soundGroup == BlockSoundGroup.CHERRY_LEAVES) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// CHERRY_WOOD_HANGING_SIGN
			if(soundGroup == BlockSoundGroup.CHISELED_BOOKSHELF) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			// SUSPICIOUS_SAND, SUSPICIOUS_GRAVEL, DECORATED_POT, DECORATED_POT_SHATTER
			if(soundGroup == BlockSoundGroup.SPONGE) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
			if(soundGroup == BlockSoundGroup.WET_SPONGE) DEFAULT_LOW_REVERB_BLOCKS.add(blockID);
		}
	}
	
	public boolean enabled = true;
	
	public float reverbPercent = 1.0f;
	
	public int maxBlocks = 1024;
	public boolean checkSky = true;
	
	public List<String> dimensionBaseReverb = Arrays.asList("minecraft:the_nether;1.0");
	public List<Identifier> lowReverbBlocks = new ArrayList<>(DEFAULT_LOW_REVERB_BLOCKS);
	public List<Identifier> highReverbBlocks = new ArrayList<>(DEFAULT_HIGH_REVERB_BLOCKS);
	
	public float density = 0.2f;
	public float diffusion = 0.6f;
	public float gain = 0.15f;
	public float gainHF = 0.8f;
	public float minDecayTime = 0.1f;
	public float decayHFRatio = 0.7f;
	public float airAbsorptionGainHF = 0.99f;
	public float reflectionsGainBase = 0.05f;
	public float reflectionsGainMultiplier = 0.05f;
	public float reflectionsDelayMultiplier = 0.025f;
	public float lateReverbGainBase = 1.26f;
	public float lateReverbGainMultiplier = 0.1f;
	public float lateReverbDelayMultiplier = 0.01f;
	
	
	//-----------------------------------------------------------------------
	private transient boolean cached = false;
	private transient Map<Identifier, Float> dimensionBaseReverbMap = new HashMap<>();
	
	private void calculateCache() {
		if(!cached) {
			dimensionBaseReverbMap = ConfigHelper.parseToMap(dimensionBaseReverb, Identifier::new, Float::parseFloat);
			
			cached = true;
		}
	}
	
	public void recalculateCache() {
		cached = false;
		calculateCache();
	}
	
	public float getDimensionBaseReverb(Identifier dimension) {
		calculateCache();
		
		if(dimensionBaseReverbMap.containsKey(dimension)) {
			return dimensionBaseReverbMap.get(dimension);
		}
		
		return 0f;
	}
	
}
