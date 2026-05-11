package com.knoxhack.echodatacore.test;

import com.knoxhack.echocore.api.DataScope;
import com.knoxhack.echocore.api.EchoDataBus;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.EchoWorldRuntimeBus;
import com.knoxhack.echocore.api.IDataKey;
import com.knoxhack.echocore.api.NoOpDataService;
import com.knoxhack.echocore.api.WorldDiscoverySource;
import com.knoxhack.echocore.api.WorldHazardSnapshot;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldMarkerType;
import com.knoxhack.echocore.api.WorldRegionInstance;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echodatacore.integration.DataCoreWorldCoreConsumer;
import com.knoxhack.echodatacore.DataCoreBuiltinKeys;
import com.knoxhack.echodatacore.DataCoreDataService;
import com.knoxhack.echodatacore.EchoDataCore;
import net.minecraft.nbt.CompoundTag;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoDataCore.MODID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DATACORE_CONTRACTS =
            TEST_FUNCTIONS.register("datacore_contracts", () -> ModGameTests::dataCoreContracts);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("datacore_release"));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                100,
                0,
                true,
                net.minecraft.world.level.block.Rotation.NONE,
                false,
                1,
                1,
                false,
                2);
        event.registerTest(id("datacore_contracts"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,
                        DATACORE_CONTRACTS.getId()), data));
    }

    private static void dataCoreContracts(GameTestHelper helper) {
        noOpFallback(helper);
        duplicateKeyRegistration(helper);
        playerWorldTeamValues(helper);
        migrationAndTerminalProbe(helper);
        legacyReadThrough(helper);
        runtimeBus(helper);
        worldRuntimeBusConsumer(helper);
        helper.succeed();
    }

    private static void noOpFallback(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            NoOpDataService.INSTANCE.clearRegisteredKeysForTests();
            IDataKey<String> key = IDataKey.string(id("test/noop"), DataScope.PLAYER, "default", true);
            helper.assertTrue(EchoCoreServices.registerDataKey(key) == key,
                    "NoOp data service should retain key metadata.");
            helper.assertTrue("default".equals(EchoCoreServices.playerData(null).get(key)),
                    "NoOp reads should return defaults.");
            helper.assertFalse(EchoCoreServices.playerData(null).set(key, "changed"),
                    "NoOp writes should fail safely.");
            helper.assertTrue(EchoCoreServices.platformProviderSummary().contains("dataKeys=1"),
                    "Provider summary should expose registered NoOp data keys.");
        });
    }

    private static void duplicateKeyRegistration(GameTestHelper helper) {
        Identifier duplicateId = id("test/duplicate");
        IDataKey<Long> first = IDataKey.counter(duplicateId, DataScope.PLAYER, 7L, true);
        IDataKey<Boolean> second = IDataKey.flag(duplicateId, DataScope.WORLD, false, true);
        IDataKey<Long> registeredFirst = DataCoreDataService.INSTANCE.registerKey(first);
        IDataKey<Boolean> registeredSecond = DataCoreDataService.INSTANCE.registerKey(second);
        helper.assertTrue(registeredFirst == (Object) registeredSecond,
                "Duplicate key registration should keep the first definition.");
        helper.assertTrue(DataCoreDataService.INSTANCE.key(duplicateId).orElseThrow().scope() == DataScope.PLAYER,
                "Duplicate key scope should remain from the first definition.");
    }

    private static void playerWorldTeamValues(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        IDataKey<Boolean> flag = IDataKey.flag(id("test/player_flag"), DataScope.PLAYER, false, true);
        IDataKey<Long> counter = IDataKey.counter(id("test/world_counter"), DataScope.WORLD, 0L, true);
        IDataKey<String> enumName = IDataKey.enumName(id("test/team_mode"), DataScope.TEAM, "idle", true);
        IDataKey<CompoundTag> record = IDataKey.record(id("test/player_record"),
                DataScope.PLAYER, CompoundTag.CODEC, new CompoundTag(), true);

        DataCoreDataService.INSTANCE.registerKey(flag);
        DataCoreDataService.INSTANCE.registerKey(counter);
        DataCoreDataService.INSTANCE.registerKey(enumName);
        DataCoreDataService.INSTANCE.registerKey(record);

        int dirtyBefore = DataCoreDataService.INSTANCE.debugDirtyPlayerKeyCount(player.getUUID());
        long revisionBefore = DataCoreDataService.INSTANCE.syncBridge().revision();
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player).set(flag, true),
                "Changed player flag should write.");
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player).get(flag),
                "Player flag should read back.");
        helper.assertTrue(DataCoreDataService.INSTANCE.debugDirtyPlayerKeyCount(player.getUUID()) == dirtyBefore + 1,
                "Repeated dirty writes to the same key should coalesce by key id.");
        long revisionAfter = DataCoreDataService.INSTANCE.syncBridge().revision();
        helper.assertTrue(revisionAfter > revisionBefore, "Changed write should advance revision.");
        helper.assertFalse(DataCoreDataService.INSTANCE.player(player).set(flag, true),
                "Identical player flag write should not dirty.");
        helper.assertTrue(DataCoreDataService.INSTANCE.syncBridge().revision() == revisionAfter,
                "Identical write should not advance revision.");

        CompoundTag storedRecord = new CompoundTag();
        storedRecord.putString("mode", "scan");
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player).set(record, storedRecord),
                "Structured record should write.");
        helper.assertTrue("scan".equals(DataCoreDataService.INSTANCE.player(player).get(record).getStringOr("mode", "")),
                "Structured record should read back.");
        helper.assertTrue(player.getPersistentData().getCompoundOrEmpty(DataCoreDataService.PLAYER_ROOT)
                        .getCompoundOrEmpty("values").contains(flag.id().toString()),
                "Player values should be stored under the DataCore root.");

        helper.assertTrue(DataCoreDataService.INSTANCE.world(helper.getLevel()).set(counter, 12L),
                "World counter should write.");
        helper.assertTrue(DataCoreDataService.INSTANCE.world(helper.getLevel()).get(counter) == 12L,
                "World counter should read back.");
        Identifier teamId = id("team/release_test");
        helper.assertTrue(DataCoreDataService.INSTANCE.team(helper.getLevel(), teamId).set(enumName, "active"),
                "Team enum should write.");
        helper.assertTrue("active".equals(DataCoreDataService.INSTANCE.team(helper.getLevel(), teamId).get(enumName)),
                "Team enum should read back.");
    }

    private static void migrationAndTerminalProbe(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CompoundTag root = new CompoundTag();
        root.putInt("version", 0);
        player.getPersistentData().put(DataCoreDataService.PLAYER_ROOT, root);

        DataCoreDataService.INSTANCE.onPlayerLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        CompoundTag migrated = player.getPersistentData().getCompoundOrEmpty(DataCoreDataService.PLAYER_ROOT);
        helper.assertTrue(migrated.getIntOr("version", 0) == DataCoreDataService.CURRENT_VERSION,
                "Player migration should advance the DataCore root version.");
        helper.assertTrue(migrated.getCompoundOrEmpty("migrations")
                        .getIntOr(EchoDataCore.MODID, 0) == DataCoreDataService.CURRENT_VERSION,
                "Player migration should record the DataCore namespace version.");
        helper.assertTrue("online".equals(DataCoreDataService.INSTANCE.player(player)
                        .get(com.knoxhack.echodatacore.DataCoreBuiltinKeys.TERMINAL_PROBE)),
                "Login should expose the Terminal probe value.");

        CompoundTag firstPass = migrated.copy();
        DataCoreDataService.INSTANCE.onPlayerLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        CompoundTag secondPass = player.getPersistentData().getCompoundOrEmpty(DataCoreDataService.PLAYER_ROOT);
        helper.assertTrue(firstPass.equals(secondPass),
                "Repeated migration/login writes should be idempotent when values are unchanged.");
    }

    private static void legacyReadThrough(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("telemetry_tier", true);
        player.getPersistentData().put("echoorbitalremnants_progress", legacy.copy());

        IDataKey<Boolean> legacyKey = IDataKey.flag(
                Identifier.fromNamespaceAndPath("echoorbitalremnants", "unlock/telemetry_tier"),
                DataScope.PLAYER,
                false,
                true);
        DataCoreDataService.INSTANCE.registerKey(legacyKey);
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player).get(legacyKey),
                "Legacy adapter should read existing Orbital progress.");
        helper.assertTrue(legacy.equals(player.getPersistentData().getCompoundOrEmpty("echoorbitalremnants_progress")),
                "Legacy adapter reads should not modify the old save root.");
    }

    private static void runtimeBus(GameTestHelper helper) {
        IDataKey<Boolean> flag = IDataKey.flag(id("test/bus_flag"), DataScope.PLAYER, false, true);
        DataCoreDataService.INSTANCE.registerKey(flag);
        helper.assertFalse(DataCoreDataService.INSTANCE.player(null).get(flag),
                "Null player reads should return the key default.");
        helper.assertFalse(DataCoreDataService.INSTANCE.player(null).set(flag, true),
                "Null player writes should fail safely.");

        AtomicInteger events = new AtomicInteger();
        try {
            AutoCloseable ignored = EchoDataBus.subscribe(message -> {
                if (flag.id().equals(message.keyId())) {
                    events.incrementAndGet();
                }
            });
            DataCoreDataService.INSTANCE.syncBridge().markDirty(DataScope.PLAYER, "test-player", flag.id());
            ignored.close();
        } catch (Exception exception) {
            throw new AssertionError("Data bus listener should close cleanly.", exception);
        }
        helper.assertTrue(events.get() == 1, "Data bus should publish one change message.");
    }

    private static void worldRuntimeBusConsumer(GameTestHelper helper) {
        EchoWorldRuntimeBus.clearForTests();
        DataCoreWorldCoreConsumer.registerForTests();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Identifier regionId = Identifier.fromNamespaceAndPath("echoashfallprotocol", "crash_zone_wasteland");
        WorldRegionInstance region = new WorldRegionInstance(id("runtime/region"), regionId,
                WorldRegionType.CRASH_ZONE, "Crash Zone", player.level().dimension(),
                BlockPos.ZERO, 96, List.of(id("hazard/radiation")), true);
        WorldMarker marker = new WorldMarker(id("runtime/marker"), regionId, WorldMarkerType.CRASH_SITE,
                "Runtime Marker", "Runtime marker.", player.level().dimension(), BlockPos.ZERO,
                32, true, player.level().getGameTime());

        EchoWorldRuntimeBus.fireRegionEntered(new EchoWorldRuntimeBus.RegionEntered(player, region));
        helper.assertTrue(regionId.toString().equals(DataCoreDataService.INSTANCE.player(player)
                        .get(DataCoreBuiltinKeys.WORLDCORE_LAST_REGION)),
                "WorldCore region enter should update DataCore last-region state.");
        EchoWorldRuntimeBus.fireRegionDiscovered(new EchoWorldRuntimeBus.RegionDiscovered(
                player, region, WorldDiscoverySource.ENTER, true));
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player)
                        .get(DataCoreBuiltinKeys.WORLDCORE_REGION_DISCOVERIES) == 1L,
                "WorldCore first discovery should increment DataCore discovery count.");
        EchoWorldRuntimeBus.fireMarkerRevealed(new EchoWorldRuntimeBus.MarkerRevealed(player, marker));
        helper.assertTrue(marker.id().toString().equals(DataCoreDataService.INSTANCE.player(player)
                        .get(DataCoreBuiltinKeys.WORLDCORE_LAST_MARKER)),
                "WorldCore marker reveal should update DataCore marker state.");
        EchoWorldRuntimeBus.fireHazardChanged(new EchoWorldRuntimeBus.HazardChanged(player,
                WorldHazardSnapshot.nominal(),
                new WorldHazardSnapshot(List.of(regionId), List.of(id("hazard/radiation")), 41, false, "Radiation")));
        helper.assertTrue(DataCoreDataService.INSTANCE.player(player)
                        .get(DataCoreBuiltinKeys.WORLDCORE_ACTIVE_HAZARD_SEVERITY) == 41L,
                "WorldCore hazard changes should update DataCore hazard severity.");
        EchoWorldRuntimeBus.clearForTests();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoDataCore.MODID, path);
    }
}
