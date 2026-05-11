package com.knoxhack.echoholomap;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_MARKERS = BUILDER
            .comment("Maximum HoloMap markers sent to one client snapshot.")
            .defineInRange("map.maxMarkers", 384, 32, 2048);

    public static final ModConfigSpec.IntValue DETAIL_LABEL_LIMIT = BUILDER
            .comment("Maximum number of labels drawn in high-detail HoloMap mode.")
            .defineInRange("map.detailLabelLimit", 42, 0, 256);

    public static final ModConfigSpec.DoubleValue VIRTUAL_MAP_SCALE = BUILDER
            .comment("Scale applied to virtual atlas coordinates for providers that do not yet expose exact world positions.")
            .defineInRange("map.virtualScale", 1.0D, 0.25D, 4.0D);

    public static final ModConfigSpec.BooleanValue SHOW_HIDDEN_MARKERS = BUILDER
            .comment("Shows markers flagged as HIDDEN in snapshots. Intended for debugging only.")
            .define("map.showHiddenMarkers", false);

    public static final ModConfigSpec.BooleanValue DEBUG_MARKERS = BUILDER
            .comment("Enables permission-gated /echoholomap debug marker commands and Terminal test marker hook.")
            .define("debug.markersEnabled", true);

    public static final ModConfigSpec.IntValue TERRAIN_SCAN_INTERVAL = BUILDER
            .comment("Server ticks between automatic terrain discovery scans for each player.")
            .defineInRange("terrain.scanIntervalTicks", 40, 5, 600);

    public static final ModConfigSpec.IntValue TERRAIN_SCAN_RADIUS = BUILDER
            .comment("Loaded chunk radius around each player eligible for automatic HoloMap terrain discovery.")
            .defineInRange("terrain.scanRadiusChunks", 5, 0, 16);

    public static final ModConfigSpec.IntValue TERRAIN_MAX_SAMPLE_CHUNKS_PER_TICK = BUILDER
            .comment("Maximum loaded chunks sampled for terrain per player scan pass.")
            .defineInRange("terrain.maxSampleChunksPerTick", 6, 1, 64);

    public static final ModConfigSpec.IntValue TERRAIN_MAX_TILES_PER_PLAYER = BUILDER
            .comment("Maximum discovered terrain tiles retained per player before oldest tiles are evicted.")
            .defineInRange("terrain.maxTilesPerPlayer", 4096, 128, 65536);

    public static final ModConfigSpec.IntValue TERRAIN_MAX_TILE_BATCH_SIZE = BUILDER
            .comment("Maximum terrain tiles sent to one client in a single batch packet.")
            .defineInRange("terrain.maxTileBatchSize", 128, 8, 1024);

    public static final ModConfigSpec.IntValue TERRAIN_RESAMPLE_INTERVAL = BUILDER
            .comment("Minimum ticks before an already discovered loaded chunk is resampled. Set to 0 to resample whenever reached by the scanner.")
            .defineInRange("terrain.resampleIntervalTicks", 2400, 0, 24000);

    public static final ModConfigSpec.IntValue TERRAIN_MAX_REQUEST_RADIUS = BUILDER
            .comment("Maximum chunk radius accepted for a client terrain tile viewport request.")
            .defineInRange("terrain.maxRequestRadiusChunks", 8, 1, 24);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static final ModConfigSpec.BooleanValue MINIMAP_ENABLED = CLIENT_BUILDER
            .comment("Shows the HoloMap minimap HUD when toggled on.")
            .define("minimap.enabled", true);

    public static final ModConfigSpec.EnumValue<MiniMapCorner> MINIMAP_CORNER = CLIENT_BUILDER
            .comment("Screen corner used by the HoloMap minimap HUD.")
            .defineEnum("minimap.corner", MiniMapCorner.TOP_RIGHT);

    public static final ModConfigSpec.IntValue MINIMAP_SIZE = CLIENT_BUILDER
            .comment("Square minimap size in GUI pixels.")
            .defineInRange("minimap.size", 104, 64, 196);

    public static final ModConfigSpec.DoubleValue MINIMAP_ZOOM = CLIENT_BUILDER
            .comment("Minimap terrain zoom in pixels per world block.")
            .defineInRange("minimap.zoom", 1.35D, 0.5D, 4.0D);

    public static final ModConfigSpec.IntValue TERRAIN_CLIENT_CACHE_SIZE = CLIENT_BUILDER
            .comment("Maximum discovered terrain tiles retained in the in-memory client cache.")
            .defineInRange("terrain.cacheSize", 2048, 128, 32768);

    public static final ModConfigSpec.BooleanValue TERRAIN_LABELS = CLIENT_BUILDER
            .comment("Allows high-detail map labels for terrain-backed HoloMap views.")
            .define("terrain.labels", true);

    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    private Config() {
    }

    public enum MiniMapCorner {
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoHoloMap.MODID, () -> new EchoConfigModule(
                EchoHoloMap.MODID,
                "HoloMap",
                List.of(new EchoConfigCategory("map", "Map", List.of(
                        EchoConfigEntry.intSpec("max_markers", "Max Markers",
                                "Maximum HoloMap markers sent to one client snapshot.",
                                EchoConfigSide.COMMON, MAX_MARKERS, 32, 2048,
                                true, false, false),
                        EchoConfigEntry.intSpec("detail_label_limit", "Detail Label Limit",
                                "Maximum number of labels drawn in high-detail HoloMap mode.",
                                EchoConfigSide.COMMON, DETAIL_LABEL_LIMIT, 0, 256,
                                true, false, false),
                        EchoConfigEntry.doubleSpec("virtual_map_scale", "Virtual Map Scale",
                                "Scale applied to virtual atlas coordinates without exact world positions.",
                                EchoConfigSide.COMMON, VIRTUAL_MAP_SCALE, 0.25D, 4.0D,
                                true, false, false))),
                        new EchoConfigCategory("terrain", "Terrain Discovery", List.of(
                                EchoConfigEntry.intSpec("scan_interval", "Scan Interval",
                                        "Server ticks between automatic terrain discovery scans.",
                                        EchoConfigSide.COMMON, TERRAIN_SCAN_INTERVAL, 5, 600,
                                        true, false, false),
                                EchoConfigEntry.intSpec("scan_radius", "Scan Radius",
                                        "Loaded chunk radius around each player eligible for terrain discovery.",
                                        EchoConfigSide.COMMON, TERRAIN_SCAN_RADIUS, 0, 16,
                                        true, false, false),
                                EchoConfigEntry.intSpec("sample_cap", "Sample Cap",
                                        "Maximum loaded chunks sampled for terrain per player scan pass.",
                                        EchoConfigSide.COMMON, TERRAIN_MAX_SAMPLE_CHUNKS_PER_TICK, 1, 64,
                                        true, false, false),
                                EchoConfigEntry.intSpec("tile_batch", "Tile Batch",
                                        "Maximum terrain tiles sent to a client in a single packet.",
                                        EchoConfigSide.COMMON, TERRAIN_MAX_TILE_BATCH_SIZE, 8, 1024,
                                        true, false, false))),
                        new EchoConfigCategory("minimap", "Minimap", List.of(
                                EchoConfigEntry.booleanSpec("enabled", "Enabled",
                                        "Shows the HoloMap minimap HUD when toggled on.",
                                        EchoConfigSide.CLIENT, MINIMAP_ENABLED,
                                        true, false, false),
                                EchoConfigEntry.intSpec("size", "Size",
                                        "Square minimap size in GUI pixels.",
                                        EchoConfigSide.CLIENT, MINIMAP_SIZE, 64, 196,
                                        true, false, false),
                                EchoConfigEntry.doubleSpec("zoom", "Zoom",
                                        "Minimap terrain zoom in pixels per world block.",
                                        EchoConfigSide.CLIENT, MINIMAP_ZOOM, 0.5D, 4.0D,
                                        true, false, false)))))));
    }
}
