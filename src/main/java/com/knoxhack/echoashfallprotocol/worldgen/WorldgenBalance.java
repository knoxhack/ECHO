package com.knoxhack.echoashfallprotocol.worldgen;

/**
 * Shared world-generation density targets for the Sparse Survival pass.
 *
 * Values are in chunks and mirror the datapack structure_set JSONs. Keeping the
 * numbers here gives the legacy Java spawner and future debug tools one clear
 * reference for the intended cadence.
 *
 * POI Category Definitions (Sparse Survival target):
 *
 * | Category   | Spacing | Separation | Examples                                  |
 * |------------|---------|------------|-------------------------------------------|
 * | Global     | 32      | 12         | survivor_cache, road_wreck, radio_relay   |
 * | Biome/Micro| 20      | 8          | nomad_camp, windmill_ruin, scrap_piles    |
 * | Crash      | 18      | 7          | crash debris, wreck camps, salvage scars   |
 * | Urban      | 16      | 6          | city blocks, factories, industrial yards   |
 * | Camp       | 20      | 8          | scavenger_camp, supply_drop (ruined_plains)|
 * | Landmark   | 48      | 18         | faction villages, industrial_factory, subway|
 * | Major      | 56      | 22         | bio_lab, data_center, military_vault, reactor|
 *
 * Datapack structure_set JSONs are the authority for POI generation.
 * The legacy POIStructureSpawner is disabled by default.
 */
public final class WorldgenBalance {
    private WorldgenBalance() {}

    /** Biome-specific micro POIs (nomad_camp, windmill_ruin, etc.) */
    public static final int MICRO_POI_SPACING = 20;
    public static final int MICRO_POI_SEPARATION = 8;

    /** Crash-zone POIs: denser wreckage without becoming a city biome. */
    public static final int CRASH_POI_SPACING = 18;
    public static final int CRASH_POI_SEPARATION = 7;

    /** Urban and industrial POIs: denser ruin fields for built-up biomes. */
    public static final int URBAN_POI_SPACING = 16;
    public static final int URBAN_POI_SEPARATION = 6;

    /** Global POIs that can spawn in any biome */
    public static final int GLOBAL_POI_SPACING = 32;
    public static final int GLOBAL_POI_SEPARATION = 12;

    /** Camp-tier POIs (scavenger_camp, supply_drop) */
    public static final int CAMP_SPACING = 20;
    public static final int CAMP_SEPARATION = 8;

    /** Landmark structures (faction villages, factories) */
    public static final int LANDMARK_SPACING = 48;
    public static final int LANDMARK_SEPARATION = 18;

    /** Major rare structures (bio_lab, data_center, etc.) */
    public static final int MAJOR_SPACING = 56;
    public static final int MAJOR_SEPARATION = 22;
}
