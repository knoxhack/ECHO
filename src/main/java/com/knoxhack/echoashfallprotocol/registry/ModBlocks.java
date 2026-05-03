package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoAshfallProtocol.MODID);
    // We register BlockItems alongside blocks using the items register from ModItems
    public static final DeferredRegister.Items BLOCK_ITEMS = DeferredRegister.createItems(EchoAshfallProtocol.MODID);

    // === ENVIRONMENTAL BLOCKS ===
    public static final DeferredBlock<Block> DEBRIS_BLOCK = registerCustomBlock("debris_block",
            DebrisBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.0f, 2.0f)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DEBRIS_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("debris_block", DEBRIS_BLOCK);

    public static final DeferredBlock<Block> TOXIC_PUDDLE = registerCustomBlock("toxic_puddle",
            ToxicPuddleBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.5f)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .speedFactor(0.6f));
    public static final DeferredItem<BlockItem> TOXIC_PUDDLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_puddle", TOXIC_PUDDLE);

    public static final DeferredBlock<Block> RADIATION_BLOCK = BLOCKS.registerSimpleBlock("radiation_block",
            p -> p.mapColor(MapColor.COLOR_YELLOW)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .lightLevel(s -> 7));
    public static final DeferredItem<BlockItem> RADIATION_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("radiation_block", RADIATION_BLOCK);

    // === WASTELAND HAZARD BLOCKS ===
    public static final DeferredBlock<Block> ACIDIC_SLUDGE = registerCustomBlock("acidic_sludge",
            AcidicSludgeBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.3f)
                    .sound(SoundType.HONEY_BLOCK)
                    .noOcclusion()
                    .speedFactor(0.4f));
    public static final DeferredItem<BlockItem> ACIDIC_SLUDGE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("acidic_sludge", ACIDIC_SLUDGE);

    public static final DeferredBlock<Block> FALLOUT_DUST = registerCustomBlock("fallout_dust",
            FalloutDustBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.2f)
                    .sound(SoundType.SAND)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> FALLOUT_DUST_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("fallout_dust", FALLOUT_DUST);

    public static final DeferredBlock<Block> CONTAMINATED_SOIL = registerCustomBlock("contaminated_soil",
            ContaminatedSoilBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> CONTAMINATED_SOIL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("contaminated_soil", CONTAMINATED_SOIL);

    // === WASTELAND TERRAIN SURFACES ===
    public static final DeferredBlock<Block> WASTELAND_DIRT = BLOCKS.registerSimpleBlock("wasteland_dirt",
            p -> p.mapColor(MapColor.DIRT)
                    .strength(0.55f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> WASTELAND_DIRT_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_dirt", WASTELAND_DIRT);

    public static final DeferredBlock<Block> WASTELAND_GRASS_BLOCK = BLOCKS.registerSimpleBlock("wasteland_grass_block",
            p -> p.mapColor(MapColor.COLOR_BROWN)
                    .strength(0.6f)
                    .sound(SoundType.GRASS));
    public static final DeferredItem<BlockItem> WASTELAND_GRASS_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_grass_block", WASTELAND_GRASS_BLOCK);

    public static final DeferredBlock<Block> ASHEN_WASTELAND_DIRT = BLOCKS.registerSimpleBlock("ashen_wasteland_dirt",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ASHEN_WASTELAND_DIRT_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ashen_wasteland_dirt", ASHEN_WASTELAND_DIRT);

    public static final DeferredBlock<Block> BURNT_WASTELAND_SOIL = BLOCKS.registerSimpleBlock("burnt_wasteland_soil",
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(0.65f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> BURNT_WASTELAND_SOIL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("burnt_wasteland_soil", BURNT_WASTELAND_SOIL);

    public static final DeferredBlock<Block> TOXIC_WASTELAND_GRASS_BLOCK = BLOCKS.registerSimpleBlock("toxic_wasteland_grass_block",
            p -> p.mapColor(MapColor.COLOR_GREEN)
                    .strength(0.6f)
                    .sound(SoundType.GRASS)
                    .speedFactor(0.92f));
    public static final DeferredItem<BlockItem> TOXIC_WASTELAND_GRASS_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_wasteland_grass_block", TOXIC_WASTELAND_GRASS_BLOCK);

    public static final DeferredBlock<Block> MUTATED_WASTELAND_GRASS_BLOCK = BLOCKS.registerSimpleBlock("mutated_wasteland_grass_block",
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.6f)
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 1));
    public static final DeferredItem<BlockItem> MUTATED_WASTELAND_GRASS_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("mutated_wasteland_grass_block", MUTATED_WASTELAND_GRASS_BLOCK);

    public static final DeferredBlock<Block> IRRADIATED_CRUST = BLOCKS.registerSimpleBlock("irradiated_crust",
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.8f)
                    .sound(SoundType.GRAVEL)
                    .lightLevel(s -> 2));
    public static final DeferredItem<BlockItem> IRRADIATED_CRUST_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("irradiated_crust", IRRADIATED_CRUST);

    public static final DeferredBlock<Block> NEXUS_CRACKED_SOIL = BLOCKS.registerSimpleBlock("nexus_cracked_soil",
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.85f)
                    .sound(SoundType.GRAVEL)
                    .lightLevel(s -> 3));
    public static final DeferredItem<BlockItem> NEXUS_CRACKED_SOIL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nexus_cracked_soil", NEXUS_CRACKED_SOIL);

    public static final DeferredBlock<Block> OIL_STAINED_CONCRETE = registerCustomBlock("oil_stained_concrete",
            OilStainedConcreteBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .speedFactor(0.7f));
    public static final DeferredItem<BlockItem> OIL_STAINED_CONCRETE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("oil_stained_concrete", OIL_STAINED_CONCRETE);

    public static final DeferredBlock<Block> CRACKED_ASPHALT = BLOCKS.registerSimpleBlock("cracked_asphalt",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(1.25f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CRACKED_ASPHALT_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("cracked_asphalt", CRACKED_ASPHALT);

    public static final DeferredBlock<Block> CONCRETE_RUBBLE = registerCustomBlock("concrete_rubble",
            ConcreteRubbleBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.8f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> CONCRETE_RUBBLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("concrete_rubble", CONCRETE_RUBBLE);

    public static final DeferredBlock<Block> RUSTED_METAL_SHEET = registerCustomBlock("rusted_metal_sheet",
            RustedMetalSheetBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> RUSTED_METAL_SHEET_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rusted_metal_sheet", RUSTED_METAL_SHEET);

    public static final DeferredBlock<Block> TOXIC_WASTE_BARREL = registerCustomBlock("toxic_waste_barrel",
            ToxicWasteBarrelBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(2.0f)
                    .sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> TOXIC_WASTE_BARREL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_waste_barrel", TOXIC_WASTE_BARREL);

    public static final DeferredBlock<Block> MUTATED_BUSH = registerCustomBlock("mutated_bush",
            HazardousBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> MUTATED_BUSH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("mutated_bush", MUTATED_BUSH);

    // === WASTELAND VEGETATION ===
    public static final DeferredBlock<Block> DEAD_WOOD_LOG = registerCustomBlock("dead_wood_log",
            p -> new net.minecraft.world.level.block.RotatedPillarBlock(p), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> DEAD_WOOD_LOG_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("dead_wood_log", DEAD_WOOD_LOG);

    public static final DeferredBlock<Block> CHARRED_WOOD_LOG = registerCustomBlock("charred_wood_log",
            p -> new net.minecraft.world.level.block.RotatedPillarBlock(p), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0f)
                    .sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> CHARRED_WOOD_LOG_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("charred_wood_log", CHARRED_WOOD_LOG);

    public static final DeferredBlock<Block> DRY_GRASS = registerCustomBlock("dry_grass",
            HazardousBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> DRY_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("dry_grass", DRY_GRASS);

    public static final DeferredBlock<Block> DRY_TALL_GRASS = registerCustomBlock("dry_tall_grass",
            HazardousDoublePlantBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> DRY_TALL_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("dry_tall_grass", DRY_TALL_GRASS);

    // === BIOME-TINTED GRASS OVERHAUL ===
    public static final DeferredBlock<Block> WASTELAND_GRASS = registerCustomBlock("wasteland_grass",
            BiomeTintedGrassBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> WASTELAND_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_grass", WASTELAND_GRASS);

    public static final DeferredBlock<Block> WASTELAND_TALL_GRASS = registerCustomBlock("wasteland_tall_grass",
            HazardousDoublePlantBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> WASTELAND_TALL_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_tall_grass", WASTELAND_TALL_GRASS);

    public static final DeferredBlock<Block> TOXIC_GRASS = registerCustomBlock("toxic_grass",
            BiomeTintedGrassBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> TOXIC_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_grass", TOXIC_GRASS);

    public static final DeferredBlock<Block> TOXIC_TALL_GRASS = registerCustomBlock("toxic_tall_grass",
            HazardousDoublePlantBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> TOXIC_TALL_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_tall_grass", TOXIC_TALL_GRASS);

    public static final DeferredBlock<Block> NUCLEAR_GRASS = registerCustomBlock("nuclear_grass",
            BiomeTintedGrassBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 3)
                    .randomTicks()
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> NUCLEAR_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nuclear_grass", NUCLEAR_GRASS);

    public static final DeferredBlock<Block> NUCLEAR_TALL_GRASS = registerCustomBlock("nuclear_tall_grass",
            HazardousDoublePlantBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 4)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> NUCLEAR_TALL_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nuclear_tall_grass", NUCLEAR_TALL_GRASS);

    public static final DeferredBlock<Block> BURNT_GRASS = registerCustomBlock("burnt_grass",
            BiomeTintedGrassBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> BURNT_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("burnt_grass", BURNT_GRASS);

    public static final DeferredBlock<Block> BURNT_TALL_GRASS = registerCustomBlock("burnt_tall_grass",
            HazardousDoublePlantBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> BURNT_TALL_GRASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("burnt_tall_grass", BURNT_TALL_GRASS);

    public static final DeferredBlock<Block> MUTATED_LEAVES_PURPLE = registerCustomBlock("mutated_leaves_purple",
            MutatedLeavesBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.2f)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> MUTATED_LEAVES_PURPLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("mutated_leaves_purple", MUTATED_LEAVES_PURPLE);

    public static final DeferredBlock<Block> MUTATED_LEAVES_GRAY = registerCustomBlock("mutated_leaves_gray",
            MutatedLeavesBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.2f)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> MUTATED_LEAVES_GRAY_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("mutated_leaves_gray", MUTATED_LEAVES_GRAY);

    public static final DeferredBlock<Block> ASH_LAYER = registerCustomBlock("ash_layer",
            net.minecraft.world.level.block.SnowLayerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.1f)
                    .sound(SoundType.SNOW)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> ASH_LAYER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ash_layer", ASH_LAYER);

    // === NEW WASTELAND VEGETATION (Biome Overhaul) ===
    public static final DeferredBlock<Block> IRRADIATED_CACTUS = registerCustomBlock("irradiated_cactus",
            IrradiatedCactusBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.4f)
                    .sound(SoundType.WOOL)
                    .lightLevel(s -> 8)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> IRRADIATED_CACTUS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("irradiated_cactus", IRRADIATED_CACTUS);

    public static final DeferredBlock<Block> WASTELAND_REED = registerCustomBlock("wasteland_reed",
            WastelandReedBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> WASTELAND_REED_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_reed", WASTELAND_REED);

    public static final DeferredBlock<Block> ASH_BUSH = registerCustomBlock("ash_bush",
            AshBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.2f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> ASH_BUSH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ash_bush", ASH_BUSH);

    // === FIRST LIGHT WILDERNESS BLOCKS ===
    public static final DeferredBlock<Block> WILD_BERRY_BUSH = registerCustomBlock("wild_berry_bush",
            WildBerryBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> WILD_BERRY_BUSH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wild_berry_bush", WILD_BERRY_BUSH);

    public static final DeferredBlock<Block> RAIN_COLLECTOR = registerCustomBlock("rain_collector",
            RainCollectorBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.8f)
                    .sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> RAIN_COLLECTOR_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rain_collector", RAIN_COLLECTOR);

    public static final DeferredBlock<Block> ASH_CAMPFIRE = registerCustomBlock("ash_campfire",
            AshCampfireBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0f)
                    .sound(SoundType.WOOD)
                    .lightLevel(s -> 13)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> ASH_CAMPFIRE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ash_campfire", ASH_CAMPFIRE);

    public static final DeferredBlock<Block> NUCLEAR_FUNGUS = registerCustomBlock("nuclear_fungus",
            NuclearFungusBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(0.1f)
                    .sound(SoundType.FUNGUS)
                    .lightLevel(s -> 6)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> NUCLEAR_FUNGUS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nuclear_fungus", NUCLEAR_FUNGUS);

    public static final DeferredBlock<Block> RUSTY_WHEAT = registerCustomBlock("rusty_wheat",
            RustyWheatBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.0f)
                    .sound(SoundType.CROP)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> RUSTY_WHEAT_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rusty_wheat", RUSTY_WHEAT);

    public static final DeferredBlock<Block> TOXIC_MOSS = registerCustomBlock("toxic_moss",
            ToxicMossBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1f)
                    .sound(SoundType.MOSS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> TOXIC_MOSS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_moss", TOXIC_MOSS);

    public static final DeferredBlock<Block> BURNT_FERN = registerCustomBlock("burnt_fern",
            BurntFernBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> BURNT_FERN_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("burnt_fern", BURNT_FERN);

    public static final DeferredBlock<Block> MUTATED_SAPLING = registerCustomBlock("mutated_sapling",
            MutatedSaplingBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.0f)
                    .sound(SoundType.GRASS)
                    .lightLevel(s -> 3)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> MUTATED_SAPLING_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("mutated_sapling", MUTATED_SAPLING);

    // === GROUND DEBRIS BLOCKS (Biome Overhaul) ===
    public static final DeferredBlock<Block> RUBBLE = registerCustomBlock("rubble",
            RubbleBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> RUBBLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rubble", RUBBLE);

    public static final DeferredBlock<Block> CONCRETE_CHUNK = registerCustomBlock("concrete_chunk",
            ConcreteChunkBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(1.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CONCRETE_CHUNK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("concrete_chunk", CONCRETE_CHUNK);

    public static final DeferredBlock<Block> RUSTED_METAL_DEBRIS = registerCustomBlock("rusted_metal_debris",
            RustedMetalDebrisBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> RUSTED_METAL_DEBRIS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rusted_metal_debris", RUSTED_METAL_DEBRIS);

    public static final DeferredBlock<Block> SCATTERED_BONES = registerCustomBlock("scattered_bones",
            ScatteredBonesBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(0.1f)
                    .sound(SoundType.BONE_BLOCK)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> SCATTERED_BONES_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("scattered_bones", SCATTERED_BONES);

    public static final DeferredBlock<Block> DEEP_ASH = registerCustomBlock("deep_ash",
            DeepAshBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(0.2f)
                    .sound(SoundType.SAND)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> DEEP_ASH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deep_ash", DEEP_ASH);

    // === BIOME RESOURCE SUBSTRATES ===
    public static final DeferredBlock<Block> WASTELAND_STONE = BLOCKS.registerSimpleBlock("wasteland_stone",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> WASTELAND_STONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_stone", WASTELAND_STONE);

    public static final DeferredBlock<Block> WASTELAND_TRACE_RUBBLE = BLOCKS.registerSimpleBlock("wasteland_trace_rubble",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(0.9f)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> WASTELAND_TRACE_RUBBLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("wasteland_trace_rubble", WASTELAND_TRACE_RUBBLE);

    public static final DeferredBlock<Block> INDUSTRIAL_AGGREGATE = BLOCKS.registerSimpleBlock("industrial_aggregate",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(1.2f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> INDUSTRIAL_AGGREGATE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("industrial_aggregate", INDUSTRIAL_AGGREGATE);

    public static final DeferredBlock<Block> TOXIC_SLAGSTONE = BLOCKS.registerSimpleBlock("toxic_slagstone",
            p -> p.mapColor(MapColor.COLOR_GREEN)
                    .strength(1.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 1));
    public static final DeferredItem<BlockItem> TOXIC_SLAGSTONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("toxic_slagstone", TOXIC_SLAGSTONE);

    public static final DeferredBlock<Block> IRRADIATED_SHALE = BLOCKS.registerSimpleBlock("irradiated_shale",
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(1.1f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 3));
    public static final DeferredItem<BlockItem> IRRADIATED_SHALE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("irradiated_shale", IRRADIATED_SHALE);

    public static final DeferredBlock<Block> CRYOGENIC_FRACTURED_STONE = BLOCKS.registerSimpleBlock("cryogenic_fractured_stone",
            p -> p.mapColor(MapColor.ICE)
                    .strength(1.1f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CRYOGENIC_FRACTURED_STONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("cryogenic_fractured_stone", CRYOGENIC_FRACTURED_STONE);

    public static final DeferredBlock<Block> CRASH_SLAG = BLOCKS.registerSimpleBlock("crash_slag",
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(1.2f)
                    .sound(SoundType.BASALT)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 1));
    public static final DeferredItem<BlockItem> CRASH_SLAG_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("crash_slag", CRASH_SLAG);

    // === BIOME OVERHAUL VISUAL BLOCKS ===
    public static final DeferredBlock<Block> RIFTSTONE = BLOCKS.registerSimpleBlock("riftstone",
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.6f, 6.0f)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 1));
    public static final DeferredItem<BlockItem> RIFTSTONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("riftstone", RIFTSTONE);

    public static final DeferredBlock<Block> ECHO_CRYSTAL = BLOCKS.registerSimpleBlock("echo_crystal",
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0f)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 8));
    public static final DeferredItem<BlockItem> ECHO_CRYSTAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("echo_crystal", ECHO_CRYSTAL);

    public static final DeferredBlock<Block> ENERGIZED_FISSURE = registerCustomBlock("energized_fissure",
            EnergizedFissureBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.7f)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 10));
    public static final DeferredItem<BlockItem> ENERGIZED_FISSURE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("energized_fissure", ENERGIZED_FISSURE);

    public static final DeferredBlock<Block> SCORCHED_ASH = BLOCKS.registerSimpleBlock("scorched_ash",
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(0.4f)
                    .sound(SoundType.SAND));
    public static final DeferredItem<BlockItem> SCORCHED_ASH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("scorched_ash", SCORCHED_ASH);

    public static final DeferredBlock<Block> TWISTED_METAL = BLOCKS.registerSimpleBlock("twisted_metal",
            p -> p.mapColor(MapColor.METAL)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> TWISTED_METAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("twisted_metal", TWISTED_METAL);

    public static final DeferredBlock<Block> CABLE_BUNDLE = BLOCKS.registerSimpleBlock("cable_bundle",
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(0.8f)
                    .sound(SoundType.WOOL)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> CABLE_BUNDLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("cable_bundle", CABLE_BUNDLE);

    public static final DeferredBlock<Block> CRACKED_EARTH = BLOCKS.registerSimpleBlock("cracked_earth",
            p -> p.mapColor(MapColor.DIRT)
                    .strength(0.7f)
                    .sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> CRACKED_EARTH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("cracked_earth", CRACKED_EARTH);

    public static final DeferredBlock<Block> ASH_STONE = BLOCKS.registerSimpleBlock("ash_stone",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(1.4f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> ASH_STONE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ash_stone", ASH_STONE);

    public static final DeferredBlock<Block> SCRAP_ORE = BLOCKS.registerSimpleBlock("scrap_ore",
            p -> p.mapColor(MapColor.METAL)
                    .strength(2.4f, 6.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> SCRAP_ORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("scrap_ore", SCRAP_ORE);

    public static final DeferredBlock<Block> THORN_SCRUB = registerCustomBlock("thorn_scrub",
            HazardousBushBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.1f)
                    .sound(SoundType.GRASS)
                    .noCollision()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> THORN_SCRUB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("thorn_scrub", THORN_SCRUB);

    public static final DeferredBlock<Block> ACID_MUD = registerCustomBlock("acid_mud",
            AcidicSludgeBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.5f)
                    .sound(SoundType.HONEY_BLOCK)
                    .lightLevel(s -> 2));
    public static final DeferredItem<BlockItem> ACID_MUD_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("acid_mud", ACID_MUD);

    public static final DeferredBlock<Block> OOZE_CRYSTAL = BLOCKS.registerSimpleBlock("ooze_crystal",
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.8f)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 7));
    public static final DeferredItem<BlockItem> OOZE_CRYSTAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ooze_crystal", OOZE_CRYSTAL);

    public static final DeferredBlock<Block> CORRODED_PIPE = BLOCKS.registerSimpleBlock("corroded_pipe",
            p -> p.mapColor(MapColor.METAL)
                    .strength(1.4f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> CORRODED_PIPE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("corroded_pipe", CORRODED_PIPE);

    public static final DeferredBlock<Block> REBAR_BLOCK = BLOCKS.registerSimpleBlock("rebar_block",
            p -> p.mapColor(MapColor.METAL)
                    .strength(1.6f, 5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> REBAR_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("rebar_block", REBAR_BLOCK);

    public static final DeferredBlock<Block> SHATTERED_GLASS = BLOCKS.registerSimpleBlock("shattered_glass",
            p -> p.mapColor(MapColor.ICE)
                    .strength(0.4f)
                    .sound(SoundType.GLASS)
                    .noOcclusion());
    public static final DeferredItem<BlockItem> SHATTERED_GLASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("shattered_glass", SHATTERED_GLASS);

    public static final DeferredBlock<Block> URANIUM_CRYSTAL = BLOCKS.registerSimpleBlock("uranium_crystal",
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(1.2f)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 9));
    public static final DeferredItem<BlockItem> URANIUM_CRYSTAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("uranium_crystal", URANIUM_CRYSTAL);

    public static final DeferredBlock<Block> RADIOACTIVE_SLUDGE = registerCustomBlock("radioactive_sludge",
            RadioactiveSludgeBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.5f)
                    .sound(SoundType.HONEY_BLOCK)
                    .lightLevel(s -> 3));
    public static final DeferredItem<BlockItem> RADIOACTIVE_SLUDGE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("radioactive_sludge", RADIOACTIVE_SLUDGE);

    public static final DeferredBlock<Block> PERMAFROST = BLOCKS.registerSimpleBlock("permafrost",
            p -> p.mapColor(MapColor.ICE)
                    .strength(1.1f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> PERMAFROST_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("permafrost", PERMAFROST);

    public static final DeferredBlock<Block> BLUE_ICE_CRYSTAL = BLOCKS.registerSimpleBlock("blue_ice_crystal",
            p -> p.mapColor(MapColor.ICE)
                    .strength(0.9f)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 6));
    public static final DeferredItem<BlockItem> BLUE_ICE_CRYSTAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("blue_ice_crystal", BLUE_ICE_CRYSTAL);

    public static final DeferredBlock<Block> FROZEN_CONDUIT = BLOCKS.registerSimpleBlock("frozen_conduit",
            p -> p.mapColor(MapColor.METAL)
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> FROZEN_CONDUIT_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("frozen_conduit", FROZEN_CONDUIT);

    // === STRUCTURE BLOCKS ===
    public static final DeferredBlock<Block> DROP_POD_HULL = BLOCKS.registerSimpleBlock("drop_pod_hull",
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(50.0f, 1200.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> DROP_POD_HULL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("drop_pod_hull", DROP_POD_HULL);

    public static final DeferredBlock<Block> DROP_POD_GLASS = BLOCKS.registerSimpleBlock("drop_pod_glass",
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(10.0f, 600.0f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(s -> 4));
    public static final DeferredItem<BlockItem> DROP_POD_GLASS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("drop_pod_glass", DROP_POD_GLASS);

    // === MACHINES ===
    public static final DeferredBlock<Block> HAND_RECYCLER = registerCustomBlock("hand_recycler",
            HandRecyclerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(HandRecyclerBlock.ACTIVE) ? 8 : 0));
    public static final DeferredItem<BlockItem> HAND_RECYCLER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("hand_recycler", HAND_RECYCLER);

    public static final DeferredBlock<Block> THERMAL_BURNER = registerCustomBlock("thermal_burner",
            ThermalBurnerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(ThermalBurnerBlock.ACTIVE) ? 13 : 0));
    public static final DeferredItem<BlockItem> THERMAL_BURNER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("thermal_burner", THERMAL_BURNER);

    public static final DeferredBlock<Block> WATER_PURIFIER = registerCustomBlock("water_purifier",
            WaterPurifierBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(WaterPurifierBlock.ACTIVE) ? 6 : 0));
    public static final DeferredItem<BlockItem> WATER_PURIFIER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("water_purifier", WATER_PURIFIER);

    public static final DeferredBlock<Block> MICRO_GENERATOR = registerCustomBlock("micro_generator",
            MicroGeneratorBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(MicroGeneratorBlock.ACTIVE) ? 10 : 0));
    public static final DeferredItem<BlockItem> MICRO_GENERATOR_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("micro_generator", MICRO_GENERATOR);

    // === TIER 2.5 POWER GENERATION (Machinery Expansion) ===
    public static final DeferredBlock<Block> THERMAL_ARRAY = registerCustomBlock("thermal_array",
            com.knoxhack.echoashfallprotocol.block.ThermalArrayBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.ThermalArrayBlock.ACTIVE) ? 13 : 0));
    public static final DeferredItem<BlockItem> THERMAL_ARRAY_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("thermal_array", THERMAL_ARRAY);

    public static final DeferredBlock<Block> FILTER_WORKBENCH = registerCustomBlock("filter_workbench",
            FilterWorkbenchBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FILTER_WORKBENCH_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("filter_workbench", FILTER_WORKBENCH);

    public static final DeferredBlock<Block> BATTERY_BANK = registerCustomBlock("battery_bank",
            BatteryBankBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 4));
    public static final DeferredItem<BlockItem> BATTERY_BANK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("battery_bank", BATTERY_BANK);

    public static final DeferredBlock<Block> SCRAP_DYNAMO = registerCustomBlock("scrap_dynamo",
            ScrapDynamoBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(ScrapDynamoBlock.ACTIVE) ? 12 : 0));
    public static final DeferredItem<BlockItem> SCRAP_DYNAMO_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("scrap_dynamo", SCRAP_DYNAMO);

    public static final DeferredBlock<Block> SCRAP_PRESS = registerCustomBlock("scrap_press",
            ScrapPressBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 6));
    public static final DeferredItem<BlockItem> SCRAP_PRESS_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("scrap_press", SCRAP_PRESS);

    public static final DeferredBlock<Block> SIGNAL_SCANNER = registerCustomBlock("signal_scanner",
            SignalScannerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 8));
    public static final DeferredItem<BlockItem> SIGNAL_SCANNER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("signal_scanner", SIGNAL_SCANNER);

    public static final DeferredBlock<Block> FIELD_MED_BAY = registerCustomBlock("field_med_bay",
            FieldMedBayBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 5));
    public static final DeferredItem<BlockItem> FIELD_MED_BAY_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("field_med_bay", FIELD_MED_BAY);

    public static final DeferredBlock<Block> ATMOSPHERIC_SCRUBBER = registerCustomBlock("atmospheric_scrubber",
            AtmosphericScrubberBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 10));
    public static final DeferredItem<BlockItem> ATMOSPHERIC_SCRUBBER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("atmospheric_scrubber", ATMOSPHERIC_SCRUBBER);

    public static final DeferredBlock<Block> AUTOFEED_HOPPER = registerCustomBlock("autofeed_hopper",
            AutofeedHopperBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 3));
    public static final DeferredItem<BlockItem> AUTOFEED_HOPPER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("autofeed_hopper", AUTOFEED_HOPPER);

    public static final DeferredBlock<Block> CONTAMINANT_CONDENSER = registerCustomBlock("contaminant_condenser",
            ContaminantCondenserBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 7));
    public static final DeferredItem<BlockItem> CONTAMINANT_CONDENSER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("contaminant_condenser", CONTAMINANT_CONDENSER);

    // === GEO-EXTRACTOR MACHINES ===
    public static final DeferredBlock<Block> ORE_GRINDER = registerCustomBlock("ore_grinder",
            com.knoxhack.echoashfallprotocol.block.OreGrinderBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.OreGrinderBlock.ACTIVE) ? 6 : 0));
    public static final DeferredItem<BlockItem> ORE_GRINDER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("ore_grinder", ORE_GRINDER);

    public static final DeferredBlock<Block> ISOTOPE_REFINER = registerCustomBlock("isotope_refiner",
            com.knoxhack.echoashfallprotocol.block.IsotopeRefinerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(4.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.IsotopeRefinerBlock.ACTIVE) ? 12 : 0));
    public static final DeferredItem<BlockItem> ISOTOPE_REFINER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("isotope_refiner", ISOTOPE_REFINER);

    public static final DeferredBlock<Block> CRYSTALLINE_SYNTHESIZER = registerCustomBlock("crystalline_synthesizer",
            com.knoxhack.echoashfallprotocol.block.CrystallineSynthesizerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> switch (s.getValue(com.knoxhack.echoashfallprotocol.block.CrystallineSynthesizerBlock.PHASE)) {
                        case 1 -> 5; case 2 -> 10; case 3 -> 15; case 4 -> 8; default -> 0;
                    }));
    public static final DeferredItem<BlockItem> CRYSTALLINE_SYNTHESIZER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("crystalline_synthesizer", CRYSTALLINE_SYNTHESIZER);

    // === ENDGAME / POWER GRID ===
    public static final DeferredBlock<Block> POWER_NODE = registerCustomBlock("power_node",
            com.knoxhack.echoashfallprotocol.block.PowerNodeBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(8.0f, 1200.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.PowerNodeBlock.ACTIVE) ? 12 : 3));
    public static final DeferredItem<BlockItem> POWER_NODE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("power_node", POWER_NODE);

    public static final DeferredBlock<Block> NEXUS_CORE = registerCustomBlock("nexus_core",
            com.knoxhack.echoashfallprotocol.block.NexusCoreBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2000.0f, 3600000.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 15));
    public static final DeferredItem<BlockItem> NEXUS_CORE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nexus_core", NEXUS_CORE);

    // === ENDGAME MACHINES ===
    public static final DeferredBlock<Block> DEEP_CORE_MINER = registerCustomBlock("deep_core_miner",
            com.knoxhack.echoashfallprotocol.block.DeepCoreMinerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 5));
    public static final DeferredItem<BlockItem> DEEP_CORE_MINER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("deep_core_miner", DEEP_CORE_MINER);

    public static final DeferredBlock<Block> RADIATION_CLEANSER = registerCustomBlock("radiation_cleanser",
            com.knoxhack.echoashfallprotocol.block.RadiationCleanserBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 4));
    public static final DeferredItem<BlockItem> RADIATION_CLEANSER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("radiation_cleanser", RADIATION_CLEANSER);

    // === EXPLORATION 1.1 - RESEARCH SYSTEM ===
    public static final DeferredBlock<Block> RESEARCH_LAB = registerCustomBlock("research_lab",
            com.knoxhack.echoashfallprotocol.block.ResearchLabBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(4.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 8));
    public static final DeferredItem<BlockItem> RESEARCH_LAB_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("research_lab", RESEARCH_LAB);

    public static final DeferredBlock<Block> RELAY_STATION = registerCustomBlock("relay_station",
            com.knoxhack.echoashfallprotocol.block.RelayStationBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.RelayStationBlock.ACTIVE) ? 12 : 0));
    public static final DeferredItem<BlockItem> RELAY_STATION_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("relay_station", RELAY_STATION);

    public static final DeferredBlock<Block> WORKSHOP_BLOCK = registerCustomBlock("workshop_block",
            com.knoxhack.echoashfallprotocol.block.WorkshopBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 6));
    public static final DeferredItem<BlockItem> WORKSHOP_BLOCK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("workshop_block", WORKSHOP_BLOCK);

    // === MACHINE INTEGRATION ===
    public static final DeferredBlock<Block> ITEM_PIPE = registerCustomBlock("item_pipe",
            com.knoxhack.echoashfallprotocol.block.ItemPipeBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion());
    public static final DeferredItem<BlockItem> ITEM_PIPE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("item_pipe", ITEM_PIPE);

    public static final DeferredBlock<Block> POWER_CABLE = registerCustomBlock("power_cable",
            com.knoxhack.echoashfallprotocol.block.PowerCableBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.PowerCableBlock.ACTIVE) ? 4 : 0));
    public static final DeferredItem<BlockItem> POWER_CABLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("power_cable", POWER_CABLE);

    public static final DeferredBlock<Block> REINFORCED_POWER_CABLE = registerCustomBlock("reinforced_power_cable",
            props -> new com.knoxhack.echoashfallprotocol.block.PowerCableBlock(props, 2000, 256), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.PowerCableBlock.ACTIVE) ? 5 : 0));
    public static final DeferredItem<BlockItem> REINFORCED_POWER_CABLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("reinforced_power_cable", REINFORCED_POWER_CABLE);

    public static final DeferredBlock<Block> HIGH_VOLTAGE_POWER_CABLE = registerCustomBlock("high_voltage_power_cable",
            props -> new com.knoxhack.echoashfallprotocol.block.PowerCableBlock(props, 4000, 1024), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.PowerCableBlock.ACTIVE) ? 7 : 1));
    public static final DeferredItem<BlockItem> HIGH_VOLTAGE_POWER_CABLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("high_voltage_power_cable", HIGH_VOLTAGE_POWER_CABLE);

    public static final DeferredBlock<Block> ENERGY_METER = registerCustomBlock("energy_meter",
            EnergyMeterBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(2.5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 4));
    public static final DeferredItem<BlockItem> ENERGY_METER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("energy_meter", ENERGY_METER);

    public static final DeferredBlock<Block> LOAD_DISTRIBUTOR = registerCustomBlock("load_distributor",
            LoadDistributorBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 5));
    public static final DeferredItem<BlockItem> LOAD_DISTRIBUTOR_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("load_distributor", LOAD_DISTRIBUTOR);

    public static final DeferredBlock<Block> NEXUS_CAPACITOR = registerCustomBlock("nexus_capacitor",
            NexusCapacitorBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(6.0f)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> 8));
    public static final DeferredItem<BlockItem> NEXUS_CAPACITOR_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("nexus_capacitor", NEXUS_CAPACITOR);

    public static final DeferredBlock<Block> FACTORY_CONTROLLER = registerCustomBlock("factory_controller",
            com.knoxhack.echoashfallprotocol.block.FactoryControllerBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(com.knoxhack.echoashfallprotocol.block.FactoryControllerBlock.ACTIVE) ? 8 : 4));
    public static final DeferredItem<BlockItem> FACTORY_CONTROLLER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("factory_controller", FACTORY_CONTROLLER);

    // === FACTION VILLAGER PROFESSION BLOCKS ===
    // Remnant Professions
    public static final DeferredBlock<Block> WEAPON_RACK = registerSimpleProfessionBlock("weapon_rack",
            MapColor.COLOR_BLUE, SoundType.METAL);
    public static final DeferredItem<BlockItem> WEAPON_RACK_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("weapon_rack", WEAPON_RACK);

    public static final DeferredBlock<Block> SUPPLY_CRATE = registerSimpleProfessionBlock("supply_crate",
            MapColor.COLOR_LIGHT_BLUE, SoundType.WOOD);
    public static final DeferredItem<BlockItem> SUPPLY_CRATE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("supply_crate", SUPPLY_CRATE);

    // Salvager Professions
    public static final DeferredBlock<Block> TRADE_COUNTER = registerSimpleProfessionBlock("trade_counter",
            MapColor.COLOR_YELLOW, SoundType.WOOD);
    public static final DeferredItem<BlockItem> TRADE_COUNTER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("trade_counter", TRADE_COUNTER);

    public static final DeferredBlock<Block> MAP_TABLE = registerSimpleProfessionBlock("map_table",
            MapColor.COLOR_BROWN, SoundType.WOOD);
    public static final DeferredItem<BlockItem> MAP_TABLE_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("map_table", MAP_TABLE);

    // Mutant Professions
    public static final DeferredBlock<Block> BIO_PROCESSING_STATION = registerSimpleProfessionBlock("bio_processing_station",
            MapColor.COLOR_GREEN, SoundType.SLIME_BLOCK);
    public static final DeferredItem<BlockItem> BIO_PROCESSING_STATION_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("bio_processing_station", BIO_PROCESSING_STATION);

    public static final DeferredBlock<Block> SPORE_GARDEN = registerSimpleProfessionBlock("spore_garden",
            MapColor.COLOR_LIGHT_GREEN, SoundType.GRASS);
    public static final DeferredItem<BlockItem> SPORE_GARDEN_ITEM = BLOCK_ITEMS.registerSimpleBlockItem("spore_garden", SPORE_GARDEN);

    private static DeferredBlock<Block> registerSimpleProfessionBlock(String name, MapColor color, SoundType sound) {
        return BLOCKS.register(name, id -> new ProfessionBlock(withId(BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(2.5f)
                .sound(sound)
                .noOcclusion(), id)));
    }

    private static <T extends Block> DeferredBlock<T> registerCustomBlock(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, id -> factory.apply(withId(properties, id)));
    }

    private static BlockBehaviour.Properties withId(BlockBehaviour.Properties properties, Identifier id) {
        return properties.setId(ResourceKey.create(Registries.BLOCK, id));
    }
}
