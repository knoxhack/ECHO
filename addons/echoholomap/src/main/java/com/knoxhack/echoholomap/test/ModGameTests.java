package com.knoxhack.echoholomap.test;

import com.google.gson.JsonElement;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoMapLayer;
import com.knoxhack.echocore.api.EchoMapMarker;
import com.knoxhack.echocore.api.IMapDataProvider;
import com.knoxhack.echocore.api.IMapLayer;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echocore.api.mission.InMemoryMissionRegistry;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.EchoHoloMap;
import com.knoxhack.echoholomap.HoloMapIds;
import com.knoxhack.echoholomap.client.HoloMapVisualStyle;
import com.knoxhack.echoholomap.integration.HoloMapMissionCoreIntegration;
import com.knoxhack.echoholomap.map.HoloMapLayers;
import com.knoxhack.echoholomap.map.HoloMapService;
import com.knoxhack.echoholomap.map.HoloMapTerrainPalette;
import com.knoxhack.echoholomap.map.HoloMapTerrainScanner;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.network.HoloMapTileBatchPacket;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import com.knoxhack.echoholomap.network.HoloMapWaypointClientState;
import com.knoxhack.echoholomap.network.HoloMapWaypointSyncPacket;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint;
import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint.Scope;
import com.knoxhack.echoholomap.world.HoloMapSavedData;
import com.knoxhack.echoholomap.world.HoloMapTerrainSavedData;
import com.knoxhack.echoholomap.world.HoloMapWaypointSavedData;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoHoloMap.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BUILTIN_PROVIDER_REGISTRATION =
            TEST_FUNCTIONS.register("builtin_provider_registration", () -> ModGameTests::builtinProviderRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVICE_PROVIDER_ISOLATION =
            TEST_FUNCTIONS.register("service_provider_isolation", () -> ModGameTests::serviceProviderIsolation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SNAPSHOT_FILTERING_AND_CAP =
            TEST_FUNCTIONS.register("snapshot_filtering_and_cap", () -> ModGameTests::snapshotFilteringAndCap);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEBUG_MARKER_SAVED_DATA_CODEC =
            TEST_FUNCTIONS.register("debug_marker_saved_data_codec", () -> ModGameTests::debugMarkerSavedDataCodec);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_SAVED_DATA_CODEC =
            TEST_FUNCTIONS.register("terrain_saved_data_codec", () -> ModGameTests::terrainSavedDataCodec);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_V3_TILE_METADATA =
            TEST_FUNCTIONS.register("terrain_v3_tile_metadata", () -> ModGameTests::terrainV3TileMetadata);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_PALETTE_DETERMINISM =
            TEST_FUNCTIONS.register("terrain_palette_determinism", () -> ModGameTests::terrainPaletteDeterminism);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_SCANNER_AND_REQUEST_CAPS =
            TEST_FUNCTIONS.register("terrain_scanner_and_request_caps", () -> ModGameTests::terrainScannerAndRequestCaps);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WAYPOINT_SAVED_DATA_CODEC =
            TEST_FUNCTIONS.register("waypoint_saved_data_codec", () -> ModGameTests::waypointSavedDataCodec);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WAYPOINT_MUTATION_RULES =
            TEST_FUNCTIONS.register("waypoint_mutation_rules", () -> ModGameTests::waypointMutationRules);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WAYPOINT_CLIENT_MERGE =
            TEST_FUNCTIONS.register("waypoint_client_merge", () -> ModGameTests::waypointClientMerge);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_CORE_CONTENT =
            TEST_FUNCTIONS.register("missioncore_content_registration", () -> ModGameTests::missionCoreContentRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> THEME_CORE_STYLE_FALLBACK =
            TEST_FUNCTIONS.register("theme_core_style_fallback", () -> ModGameTests::themeCoreStyleFallback);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("holomap_service"));
        register(event, environment, "builtin_provider_registration", BUILTIN_PROVIDER_REGISTRATION.getId());
        register(event, environment, "service_provider_isolation", SERVICE_PROVIDER_ISOLATION.getId());
        register(event, environment, "snapshot_filtering_and_cap", SNAPSHOT_FILTERING_AND_CAP.getId());
        register(event, environment, "debug_marker_saved_data_codec", DEBUG_MARKER_SAVED_DATA_CODEC.getId());
        register(event, environment, "terrain_saved_data_codec", TERRAIN_SAVED_DATA_CODEC.getId());
        register(event, environment, "terrain_v3_tile_metadata", TERRAIN_V3_TILE_METADATA.getId());
        register(event, environment, "terrain_palette_determinism", TERRAIN_PALETTE_DETERMINISM.getId());
        register(event, environment, "terrain_scanner_and_request_caps", TERRAIN_SCANNER_AND_REQUEST_CAPS.getId());
        register(event, environment, "waypoint_saved_data_codec", WAYPOINT_SAVED_DATA_CODEC.getId());
        register(event, environment, "waypoint_mutation_rules", WAYPOINT_MUTATION_RULES.getId());
        register(event, environment, "waypoint_client_merge", WAYPOINT_CLIENT_MERGE.getId());
        register(event, environment, "missioncore_content_registration", MISSION_CORE_CONTENT.getId());
        register(event, environment, "theme_core_style_fallback", THEME_CORE_STYLE_FALLBACK.getId());
    }

    private static void builtinProviderRegistration(GameTestHelper helper) {
        HoloMapService.INSTANCE.clearForTests();
        HoloMapService.INSTANCE.registerBuiltins();
        HoloMapService.INSTANCE.registerBuiltins();
        helper.assertTrue(HoloMapService.INSTANCE.providerCount() == 1,
                "Built-in HoloMap provider should register once");
        List<Identifier> layerIds = HoloMapService.INSTANCE.layers(null).stream().map(IMapLayer::id).toList();
        helper.assertTrue(layerIds.containsAll(requiredLayerIds()),
                "Built-in HoloMap provider should expose every required layer");
        resetHoloMapService();
        helper.succeed();
    }

    private static void themeCoreStyleFallback(GameTestHelper helper) {
        HoloMapSnapshotPacket.MarkerData route = HoloMapSnapshotPacket.MarkerData.from(marker(
                id("theme_route"), HoloMapIds.ROUTES, HoloMapIds.ROUTE_SOURCE,
                IMapMarker.MarkerKind.ROUTE, IMapMarker.MarkerState.DISCOVERED, 0.0D));
        HoloMapSnapshotPacket.MarkerData hazard = HoloMapSnapshotPacket.MarkerData.from(marker(
                id("theme_hazard"), HoloMapIds.HAZARDS, HoloMapIds.HAZARD_SOURCE,
                IMapMarker.MarkerKind.HAZARD, IMapMarker.MarkerState.DISCOVERED, 1.0D));
        HoloMapSnapshotPacket.MarkerData nexus = HoloMapSnapshotPacket.MarkerData.from(marker(
                id("theme_nexus"), HoloMapIds.NEXUS_ANOMALY, HoloMapIds.CORE_SOURCE,
                IMapMarker.MarkerKind.NEXUS_ANOMALY, IMapMarker.MarkerState.DISCOVERED, 2.0D));
        helper.assertTrue(HoloMapVisualStyle.markerColor(null, route) == HoloMapVisualStyle.SUCCESS,
                "HoloMap route marker fallback should remain stable without ThemeCore.");
        helper.assertTrue(HoloMapVisualStyle.markerColor(null, hazard) == HoloMapVisualStyle.DANGER,
                "HoloMap hazard marker fallback should remain stable without ThemeCore.");
        helper.assertTrue(HoloMapVisualStyle.markerColor(null, nexus) == 0xFFFF8FEA,
                "HoloMap Nexus marker fallback should remain stable without ThemeCore.");
        helper.succeed();
    }

    private static void serviceProviderIsolation(GameTestHelper helper) {
        HoloMapService.INSTANCE.clearForTests();
        Identifier layerId = id("layer/test_isolation");
        Identifier goodProvider = id("provider/good");
        Identifier failingProvider = id("provider/failing");
        HoloMapService.INSTANCE.registerProvider(new IMapDataProvider() {
            @Override
            public Identifier providerId() {
                return goodProvider;
            }

            @Override
            public List<IMapLayer> layers(Player player) {
                return List.of(new EchoMapLayer(layerId, "Isolation", 500, 0xFF66E8FF, true));
            }

            @Override
            public List<IMapMarker> markers(Player player) {
                return List.of(marker(id("marker/good"), layerId, goodProvider,
                        IMapMarker.MarkerKind.GENERIC, IMapMarker.MarkerState.DISCOVERED, 12.0D));
            }

            @Override
            public boolean refresh(ServerPlayer player, String reason) {
                return true;
            }
        });
        HoloMapService.INSTANCE.registerProvider(new IMapDataProvider() {
            @Override
            public Identifier providerId() {
                return failingProvider;
            }

            @Override
            public List<IMapLayer> layers(Player player) {
                throw new IllegalStateException("intentional layer failure");
            }

            @Override
            public List<IMapMarker> markers(Player player) {
                throw new IllegalStateException("intentional marker failure");
            }

            @Override
            public boolean refresh(ServerPlayer player, String reason) {
                throw new IllegalStateException("intentional refresh failure");
            }
        });
        helper.assertTrue(HoloMapService.INSTANCE.providerCount() == 2,
                "HoloMap should retain healthy and failing providers for isolation");
        helper.assertTrue(HoloMapService.INSTANCE.layers(null).stream().anyMatch(layer -> layer.id().equals(layerId)),
                "Healthy provider layer should survive a failing provider");
        helper.assertTrue(HoloMapService.INSTANCE.markers(null).size() == 1,
                "Healthy provider marker should survive a failing provider");
        helper.assertTrue(HoloMapService.INSTANCE.refresh(helper.makeMockServerPlayerInLevel(), "gametest"),
                "Healthy provider refresh should still report success");
        resetHoloMapService();
        helper.succeed();
    }

    private static void snapshotFilteringAndCap(GameTestHelper helper) {
        HoloMapService.INSTANCE.clearForTests();
        EchoCoreServices.registerMapMarkerService(HoloMapService.INSTANCE);
        Identifier layerId = id("layer/snapshot_cap");
        Identifier providerId = id("provider/snapshot_cap");
        int cap = configuredMarkerCap();
        int visibleMarkerCount = cap + 12;
        HoloMapService.INSTANCE.registerProvider(new IMapDataProvider() {
            @Override
            public Identifier providerId() {
                return providerId;
            }

            @Override
            public List<IMapLayer> layers(Player player) {
                return List.of(new EchoMapLayer(layerId, "Snapshot Cap", 501, 0xFF92F7A6, true));
            }

            @Override
            public List<IMapMarker> markers(Player player) {
                List<IMapMarker> markers = new ArrayList<>();
                markers.add(marker(id("marker/hidden"), layerId, providerId,
                        IMapMarker.MarkerKind.GENERIC, IMapMarker.MarkerState.HIDDEN, -999.0D));
                for (int i = 0; i < visibleMarkerCount; i++) {
                    IMapMarker.MarkerState state = i == 0
                            ? IMapMarker.MarkerState.LOCKED
                            : IMapMarker.MarkerState.DISCOVERED;
                    markers.add(marker(id("marker/visible_" + i), layerId, providerId,
                            IMapMarker.MarkerKind.GENERIC, state, i));
                }
                return markers;
            }
        });

        HoloMapSnapshotPacket snapshot = HoloMapSnapshotPacket.from(helper.makeMockServerPlayerInLevel());
        helper.assertTrue(snapshot.markers().size() == cap,
                "Snapshot should cap visible markers at configured max");
        helper.assertTrue(snapshot.markers().stream().noneMatch(marker -> marker.state() == IMapMarker.MarkerState.HIDDEN),
                "Hidden markers should be omitted from normal snapshots");
        helper.assertTrue(snapshot.markers().stream().anyMatch(marker -> marker.state() == IMapMarker.MarkerState.LOCKED),
                "Locked marker state should be preserved in snapshots");
        resetHoloMapService();
        helper.succeed();
    }

    private static void debugMarkerSavedDataCodec(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HoloMapSavedData data = new HoloMapSavedData();
        EchoMapMarker marker = data.addDebugMarker(player, HoloMapIds.HAZARDS);
        helper.assertTrue(marker != null, "Debug marker should be created for server players");
        helper.assertTrue(data.debugMarkers(player.level()).size() == 1,
                "Debug marker should be queryable for the player dimension");

        JsonElement encoded = HoloMapSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        HoloMapSavedData decoded = HoloMapSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        List<EchoMapMarker> decodedMarkers = decoded.debugMarkers(player.level());
        helper.assertTrue(decodedMarkers.size() == 1, "Debug marker should survive codec save/load");
        EchoMapMarker decodedMarker = decodedMarkers.getFirst();
        helper.assertTrue(HoloMapIds.HAZARDS.equals(decodedMarker.layerId()),
                "Debug marker layer should survive codec save/load");
        helper.assertTrue(HoloMapIds.DEBUG_SOURCE.equals(decodedMarker.sourceId()),
                "Debug marker source should remain the debug source");
        helper.assertTrue(decodedMarker.precise(), "Debug marker should preserve precise coordinates");
        resetHoloMapService();
        helper.succeed();
    }

    private static void terrainSavedDataCodec(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HoloMapTerrainSavedData data = new HoloMapTerrainSavedData();
        int[] pixels = filledPixels(0xFF356E4A);
        data.putForTests(player.getUUID().toString(), Level.OVERWORLD.identifier().toString(), 4, -7, 99L, pixels);
        JsonElement encoded = HoloMapTerrainSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        HoloMapTerrainSavedData decoded = HoloMapTerrainSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        List<HoloMapTerrainTile> tiles = decoded.tiles(player.getUUID(), Level.OVERWORLD, 4, -7, 1, 8);
        helper.assertTrue(tiles.size() == 1, "Terrain tile should survive codec save/load");
        HoloMapTerrainTile tile = tiles.getFirst();
        helper.assertTrue(tile.chunkX() == 4 && tile.chunkZ() == -7,
                "Terrain tile chunk coordinates should survive codec save/load");
        helper.assertTrue(tile.pixel(0, 0) == 0xFF356E4A,
                "Terrain tile pixels should survive codec save/load");
        helper.assertTrue(tile.version() == HoloMapTerrainTile.LEGACY_VERSION,
                "Legacy terrain tiles without explicit metadata should remain readable");
        helper.assertTrue(tile.detailMode() == HoloMapTerrainTile.DetailMode.BIOME_FALLBACK,
                "Legacy terrain tiles should default to biome fallback detail");
        helper.succeed();
    }

    private static void terrainV3TileMetadata(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HoloMapTerrainSavedData data = new HoloMapTerrainSavedData();
        int[] pixels = filledPixels(0xFF6D7C70);
        data.putForTests(player.getUUID().toString(), Level.OVERWORLD.identifier().toString(), 2, 3, 144L,
                HoloMapTerrainTile.CURRENT_VERSION, HoloMapTerrainTile.DetailMode.SURFACE_SHADED, pixels);

        JsonElement encoded = HoloMapTerrainSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        HoloMapTerrainSavedData decoded = HoloMapTerrainSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        List<HoloMapTerrainTile> tiles = decoded.tiles(player.getUUID(), Level.OVERWORLD, 2, 3, 1, 8);
        helper.assertTrue(tiles.size() == 1, "V3 terrain tile should survive codec save/load");
        HoloMapTerrainTile tile = tiles.getFirst();
        helper.assertTrue(tile.version() == HoloMapTerrainTile.CURRENT_VERSION,
                "V3 terrain tile version should survive codec save/load");
        helper.assertTrue(tile.detailMode() == HoloMapTerrainTile.DetailMode.SURFACE_SHADED,
                "V3 terrain tile detail mode should survive codec save/load");
        HoloMapTerrainSavedData.TerrainStats stats = decoded.stats(player.getUUID(), Level.OVERWORLD);
        helper.assertTrue(stats.surfaceShaded() == 1 && stats.legacy() == 0,
                "V3 terrain stats should count shaded non-legacy tiles");
        helper.assertFalse(decoded.needsSample(player.getUUID(), Level.OVERWORLD, 2, 3, 145L, 2400L),
                "Fresh V3 terrain tiles should not need immediate resampling");

        HoloMapTerrainSavedData legacy = new HoloMapTerrainSavedData();
        legacy.putForTests(player.getUUID().toString(), Level.OVERWORLD.identifier().toString(), 2, 3, 144L, pixels);
        helper.assertTrue(legacy.needsSample(player.getUUID(), Level.OVERWORLD, 2, 3, 145L, 2400L),
                "Legacy terrain tiles should be eligible for lazy V3 resampling");
        helper.succeed();
    }

    private static void terrainPaletteDeterminism(GameTestHelper helper) {
        int plainsA = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "plains", 64, false);
        int plainsB = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "plains", 64, false);
        int water = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "river", 64, true);
        int end = HoloMapTerrainPalette.colorForBiome("minecraft:the_end", "end_highlands", 64, false);
        int sand = HoloMapTerrainPalette.colorForDescriptor("minecraft:overworld", "desert", 64,
                "sand", false, false);
        int stone = HoloMapTerrainPalette.colorForDescriptor("minecraft:overworld", "stony_peaks", 90,
                "stone", false, false);
        int snow = HoloMapTerrainPalette.colorForDescriptor("minecraft:overworld", "snowy_plains", 74,
                "snow", false, false);
        int lava = HoloMapTerrainPalette.colorForDescriptor("minecraft:the_nether", "nether_wastes", 32,
                "lava", false, false);
        helper.assertTrue(plainsA == plainsB, "Terrain palette should be deterministic for identical input");
        helper.assertTrue(plainsA != water, "Terrain palette should distinguish land from water");
        helper.assertTrue(end != plainsA, "Terrain palette should distinguish End terrain from overworld plains");
        helper.assertTrue(sand != stone, "Surface palette should distinguish sand from stone");
        helper.assertTrue(snow != sand, "Surface palette should highlight snow separately from sand");
        helper.assertTrue(lava != stone, "Surface palette should highlight lava separately from stone");
        helper.succeed();
    }

    private static void terrainScannerAndRequestCaps(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ServerLevel level = (ServerLevel) player.level();
        HoloMapTerrainSavedData data = HoloMapTerrainSavedData.get(level);
        data.clear(player.getUUID());

        int sampled = HoloMapTerrainScanner.scanAround(player, 2, 1);
        helper.assertTrue(sampled <= 1, "Terrain scanner should respect the per-pass sample cap");
        helper.assertTrue(data.discoverableTileCount(player.getUUID(), level.dimension()) <= 1,
                "Scanner should not save more terrain tiles than the per-pass cap");

        int centerChunkX = Math.floorDiv(player.blockPosition().getX(), 16);
        int centerChunkZ = Math.floorDiv(player.blockPosition().getZ(), 16);
        level.getChunk(centerChunkX, centerChunkZ);
        data.clear(player.getUUID());
        data.putForTests(player.getUUID().toString(), level.dimension().identifier().toString(),
                centerChunkX, centerChunkZ, level.getGameTime(),
                HoloMapTerrainTile.CURRENT_VERSION, HoloMapTerrainTile.DetailMode.SURFACE_SHADED,
                filledPixels(0xFF51615F));
        HoloMapTerrainScanner.clearForTests();
        int skipped = HoloMapTerrainScanner.scanAround(player, 0, 1, false);
        helper.assertTrue(skipped == 0, "Scanner should skip fresh current-version terrain tiles");
        int forced = HoloMapTerrainScanner.scanAround(player, 0, 1, true);
        helper.assertTrue(forced == 1, "Forced terrain resample should refresh a loaded current tile");

        int beforeRemote = data.discoverableTileCount(player.getUUID(), level.dimension());
        HoloMapTileBatchPacket remote = HoloMapTileBatchPacket.from(player, new HoloMapTileRequestPacket(
                level.dimension().identifier().toString(), centerChunkX + 1000, centerChunkZ + 1000, 4));
        int afterRemote = data.discoverableTileCount(player.getUUID(), level.dimension());
        helper.assertTrue(beforeRemote == afterRemote,
                "Tile requests should not reveal or save undiscovered remote terrain");
        helper.assertTrue(remote.tiles().isEmpty(), "Remote undiscovered tile requests should return no tiles");

        data.clear(player.getUUID());
        int maxBatch = HoloMapTileBatchPacket.maxBatchSize();
        for (int i = 0; i < maxBatch + 24; i++) {
            data.putForTests(player.getUUID().toString(), level.dimension().identifier().toString(),
                    centerChunkX + i % 24, centerChunkZ + i / 24, i, filledPixels(0xFF245C66 + i));
        }
        HoloMapTileBatchPacket batch = HoloMapTileBatchPacket.from(player, new HoloMapTileRequestPacket(
                level.dimension().identifier().toString(), centerChunkX, centerChunkZ, 24));
        helper.assertTrue(batch.tiles().size() <= maxBatch,
                "Tile batch response should respect configured packet cap");
        data.clear(player.getUUID());
        HoloMapTerrainScanner.clearForTests();
        helper.succeed();
    }

    private static void waypointSavedDataCodec(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HoloMapWaypointSavedData data = new HoloMapWaypointSavedData();
        HoloMapWaypoint waypoint = HoloMapWaypoint.create(Scope.PERSONAL, player.getUUID(),
                Level.OVERWORLD.identifier().toString(), 32.0D, 70.0D, -48.0D,
                "Codec Relay", 0xFF92F7A6, 12L);
        helper.assertTrue(data.upsert(player, waypoint, false), "Personal waypoint should be accepted");
        JsonElement encoded = HoloMapWaypointSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        HoloMapWaypointSavedData decoded = HoloMapWaypointSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        List<HoloMapWaypoint> waypoints = decoded.waypointsFor(player.getUUID(), 16);
        helper.assertTrue(waypoints.size() == 1, "Waypoint should survive codec save/load");
        HoloMapWaypoint decodedWaypoint = waypoints.getFirst();
        helper.assertTrue(decodedWaypoint.scope() == Scope.PERSONAL, "Waypoint scope should survive codec save/load");
        helper.assertTrue(decodedWaypoint.owner().equals(player.getUUID()), "Waypoint owner should survive codec save/load");
        helper.assertTrue(decodedWaypoint.x() == 32.0D && decodedWaypoint.z() == -48.0D,
                "Waypoint coordinates should survive codec save/load");
        helper.succeed();
    }

    private static void waypointMutationRules(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        HoloMapWaypointSavedData data = new HoloMapWaypointSavedData();
        HoloMapWaypoint personal = HoloMapWaypoint.create(Scope.PERSONAL, player.getUUID(),
                Level.OVERWORLD.identifier().toString(), 8.0D, 64.0D, 8.0D,
                "Personal", 0xFF92F7A6, 1L);
        HoloMapWaypoint shared = HoloMapWaypoint.create(Scope.SHARED, player.getUUID(),
                Level.OVERWORLD.identifier().toString(), 16.0D, 64.0D, 16.0D,
                "Shared", 0xFFFFDA73, 1L);
        helper.assertTrue(data.upsert(player, personal, false), "Players should be able to upsert personal waypoints");
        helper.assertFalse(data.upsert(player, shared, false), "Shared waypoint upsert should require permission");
        helper.assertTrue(data.upsert(player, shared, true), "Shared waypoint upsert should work with permission");
        helper.assertFalse(data.delete(player, shared.id(), false), "Shared waypoint delete should require permission");
        helper.assertTrue(data.delete(player, shared.id(), true), "Shared waypoint delete should work with permission");
        helper.assertTrue(data.delete(player, personal.id(), false), "Personal waypoint delete should work for owner");
        helper.succeed();
    }

    private static void waypointClientMerge(GameTestHelper helper) {
        HoloMapWaypointClientState.clearForTests();
        HoloMapWaypoint local = HoloMapWaypoint.create(Scope.LOCAL, HoloMapWaypoint.NO_OWNER,
                Level.OVERWORLD.identifier().toString(), 1.0D, 64.0D, 1.0D,
                "Local", 0xFF38DFF4, 1L);
        HoloMapWaypoint personal = HoloMapWaypoint.create(Scope.PERSONAL, java.util.UUID.randomUUID(),
                Level.OVERWORLD.identifier().toString(), 2.0D, 64.0D, 2.0D,
                "Personal", 0xFF92F7A6, 2L);
        HoloMapWaypointClientState.setLocalWaypoints(List.of(local));
        HoloMapWaypointClientState.apply(new HoloMapWaypointSyncPacket(List.of(personal), 42L));
        List<HoloMapWaypoint> merged = HoloMapWaypointClientState.waypoints();
        helper.assertTrue(merged.size() == 2, "Client waypoint cache should merge local and server waypoints");
        helper.assertTrue(merged.getFirst().scope() == Scope.LOCAL,
                "Local waypoints should sort before synced server waypoints");
        helper.assertTrue(HoloMapWaypointClientState.lastSyncGameTime() == 42L,
                "Waypoint sync packet should update client sync time");
        HoloMapWaypointClientState.clearForTests();
        helper.succeed();
    }

    private static void register(
            RegisterGameTestsEvent event,
            Holder<TestEnvironmentDefinition<?>> environment,
            String testName,
            Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                200,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                2);
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static void resetHoloMapService() {
        HoloMapService.INSTANCE.clearForTests();
        HoloMapService.INSTANCE.registerBuiltins();
        EchoCoreServices.registerMapMarkerService(HoloMapService.INSTANCE);
    }

    private static void missionCoreContentRegistration(GameTestHelper helper) {
        InMemoryMissionRegistry registry = new InMemoryMissionRegistry();
        HoloMapMissionCoreIntegration.registerContent(registry);
        helper.assertTrue(registry.chapter(id("holomap")).isPresent(), "HoloMap MissionCore chapter should be owned by HoloMap.");
        assertMission(helper, registry, "discover_terrain", "terrain", MissionObjectiveType.ENTER_REGION);
        assertMission(helper, registry, "create_waypoint", "waypoint", MissionObjectiveType.CUSTOM);
        assertMission(helper, registry, "reveal_marker", "marker", MissionObjectiveType.DISCOVER_STRUCTURE);
        assertMission(helper, registry, "sync_route", "sync", MissionObjectiveType.ESTABLISH_ROUTE);
        helper.succeed();
    }

    private static void assertMission(
            GameTestHelper helper,
            InMemoryMissionRegistry registry,
            String missionPath,
            String objectiveKey,
            MissionObjectiveType type) {
        Identifier missionId = id(missionPath);
        MissionDefinition mission = registry.missionDefinition(missionId)
                .orElseThrow(() -> new AssertionError("Missing MissionCore mission: " + missionId));
        helper.assertTrue(mission.kind() == MissionKind.SIDE_OP, "HoloMap MissionCore missions should be side ops.");
        helper.assertTrue(!mission.rewards().isEmpty(), "HoloMap MissionCore mission should have a claimable reward: " + missionId);
        helper.assertTrue(mission.objectives().size() == 1, "HoloMap MissionCore mission should have one direct objective: " + missionId);
        helper.assertTrue(mission.objectives().getFirst().type() == type, "HoloMap objective type should stay stable: " + missionId);
        String target = mission.objectives().getFirst().criteria().get("target");
        helper.assertTrue(MissionHookTargets.objectiveTarget(EchoHoloMap.MODID, missionId, objectiveKey).toString().equals(target),
                "HoloMap MissionCore objective target should use MissionHookTargets: " + missionId);
    }

    private static List<Identifier> requiredLayerIds() {
        return HoloMapLayers.REQUIRED.stream().map(IMapLayer::id).toList();
    }

    private static int configuredMarkerCap() {
        try {
            return Math.max(32, Math.min(2048, Config.MAX_MARKERS.get()));
        } catch (RuntimeException exception) {
            return 384;
        }
    }

    private static EchoMapMarker marker(Identifier id, Identifier layerId, Identifier sourceId,
            IMapMarker.MarkerKind kind, IMapMarker.MarkerState state, double x) {
        return new EchoMapMarker(
                id,
                layerId,
                sourceId,
                kind,
                state,
                state == IMapMarker.MarkerState.HIDDEN ? "Hidden marker" : "Marker " + id.getPath(),
                "GameTest marker for HoloMap service validation.",
                Level.OVERWORLD,
                x,
                64.0D,
                x,
                16.0F,
                null,
                null,
                -1,
                true);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, path);
    }

    private static int[] filledPixels(int color) {
        int[] pixels = new int[HoloMapTerrainTile.PIXELS];
        java.util.Arrays.fill(pixels, color);
        return pixels;
    }
}
