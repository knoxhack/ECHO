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
    private static List<HoloMapTerrainTile> cachedVisibleTiles = List.of();
    private static String cachedVisibleDimension = "";
    private static int cachedMinChunkX = Integer.MIN_VALUE;
    private static int cachedMaxChunkX = Integer.MIN_VALUE;
    private static int cachedMinChunkZ = Integer.MIN_VALUE;
    private static int cachedMaxChunkZ = Integer.MIN_VALUE;
    private static final Map<String, DetailStats> DETAIL_STATS = new LinkedHashMap<>();

    private HoloMapTerrainClientState() {
    }

    public static synchronized void apply(HoloMapTileBatchPacket packet) {
        if (packet == null) {
            return;
        }
        currentDimension = normalizeDimension(packet.dimension());
        discoveredCount = packet.discoveredCount();
        lastGameTime = packet.gameTime();
        if (packet.discoveredCount() == 0 && packet.tiles().isEmpty()) {
            TILES.keySet().removeIf(key -> key.dimension().equals(currentDimension));
        }
        for (HoloMapTerrainTile tile : packet.tiles()) {
            TILES.put(new TileKey(tile.dimension(), tile.chunkX(), tile.chunkZ()), tile.copy());
        }
        trim();
        invalidateCaches();
    }

    public static synchronized List<HoloMapTerrainTile> tiles(String dimension,
            int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        String dim = normalizeDimension(dimension);
        if (dim.equals(cachedVisibleDimension)
                && minChunkX == cachedMinChunkX
                && maxChunkX == cachedMaxChunkX
                && minChunkZ == cachedMinChunkZ
                && maxChunkZ == cachedMaxChunkZ) {
            return cachedVisibleTiles;
        }
        List<HoloMapTerrainTile> visible = new ArrayList<>();
        for (HoloMapTerrainTile tile : TILES.values()) {
            if (tile.dimension().equals(dim)
                    && tile.chunkX() >= minChunkX && tile.chunkX() <= maxChunkX
                    && tile.chunkZ() >= minChunkZ && tile.chunkZ() <= maxChunkZ) {
                visible.add(tile);
            }
        }
        cachedVisibleDimension = dim;
        cachedMinChunkX = minChunkX;
        cachedMaxChunkX = maxChunkX;
        cachedMinChunkZ = minChunkZ;
        cachedMaxChunkZ = maxChunkZ;
        cachedVisibleTiles = List.copyOf(visible);
        return cachedVisibleTiles;
    }

    public static synchronized int tileCount(String dimension) {
        String dim = normalizeDimension(dimension);
        int count = 0;
        for (TileKey key : TILES.keySet()) {
            if (key.dimension().equals(dim)) {
                count++;
            }
        }
        return count;
    }

    public static synchronized DetailStats detailStats(String dimension) {
        String dim = normalizeDimension(dimension);
        DetailStats cached = DETAIL_STATS.get(dim);
        if (cached != null) {
            return cached;
        }
        int total = 0;
        int legacy = 0;
        int biomeFallback = 0;
        int surfaceBlock = 0;
        int surfaceShaded = 0;
        long newestSample = 0L;
        for (HoloMapTerrainTile tile : TILES.values()) {
            if (!tile.dimension().equals(dim)) {
                continue;
            }
            total++;
            if (tile.version() < HoloMapTerrainTile.CURRENT_VERSION) {
                legacy++;
            }
            if (tile.detailMode() == HoloMapTerrainTile.DetailMode.BIOME_FALLBACK) {
                biomeFallback++;
            } else if (tile.detailMode() == HoloMapTerrainTile.DetailMode.SURFACE_BLOCK) {
                surfaceBlock++;
            } else if (tile.detailMode() == HoloMapTerrainTile.DetailMode.SURFACE_SHADED) {
                surfaceShaded++;
            }
            newestSample = Math.max(newestSample, tile.sampledTime());
        }
        DetailStats stats = new DetailStats(total, legacy, biomeFallback, surfaceBlock, surfaceShaded, newestSample);
        DETAIL_STATS.put(dim, stats);
        return stats;
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
        invalidateCaches();
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
            return 1024;
        }
    }

    private static void invalidateCaches() {
        cachedVisibleTiles = List.of();
        cachedVisibleDimension = "";
        cachedMinChunkX = Integer.MIN_VALUE;
        cachedMaxChunkX = Integer.MIN_VALUE;
        cachedMinChunkZ = Integer.MIN_VALUE;
        cachedMaxChunkZ = Integer.MIN_VALUE;
        DETAIL_STATS.clear();
    }

    private static String normalizeDimension(String dimension) {
        return dimension == null || dimension.isBlank() ? currentDimension : dimension.strip();
    }

    public record TileKey(String dimension, int chunkX, int chunkZ) {
        public TileKey {
            dimension = dimension == null || dimension.isBlank() ? "minecraft:overworld" : dimension.strip();
        }
    }

    public record DetailStats(int total, int legacy, int biomeFallback, int surfaceBlock, int surfaceShaded,
            long newestSample) {
        public String label() {
            if (total <= 0) {
                return "pending";
            }
            if (surfaceShaded > 0) {
                return "v" + HoloMapTerrainTile.CURRENT_VERSION + " shaded " + surfaceShaded + "/" + total;
            }
            if (surfaceBlock > 0) {
                return "v" + HoloMapTerrainTile.CURRENT_VERSION + " block " + surfaceBlock + "/" + total;
            }
            return "legacy biome " + biomeFallback + "/" + total;
        }

        public String compactLabel() {
            if (total <= 0) {
                return "PENDING";
            }
            if (surfaceShaded > 0) {
                return "SHADED " + surfaceShaded;
            }
            if (surfaceBlock > 0) {
                return "BLOCK " + surfaceBlock;
            }
            return "BIO " + biomeFallback;
        }
    }
}
