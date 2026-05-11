package com.knoxhack.echoholomap.test;

import com.google.gson.JsonElement;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoMapLayer;
import com.knoxhack.echocore.api.EchoMapMarker;
import com.knoxhack.echocore.api.IMapDataProvider;
import com.knoxhack.echocore.api.IMapLayer;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.EchoHoloMap;
import com.knoxhack.echoholomap.HoloMapIds;
import com.knoxhack.echoholomap.map.HoloMapLayers;
import com.knoxhack.echoholomap.map.HoloMapService;
import com.knoxhack.echoholomap.map.HoloMapTerrainPalette;
import com.knoxhack.echoholomap.map.HoloMapTerrainScanner;
import com.knoxhack.echoholomap.map.HoloMapTerrainTile;
import com.knoxhack.echoholomap.network.HoloMapSnapshotPacket;
import com.knoxhack.echoholomap.network.HoloMapTileBatchPacket;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import com.knoxhack.echoholomap.world.HoloMapSavedData;
import com.knoxhack.echoholomap.world.HoloMapTerrainSavedData;
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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_PALETTE_DETERMINISM =
            TEST_FUNCTIONS.register("terrain_palette_determinism", () -> ModGameTests::terrainPaletteDeterminism);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERRAIN_SCANNER_AND_REQUEST_CAPS =
            TEST_FUNCTIONS.register("terrain_scanner_and_request_caps", () -> ModGameTests::terrainScannerAndRequestCaps);

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
        register(event, environment, "terrain_palette_determinism", TERRAIN_PALETTE_DETERMINISM.getId());
        register(event, environment, "terrain_scanner_and_request_caps", TERRAIN_SCANNER_AND_REQUEST_CAPS.getId());
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
        helper.succeed();
    }

    private static void terrainPaletteDeterminism(GameTestHelper helper) {
        int plainsA = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "plains", 64, false);
        int plainsB = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "plains", 64, false);
        int water = HoloMapTerrainPalette.colorForBiome("minecraft:overworld", "river", 64, true);
        int end = HoloMapTerrainPalette.colorForBiome("minecraft:the_end", "end_highlands", 64, false);
        helper.assertTrue(plainsA == plainsB, "Terrain palette should be deterministic for identical input");
        helper.assertTrue(plainsA != water, "Terrain palette should distinguish land from water");
        helper.assertTrue(end != plainsA, "Terrain palette should distinguish End terrain from overworld plains");
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
