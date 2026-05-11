package com.knoxhack.echoholomap.network;

import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HoloMapTerrainClientState {
    private static final Map<TileKey, HoloMapTerrainTile> TILES =
            new LinkedHashMap<>(256, 0.75F, true);
    private static String currentDimension = "minecraft:overworld";
    private static int discoveredCount = 0;
    private static long lastGameTime = 0L;

    private HoloMapTerrainClientState() {
    }

    public static synchronized void apply(HoloMapTileBatchPacket packet) {
        if (packet == null) {
            return;
        }
        currentDimension = packet.dimension();
        discoveredCount = packet.discoveredCount();
        lastGameTime = packet.gameTime();
        if (packet.discoveredCount() == 0 && packet.tiles().isEmpty()) {
            TILES.keySet().removeIf(key -> key.dimension().equals(packet.dimension()));
        }
        for (HoloMapTerrainTile tile : packet.tiles()) {
            TILES.put(new TileKey(tile.dimension(), tile.chunkX(), tile.chunkZ()), tile.copy());
        }
        trim();
    }

    public static synchronized List<HoloMapTerrainTile> tiles(String dimension,
            int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        String dim = dimension == null || dimension.isBlank() ? currentDimension : dimension;
        List<HoloMapTerrainTile> visible = new ArrayList<>();
        for (HoloMapTerrainTile tile : TILES.values()) {
            if (tile.dimension().equals(dim)
                    && tile.chunkX() >= minChunkX && tile.chunkX() <= maxChunkX
                    && tile.chunkZ() >= minChunkZ && tile.chunkZ() <= maxChunkZ) {
                visible.add(tile.copy());
            }
        }
        return visible;
    }

    public static synchronized int tileCount(String dimension) {
        String dim = dimension == null || dimension.isBlank() ? currentDimension : dimension;
        int count = 0;
        for (TileKey key : TILES.keySet()) {
            if (key.dimension().equals(dim)) {
                count++;
            }
        }
        return count;
    }

    public static synchronized int discoveredCount() {
        return discoveredCount;
    }

    public static synchronized long lastGameTime() {
        return lastGameTime;
    }

    public static synchronized void clear() {
        TILES.clear();
        discoveredCount = 0;
        lastGameTime = 0L;
    }

    private static void trim() {
        int max = maxCacheSize();
        while (TILES.size() > max) {
            TileKey eldest = TILES.keySet().iterator().next();
            TILES.remove(eldest);
        }
    }

    private static int maxCacheSize() {
        try {
            return Math.max(128, Config.TERRAIN_CLIENT_CACHE_SIZE.get());
        } catch (RuntimeException exception) {
            return 2048;
        }
    }

    public record TileKey(String dimension, int chunkX, int chunkZ) {
        public TileKey {
            dimension = dimension == null || dimension.isBlank() ? "minecraft:overworld" : dimension.strip();
        }
    }
}
