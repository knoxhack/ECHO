package com.knoxhack.echoholomap.map;

import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.integration.HoloMapMissionHooks;
import com.knoxhack.echoholomap.integration.runtimeguard.HoloMapRuntimeGuardHooks;
import com.knoxhack.echoholomap.world.HoloMapTerrainSavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class HoloMapTerrainScanner {
    private static final Map<UUID, Integer> SCAN_CURSORS = new HashMap<>();
    private static final Map<UUID, ScanState> LAST_SCAN_STATES = new HashMap<>();

    private HoloMapTerrainScanner() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int interval = HoloMapRuntimeGuardHooks.refreshIntervalTicks(scanIntervalTicks());
        long now = player.level().getGameTime();
        if (interval <= 0 || Math.floorMod(now + playerScanStagger(player, interval), interval) != 0L) {
            return;
        }
        int centerChunkX = Math.floorDiv(player.blockPosition().getX(), HoloMapTerrainTile.SIZE);
        int centerChunkZ = Math.floorDiv(player.blockPosition().getZ(), HoloMapTerrainTile.SIZE);
        String dimension = player.level().dimension().identifier().toString();
        ScanState previous = LAST_SCAN_STATES.get(player.getUUID());
        if (previous != null && previous.matches(dimension, centerChunkX, centerChunkZ)
                && previous.complete()
                && now - previous.scanTick() < stationaryRescanTicks(interval)) {
            return;
        }
        int sampled = scanAround(player, scanRadiusChunks(), maxSampleChunksPerTick());
        LAST_SCAN_STATES.put(player.getUUID(), new ScanState(dimension, centerChunkX, centerChunkZ, now, sampled == 0));
    }

    public static int scanAround(ServerPlayer player, int radius, int maxChunks) {
        return scanAround(player, radius, maxChunks, false);
    }

    public static int scanAround(ServerPlayer player, int radius, int maxChunks, boolean forceResample) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        int safeRadius = Math.max(0, Math.min(32, radius));
        int safeMax = Math.max(1, Math.min(256, maxChunks));
        int diameter = safeRadius * 2 + 1;
        int total = diameter * diameter;
        int cursor = SCAN_CURSORS.getOrDefault(player.getUUID(), 0);
        int centerChunkX = Math.floorDiv(player.blockPosition().getX(), HoloMapTerrainTile.SIZE);
        int centerChunkZ = Math.floorDiv(player.blockPosition().getZ(), HoloMapTerrainTile.SIZE);
        long now = level.getGameTime();
        long resampleInterval = terrainResampleInterval();
        HoloMapTerrainSavedData data = HoloMapTerrainSavedData.get(level);
        int sampled = 0;
        int visited = 0;
        while (visited < total && sampled < safeMax) {
            int index = Math.floorMod(cursor + visited, total);
            int offsetX = index % diameter - safeRadius;
            int offsetZ = index / diameter - safeRadius;
            int chunkX = centerChunkX + offsetX;
            int chunkZ = centerChunkZ + offsetZ;
            visited++;
            if (!level.hasChunk(chunkX, chunkZ)) {
                continue;
            }
            if (!forceResample
                    && !data.needsSample(player.getUUID(), level.dimension(), chunkX, chunkZ, now, resampleInterval)) {
                continue;
            }
            HoloMapTerrainTile tile = sampleChunk(level, chunkX, chunkZ, now);
            data.saveTile(player.getUUID(), level.dimension(), chunkX, chunkZ, tile);
            sampled++;
        }
        SCAN_CURSORS.put(player.getUUID(), Math.floorMod(cursor + visited, Math.max(1, total)));
        if (sampled > 0) {
            HoloMapMissionHooks.recordTerrainDiscovered(player, sampled);
        }
        return sampled;
    }

    public static HoloMapTerrainTile sampleChunk(ServerLevel level, int chunkX, int chunkZ, long sampledTime) {
        int[] pixels = new int[HoloMapTerrainTile.PIXELS];
        int baseX = chunkX * HoloMapTerrainTile.SIZE;
        int baseZ = chunkZ * HoloMapTerrainTile.SIZE;
        int fallbackPixels = 0;
        int surfacePixels = 0;
        for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
            for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                SurfaceSample surface = findSurface(level, worldX, worldZ);
                HoloMapTerrainPalette.SurfaceColor color = surface.fallback()
                        ? biomeFallback(level, worldX, worldZ, surface.height(), surface.water())
                        : HoloMapTerrainPalette.surfaceColorFor(level, surface.pos(), surface.shore());
                if (color.detailMode() == HoloMapTerrainTile.DetailMode.BIOME_FALLBACK) {
                    fallbackPixels++;
                } else {
                    surfacePixels++;
                }
                pixels[localZ * HoloMapTerrainTile.SIZE + localX] = color.argb();
            }
        }
        HoloMapTerrainTile.DetailMode detailMode = surfacePixels == 0
                ? HoloMapTerrainTile.DetailMode.BIOME_FALLBACK
                : fallbackPixels > 0
                        ? HoloMapTerrainTile.DetailMode.SURFACE_BLOCK
                        : HoloMapTerrainTile.DetailMode.SURFACE_SHADED;
        return new HoloMapTerrainTile(level.dimension().identifier().toString(), chunkX, chunkZ,
                sampledTime, HoloMapTerrainTile.CURRENT_VERSION, detailMode, pixels);
    }

    public static void clearForTests() {
        SCAN_CURSORS.clear();
        LAST_SCAN_STATES.clear();
    }

    private static int scanIntervalTicks() {
        try {
            return Math.max(5, Config.TERRAIN_SCAN_INTERVAL.get());
        } catch (RuntimeException exception) {
            return 100;
        }
    }

    private static int scanRadiusChunks() {
        try {
            return Math.max(0, Config.TERRAIN_SCAN_RADIUS.get());
        } catch (RuntimeException exception) {
            return 5;
        }
    }

    private static int maxSampleChunksPerTick() {
        try {
            return Math.max(1, Config.TERRAIN_MAX_SAMPLE_CHUNKS_PER_TICK.get());
        } catch (RuntimeException exception) {
            return 3;
        }
    }

    private static long playerScanStagger(ServerPlayer player, int interval) {
        UUID id = player.getUUID();
        return Math.floorMod(id.getMostSignificantBits() ^ id.getLeastSignificantBits(), Math.max(1, interval));
    }

    private static long stationaryRescanTicks(int interval) {
        long resample = terrainResampleInterval();
        return resample <= 0L ? Math.max(20L, interval) : Math.max(interval, resample);
    }

    private static long terrainResampleInterval() {
        try {
            return Math.max(0, Config.TERRAIN_RESAMPLE_INTERVAL.get());
        } catch (RuntimeException exception) {
            return 2400L;
        }
    }

    private static SurfaceSample findSurface(ServerLevel level, int worldX, int worldZ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
        int minY = level.getMinY();
        int safeTop = Math.max(minY, topY);
        if (isCeilingFallback(level, safeTop)) {
            return new SurfaceSample(new BlockPos(worldX, safeTop, worldZ), true, false, false, safeTop);
        }
        int floor = Math.max(minY, safeTop - 48);
        for (int y = safeTop; y >= floor; y--) {
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (state.is(Blocks.BEDROCK) && y >= level.getMaxY() - 8) {
                continue;
            }
            boolean water = isWater(level, pos, state);
            boolean shore = water && hasAdjacentLand(level, pos);
            return new SurfaceSample(pos, false, water, shore, y);
        }
        return new SurfaceSample(new BlockPos(worldX, safeTop, worldZ), true, false, false, safeTop);
    }

    private static HoloMapTerrainPalette.SurfaceColor biomeFallback(ServerLevel level, int worldX, int worldZ,
            int height, boolean water) {
        BlockPos biomePos = new BlockPos(worldX, Math.max(level.getMinY(), height), worldZ);
        String biome = level.getBiome(biomePos)
                .unwrapKey()
                .map(key -> key.identifier().getPath())
                .orElse("plains");
        int color = HoloMapTerrainPalette.colorForBiome(
                level.dimension().identifier().toString(),
                biome,
                height,
                water);
        return new HoloMapTerrainPalette.SurfaceColor(color, HoloMapTerrainTile.DetailMode.BIOME_FALLBACK);
    }

    private static boolean isCeilingFallback(ServerLevel level, int topY) {
        return level.dimension().equals(Level.NETHER) && topY >= level.getMaxY() - 16;
    }

    private static boolean isWater(ServerLevel level, BlockPos pos, BlockState state) {
        return state.is(Blocks.WATER) || level.getFluidState(pos).is(FluidTags.WATER);
    }

    private static boolean hasAdjacentLand(ServerLevel level, BlockPos pos) {
        return isLand(level, pos.north()) || isLand(level, pos.south())
                || isLand(level, pos.east()) || isLand(level, pos.west());
    }

    private static boolean isLand(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !isWater(level, pos, state);
    }

    private record SurfaceSample(BlockPos pos, boolean fallback, boolean water, boolean shore, int height) {
    }

    private record ScanState(String dimension, int chunkX, int chunkZ, long scanTick, boolean complete) {
        private boolean matches(String dimension, int chunkX, int chunkZ) {
            return this.dimension.equals(dimension) && this.chunkX == chunkX && this.chunkZ == chunkZ;
        }
    }
}
