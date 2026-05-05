package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.worldgen.ProceduralStructureGenerator;
import com.knoxhack.echoashfallprotocol.worldgen.StructureType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Event handler for procedural structure generation during chunk loading.
 * Generates unique, randomized structures based on chunk position and biome.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class ProceduralStructureHandler {
    private static final Map<StructureType, Set<String>> VALID_BIOMES = Map.ofEntries(
            Map.entry(StructureType.DROP_POD, Set.of("crash_zone_wasteland", "ruined_plains")),
            Map.entry(StructureType.BIO_LAB, Set.of("toxic_swamp", "ruined_plains")),
            Map.entry(StructureType.DATA_CENTER, Set.of("industrial_ruins", "ruined_cityscape")),
            Map.entry(StructureType.MILITARY_VAULT, Set.of("crash_zone_wasteland", "industrial_ruins", "radiation_zone")),
            Map.entry(StructureType.REACTOR_RUIN, Set.of("radiation_zone")),
            Map.entry(StructureType.SUBWAY_STATION, Set.of("ruined_cityscape", "industrial_ruins")),
            Map.entry(StructureType.SATELLITE_ARRAY, Set.of("crash_zone_wasteland", "radiation_zone")),
            Map.entry(StructureType.RADIO_TOWER, Set.of("ruined_plains", "crash_zone_wasteland", "radiation_zone")),
            Map.entry(StructureType.SEWER_JUNCTION, Set.of("toxic_swamp", "ruined_cityscape", "industrial_ruins")),
            Map.entry(StructureType.TRAIN_YARD, Set.of("industrial_ruins", "ruined_cityscape", "crash_zone_wasteland")),
            Map.entry(StructureType.RADWARDEN_OUTPOST, Set.of("ruined_plains", "crash_zone_wasteland", "radiation_zone")),
            Map.entry(StructureType.CRASHBREAK_SALVAGE_YARD, Set.of("ruined_plains", "crash_zone_wasteland", "ruined_cityscape")),
            Map.entry(StructureType.SPOREBOUND_SANCTUM, Set.of("toxic_swamp")),
            Map.entry(StructureType.CRYOGENIC_RUINS, Set.of("cryogenic_ruins")),
            Map.entry(StructureType.RELAY_STATION, Set.of("ruined_plains", "crash_zone_wasteland")),
            Map.entry(StructureType.DERELICT_WORKSHOP, Set.of("ruined_plains", "industrial_ruins", "crash_zone_wasteland")),
            Map.entry(StructureType.ABANDONED_MINE, Set.of("industrial_ruins", "ruined_cityscape", "crash_zone_wasteland")),
            Map.entry(StructureType.OBSERVATION_POST, Set.of("ruined_plains", "crash_zone_wasteland", "cryogenic_ruins"))
    );

    
    // Structure spawn configuration: spacing, separation, salt
    private static final Map<StructureType, SpawnConfig> SPAWN_CONFIGS = new HashMap<>();
    
    static {
        // Structure type -> spacing, separation, salt
        // Spacing: chunks between structure attempts
        // Separation: minimum chunks between structures
        // Salt: unique seed modifier per structure type

        // Core structures (v1.0)
        SPAWN_CONFIGS.put(StructureType.DROP_POD, new SpawnConfig(20, 5, 210415001));
        SPAWN_CONFIGS.put(StructureType.BIO_LAB, new SpawnConfig(28, 7, 210415002));
        SPAWN_CONFIGS.put(StructureType.DATA_CENTER, new SpawnConfig(36, 9, 210415003));
        SPAWN_CONFIGS.put(StructureType.MILITARY_VAULT, new SpawnConfig(56, 14, 210415004));
        SPAWN_CONFIGS.put(StructureType.REACTOR_RUIN, new SpawnConfig(72, 18, 210415005));

        // Infrastructure structures
        SPAWN_CONFIGS.put(StructureType.SUBWAY_STATION, new SpawnConfig(32, 8, 210415006));
        SPAWN_CONFIGS.put(StructureType.SEWER_JUNCTION, new SpawnConfig(26, 6, 210415007));
        SPAWN_CONFIGS.put(StructureType.TRAIN_YARD, new SpawnConfig(40, 10, 210415008));

        // Tech/Communication structures
        SPAWN_CONFIGS.put(StructureType.SATELLITE_ARRAY, new SpawnConfig(48, 12, 210415009));
        SPAWN_CONFIGS.put(StructureType.RADIO_TOWER, new SpawnConfig(44, 11, 210415010));
        SPAWN_CONFIGS.put(StructureType.RELAY_STATION, new SpawnConfig(48, 12, 210415011));
        SPAWN_CONFIGS.put(StructureType.OBSERVATION_POST, new SpawnConfig(60, 15, 210415012));

        // Exploration 1.1: faction hubs
        SPAWN_CONFIGS.put(StructureType.RADWARDEN_OUTPOST, new SpawnConfig(50, 12, 210415013));
        SPAWN_CONFIGS.put(StructureType.CRASHBREAK_SALVAGE_YARD, new SpawnConfig(42, 10, 210415014));
        SPAWN_CONFIGS.put(StructureType.SPOREBOUND_SANCTUM, new SpawnConfig(46, 11, 210415015));

        // Exploration 1.1: world POIs
        SPAWN_CONFIGS.put(StructureType.CRYOGENIC_RUINS, new SpawnConfig(54, 13, 210415016));
        SPAWN_CONFIGS.put(StructureType.DERELICT_WORKSHOP, new SpawnConfig(38, 9, 210415017));
        SPAWN_CONFIGS.put(StructureType.ABANDONED_MINE, new SpawnConfig(58, 14, 210415018));
    }
    
    /**
     * Called during chunk generation to potentially spawn procedural structures
     */
    @SubscribeEvent
    public static void onChunkGenerate(ChunkEvent.Load event) {
        // Only run on server side during world generation
        if (event.getLevel().isClientSide()) return;
        if (!event.isNewChunk()) return;
        
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        
        ChunkAccess chunk = event.getChunk();
        BlockPos chunkCenter = chunk.getPos().getWorldPosition().offset(8, 0, 8);
        
        // Get chunk coordinates from world position
        int chunkX = chunkCenter.getX() >> 4;
        int chunkZ = chunkCenter.getZ() >> 4;
        
        // Check each structure type for spawning
        for (StructureType type : StructureType.values()) {
            if (shouldSpawnStructure(chunkX, chunkZ, type, serverLevel, chunkCenter)) {
                BlockPos spawnPos = findValidSpawnPosition(serverLevel, chunkCenter, type);
                if (spawnPos != null) {
                    generateStructure(serverLevel, spawnPos, type);
                    break; // Only spawn one structure per chunk
                }
            }
        }
    }
    
    /**
     * Determine if a structure should spawn in this chunk
     */
    private static boolean shouldSpawnStructure(int chunkX, int chunkZ, StructureType type,
                                                 ServerLevel level, BlockPos center) {
        SpawnConfig config = SPAWN_CONFIGS.get(type);
        if (config == null) return false;
        if (!Config.isStructureEnabled(type)) return false;
        
        // Check spacing using salt-based randomization
        long seed = level.getSeed();
        int spacing = Config.getStructureSpacing(type);
        int separation = Config.getStructureSeparation(type);
        int salt = config.salt;
        
        // Calculate region coordinates
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);
        
        // Create deterministic random for this region
        java.util.Random random = new java.util.Random(
            seed + (long) regionX * 341873128712L + (long) regionZ * 132897987541L + salt
        );
        
        // Pick a random chunk within the region
        int spawnChunkX = regionX * spacing + random.nextInt(spacing - separation);
        int spawnChunkZ = regionZ * spacing + random.nextInt(spacing - separation);
        
        // Check if this is the selected chunk
        if (chunkX != spawnChunkX || chunkZ != spawnChunkZ) {
            return false;
        }
        
        // Check biome compatibility
        return isValidBiome(level, center, type);
    }
    
    /**
     * Check if the biome at this position can spawn this structure type
     */
    private static boolean isValidBiome(ServerLevel level, BlockPos pos, StructureType type) {
        var biome = level.getBiome(pos);
        String biomePath = biome.unwrapKey()
                .map(Object::toString)
                .map(ProceduralStructureHandler::extractBiomePath)
                .orElse("");

        if (!Config.isBiomeContentEnabled(biomePath)) {
            return false;
        }

        if (ProceduralStructureGenerator.isProfileStructureForBiome(biomePath, type)) {
            return true;
        }

        Set<String> validBiomes = VALID_BIOMES.get(type);
        return validBiomes != null && validBiomes.contains(biomePath);
    }

    private static String extractBiomePath(String keyString) {
        int lastSlash = keyString.lastIndexOf('/');
        int lastBracket = keyString.lastIndexOf(']');
        if (lastSlash >= 0 && lastBracket > lastSlash) {
            return keyString.substring(lastSlash + 1, lastBracket);
        }
        int namespaceSep = keyString.indexOf(':');
        if (namespaceSep >= 0 && namespaceSep + 1 < keyString.length()) {
            return keyString.substring(namespaceSep + 1);
        }
        return keyString;
    }
    
    /**
     * Find a valid surface position for structure spawning
     */
    private static BlockPos findValidSpawnPosition(ServerLevel level, BlockPos center, StructureType type) {
        // Scan area to find suitable terrain
        int searchRadius = 8;
        
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                BlockPos checkPos = center.offset(dx, 0, dz);
                
                // Get surface height
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                                checkPos.getX(), checkPos.getZ());
                
                BlockPos surfacePos = checkPos.atY(surfaceY);
                
                // Validate position
                if (isValidSurface(level, surfacePos, type)) {
                    return surfacePos;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if surface is valid for this structure type
     */
    private static boolean isValidSurface(ServerLevel level, BlockPos pos, StructureType type) {
        // Basic checks
        if (pos.getY() < -64 + 5) return false;
        if (pos.getY() > 320 - 20) return false;
        
        // Check ground is solid
        BlockPos groundPos = pos.below();
        if (!level.getBlockState(groundPos).canOcclude()) return false;
        
        // Check water
        if (!level.getFluidState(pos).isEmpty()) return false;
        
        // Type-specific checks
        return switch (type) {
            case REACTOR_RUIN -> true; // Can spawn anywhere
            case MILITARY_VAULT -> pos.getY() > 60; // Prefer higher ground
            case DROP_POD -> pos.getY() > 62; // Flat areas
            default -> true;
        };
    }
    
    /**
     * Generate the structure at the given position
     */
    private static void generateStructure(ServerLevel level, BlockPos pos, StructureType type) {
        try {
            // Use the world's random source for consistency
            net.minecraft.util.RandomSource random = level.getRandom();
            
            // Log generation
            EchoAshfallProtocol.LOGGER.info("Generating procedural {} at {}", 
                    type.getName(), pos);
            
            // Generate the structure using LevelAccessor
            ProceduralStructureGenerator.generateStructure(level, pos, type, random);
            
        } catch (Exception e) {
            EchoAshfallProtocol.LOGGER.error("Failed to generate procedural structure {} at {}: {}",
                    type.getName(), pos, e.getMessage());
        }
    }
    
    /**
     * Configuration for structure spawning
     */
    private record SpawnConfig(int spacing, int separation, int salt) {}
}
