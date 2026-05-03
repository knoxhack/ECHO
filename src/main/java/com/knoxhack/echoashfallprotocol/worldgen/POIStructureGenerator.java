package com.knoxhack.echoashfallprotocol.worldgen;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.worldgen.StructureBuilder.Palette;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Generates all POI structure NBT files using StructureBuilder.
 * 34 existing structures rewritten with biome palettes + 8 new full-building POIs.
 * Run via the in-game /echogenstructures command (registries must be populated).
 */
public class POIStructureGenerator {

    private static net.minecraft.world.level.block.Block terminalBlock() {
        return EchoCoreServices.terminalStructureBlockState().getBlock();
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("POI Structure Generator v3");
        System.out.println("Generating biome-themed POI structures...");
        System.out.println("========================================");
        generateAll();
        System.out.println("========================================");
        System.out.println("Generation complete!");
        System.out.println("========================================");
    }

    public static void generateAll() {
        System.out.println("[POIStructureGenerator] Starting structure generation...");

        // Crash Zone Wasteland (4 existing + 2 new)
        generateScrapPileSmall();
        generateScrapPileMedium();
        generateWreckageCluster();
        generateAshCoveredRuin();
        generateWreckageCommandPost();
        generateSalvagerHut();

        // Ruined Cityscape (4 existing)
        generateCollapsedBuildingSmall();
        generateCollapsedBuildingTall();
        generateStreetBarricade();
        generateParkingRuin();

        // Radiation Zone (4 existing + 2 new)
        generateContainmentBreach();
        generateWasteBarrelCluster();
        generateIrradiatedVehicle();
        generateRadiationCrater();
        generateContaminatedLab();
        generateFalloutShelter();

        // Toxic Swamp (4 existing + 2 new)
        generateChemicalSpill();
        generateBrokenPipeline();
        generateAbandonedShed();
        generateToxicPoolSmall();
        generateSporeResearchHut();
        generateStiltedOutpost();

        // Industrial Ruins (4 existing)
        generateConveyorRuin();
        generateStorageYard();
        generateCraneWreck();
        generatePipeCluster();

        // Cryogenic Ruins (4 existing)
        generateFrozenVehicle();
        generateIceCoveredRuin();
        generateBrokenTank();
        generateFrozenCache();

        // Ruined Plains (4 existing + 2 new)
        generateNomadCamp();
        generateWindmillRuin();
        generateImpactCrater();
        generateSupplyDrop();
        generateRelayTower();
        generateTraderPost();

        // Global (6 existing)
        generateDebrisFieldSmall();
        generateDebrisFieldLarge();
        generateSurvivorCache();
        generateRadioRelaySmall();
        generateAbandonedCamp();
        generateRoadWreck();

        System.out.println("[POIStructureGenerator] All structures generated!");
    }

    // ================= CRASH ZONE WASTELAND =================

    private static void generateScrapPileSmall() {
        new StructureBuilder(12, 5, 12)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 11, 0, 11, ModBlocks.ASHEN_WASTELAND_DIRT.get())
            .fill(2, 1, 2, 9, 1, 9, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .fill(4, 2, 4, 7, 2, 7, ModBlocks.RUSTED_METAL_SHEET.get())
            .set(5, 3, 5, ModBlocks.DROP_POD_HULL.get())
            .set(6, 3, 6, ModBlocks.DROP_POD_HULL.get())
            .scatterDebris(0, 0, 11, 11, 1, 0.25f)
            .save("crash_zone_wasteland", "scrap_pile_small");
    }

    private static void generateScrapPileMedium() {
        new StructureBuilder(14, 6, 14)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 13, 0, 13, ModBlocks.ASHEN_WASTELAND_DIRT.get())
            .fill(2, 1, 2, 11, 1, 11, ModBlocks.BURNT_WASTELAND_SOIL.get())
            .fill(4, 2, 4, 9, 3, 9, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .fill(5, 4, 5, 8, 4, 8, ModBlocks.RUSTED_METAL_SHEET.get())
            .set(6, 5, 6, ModBlocks.DROP_POD_HULL.get())
            .set(7, 5, 7, ModBlocks.DROP_POD_HULL.get())
            .scatter(0.15f, ModBlocks.SCATTERED_BONES.get().defaultBlockState())
            .scatter(0.1f, Blocks.RAIL.defaultBlockState())
            .save("crash_zone_wasteland", "scrap_pile_medium");
    }

    private static void generateWreckageCluster() {
        new StructureBuilder(16, 6, 10)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 15, 0, 9, ModBlocks.ASHEN_WASTELAND_DIRT.get())
            // Drop pod (left)
            .room(1, 1, 2, 5, 4, 6)
            .door(3, 1, 6, 'x')
            .set(2, 2, 3, ModBlocks.DROP_POD_GLASS.get())
            .set(2, 2, 5, ModBlocks.DROP_POD_GLASS.get())
            .set(4, 2, 3, ModBlocks.DROP_POD_GLASS.get())
            // Vehicle wreck (right)
            .fill(8, 1, 3, 14, 2, 6, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(9, 3, 4, 13, 3, 5, ModBlocks.DROP_POD_GLASS.get())
            .set(8, 0, 3, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .set(14, 0, 6, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .scatterDebris(0, 0, 15, 9, 1, 0.18f)
            .ruin(7L, 0.12f)
            .save("crash_zone_wasteland", "wreckage_cluster");
    }

    private static void generateAshCoveredRuin() {
        new StructureBuilder(13, 7, 13)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.ASHEN_WASTELAND_DIRT.get())
            .room(2, 1, 2, 10, 5, 10)
            .door(6, 1, 2, 'x')
            .window(2, 3, 4, 'z', 5)
            .window(10, 3, 4, 'z', 5)
            .roofGable(2, 2, 10, 10, 6, 'x')
            .floorPattern(3, 3, 9, 9, 1)
            .chest(4, 2, 8, cache("crash_zone_wasteland_cache"))
            .scatterDebris(2, 2, 10, 10, 1, 0.15f)
            .ruin(11L, 0.18f)
            .save("crash_zone_wasteland", "ash_covered_ruin");
    }

    // New full-building POI: Wreckage Command Post
    private static void generateWreckageCommandPost() {
        new StructureBuilder(15, 11, 15)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 14, 0, 14, ModBlocks.ASHEN_WASTELAND_DIRT.get())
            // Ground floor
            .room(1, 1, 1, 13, 4, 13)
            .door(7, 1, 1, 'x')
            .window(1, 3, 4, 'z', 7)
            .window(13, 3, 4, 'z', 7)
            .floorPattern(2, 2, 12, 12, 1)
            // Upper floor
            .room(1, 5, 1, 13, 8, 13)
            .floorPattern(2, 5, 12, 12, 5)
            .window(1, 7, 5, 'z', 5)
            .window(13, 7, 5, 'z', 5)
            // Roof
            .roofGable(1, 1, 13, 13, 9, 'x')
            // Ladder
            .ladder(2, 1, 7, 5, Direction.NORTH)
            .set(2, 5, 7, Blocks.AIR.defaultBlockState())
            .set(3, 5, 7, Blocks.AIR.defaultBlockState())
            // Furnishings (ground)
            .furnish(11, 2, 11, terminalBlock())
            .furnish(3, 2, 3, ModBlocks.MAP_TABLE.get())
            .furnish(11, 2, 3, ModBlocks.WEAPON_RACK.get())
            .furnish(3, 2, 11, ModBlocks.SUPPLY_CRATE.get())
            // Furnishings (upper)
            .furnish(11, 6, 11, ModBlocks.MAP_TABLE.get())
            .chest(7, 6, 12, cache("crash_zone_wasteland_cache"))
            .ruin(101L, 0.1f)
            .scatterDebris(0, 0, 14, 14, 1, 0.1f)
            .save("crash_zone_wasteland", "wreckage_command_post");
    }

