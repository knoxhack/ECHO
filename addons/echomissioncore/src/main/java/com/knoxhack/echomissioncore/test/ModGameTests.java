package com.knoxhack.echomissioncore.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.EchoWorldRuntimeBus;
import com.knoxhack.echocore.api.WorldDiscoverySource;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldMarkerType;
import com.knoxhack.echocore.api.WorldRegionInstance;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echocore.api.mission.IMissionProgressView;
import com.knoxhack.echocore.api.mission.MissionActionView;
import com.knoxhack.echocore.api.mission.MissionChapterDefinition;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echocore.api.mission.MissionRewardClaimMode;
import com.knoxhack.echocore.api.mission.MissionRuntimeBus;
import com.knoxhack.echocore.api.mission.MissionRuntimeEvent;
import com.knoxhack.echocore.api.mission.MissionStatus;
import com.knoxhack.echocore.api.mission.ObjectiveDefinition;
import com.knoxhack.echocore.api.mission.RewardDefinition;
import com.knoxhack.echomissioncore.EchoMissionCore;
import com.knoxhack.echomissioncore.content.MissionCoreJsonReloadListener;
import com.knoxhack.echomissioncore.integration.MissionCoreWorldCoreConsumer;
import com.knoxhack.echomissioncore.service.MissionCoreService;
import com.knoxhack.echomissioncore.storage.MissionPlayerData;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoMissionCore.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NO_OP_FALLBACK =
            TEST_FUNCTIONS.register("no_op_fallback", () -> ModGameTests::noOpFallback);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JSON_VALIDATION =
            TEST_FUNCTIONS.register("json_validation", () -> ModGameTests::jsonValidation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OBJECTIVE_REWARD_FLOW =
            TEST_FUNCTIONS.register("objective_reward_flow", () -> ModGameTests::objectiveRewardFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PLAYER_DATA_ROUND_TRIP =
            TEST_FUNCTIONS.register("player_data_round_trip", () -> ModGameTests::playerDataRoundTrip);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_PROVIDER =
            TEST_FUNCTIONS.register("terminal_provider_snapshot", () -> ModGameTests::terminalProviderSnapshot);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CUSTOM_ACTIONS =
            TEST_FUNCTIONS.register("custom_action_bridge", () -> ModGameTests::customActionBridge);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDCORE_CONSUMER =
            TEST_FUNCTIONS.register("worldcore_consumer", () -> ModGameTests::worldCoreConsumer);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HOOK_COVERAGE =
            TEST_FUNCTIONS.register("hook_coverage", () -> ModGameTests::hookCoverage);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("missioncore_framework"));
        register(event, environment, "no_op_fallback", NO_OP_FALLBACK.getId());
        register(event, environment, "json_validation", JSON_VALIDATION.getId());
        register(event, environment, "objective_reward_flow", OBJECTIVE_REWARD_FLOW.getId());
        register(event, environment, "player_data_round_trip", PLAYER_DATA_ROUND_TRIP.getId());
        register(event, environment, "terminal_provider_snapshot", TERMINAL_PROVIDER.getId());
        register(event, environment, "custom_action_bridge", CUSTOM_ACTIONS.getId());
        register(event, environment, "worldcore_consumer", WORLDCORE_CONSUMER.getId());
        register(event, environment, "hook_coverage", HOOK_COVERAGE.getId());
    }

    private static void noOpFallback(GameTestHelper helper) {
        AtomicBoolean checked = new AtomicBoolean(false);
        EchoServiceRegistry.withClearedForTests(() -> {
            helper.assertFalse(EchoCoreServices.missionService().available(), "Mission service should no-op when MissionCore is absent");
            helper.assertTrue(EchoCoreServices.missionService().missionDefinitions().isEmpty(), "No-op mission definitions should be empty");
            checked.set(!EchoCoreServices.startMission(null, id("missing")));
        });
        helper.assertTrue(checked.get(), "No-op mission start should fail safely");
        helper.succeed();
    }

    private static void jsonValidation(GameTestHelper helper) {
        JsonObject chapter = JsonParser.parseString("{\"title\":\"Tests\",\"order\":1}").getAsJsonObject();
        helper.assertTrue(
                MissionCoreJsonReloadListener.parseChapterForTests(id("json_chapter"), chapter).id().equals(id("json_chapter")),
                "Chapter JSON should default id from resource location");

        JsonObject mission = JsonParser.parseString("""
                {"chapter":"echomissioncore:json_chapter","title":"JSON Test","objectives":[{"type":"obtain_item","target":"minecraft:apple"}],"rewards":[{"item":"minecraft:emerald","claimMode":"claimable"}]}
                """).getAsJsonObject();
        helper.assertTrue(
                MissionCoreJsonReloadListener.parseMissionForTests(id("json_mission"), mission).objectives().getFirst().type() == MissionObjectiveType.OBTAIN_ITEM,
                "Mission JSON should parse objective type");

        boolean failed = false;
        try {
            MissionCoreJsonReloadListener.parseMissionForTests(id("bad_json"), JsonParser.parseString("""
                    {"chapter":"echomissioncore:json_chapter","objectives":[{"type":"not_real"}]}
                    """).getAsJsonObject());
        } catch (RuntimeException exception) {
            failed = true;
        }
        helper.assertTrue(failed, "Invalid objective type should fail validation");
        helper.succeed();
    }

    private static void objectiveRewardFlow(GameTestHelper helper) {
        MissionCoreService service = MissionCoreService.INSTANCE;
        service.clearForTests();
        Identifier chapterId = id("test_chapter");
        Identifier missionId = id("test_mission");
        Identifier objectiveId = id("test_mission/apple");
        Identifier rewardId = id("test_mission/reward");
        service.registerChapter("gametest", new MissionChapterDefinition(chapterId, "Tests", "MissionCore tests", 0, 0x55FFDD));
        service.registerMission("gametest", MissionDefinition.builder(missionId, chapterId)
                .text("Apple Test", "Obtain an apple.", "GameTest")
                .objective(new ObjectiveDefinition(objectiveId, MissionObjectiveType.OBTAIN_ITEM, "Apple", "", new ItemStack(Items.APPLE), 1, true, Map.of("target", "minecraft:apple")))
                .reward(RewardDefinition.item(rewardId, MissionRewardClaimMode.CLAIMABLE, new ItemStack(Items.EMERALD)))
                .build());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        List<MissionRuntimeEvent> events = new ArrayList<>();
        MissionRuntimeBus.clearForTests();
        try {
            AutoCloseable listener = MissionRuntimeBus.register(events::add);
            try {
                helper.assertTrue(service.startMission(player, missionId), "Mission should start");
                helper.assertTrue(service.recordObjective(player, MissionObjectiveType.OBTAIN_ITEM, Identifier.withDefaultNamespace("apple"), 1, Map.of()), "Objective should record");
                helper.assertTrue(service.mission(player, missionId).orElseThrow().status() == MissionStatus.CLAIMABLE, "Mission should become claimable");
                helper.assertTrue(events.stream().anyMatch(event -> MissionRuntimeEvent.OBJECTIVE_PROGRESSED.equals(event.eventType())), "Objective event should fire");
                helper.assertTrue(events.stream().anyMatch(event -> MissionRuntimeEvent.MISSION_COMPLETED.equals(event.eventType())), "Completion event should fire");
                helper.assertTrue(service.claimReward(player, missionId), "Claim should succeed once");
                helper.assertFalse(service.claimReward(player, missionId), "Claim should be idempotent");
                helper.assertTrue(events.stream().anyMatch(event -> MissionRuntimeEvent.REWARD_CLAIMED.equals(event.eventType())), "Reward event should fire");
            } finally {
                listener.close();
            }
        } catch (Exception exception) {
            helper.fail("MissionCore listener cleanup failed: " + exception.getMessage());
        } finally {
            MissionRuntimeBus.clearForTests();
        }
        helper.succeed();
    }

    private static void playerDataRoundTrip(GameTestHelper helper) {
        Identifier missionId = id("persisted");
        Identifier chapterId = id("persisted_chapter");
        Identifier objectiveId = id("persisted/objective");
        Identifier rewardId = id("persisted/reward");
        MissionPlayerData data = new MissionPlayerData();
        data.trackMission(missionId);
        data.markMigrated("gametest");
        data.markUnlockedChapter(chapterId);
        MissionPlayerData.MissionState state = data.state(missionId);
        state.status(MissionStatus.CLAIMED);
        state.setObjectiveProgress(objectiveId, 3);
        state.claimReward(rewardId);
        state.revealObjective(objectiveId);

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        data.serialize(output);
        CompoundTag tag = output.buildResult();
        MissionPlayerData restored = new MissionPlayerData();
        restored.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        MissionPlayerData.MissionState restoredState = restored.stateIfPresent(missionId);
        helper.assertTrue(restoredState != null && restoredState.status() == MissionStatus.CLAIMED, "Mission status should round trip");
        helper.assertTrue(restoredState.objectiveProgress(objectiveId) == 3, "Objective progress should round trip");
        helper.assertTrue(restoredState.isRewardClaimed(rewardId), "Reward claim should round trip");
        helper.assertTrue(restored.hasUnlockedChapter(chapterId), "Unlocked chapter should round trip");
        helper.succeed();
    }

    private static void terminalProviderSnapshot(GameTestHelper helper) {
        if (!ModList.get().isLoaded("echoterminal")) {
            helper.succeed();
            return;
        }
        try {
            MissionCoreService service = MissionCoreService.INSTANCE;
            if (service.missionDefinitions().isEmpty()) {
                Identifier chapterId = id("terminal_smoke_chapter");
                Identifier missionId = id("terminal_smoke_mission");
                service.registerChapter("gametest", new MissionChapterDefinition(chapterId, "Terminal Smoke", "Terminal provider smoke test", 99, 0x55FFDD));
                service.registerMission("gametest", MissionDefinition.builder(missionId, chapterId)
                        .text("Terminal Smoke", "Expose one MissionCore mission to Terminal.", "GameTest")
                        .objective(ObjectiveDefinition.simple(id("terminal_smoke_mission/objective"),
                                MissionObjectiveType.CUSTOM, "Smoke", "Provider row", ItemStack.EMPTY, 1))
                        .build());
            }
            Object provider = Class.forName("com.knoxhack.echomissioncore.integration.MissionCoreTerminalProvider")
                    .getField("INSTANCE")
                    .get(null);
            Method missions = provider.getClass().getMethod("missions", Player.class);
            Object result = missions.invoke(provider, helper.makeMockServerPlayerInLevel());
            helper.assertTrue(result instanceof List<?> list && !list.isEmpty(), "MissionCore Terminal provider should expose missions");
            helper.succeed();
        } catch (ReflectiveOperationException exception) {
            helper.fail("MissionCore Terminal provider reflection failed: " + exception.getMessage());
        }
    }

    private static void customActionBridge(GameTestHelper helper) {
        MissionCoreService service = MissionCoreService.INSTANCE;
        service.clearForTests();
        Identifier chapterId = id("custom_action_chapter");
        Identifier missionId = id("custom_action_mission");
        AtomicBoolean handled = new AtomicBoolean(false);
        service.registerChapter("gametest", new MissionChapterDefinition(chapterId, "Actions", "Custom action bridge tests", 0, 0x55FFDD));
        service.registerMission("gametest", MissionDefinition.builder(missionId, chapterId)
                .text("Custom Action", "Expose a Java-only action.", "GameTest")
                .objective(ObjectiveDefinition.simple(id("custom_action_mission/objective"),
                        MissionObjectiveType.CUSTOM, "Bridge", "Custom action is visible.", ItemStack.EMPTY, 1))
                .actionProvider((player, mission, status, completeNow) ->
                        List.of(MissionActionView.enabled("custom_ping", "Ping Relay")))
                .actionHandler((player, mission, actionId) -> {
                    if ("custom_ping".equals(actionId)) {
                        handled.set(true);
                        return true;
                    }
                    return false;
                })
                .build());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        IMissionProgressView view = service.mission(player, missionId).orElseThrow();
        helper.assertTrue(view.actions().stream().anyMatch(action -> "custom_ping".equals(action.id())),
                "MissionCore view should merge Java custom actions into the action list.");
        helper.assertTrue(service.handleAction(player, missionId, "custom_ping"),
                "MissionCore should delegate unknown action ids to the Java action handler.");
        helper.assertTrue(handled.get(), "Custom action handler should receive delegated action id.");
        helper.succeed();
    }

    private static void worldCoreConsumer(GameTestHelper helper) {
        MissionCoreService service = MissionCoreService.INSTANCE;
        service.clearForTests();
        EchoWorldRuntimeBus.clearForTests();
        MissionCoreWorldCoreConsumer.registerForTests();

        Identifier chapterId = id("worldcore_chapter");
        Identifier missionId = id("worldcore_bridge");
        Identifier regionId = Identifier.fromNamespaceAndPath("echoashfallprotocol", "crash_zone_wasteland");
        Identifier scanObjective = id("worldcore_bridge/scan");
        service.registerChapter("gametest", new MissionChapterDefinition(chapterId, "World", "WorldCore bridge tests", 0, 0x66E8FF));
        service.registerMission("gametest", MissionDefinition.builder(missionId, chapterId)
                .text("WorldCore Bridge", "Respond to shared world events.", "GameTest")
                .objective(new ObjectiveDefinition(id("worldcore_bridge/enter"),
                        MissionObjectiveType.ENTER_REGION, "Enter", "Enter the test region.", ItemStack.EMPTY,
                        1, true, Map.of("target", regionId.toString())))
                .objective(new ObjectiveDefinition(scanObjective,
                        MissionObjectiveType.DISCOVER_STRUCTURE, "Scan", "Scan the test marker.", ItemStack.EMPTY,
                        1, true, Map.of("target", regionId.toString())))
                .build());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(service.startMission(player, missionId), "WorldCore bridge mission should start.");
        WorldRegionInstance region = new WorldRegionInstance(id("worldcore/region"), regionId,
                WorldRegionType.CRASH_ZONE, "Crash Zone", player.level().dimension(),
                BlockPos.ZERO, 96, List.of(), true);
        WorldMarker marker = new WorldMarker(id("worldcore/marker"), regionId, WorldMarkerType.CRASH_SITE,
                "Crash Site", "Scanned crash site.", player.level().dimension(),
                BlockPos.ZERO, 64, true, player.level().getGameTime());

        EchoWorldRuntimeBus.fireRegionEntered(new EchoWorldRuntimeBus.RegionEntered(player, region));
        EchoWorldRuntimeBus.fireRegionDiscovered(new EchoWorldRuntimeBus.RegionDiscovered(
                player, region, WorldDiscoverySource.ENTER, true));
        EchoWorldRuntimeBus.fireRegionScanned(new EchoWorldRuntimeBus.RegionScanned(player, region, marker));

        IMissionProgressView view = service.mission(player, missionId).orElseThrow();
        helper.assertTrue(view.objectives().stream().allMatch(objective -> objective.progress() >= objective.required()),
                "WorldCore runtime events should progress MissionCore region and scan objectives.");
        helper.assertTrue(view.status() == MissionStatus.COMPLETED,
                "WorldCore bridge mission should complete after matching events.");
        EchoWorldRuntimeBus.clearForTests();
        helper.succeed();
    }

    private static void hookCoverage(GameTestHelper helper) {
        MissionCoreService service = MissionCoreService.INSTANCE;
        service.clearForTests();

        String directSource = "echoblackboxprotocol";
        Identifier directChapter = Identifier.fromNamespaceAndPath(directSource, "hook_chapter");
        Identifier directMission = Identifier.fromNamespaceAndPath(directSource, "decode_cache");
        Identifier directTarget = MissionHookTargets.objectiveTarget(directSource, directMission, 0);
        service.registerChapter(directSource, new MissionChapterDefinition(
                directChapter, "Hooks", "Direct hook coverage.", 0, 0x55FFDD));
        service.registerMission(directSource, MissionDefinition.builder(directMission, directChapter)
                .text("Decode Cache", "Decode the cache.", "Hook proof")
                .objective(new ObjectiveDefinition(Identifier.fromNamespaceAndPath(directSource, "decode_cache/objective"),
                        MissionObjectiveType.CUSTOM, "Decode", "Decode the cache.", ItemStack.EMPTY,
                        1, false, Map.of("target", directTarget.toString())))
                .build());
        EchoCoreServices.registerMissionHookCoverage(directSource, directMission, directTarget);
        EchoCoreServices.registerMissionHookCoverage(directSource, directMission, directTarget);

        String mixedSource = "echoconvoyprotocol";
        Identifier mixedChapter = Identifier.fromNamespaceAndPath(mixedSource, "hook_chapter");
        Identifier mixedMission = Identifier.fromNamespaceAndPath(mixedSource, "route_alpha");
        Identifier mixedTarget0 = MissionHookTargets.objectiveTarget(mixedSource, mixedMission, 0);
        Identifier mixedTarget1 = MissionHookTargets.objectiveTarget(mixedSource, mixedMission, 1);
        service.registerChapter(mixedSource, new MissionChapterDefinition(
                mixedChapter, "Convoy Hooks", "Mixed hook coverage.", 0, 0x55FFDD));
        service.registerMission(mixedSource, MissionDefinition.builder(mixedMission, mixedChapter)
                .text("Route Alpha", "Complete two route milestones.", "Hook proof")
                .objective(new ObjectiveDefinition(Identifier.fromNamespaceAndPath(mixedSource, "route_alpha/one"),
                        MissionObjectiveType.ESTABLISH_ROUTE, "Activate", "Activate the route.", ItemStack.EMPTY,
                        1, false, Map.of("target", mixedTarget0.toString())))
                .objective(new ObjectiveDefinition(Identifier.fromNamespaceAndPath(mixedSource, "route_alpha/two"),
                        MissionObjectiveType.ESTABLISH_ROUTE, "Complete", "Complete the route.", ItemStack.EMPTY,
                        1, false, Map.of("target", mixedTarget1.toString())))
                .build());
        EchoCoreServices.registerMissionHookCoverage(mixedSource, mixedMission, mixedTarget0);

        String adapterSource = "echoorbitalremnants";
        Identifier adapterChapter = Identifier.fromNamespaceAndPath(adapterSource, "hook_chapter");
        Identifier adapterMission = Identifier.fromNamespaceAndPath(adapterSource, "orbital_scan");
        Identifier adapterTarget = MissionHookTargets.objectiveTarget(adapterSource, adapterMission, 0);
        service.registerChapter(adapterSource, new MissionChapterDefinition(
                adapterChapter, "Orbital Hooks", "Adapter fallback coverage.", 0, 0x55FFDD));
        service.registerMission(adapterSource, MissionDefinition.builder(adapterMission, adapterChapter)
                .text("Orbital Scan", "Legacy scan state still imports.", "Hook proof")
                .objective(new ObjectiveDefinition(Identifier.fromNamespaceAndPath(adapterSource, "orbital_scan/objective"),
                        MissionObjectiveType.COMPLETE_ORBITAL_SCAN, "Scan", "Complete scan.", ItemStack.EMPTY,
                        1, false, Map.of("target", adapterTarget.toString())))
                .completionRule((player, mission) -> true)
                .build());

        Map<String, String> coverage = EchoCoreServices.missionHookCoverageSummary();
        helper.assertTrue("direct-hooks".equals(coverage.get(directSource)), "Full target coverage should report direct-hooks.");
        helper.assertTrue("mixed".equals(coverage.get(mixedSource)), "Partial target coverage should report mixed.");
        helper.assertTrue("adapter-state".equals(coverage.get(adapterSource)), "Missing hook coverage should report adapter-state.");
        helper.assertTrue(service.validateContent().stream().anyMatch(warning -> warning.contains("adapter-state")),
                "Validation should warn about adapter-state migrated sources.");

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(service.startMission(player, mixedMission), "Mixed coverage mission should start.");
        helper.assertTrue(EchoCoreServices.recordMissionObjective(player, MissionObjectiveType.ESTABLISH_ROUTE, mixedTarget0, 1,
                        MissionHookTargets.context(mixedSource, mixedMission, "route", "alpha")),
                "Direct route activation hook should progress.");
        helper.assertTrue(EchoCoreServices.recordMissionObjective(player, MissionObjectiveType.ESTABLISH_ROUTE, mixedTarget1, 1,
                        MissionHookTargets.context(mixedSource, mixedMission, "route", "alpha")),
                "Direct route completion hook should progress.");
        helper.assertTrue(service.mission(player, mixedMission).orElseThrow().status() == MissionStatus.COMPLETED,
                "Direct hooks should complete the mission without Terminal state.");
        helper.assertFalse(EchoCoreServices.recordMissionObjective(player, MissionObjectiveType.ESTABLISH_ROUTE, mixedTarget0, 1,
                        MissionHookTargets.context(mixedSource, mixedMission, "route", "alpha")),
                "Once-only completed missions should ignore duplicate hook progress.");
        helper.succeed();
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                400,
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

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoMissionCore.MODID, path);
    }
}
