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
            .defineInRange("terrain.scanIntervalTicks", 100, 5, 600);

    public static final ModConfigSpec.IntValue TERRAIN_SCAN_RADIUS = BUILDER
            .comment("Loaded chunk radius around each player eligible for automatic HoloMap terrain discovery.")
            .defineInRange("terrain.scanRadiusChunks", 5, 0, 16);

    public static final ModConfigSpec.IntValue TERRAIN_MAX_SAMPLE_CHUNKS_PER_TICK = BUILDER
            .comment("Maximum loaded chunks sampled for terrain per player scan pass.")
            .defineInRange("terrain.maxSampleChunksPerTick", 3, 1, 64);

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

    public static final ModConfigSpec.IntValue WAYPOINT_SYNC_LIMIT = BUILDER
            .comment("Maximum personal/shared HoloMap waypoints sent to one client sync packet.")
            .defineInRange("waypoints.syncLimit", 256, 16, 2048);

    public static final ModConfigSpec.BooleanValue DEATHPOINTS_ENABLED = BUILDER
            .comment("Creates personal HoloMap deathpoints when players die.")
            .define("deathpoints.enabled", true);

    public static final ModConfigSpec.IntValue DEATHPOINTS_MAX_PER_PLAYER = BUILDER
            .comment("Maximum personal deathpoints retained per player.")
            .defineInRange("deathpoints.maxPerPlayer", 10, 0, 128);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static final ModConfigSpec.BooleanValue MINIMAP_ENABLED = CLIENT_BUILDER
            .comment("Shows the HoloMap minimap HUD when toggled on.")
            .define("minimap.enabled", false);

    public static final ModConfigSpec.EnumValue<MiniMapCorner> MINIMAP_CORNER = CLIENT_BUILDER
            .comment("Screen corner used by the HoloMap minimap HUD.")
            .defineEnum("minimap.corner", MiniMapCorner.TOP_RIGHT);

    public static final ModConfigSpec.IntValue MINIMAP_SIZE = CLIENT_BUILDER
            .comment("Square minimap size in GUI pixels.")
            .defineInRange("minimap.size", 104, 64, 196);

    public static final ModConfigSpec.DoubleValue MINIMAP_ZOOM = CLIENT_BUILDER
            .comment("Minimap terrain zoom in pixels per world block.")
            .defineInRange("minimap.zoom", 1.35D, 0.5D, 4.0D);

    public static final ModConfigSpec.DoubleValue MINIMAP_OPACITY = CLIENT_BUILDER
            .comment("Minimap panel opacity from transparent to solid.")
            .defineInRange("minimap.opacity", 0.72D, 0.2D, 1.0D);

    public static final ModConfigSpec.IntValue MINIMAP_MARKER_DENSITY = CLIENT_BUILDER
            .comment("Maximum markers and waypoints drawn on the minimap.")
            .defineInRange("minimap.markerDensity", 24, 0, 192);

    public static final ModConfigSpec.DoubleValue MINIMAP_MARKER_SCALE = CLIENT_BUILDER
            .comment("Marker icon scale multiplier for HoloMap HUD rendering.")
            .defineInRange("minimap.markerScale", 1.0D, 0.7D, 1.6D);

    public static final ModConfigSpec.DoubleValue MINIMAP_TERRAIN_BRIGHTNESS = CLIENT_BUILDER
            .comment("Brightness multiplier applied to minimap terrain colors.")
            .defineInRange("minimap.terrainBrightness", 1.0D, 0.55D, 1.45D);

    public static final ModConfigSpec.DoubleValue MINIMAP_TERRAIN_CONTRAST = CLIENT_BUILDER
            .comment("Contrast multiplier applied to minimap terrain colors.")
            .defineInRange("minimap.terrainContrast", 1.08D, 0.55D, 1.65D);

    public static final ModConfigSpec.EnumValue<LabelMode> MINIMAP_LABEL_MODE = CLIENT_BUILDER
            .comment("Controls which minimap marker labels are drawn.")
            .defineEnum("minimap.labelMode", LabelMode.SELECTED);

    public static final ModConfigSpec.EnumValue<VisualDensity> MAP_VISUAL_DENSITY = CLIENT_BUILDER
            .comment("Overall HoloMap visual density preference.")
            .defineEnum("map.visualDensity", VisualDensity.MEDIUM);

    public static final ModConfigSpec.BooleanValue MINIMAP_HIGH_DETAIL_TERRAIN = CLIENT_BUILDER
            .comment("Allows the minimap to draw per-block terrain pixels at high zoom. Disabled by default for FPS.")
            .define("minimap.highDetailTerrain", false);

    public static final ModConfigSpec.BooleanValue MINIMAP_LABELS = CLIENT_BUILDER
            .comment("Shows compact labels for nearby minimap waypoints and checked mission markers.")
            .define("minimap.labels", false);

    public static final ModConfigSpec.BooleanValue MINIMAP_SHOW_COMPASS = CLIENT_BUILDER
            .comment("Shows the player heading indicator on the minimap.")
            .define("minimap.showCompass", true);

    public static final ModConfigSpec.BooleanValue MINIMAP_SHOW_SCALE = CLIENT_BUILDER
            .comment("Shows the minimap world-distance scale.")
            .define("minimap.showScale", true);

    public static final ModConfigSpec.BooleanValue MINIMAP_SHOW_COORDINATES = CLIENT_BUILDER
            .comment("Shows compact player coordinates below the minimap.")
            .define("minimap.showCoordinates", true);

    public static final ModConfigSpec.BooleanValue MINIMAP_SHOW_TILE_FRESHNESS = CLIENT_BUILDER
            .comment("Shows discovered terrain tile cache status below the minimap.")
            .define("minimap.showTileFreshness", true);

    public static final ModConfigSpec.BooleanValue MINIMAP_SHOW_FOV_WEDGE = CLIENT_BUILDER
            .comment("Shows the player field-of-view wedge on the minimap.")
            .define("minimap.showFovWedge", true);

    public static final ModConfigSpec.IntValue TERRAIN_CLIENT_CACHE_SIZE = CLIENT_BUILDER
            .comment("Maximum discovered terrain tiles retained in the in-memory client cache.")
            .defineInRange("terrain.cacheSize", 1024, 128, 32768);

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

    public enum LabelMode {
        OFF,
        SELECTED,
        NEARBY
    }

    public enum VisualDensity {
        LOW,
        MEDIUM,
        HIGH
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
                                        true, false, false),
                                EchoConfigEntry.intSpec("waypoint_sync", "Waypoint Sync",
                                        "Maximum personal/shared waypoints sent to one client sync packet.",
                                        EchoConfigSide.COMMON, WAYPOINT_SYNC_LIMIT, 16, 2048,
                                        true, false, false))),
                        new EchoConfigCategory("deathpoints", "Deathpoints", List.of(
                                EchoConfigEntry.booleanSpec("enabled", "Enabled",
                                        "Creates personal HoloMap deathpoints when players die.",
                                        EchoConfigSide.COMMON, DEATHPOINTS_ENABLED,
                                        true, false, false),
                                EchoConfigEntry.intSpec("max_per_player", "Max Per Player",
                                        "Maximum personal deathpoints retained per player.",
                                        EchoConfigSide.COMMON, DEATHPOINTS_MAX_PER_PLAYER, 0, 128,
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
                                        true, false, false),
                                EchoConfigEntry.doubleSpec("opacity", "Opacity",
                                        "Minimap panel opacity from transparent to solid.",
                                        EchoConfigSide.CLIENT, MINIMAP_OPACITY, 0.2D, 1.0D,
                                        true, false, false),
                                EchoConfigEntry.intSpec("marker_density", "Marker Density",
                                        "Maximum markers and waypoints drawn on the minimap.",
                                        EchoConfigSide.CLIENT, MINIMAP_MARKER_DENSITY, 0, 192,
                                        true, false, false),
                                EchoConfigEntry.doubleSpec("marker_scale", "Marker Scale",
                                        "Marker icon scale multiplier for HoloMap HUD rendering.",
                                        EchoConfigSide.CLIENT, MINIMAP_MARKER_SCALE, 0.7D, 1.6D,
                                        true, false, false),
                                EchoConfigEntry.doubleSpec("terrain_brightness", "Terrain Brightness",
                                        "Brightness multiplier applied to minimap terrain colors.",
                                        EchoConfigSide.CLIENT, MINIMAP_TERRAIN_BRIGHTNESS, 0.55D, 1.45D,
                                        true, false, false),
                                EchoConfigEntry.doubleSpec("terrain_contrast", "Terrain Contrast",
                                        "Contrast multiplier applied to minimap terrain colors.",
                                        EchoConfigSide.CLIENT, MINIMAP_TERRAIN_CONTRAST, 0.55D, 1.65D,
                                        true, false, false),
                                EchoConfigEntry.enumSpec("label_mode", "Label Mode",
                                        "Controls which minimap marker labels are drawn.",
                                        EchoConfigSide.CLIENT, MINIMAP_LABEL_MODE, LabelMode.class,
                                        true, false, false),
                                EchoConfigEntry.enumSpec("visual_density", "Visual Density",
                                        "Overall HoloMap visual density preference.",
                                        EchoConfigSide.CLIENT, MAP_VISUAL_DENSITY, VisualDensity.class,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("high_detail_terrain", "High Detail Terrain",
                                        "Allows per-block minimap terrain rendering at high zoom.",
                                        EchoConfigSide.CLIENT, MINIMAP_HIGH_DETAIL_TERRAIN,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_compass", "Compass",
                                        "Shows the player heading indicator on the minimap.",
                                        EchoConfigSide.CLIENT, MINIMAP_SHOW_COMPASS,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_scale", "Scale",
                                        "Shows the minimap world-distance scale.",
                                        EchoConfigSide.CLIENT, MINIMAP_SHOW_SCALE,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_coordinates", "Coordinates",
                                        "Shows compact player coordinates below the minimap.",
                                        EchoConfigSide.CLIENT, MINIMAP_SHOW_COORDINATES,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_tile_freshness", "Tile Freshness",
                                        "Shows discovered terrain tile cache status below the minimap.",
                                        EchoConfigSide.CLIENT, MINIMAP_SHOW_TILE_FRESHNESS,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_fov_wedge", "FOV Wedge",
                                        "Shows the player field-of-view wedge on the minimap.",
                                        EchoConfigSide.CLIENT, MINIMAP_SHOW_FOV_WEDGE,
                                        true, false, false)))))));
    }
}