    // New full-building POI: Salvager Hut
    private static void generateSalvagerHut() {
        new StructureBuilder(13, 9, 13)
            .palette(Palette.crashZone())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.BURNT_WASTELAND_SOIL.get())
            .room(1, 1, 1, 11, 4, 11)
            .door(6, 1, 1, 'x')
            .window(1, 3, 4, 'z', 5)
            .window(11, 3, 4, 'z', 5)
            .window(4, 3, 11, 'x', 5)
            .floorPattern(2, 2, 10, 10, 1)
            // Pyramid roof (single tier on top of room)
            .roofPyramid(1, 1, 11, 11, 5)
            // Furnishings
            .furnish(2, 2, 9, ModBlocks.TRADE_COUNTER.get())
            .furnish(10, 2, 9, ModBlocks.SUPPLY_CRATE.get())
            .furnish(2, 2, 2, ModBlocks.RAIN_COLLECTOR.get())
            .furnish(10, 2, 2, ModBlocks.SUPPLY_CRATE.get())
            .chest(6, 2, 6, cache("salvager_trading_post_cache"))
            .scatterDebris(0, 0, 12, 12, 1, 0.12f)
            .ruin(102L, 0.08f)
            .save("crash_zone_wasteland", "salvager_hut");
    }

    // ================= RUINED CITYSCAPE =================

    private static void generateCollapsedBuildingSmall() {
        new StructureBuilder(14, 9, 14)
            .palette(Palette.ruinedCityscape())
            .fill(0, 0, 0, 13, 0, 13, ModBlocks.OIL_STAINED_CONCRETE.get())
            .room(1, 1, 1, 12, 7, 12)
            .door(6, 1, 1, 'x')
            .window(1, 3, 4, 'z', 7)
            .window(12, 3, 4, 'z', 7)
            .window(3, 3, 12, 'x', 7)
            .floorPattern(2, 2, 11, 11, 1)
            .pillar(4, 1, 4, 7).pillar(9, 1, 4, 7)
            .pillar(4, 1, 9, 7).pillar(9, 1, 9, 7)
            .ruin(201L, 0.25f)
            .scatterDebris(1, 1, 12, 12, 1, 0.18f)
            .save("ruined_cityscape", "collapsed_building_small");
    }

    private static void generateCollapsedBuildingTall() {
        new StructureBuilder(14, 16, 14)
            .palette(Palette.ruinedCityscape())
            .fill(0, 0, 0, 13, 0, 13, ModBlocks.OIL_STAINED_CONCRETE.get())
            .room(1, 1, 1, 12, 4, 12)
            .room(1, 5, 1, 12, 8, 12)
            .room(1, 9, 1, 12, 12, 12)
            .door(6, 1, 1, 'x')
            .window(1, 3, 4, 'z', 7)
            .window(12, 3, 4, 'z', 7)
            .window(1, 7, 4, 'z', 7)
            .window(12, 7, 4, 'z', 7)
            .window(1, 11, 4, 'z', 7)
            .window(12, 11, 4, 'z', 7)
            .floorPattern(2, 2, 11, 11, 1)
            .floorPattern(2, 5, 11, 11, 5)
            .floorPattern(2, 9, 11, 11, 9)
            .ladder(2, 1, 11, 12, Direction.NORTH)
            .set(2, 5, 11, Blocks.AIR.defaultBlockState())
            .set(2, 9, 11, Blocks.AIR.defaultBlockState())
            .roofGable(1, 1, 12, 12, 13, 'x')
            .ruin(202L, 0.3f)
            .scatterDebris(1, 1, 12, 12, 1, 0.2f)
            .save("ruined_cityscape", "collapsed_building_tall");
    }

    private static void generateStreetBarricade() {
        new StructureBuilder(14, 5, 6)
            .palette(Palette.ruinedCityscape())
            .fill(0, 0, 0, 13, 0, 5, Blocks.GRAVEL)
            // Concrete barriers
            .fill(0, 1, 1, 13, 2, 1, ModBlocks.OIL_STAINED_CONCRETE.get())
            .fill(0, 1, 4, 13, 2, 4, ModBlocks.OIL_STAINED_CONCRETE.get())
            // Sandbag stacks
            .fill(2, 1, 2, 4, 2, 3, Blocks.SAND)
            .fill(9, 1, 2, 11, 2, 3, Blocks.SAND)
            // Wreck in middle
            .fill(6, 1, 2, 7, 2, 3, ModBlocks.RUSTED_METAL_SHEET.get())
            // Loot crate
            .barrel(7, 1, 4, cache("data_center_cache"))
            .scatterDebris(0, 0, 13, 5, 1, 0.15f)
            .ruin(203L, 0.15f)
            .save("ruined_cityscape", "street_barricade");
    }

    private static void generateParkingRuin() {
        new StructureBuilder(16, 5, 16)
            .palette(Palette.ruinedCityscape())
            .fill(0, 0, 0, 15, 0, 15, Blocks.BLACK_CONCRETE)
            // Parking lines
            .fill(3, 0, 1, 3, 0, 14, Blocks.WHITE_CONCRETE)
            .fill(7, 0, 1, 7, 0, 14, Blocks.WHITE_CONCRETE)
            .fill(11, 0, 1, 11, 0, 14, Blocks.WHITE_CONCRETE)
            // Wrecked cars
            .fill(4, 1, 3, 6, 2, 5, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(8, 1, 8, 10, 2, 10, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(12, 1, 11, 14, 2, 13, ModBlocks.RUSTED_METAL_SHEET.get())
            // Glass panes
            .fill(5, 3, 4, 5, 3, 4, ModBlocks.DROP_POD_GLASS.get())
            // Lamp post
            .pillar(1, 1, 1, 3)
            .set(1, 4, 1, Blocks.LANTERN.defaultBlockState())
            .scatterDebris(0, 0, 15, 15, 1, 0.1f)
            .save("ruined_cityscape", "parking_ruin");
    }

    // ================= RADIATION ZONE =================

    private static void generateContainmentBreach() {
        new StructureBuilder(14, 8, 14)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 13, 0, 13, ModBlocks.NEXUS_CRACKED_SOIL.get())
            .room(2, 1, 2, 11, 6, 11)
            .door(6, 1, 2, 'x')
            .window(2, 4, 4, 'z', 5)
            .window(11, 4, 4, 'z', 5)
            // Reactor core
            .fill(5, 2, 5, 8, 4, 8, ModBlocks.RADIATION_BLOCK.get())
            .set(6, 5, 6, Blocks.GLOWSTONE.defaultBlockState())
            .set(7, 5, 7, Blocks.GLOWSTONE.defaultBlockState())
            // Breach
            .fill(2, 1, 2, 4, 4, 2, Blocks.AIR.defaultBlockState())
            .fill(0, 0, 0, 1, 1, 13, ModBlocks.FALLOUT_DUST.get())
            .roofPyramid(2, 2, 11, 11, 7)
            .ruin(301L, 0.15f)
            .scatterDebris(2, 2, 11, 11, 1, 0.18f)
            .save("radiation_zone", "containment_breach");
    }

    private static void generateWasteBarrelCluster() {
        new StructureBuilder(12, 4, 12)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 11, 0, 11, ModBlocks.NEXUS_CRACKED_SOIL.get())
            .fill(1, 1, 1, 10, 1, 10, ModBlocks.FALLOUT_DUST.get())
            // Barrel grid
            .set(3, 1, 3, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(5, 1, 3, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(7, 1, 3, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(3, 1, 5, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(5, 1, 5, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(7, 1, 5, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(3, 1, 7, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(5, 1, 7, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(7, 1, 7, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(3, 2, 5, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(5, 2, 5, ModBlocks.TOXIC_WASTE_BARREL.get())
            .barrel(9, 1, 9, cache("reactor_ruin_cache"))
            .scatter(0.05f, ModBlocks.SCATTERED_BONES.get().defaultBlockState())
            .save("radiation_zone", "waste_barrel_cluster");
    }

    private static void generateIrradiatedVehicle() {
        new StructureBuilder(12, 5, 8)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 11, 0, 7, ModBlocks.NEXUS_CRACKED_SOIL.get())
            .fill(0, 1, 0, 11, 1, 7, ModBlocks.FALLOUT_DUST.get())
            // Vehicle frame
            .fill(2, 1, 2, 9, 2, 5, ModBlocks.IRRADIATED_CRUST.get())
            .fill(3, 3, 3, 8, 3, 4, ModBlocks.DROP_POD_GLASS.get())
            .set(2, 1, 2, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .set(9, 1, 5, ModBlocks.RUSTED_METAL_DEBRIS.get())
            // Glowing leak
            .set(5, 1, 1, ModBlocks.RADIATION_BLOCK.get())
            .set(6, 1, 6, ModBlocks.RADIATION_BLOCK.get())
            .ruin(302L, 0.1f)
            .save("radiation_zone", "irradiated_vehicle");
    }

    private static void generateRadiationCrater() {
        new StructureBuilder(13, 5, 13)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.NEXUS_CRACKED_SOIL.get())
            .fill(0, 1, 0, 12, 1, 12, ModBlocks.FALLOUT_DUST.get())
            // Crater bowl
            .fill(3, 0, 3, 9, 0, 9, Blocks.AIR.defaultBlockState())
            .fill(4, 0, 4, 8, 0, 8, Blocks.AIR.defaultBlockState())
            .fill(5, 0, 5, 7, 0, 7, ModBlocks.RADIATION_BLOCK.get())
            .set(6, 1, 6, Blocks.LAVA.defaultBlockState())
            // Rim debris
            .scatterDebris(2, 2, 10, 10, 1, 0.25f)
            .scatter(0.05f, ModBlocks.SCATTERED_BONES.get().defaultBlockState())
            .save("radiation_zone", "radiation_crater");
    }

    // New full-building POI: Contaminated Lab
    private static void generateContaminatedLab() {
        new StructureBuilder(15, 10, 15)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 14, 0, 14, ModBlocks.NEXUS_CRACKED_SOIL.get())
            // Ground floor
            .room(1, 1, 1, 13, 4, 13)
            .door(7, 1, 1, 'x')
            .window(1, 3, 5, 'z', 5)
            .window(13, 3, 5, 'z', 5)
            .floorPattern(2, 2, 12, 12, 1)
            // Upper floor
            .room(1, 5, 1, 13, 8, 13)
            .floorPattern(2, 5, 12, 12, 5)
            .window(1, 7, 5, 'z', 5)
            .window(13, 7, 5, 'z', 5)
            // Roof
            .roofPyramid(1, 1, 13, 13, 9)
            // Ladder
            .ladder(12, 1, 12, 5, Direction.WEST)
            .set(12, 5, 12, Blocks.AIR.defaultBlockState())
            .set(11, 5, 12, Blocks.AIR.defaultBlockState())
            // Furnishings (ground - lab)
            .furnish(3, 2, 3, ModBlocks.BIO_PROCESSING_STATION.get())
            .furnish(3, 2, 11, ModBlocks.BIO_PROCESSING_STATION.get())
            .furnish(11, 2, 3, ModBlocks.MAP_TABLE.get())
            .furnish(7, 2, 7, ModBlocks.RADIATION_BLOCK.get())
            // Furnishings (upper)
            .furnish(7, 6, 7, terminalBlock())
            .chest(3, 6, 3, cache("bio_lab_cache"))
            .ruin(303L, 0.18f)
            .scatterDebris(0, 0, 14, 14, 1, 0.15f)
            .save("radiation_zone", "contaminated_lab");
    }

    // New full-building POI: Fallout Shelter
    private static void generateFalloutShelter() {
        new StructureBuilder(13, 8, 13)
            .palette(Palette.radiation())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.IRRADIATED_CRUST.get())
            .fill(0, 1, 0, 12, 1, 12, ModBlocks.FALLOUT_DUST.get())
            // Bunker (mostly buried look — only roof shows above)
            .room(1, 1, 1, 11, 5, 11)
            .door(6, 1, 1, 'x')
            .window(1, 3, 5, 'z', 3)
            .window(11, 3, 5, 'z', 3)
            .floorPattern(2, 2, 10, 10, 1)
            .roofGable(1, 1, 11, 11, 6, 'z')
            // Furnishings
            .furnish(2, 2, 2, ModBlocks.SUPPLY_CRATE.get())
            .furnish(2, 2, 10, ModBlocks.SUPPLY_CRATE.get())
            .furnish(10, 2, 10, ModBlocks.RAIN_COLLECTOR.get())
            .furnish(10, 2, 2, ModBlocks.ASH_CAMPFIRE.get())
            .chest(6, 2, 6, cache("military_vault_cache"))
            .ruin(304L, 0.06f)
            .save("radiation_zone", "fallout_shelter");
    }

    // ================= TOXIC SWAMP =================

    private static void generateChemicalSpill() {
        new StructureBuilder(13, 4, 13)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.ACIDIC_SLUDGE.get())
            .fill(2, 0, 2, 10, 0, 10, ModBlocks.CONTAMINATED_SOIL.get())
            .fill(3, 1, 3, 9, 1, 9, ModBlocks.TOXIC_PUDDLE.get())
            .fill(5, 1, 5, 7, 1, 7, Blocks.SLIME_BLOCK.defaultBlockState())
            // Broken barrels around the edge
            .set(1, 1, 1, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(11, 1, 1, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(1, 1, 11, ModBlocks.TOXIC_WASTE_BARREL.get())
            .set(11, 1, 11, ModBlocks.TOXIC_WASTE_BARREL.get())
            .scatter(0.1f, ModBlocks.MUTATED_BUSH.get().defaultBlockState())
            .scatter(0.08f, ModBlocks.TOXIC_MOSS.get().defaultBlockState())
            .save("toxic_swamp", "chemical_spill");
    }

    private static void generateBrokenPipeline() {
        new StructureBuilder(14, 5, 6)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 13, 0, 5, ModBlocks.CONTAMINATED_SOIL.get())
            // Pipeline (split into segments with breaks)
            .fill(0, 1, 2, 4, 2, 3, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(7, 1, 2, 9, 2, 3, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(12, 1, 2, 13, 2, 3, ModBlocks.RUSTED_METAL_SHEET.get())
            // Spilled toxic puddles in the gaps
            .fill(5, 1, 2, 6, 1, 3, ModBlocks.TOXIC_PUDDLE.get())
            .fill(10, 1, 2, 11, 1, 3, ModBlocks.ACIDIC_SLUDGE.get())
            // Pipe supports
            .pillar(2, 1, 1, 2)
            .pillar(8, 1, 1, 2)
            .scatterDebris(0, 0, 13, 5, 1, 0.12f)
            .save("toxic_swamp", "broken_pipeline");
    }

    private static void generateAbandonedShed() {
        new StructureBuilder(11, 7, 11)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 10, 0, 10, ModBlocks.CONTAMINATED_SOIL.get())
            .room(1, 1, 1, 9, 4, 9)
            .door(5, 1, 1, 'x')
            .window(1, 3, 4, 'z', 3)
            .window(9, 3, 4, 'z', 3)
            .roofGable(1, 1, 9, 9, 5, 'x')
            .floorPattern(2, 2, 8, 8, 1)
            .chest(2, 2, 8, cache("bio_lab_cache"))
            .furnish(8, 2, 8, ModBlocks.BIO_PROCESSING_STATION.get())
            .scatter(0.06f, ModBlocks.TOXIC_MOSS.get().defaultBlockState())
            .ruin(401L, 0.12f)
            .save("toxic_swamp", "abandoned_shed");
    }

    private static void generateToxicPoolSmall() {
        new StructureBuilder(11, 4, 11)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 10, 0, 10, ModBlocks.CONTAMINATED_SOIL.get())
            .fill(2, 0, 2, 8, 0, 8, ModBlocks.ACIDIC_SLUDGE.get())
            .fill(3, 0, 3, 7, 0, 7, ModBlocks.TOXIC_PUDDLE.get())
            .set(5, 0, 5, Blocks.SLIME_BLOCK.defaultBlockState())
            // Mossy log around rim
            .set(1, 1, 5, ModBlocks.DEAD_WOOD_LOG.get())
            .set(9, 1, 5, ModBlocks.DEAD_WOOD_LOG.get())
            .scatter(0.08f, ModBlocks.MUTATED_BUSH.get().defaultBlockState())
            .scatter(0.06f, ModBlocks.TOXIC_MOSS.get().defaultBlockState())
            .save("toxic_swamp", "toxic_pool_small");
    }

    // New full-building POI: Spore Research Hut
    private static void generateSporeResearchHut() {
        new StructureBuilder(13, 9, 13)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.CONTAMINATED_SOIL.get())
            // Stilts (this hut sits over a small pool)
            .pillar(1, 0, 1, 1).pillar(11, 0, 1, 1)
            .pillar(1, 0, 11, 1).pillar(11, 0, 11, 1)
            .fill(2, 0, 2, 10, 0, 10, ModBlocks.TOXIC_PUDDLE.get())
            // Floor-1 (raised)
            .room(1, 2, 1, 11, 5, 11)
            .door(6, 2, 1, 'x')
            .window(1, 4, 5, 'z', 5)
            .window(11, 4, 5, 'z', 5)
            .floorPattern(2, 2, 10, 10, 2)
            .roofGable(1, 1, 11, 11, 6, 'x')
            // Spore garden inside
            .furnish(3, 3, 3, ModBlocks.SPORE_GARDEN.get())
            .furnish(9, 3, 3, ModBlocks.SPORE_GARDEN.get())
            .furnish(3, 3, 9, ModBlocks.SPORE_GARDEN.get())
            .furnish(9, 3, 9, ModBlocks.SPORE_GARDEN.get())
            .furnish(6, 3, 6, ModBlocks.BIO_PROCESSING_STATION.get())
            .chest(6, 3, 10, cache("bio_lab_cache"))
            .ruin(402L, 0.08f)
            .save("toxic_swamp", "spore_research_hut");
    }

    // New full-building POI: Stilted Outpost
    private static void generateStiltedOutpost() {
        new StructureBuilder(15, 11, 12)
            .palette(Palette.toxicSwamp())
            .fill(0, 0, 0, 14, 0, 11, ModBlocks.ACIDIC_SLUDGE.get())
            .fill(2, 0, 2, 12, 0, 9, ModBlocks.TOXIC_PUDDLE.get())
            // Stilts
            .pillar(2, 0, 2, 3).pillar(12, 0, 2, 3)
            .pillar(2, 0, 9, 3).pillar(12, 0, 9, 3)
            .pillar(7, 0, 2, 3).pillar(7, 0, 9, 3)
            // Ground floor (raised on stilts)
            .room(2, 4, 2, 12, 7, 9)
            .door(7, 4, 2, 'x')
            .window(2, 6, 4, 'z', 4)
            .window(12, 6, 4, 'z', 4)
            .floorPattern(3, 4, 11, 8, 4)
            // Watchtower on top
            .room(5, 8, 4, 9, 10, 7)
            .roofPyramid(5, 4, 9, 7, 11)
            .ladder(11, 4, 8, 8, Direction.WEST)
            .set(11, 8, 8, Blocks.AIR.defaultBlockState())
            // Furnishings
            .furnish(3, 5, 3, ModBlocks.TRADE_COUNTER.get())
            .furnish(11, 5, 8, ModBlocks.SUPPLY_CRATE.get())
            .furnish(11, 5, 3, ModBlocks.MAP_TABLE.get())
            .chest(7, 9, 5, cache("salvager_trading_post_cache"))
            .ruin(403L, 0.06f)
            .save("toxic_swamp", "stilted_outpost");
    }

    // ================= INDUSTRIAL RUINS =================

    private static void generateConveyorRuin() {
        new StructureBuilder(15, 6, 9)
            .palette(Palette.industrial())
            .fill(0, 0, 0, 14, 0, 8, Blocks.GRAVEL)
            .room(1, 1, 1, 13, 4, 7)
            .door(0, 1, 4, 'x')
            .door(13, 1, 4, 'x')
            // Conveyor belt
            .fill(1, 1, 4, 13, 1, 4, Blocks.SMOOTH_STONE)
            .fill(0, 1, 4, 14, 1, 4, Blocks.SMOOTH_STONE)
            // Support pillars under belt
            .pillar(3, 0, 4, 1).pillar(7, 0, 4, 1).pillar(11, 0, 4, 1)
            // Boxes on belt
            .set(2, 2, 4, ModBlocks.SUPPLY_CRATE.get())
            .set(8, 2, 4, ModBlocks.SUPPLY_CRATE.get())
            .roofGable(1, 1, 13, 7, 5, 'x')
            .ruin(501L, 0.2f)
            .scatterDebris(1, 1, 13, 7, 1, 0.12f)
            .save("industrial_ruins", "conveyor_ruin");
    }

    private static void generateStorageYard() {
        new StructureBuilder(16, 6, 16)
            .palette(Palette.industrial())
            .fill(0, 0, 0, 15, 0, 15, Blocks.GRAVEL)
            // Container 1 (large)
            .walls(1, 1, 1, 5, 4, 5, ModBlocks.RUSTED_METAL_SHEET.get())
            .door(2, 1, 1, 'x')
            // Container 2 (medium)
            .walls(8, 1, 1, 12, 3, 5, ModBlocks.RUSTED_METAL_SHEET.get())
            .door(10, 1, 1, 'x')
            // Container 3 (small, stacked)
            .walls(1, 1, 9, 4, 3, 13, ModBlocks.RUSTED_METAL_SHEET.get())
            .walls(2, 4, 10, 3, 5, 12, ModBlocks.RUSTED_METAL_SHEET.get())
            // Forklift
            .fill(8, 1, 9, 10, 2, 11, Blocks.IRON_BLOCK)
            // Loot
            .chest(11, 1, 12, cache("industrial_factory_cache"))
            .barrel(3, 1, 3, cache("survivor_cache"))
            .scatterDebris(0, 0, 15, 15, 1, 0.1f)
            .ruin(502L, 0.12f)
            .save("industrial_ruins", "storage_yard");
    }

    private static void generateCraneWreck() {
        new StructureBuilder(14, 11, 14)
            .palette(Palette.industrial())
            .fill(0, 0, 0, 13, 0, 13, Blocks.GRAVEL)
            // Crane base (square)
            .walls(5, 1, 5, 8, 8, 8, ModBlocks.RUSTED_METAL_SHEET.get())
            // Diagonal support trusses (stairs run)
            .stairsRun(0, 1, 6, 1, 0, 6)
            .stairsRun(13, 1, 6, -1, 0, 6)
            // Fallen arm
            .fill(2, 9, 5, 11, 9, 5, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(2, 9, 8, 11, 9, 8, ModBlocks.RUSTED_METAL_SHEET.get())
            .set(11, 8, 5, ModBlocks.RUSTED_METAL_DEBRIS.get())
            // Cables (iron bars)
            .fill(6, 9, 6, 7, 9, 7, Blocks.IRON_BARS)
            .scatterDebris(0, 0, 13, 13, 1, 0.18f)
            .ruin(503L, 0.25f)
            .save("industrial_ruins", "crane_wreck");
    }

    private static void generatePipeCluster() {
        new StructureBuilder(11, 7, 11)
            .palette(Palette.industrial())
            .fill(0, 0, 0, 10, 0, 10, Blocks.GRAVEL)
            // Vertical pipes
            .pillar(2, 1, 2, 5).pillar(5, 1, 2, 6).pillar(8, 1, 2, 4)
            .pillar(2, 1, 5, 4).pillar(8, 1, 5, 5)
            .pillar(2, 1, 8, 6).pillar(5, 1, 8, 3).pillar(8, 1, 8, 5)
            // Connecting horizontal pipe runs
            .fill(2, 5, 2, 8, 5, 2, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(2, 4, 8, 8, 4, 8, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(8, 1, 2, 8, 1, 8, ModBlocks.RUSTED_METAL_SHEET.get())
            // Valve handles
            .set(5, 5, 2, Blocks.COPPER_BLOCK.defaultBlockState())
            .set(5, 4, 8, Blocks.COPPER_BLOCK.defaultBlockState())
            .scatterDebris(0, 0, 10, 10, 1, 0.1f)
            .ruin(504L, 0.18f)
            .save("industrial_ruins", "pipe_cluster");
    }

    // ================= CRYOGENIC RUINS =================

    private static void generateFrozenVehicle() {
        new StructureBuilder(12, 6, 8)
            .palette(Palette.cryogenic())
            .fill(0, 0, 0, 11, 0, 7, Blocks.SNOW_BLOCK)
            // Vehicle frame
            .fill(2, 1, 2, 9, 2, 5, Blocks.IRON_BLOCK)
            .fill(3, 3, 3, 8, 3, 4, Blocks.LIGHT_BLUE_STAINED_GLASS)
            // Ice encasing
            .fill(0, 1, 0, 11, 4, 7, Blocks.PACKED_ICE)
            // Clear interior
            .fill(2, 1, 2, 9, 2, 5, Blocks.AIR.defaultBlockState())
            .fill(3, 3, 3, 8, 3, 4, Blocks.AIR.defaultBlockState())
            // Frosty top
            .fill(1, 4, 1, 10, 4, 6, Blocks.ICE)
            .scatter(0.1f, Blocks.SNOW.defaultBlockState())
            .save("cryogenic_ruins", "frozen_vehicle");
    }

    private static void generateIceCoveredRuin() {
        new StructureBuilder(13, 8, 13)
            .palette(Palette.cryogenic())
            .fill(0, 0, 0, 12, 0, 12, Blocks.SNOW_BLOCK)
            .room(1, 1, 1, 11, 5, 11)
            .door(6, 1, 1, 'x')
            .window(1, 3, 5, 'z', 3)
            .window(11, 3, 5, 'z', 3)
            .roofPyramid(1, 1, 11, 11, 6)
            .floorPattern(2, 2, 10, 10, 1)
            // Ice coating exterior corners
            .fill(0, 1, 0, 1, 4, 1, Blocks.ICE)
            .fill(11, 1, 0, 12, 4, 1, Blocks.ICE)
            .fill(0, 1, 11, 1, 4, 12, Blocks.ICE)
            .fill(11, 1, 11, 12, 4, 12, Blocks.ICE)
            .chest(2, 2, 9, cache("cryogenic_ruins_cache"))
            .scatter(0.15f, Blocks.SNOW.defaultBlockState())
            .ruin(601L, 0.1f)
            .save("cryogenic_ruins", "ice_covered_ruin");
    }

    private static void generateBrokenTank() {
        new StructureBuilder(11, 7, 11)
            .palette(Palette.cryogenic())
            .fill(0, 0, 0, 10, 0, 10, Blocks.GRAVEL)
            // Cylindrical-ish tank (square approximation)
            .walls(2, 1, 2, 8, 5, 8, Blocks.IRON_BLOCK)
            // Top dome
            .fill(2, 6, 2, 8, 6, 8, Blocks.IRON_BLOCK)
            .set(5, 6, 5, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState())
            // Break — rupture on one side
            .fill(2, 2, 0, 4, 4, 2, Blocks.AIR.defaultBlockState())
            .fill(2, 1, 0, 4, 1, 2, Blocks.PACKED_ICE)
            // Frozen leak spreading out
            .fill(0, 1, 0, 5, 1, 5, Blocks.PACKED_ICE)
            .fill(1, 1, 1, 4, 1, 4, Blocks.BLUE_ICE)
            .barrel(7, 1, 7, cache("cryogenic_ruins_cache"))
            .save("cryogenic_ruins", "broken_tank");
    }

    private static void generateFrozenCache() {
        new StructureBuilder(8, 5, 8)
            .palette(Palette.cryogenic())
            .fill(0, 0, 0, 7, 0, 7, Blocks.SNOW_BLOCK)
            .fill(0, 0, 0, 7, 4, 7, Blocks.PACKED_ICE)
            // Hollow chamber
            .fill(2, 1, 2, 5, 3, 5, Blocks.AIR.defaultBlockState())
            .chest(3, 1, 3, cache("cryogenic_ruins_cache"))
            .chest(4, 1, 4, cache("cryogenic_ruins_cache"))
            .chest(3, 1, 4, cache("cryogenic_ruins_cache"))
            .barrel(4, 1, 3, cache("cryogenic_ruins_cache"))
            // Light through ice
            .set(3, 4, 3, Blocks.SEA_LANTERN.defaultBlockState())
            .save("cryogenic_ruins", "frozen_cache");
    }

    // ================= RUINED PLAINS =================

    private static void generateNomadCamp() {
        new StructureBuilder(14, 5, 14)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 13, 0, 13, ModBlocks.WASTELAND_DIRT.get())
            .scatter(0.15f, ModBlocks.DRY_GRASS.get().defaultBlockState())
            // Tent 1 — gable roof of wool
            .fill(1, 1, 1, 4, 1, 4, ModBlocks.WASTELAND_DIRT.get())
            .roofGable(1, 1, 4, 4, 1, 'x')
            .fill(1, 1, 1, 4, 3, 1, Blocks.WHITE_WOOL.defaultBlockState())
            .fill(1, 1, 4, 4, 3, 4, Blocks.WHITE_WOOL.defaultBlockState())
            // Tent 2 — gable roof of brown wool
            .fill(8, 1, 8, 12, 1, 12, ModBlocks.WASTELAND_DIRT.get())
            .roofGable(8, 8, 12, 12, 1, 'x')
            .fill(8, 1, 8, 12, 3, 8, Blocks.BROWN_WOOL.defaultBlockState())
            .fill(8, 1, 12, 12, 3, 12, Blocks.BROWN_WOOL.defaultBlockState())
            // Central campfire + market
            .set(7, 1, 7, ModBlocks.ASH_CAMPFIRE.get())
            .furnish(5, 1, 7, ModBlocks.TRADE_COUNTER.get())
            .furnish(7, 1, 5, ModBlocks.SUPPLY_CRATE.get())
            .chest(7, 1, 9, cache("salvager_trading_post_cache"))
            .save("ruined_plains", "nomad_camp");
    }

    private static void generateWindmillRuin() {
        new StructureBuilder(13, 13, 13)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 12, 0, 12, ModBlocks.WASTELAND_DIRT.get())
            .scatter(0.15f, ModBlocks.DRY_GRASS.get().defaultBlockState())
            // Tower
            .walls(4, 1, 4, 8, 9, 8, ModBlocks.OIL_STAINED_CONCRETE.get())
            .door(6, 1, 4, 'x')
            .window(4, 4, 5, 'z', 3)
            .window(8, 4, 5, 'z', 3)
            // Internal ladder
            .ladder(7, 1, 7, 9, Direction.NORTH)
            // Top platform
            .fill(3, 9, 3, 9, 9, 9, ModBlocks.OIL_STAINED_CONCRETE.get())
            // Broken blade arms
            .fill(0, 11, 6, 12, 11, 6, Blocks.OAK_PLANKS)
            .fill(6, 11, 0, 6, 11, 12, Blocks.OAK_PLANKS)
            .set(0, 11, 6, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .set(12, 11, 6, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .ruin(701L, 0.15f)
            .save("ruined_plains", "windmill_ruin");
    }

    private static void generateImpactCrater() {
        new StructureBuilder(11, 4, 11)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 10, 0, 10, ModBlocks.WASTELAND_DIRT.get())
            .scatter(0.1f, ModBlocks.DRY_GRASS.get().defaultBlockState())
            // Crater
            .fill(2, 0, 2, 8, 0, 8, Blocks.AIR.defaultBlockState())
            .fill(3, 0, 3, 7, 0, 7, Blocks.AIR.defaultBlockState())
            // Impact debris core
            .set(5, 1, 5, Blocks.IRON_BLOCK.defaultBlockState())
            .set(4, 1, 5, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .set(5, 1, 4, ModBlocks.RUSTED_METAL_DEBRIS.get())
            .set(6, 1, 5, ModBlocks.CONCRETE_RUBBLE.get())
            .scatterDebris(1, 1, 9, 9, 1, 0.15f)
            .save("ruined_plains", "impact_crater");
    }

    private static void generateSupplyDrop() {
        new StructureBuilder(8, 6, 8)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 7, 0, 7, ModBlocks.WASTELAND_DIRT.get())
            // Crate
            .walls(2, 1, 2, 5, 3, 5, Blocks.GREEN_WOOL.defaultBlockState())
            .door(3, 1, 2, 'x')
            // Parachute (above)
            .fill(0, 5, 0, 7, 5, 7, Blocks.WHITE_WOOL.defaultBlockState())
            .fill(1, 5, 1, 6, 5, 6, Blocks.WHITE_WOOL.defaultBlockState())
            // Tether ropes
            .pillar(2, 1, 2, 5)
            .pillar(5, 1, 2, 5)
            .pillar(2, 1, 5, 5)
            .pillar(5, 1, 5, 5)
            .chest(3, 1, 4, cache("salvager_trading_post_cache"))
            .barrel(4, 1, 3, cache("salvager_trading_post_cache"))
            .save("ruined_plains", "supply_drop");
    }

    // New full-building POI: Relay Tower
    private static void generateRelayTower() {
        new StructureBuilder(11, 14, 11)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 10, 0, 10, ModBlocks.WASTELAND_DIRT.get())
            // Base building
            .room(1, 1, 1, 9, 4, 9)
            .door(5, 1, 1, 'x')
            .window(1, 3, 4, 'z', 3)
            .window(9, 3, 4, 'z', 3)
            .floorPattern(2, 2, 8, 8, 1)
            // Tower shaft on top
            .walls(4, 5, 4, 6, 11, 6, ModBlocks.OIL_STAINED_CONCRETE.get())
            // Antenna
            .pillar(5, 12, 5, 13)
            .set(5, 13, 5, Blocks.LIGHTNING_ROD.defaultBlockState())
            .set(4, 12, 5, Blocks.IRON_BARS.defaultBlockState())
            .set(6, 12, 5, Blocks.IRON_BARS.defaultBlockState())
            .set(5, 12, 4, Blocks.IRON_BARS.defaultBlockState())
            .set(5, 12, 6, Blocks.IRON_BARS.defaultBlockState())
            // Ladder up tower
            .ladder(5, 1, 4, 11, Direction.SOUTH)
            // Furnishings (operations room)
            .furnish(2, 2, 8, terminalBlock())
            .furnish(8, 2, 8, ModBlocks.MAP_TABLE.get())
            .chest(2, 2, 2, cache("data_center_cache"))
            .ruin(702L, 0.1f)
            .save("ruined_plains", "relay_tower");
    }

    // New full-building POI: Trader Post
    private static void generateTraderPost() {
        new StructureBuilder(15, 9, 13)
            .palette(Palette.ruinedPlains())
            .fill(0, 0, 0, 14, 0, 12, ModBlocks.WASTELAND_DIRT.get())
            .scatter(0.1f, ModBlocks.DRY_GRASS.get().defaultBlockState())
            // Main hall (wide)
            .room(1, 1, 1, 13, 5, 11)
            .door(7, 1, 1, 'x')
            .window(1, 3, 4, 'z', 5)
            .window(13, 3, 4, 'z', 5)
            .window(4, 3, 11, 'x', 7)
            .floorPattern(2, 2, 12, 10, 1)
            .roofGable(1, 1, 13, 11, 6, 'x')
            // Awning porch
            .fill(0, 4, 1, 0, 4, 11, Blocks.OAK_PLANKS.defaultBlockState())
            .pillar(0, 1, 2, 3)
            .pillar(0, 1, 10, 3)
            // Furnishings — long counter
            .furnish(2, 2, 6, ModBlocks.TRADE_COUNTER.get())
            .furnish(3, 2, 6, ModBlocks.TRADE_COUNTER.get())
            .furnish(4, 2, 6, ModBlocks.TRADE_COUNTER.get())
            .furnish(11, 2, 2, ModBlocks.SUPPLY_CRATE.get())
            .furnish(11, 2, 10, ModBlocks.SUPPLY_CRATE.get())
            .furnish(7, 2, 10, ModBlocks.WEAPON_RACK.get())
            .furnish(11, 2, 6, ModBlocks.RAIN_COLLECTOR.get())
            .chest(7, 2, 2, cache("salvager_trading_post_cache"))
            .ruin(703L, 0.06f)
            .save("ruined_plains", "trader_post");
    }

    // ================= GLOBAL =================

    private static void generateDebrisFieldSmall() {
        new StructureBuilder(11, 4, 11)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 10, 0, 10, Blocks.GRAVEL)
            .fill(3, 1, 3, 7, 1, 7, Blocks.COBBLESTONE)
            .scatterDebris(0, 0, 10, 10, 1, 0.25f)
            .scatter(0.1f, Blocks.IRON_BARS.defaultBlockState())
            .scatter(0.08f, Blocks.OAK_PLANKS.defaultBlockState())
            .save("global", "debris_field_small");
    }

    private static void generateDebrisFieldLarge() {
        new StructureBuilder(15, 5, 15)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 14, 0, 14, Blocks.GRAVEL)
            .fill(2, 1, 2, 12, 1, 12, Blocks.COBBLESTONE)
            // A few mounded piles
            .fill(4, 2, 4, 6, 2, 6, Blocks.COBBLESTONE)
            .fill(8, 2, 8, 10, 2, 10, Blocks.COBBLESTONE)
            .set(5, 3, 5, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
            .set(9, 3, 9, Blocks.MOSSY_COBBLESTONE.defaultBlockState())
            .scatterDebris(0, 0, 14, 14, 1, 0.3f)
            .scatter(0.12f, Blocks.IRON_BARS.defaultBlockState())
            .scatter(0.1f, Blocks.OAK_PLANKS.defaultBlockState())
            .barrel(7, 1, 7, cache("survivor_cache"))
            .save("global", "debris_field_large");
    }

    private static void generateSurvivorCache() {
        new StructureBuilder(9, 6, 9)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 8, 0, 8, Blocks.STONE_BRICKS)
            .room(1, 1, 1, 7, 4, 7)
            .door(4, 1, 1, 'x')
            .window(1, 3, 3, 'z', 3)
            .window(7, 3, 3, 'z', 3)
            .roofGable(1, 1, 7, 7, 5, 'x')
            .floorPattern(2, 2, 6, 6, 1)
            .chest(2, 2, 6, cache("survivor_cache"))
            .barrel(6, 2, 2, cache("survivor_cache"))
            .furnish(6, 2, 6, ModBlocks.RAIN_COLLECTOR.get())
            .ruin(801L, 0.1f)
            .save("global", "survivor_cache");
    }

    private static void generateRadioRelaySmall() {
        new StructureBuilder(7, 9, 7)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 6, 0, 6, Blocks.GRAVEL)
            // Concrete pad
            .fill(1, 1, 1, 5, 1, 5, Blocks.STONE_BRICKS)
            // Tower
            .pillar(3, 1, 3, 7)
            .fill(2, 7, 3, 4, 7, 3, Blocks.IRON_BARS)
            .fill(3, 7, 2, 3, 7, 4, Blocks.IRON_BARS)
            // Antenna top
            .set(3, 8, 3, Blocks.LIGHTNING_ROD.defaultBlockState())
            // Equipment box
            .set(1, 1, 5, terminalBlock())
            .barrel(5, 1, 1, cache("data_center_cache"))
            .save("global", "radio_relay_small");
    }

    private static void generateAbandonedCamp() {
        new StructureBuilder(11, 4, 11)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 10, 0, 10, Blocks.COARSE_DIRT)
            // Fire pit
            .set(5, 1, 5, ModBlocks.ASH_CAMPFIRE.get())
            // Logs around fire
            .set(3, 1, 5, Blocks.OAK_LOG.defaultBlockState())
            .set(7, 1, 5, Blocks.OAK_LOG.defaultBlockState())
            .set(5, 1, 3, Blocks.OAK_LOG.defaultBlockState())
            .set(5, 1, 7, Blocks.OAK_LOG.defaultBlockState())
            // Lean-to shelter
            .pillar(1, 1, 1, 3).pillar(3, 1, 1, 3)
            .fill(1, 3, 1, 3, 3, 3, Blocks.OAK_PLANKS)
            .fill(1, 3, 2, 3, 3, 2, Blocks.OAK_PLANKS)
            .chest(2, 1, 2, cache("survivor_cache"))
            .scatter(0.06f, Blocks.GRAVEL.defaultBlockState())
            .save("global", "abandoned_camp");
    }

    private static void generateRoadWreck() {
        new StructureBuilder(13, 4, 7)
            .palette(Palette.globalDefault())
            .fill(0, 0, 0, 12, 0, 6, Blocks.GRAVEL)
            // Road (asphalt)
            .fill(0, 0, 2, 12, 0, 4, Blocks.BLACK_CONCRETE)
            // Lane stripe
            .fill(0, 0, 3, 12, 0, 3, Blocks.WHITE_CONCRETE)
            // Wrecked car
            .fill(4, 1, 2, 8, 2, 4, ModBlocks.RUSTED_METAL_SHEET.get())
            .fill(5, 3, 2, 7, 3, 4, ModBlocks.DROP_POD_GLASS.get())
            // Tipped sign
            .set(1, 1, 1, Blocks.OAK_FENCE.defaultBlockState())
            .set(11, 1, 5, Blocks.OAK_FENCE.defaultBlockState())
            .scatterDebris(0, 0, 12, 6, 1, 0.12f)
            .ruin(802L, 0.15f)
            .save("global", "road_wreck");
    }

    // ================= UTILITY =================

    private static ResourceKey<LootTable> cache(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("echoashfallprotocol", "chests/" + name));
    }
}
