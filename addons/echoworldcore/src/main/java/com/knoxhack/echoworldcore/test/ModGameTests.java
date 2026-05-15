package com.knoxhack.echoworldcore.test;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echocore.api.EchoWorldRuntimeBus;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.NoOpWorldService;
import com.knoxhack.echocore.api.WorldDiscoverySource;
import com.knoxhack.echocore.api.WorldHazardDefinition;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldMarkerType;
import com.knoxhack.echocore.api.WorldRegionDefinition;
import com.knoxhack.echocore.api.WorldRegionInstance;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.content.WorldCoreJsonReloadListener;
import com.knoxhack.echoworldcore.registry.WorldCoreBuiltins;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import com.knoxhack.echoworldcore.world.WorldRegionSavedData;
import com.mojang.serialization.JsonOps;
import java.util.function.Consumer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoWorldCore.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_REGISTRY =
            TEST_FUNCTIONS.register("worldcore_registry", () -> ModGameTests::worldcoreRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_NOOP =
            TEST_FUNCTIONS.register("worldcore_noop", () -> ModGameTests::worldcoreNoop);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_JSON =
            TEST_FUNCTIONS.register("worldcore_json_definitions", () -> ModGameTests::worldcoreJsonDefinitions);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_SAVED_DATA =
            TEST_FUNCTIONS.register("worldcore_saved_data", () -> ModGameTests::worldcoreSavedData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_RUNTIME_BUS =
            TEST_FUNCTIONS.register("worldcore_runtime_bus", () -> ModGameTests::worldcoreRuntimeBus);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_RELEASE_GUARDS =
            TEST_FUNCTIONS.register("worldcore_release_guards", () -> ModGameTests::worldcoreReleaseGuards);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("worldcore"));
        register(event, environment, "worldcore_registry", WORLDCORE_REGISTRY.getId());
        register(event, environment, "worldcore_noop", WORLDCORE_NOOP.getId());
        register(event, environment, "worldcore_json_definitions", WORLDCORE_JSON.getId());
        register(event, environment, "worldcore_saved_data", WORLDCORE_SAVED_DATA.getId());
        register(event, environment, "worldcore_runtime_bus", WORLDCORE_RUNTIME_BUS.getId());
        register(event, environment, "worldcore_release_guards", WORLDCORE_RELEASE_GUARDS.getId());
    }

    private static void worldcoreRegistry(GameTestHelper helper) {
        WorldRegionService service = new WorldRegionService();
        WorldCoreBuiltins.register(service);
        helper.assertTrue(service.regionDefinition(WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD).isPresent(),
                "WorldCore should register cross-chapter regions");
        helper.assertTrue(service.regionDefinition(WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD).orElseThrow().type() == WorldRegionType.ORBITAL_DEBRIS_FIELD,
                "Orbital debris field should keep the orbital-debris-field region type");
        helper.assertTrue(service.hazardDefinitions().size() >= 4,
                "WorldCore should register shared hazard definitions");
        helper.assertTrue(service.validateMarkers(null).isEmpty(),
                "Built-in region/hazard references should validate");
        helper.succeed();
    }

    private static void worldcoreJsonDefinitions(GameTestHelper helper) {
        WorldHazardDefinition hazard = WorldCoreJsonReloadListener.parseHazardForTests(
                id("hazard/test"), JsonParser.parseString("""
                        {"displayName":"Test Hazard","summary":"Test summary","defaultSeverity":42,"ticking":true}
                        """).getAsJsonObject());
        helper.assertTrue(hazard.defaultSeverity() == 42, "JSON hazard parser should preserve severity");
        helper.assertTrue(hazard.ticking(), "JSON hazard parser should preserve ticking flag");
        WorldRegionDefinition region = WorldCoreJsonReloadListener.parseRegionForTests(
                id("test_region"), JsonParser.parseString("""
                        {
                          "type":"crash_zone",
                          "displayName":"Test Region",
                          "summary":"A test region.",
                          "biomeIds":["minecraft:plains"],
                          "hazardIds":["echoworldcore:hazard/test"],
                          "radius":64,
                          "sortOrder":7
                        }
                        """).getAsJsonObject());
        helper.assertTrue(region.type() == WorldRegionType.CRASH_ZONE, "JSON region parser should resolve region type");
        helper.assertTrue(region.biomeIds().contains(Identifier.withDefaultNamespace("plains")),
                "JSON region parser should preserve biome ids");
        helper.assertTrue(region.radius() == 64, "JSON region parser should preserve valid radius");
        try {
            WorldCoreJsonReloadListener.parseHazardForTests(id("hazard/bad"),
                    JsonParser.parseString("{\"defaultSeverity\":101}").getAsJsonObject());
            helper.fail("Invalid hazard severity should fail parsing");
        } catch (JsonParseException expected) {
        }
        try {
            WorldCoreJsonReloadListener.parseRegionForTests(id("bad_radius"),
                    JsonParser.parseString("{\"type\":\"crash_zone\",\"radius\":8}").getAsJsonObject());
            helper.fail("Invalid region radius should fail parsing");
        } catch (JsonParseException expected) {
        }
        try {
            WorldCoreJsonReloadListener.parseRegionForTests(id("bad_type"),
                    JsonParser.parseString("{\"type\":\"unknown_world_type\",\"radius\":64}").getAsJsonObject());
            helper.fail("Invalid region type should fail parsing");
        } catch (JsonParseException expected) {
        }
        helper.succeed();
    }

    private static void worldcoreSavedData(GameTestHelper helper) {
        WorldRegionSavedData data = new WorldRegionSavedData();
        Identifier regionId = WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD;
        Identifier markerId = id("marker/test");
        UUID playerId = UUID.randomUUID();
        data.saveMarker(new WorldMarker(markerId, regionId, WorldMarkerType.CRASH_SITE,
                "Test Marker", "Round trip marker.", Level.OVERWORLD, BlockPos.ZERO, 32, true, 12L));
        data.recordDiscovery(playerId, regionId, WorldDiscoverySource.SCAN, new BlockPos(1, 2, 3), 99L);
        helper.assertTrue(data.markers().size() == 1, "SavedData should retain markers");
        helper.assertTrue(data.discoveries(playerId).contains(regionId), "SavedData should retain structured discoveries");
        var encoded = WorldRegionSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
        WorldRegionSavedData decoded = WorldRegionSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();
        helper.assertTrue(decoded.markers().size() == 1, "SavedData codec should round-trip markers");
        helper.assertTrue(decoded.discoveries(playerId).contains(regionId), "SavedData codec should round-trip discoveries");
        helper.succeed();
    }

    private static void worldcoreRuntimeBus(GameTestHelper helper) {
        EchoWorldRuntimeBus.clearForTests();
        AtomicInteger entered = new AtomicInteger();
        AtomicInteger discovered = new AtomicInteger();
        AtomicInteger scanned = new AtomicInteger();
        AtomicInteger marker = new AtomicInteger();
        AtomicInteger hazard = new AtomicInteger();
        EchoWorldRuntimeBus.onRegionEntered(event -> entered.incrementAndGet());
        EchoWorldRuntimeBus.onRegionDiscovered(event -> discovered.incrementAndGet());
        EchoWorldRuntimeBus.onRegionScanned(event -> scanned.incrementAndGet());
        EchoWorldRuntimeBus.onMarkerRevealed(event -> marker.incrementAndGet());
        EchoWorldRuntimeBus.onHazardChanged(event -> hazard.incrementAndGet());
        WorldRegionInstance region = new WorldRegionInstance(id("instance/test"), WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD,
                WorldRegionType.ORBITAL_DEBRIS_FIELD, "Test Region", Level.OVERWORLD, BlockPos.ZERO, 32, java.util.List.of(), true);
        WorldMarker worldMarker = new WorldMarker(id("marker/runtime"), WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD,
                WorldMarkerType.REGION_CENTER, "Runtime Marker", "", Level.OVERWORLD, BlockPos.ZERO, 32, true, 1L);
        EchoWorldRuntimeBus.fireRegionEntered(new EchoWorldRuntimeBus.RegionEntered(null, region));
        EchoWorldRuntimeBus.fireRegionDiscovered(new EchoWorldRuntimeBus.RegionDiscovered(
                null, region, WorldDiscoverySource.DEBUG, true));
        EchoWorldRuntimeBus.fireRegionScanned(new EchoWorldRuntimeBus.RegionScanned(null, region, worldMarker));
        EchoWorldRuntimeBus.fireMarkerRevealed(new EchoWorldRuntimeBus.MarkerRevealed(null, worldMarker));
        EchoWorldRuntimeBus.fireHazardChanged(new EchoWorldRuntimeBus.HazardChanged(
                null, com.knoxhack.echocore.api.WorldHazardSnapshot.nominal(),
                new com.knoxhack.echocore.api.WorldHazardSnapshot(java.util.List.of(WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD),
                        java.util.List.of(id("hazard/test")), 25, false, "test")));
        helper.assertTrue(entered.get() == 1 && discovered.get() == 1 && scanned.get() == 1
                && marker.get() == 1 && hazard.get() == 1, "Runtime bus should deliver each world event");
        EchoWorldRuntimeBus.clearForTests();
        helper.succeed();
    }

    private static void worldcoreNoop(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            helper.assertTrue(EchoCoreServices.worldRegions() == NoOpWorldService.INSTANCE,
                    "Missing WorldCore service should resolve to the no-op implementation");
            helper.assertTrue(EchoCoreServices.regionService().regionDefinitions().isEmpty(),
                    "No-op region service should expose no definitions");
            helper.assertTrue(EchoCoreServices.hazardService().hazardSnapshot(null).safeZone(),
                    "No-op hazard service should report nominal state");
        });
        helper.succeed();
    }

    private static void worldcoreReleaseGuards(GameTestHelper helper) {
        WorldRegionService service = new WorldRegionService();
        WorldCoreBuiltins.register(service);
        helper.assertTrue(service.regionDefinitions().size() == 3,
                "WorldCore v0.2 should keep three built-in framework region definitions");
        helper.assertTrue(service.hazardDefinitions().size() == 8,
                "WorldCore v0.2 should keep the eight built-in framework hazard definitions");
        helper.assertTrue(service.regionDefinition(WorldCoreBuiltins.ORBITAL_DEBRIS_FIELD).isPresent(),
                "WorldCore v0.2 should keep echoorbitalremnants:orbital_debris_field");
        helper.assertTrue(service.regionDefinitions().stream()
                        .allMatch(definition -> definition.renderProfileId() != null && definition.audioProfileId() != null),
                "Built-in regions should expose RenderCore and AudioCore profile identifiers");
        helper.assertTrue(service.validateMarkers(null).isEmpty(),
                "WorldCore v0.2 release guard should not ship invalid built-in references");
        helper.succeed();
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            String testName, Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                100,
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

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoWorldCore.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, path);
    }
}
