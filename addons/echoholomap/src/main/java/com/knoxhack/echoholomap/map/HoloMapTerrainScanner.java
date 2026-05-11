package com.knoxhack.echoholomap.map;

import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.world.HoloMapTerrainSavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class HoloMapTerrainScanner {
    private static final Map<UUID, Integer> SCAN_CURSORS = new HashMap<>();

    private HoloMapTerrainScanner() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int interval = scanIntervalTicks();
        if (interval <= 0 || player.level().getGameTime() % interval != 0L) {
            return;
        }
        scanAround(player, scanRadiusChunks(), maxSampleChunksPerTick());
    }

    public static int scanAround(ServerPlayer player, int radius, int maxChunks) {
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
            if (!data.needsSample(player.getUUID(), level.dimension(), chunkX, chunkZ, now, resampleInterval)) {
                continue;
            }
            HoloMapTerrainTile tile = sampleChunk(level, chunkX, chunkZ, now);
            data.saveTile(player.getUUID(), level.dimension(), chunkX, chunkZ, now, tile.pixels());
            sampled++;
        }
        SCAN_CURSORS.put(player.getUUID(), Math.floorMod(cursor + visited, Math.max(1, total)));
        return sampled;
    }

    public static HoloMapTerrainTile sampleChunk(ServerLevel level, int chunkX, int chunkZ, long sampledTime) {
        int[] pixels = new int[HoloMapTerrainTile.PIXELS];
        int baseX = chunkX * HoloMapTerrainTile.SIZE;
        int baseZ = chunkZ * HoloMapTerrainTile.SIZE;
        for (int localZ = 0; localZ < HoloMapTerrainTile.SIZE; localZ++) {
            for (int localX = 0; localX < HoloMapTerrainTile.SIZE; localX++) {
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                int surfaceY = Math.max(level.getMinY(), topY - 1);
                BlockPos surface = new BlockPos(worldX, surfaceY, worldZ);
                pixels[localZ * HoloMapTerrainTile.SIZE + localX] = HoloMapTerrainPalette.colorFor(level, surface);
            }
        }
        return new HoloMapTerrainTile(level.dimension().identifier().toString(), chunkX, chunkZ,
                sampledTime, pixels);
    }

    public static void clearForTests() {
        SCAN_CURSORS.clear();
    }

    private static int scanIntervalTicks() {
        try {
            return Math.max(5, Config.TERRAIN_SCAN_INTERVAL.get());
        } catch (RuntimeException exception) {
            return 40;
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
            return 6;
        }
    }

    private static long terrainResampleInterval() {
        try {
            return Math.max(0, Config.TERRAIN_RESAMPLE_INTERVAL.get());
        } catch (RuntimeException exception) {
            return 2400L;
        }
    }
}
