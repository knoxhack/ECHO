package com.knoxhack.signalos.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.TerminalPlacementService;
import com.knoxhack.echocore.api.TerminalRewardService;
import com.knoxhack.echocore.api.mission.InMemoryMissionRegistry;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.SignalOsApp;
import com.knoxhack.signalos.api.SignalOsDataRecord;
import com.knoxhack.signalos.api.SignalOsDriveData;
import com.knoxhack.signalos.api.SignalOsDataProvider;
import com.knoxhack.signalos.api.TerminalActionRegistry;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalChapter;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.block.entity.SignalOsServerRackBlockEntity;
import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import com.knoxhack.signalos.content.SignalOsJsonContentLoader;
import com.knoxhack.signalos.item.SignalOsDataDriveItem;
import com.knoxhack.signalos.kubejs.SignalOSKubeBridge;
import com.knoxhack.signalos.kubejs.SignalOSEvents;
import com.knoxhack.signalos.integration.SignalOsMissionCoreIntegration;
import com.knoxhack.signalos.menu.SignalOsServerRackMenu;
import com.knoxhack.signalos.menu.SignalOsTerminalMenu;
import com.knoxhack.signalos.network.SignalOsRackActionPacket;
import com.knoxhack.signalos.network.SignalOsTerminalStatePacket;
import com.knoxhack.signalos.service.SignalOsBuiltinActions;
import com.knoxhack.signalos.service.SignalOsComputerNetworkService;
import com.knoxhack.signalos.service.SignalOsPlayerData;
import com.knoxhack.signalos.service.SignalOsRackActions;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, SignalOS.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REGISTRY_SORTING =
            TEST_FUNCTIONS.register("registry_sorting", () -> ModGameTests::registrySorting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DUPLICATE_ID_HANDLING =
            TEST_FUNCTIONS.register("duplicate_id_handling", () -> ModGameTests::duplicateIdHandling);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JSON_PARSE_FAILURES =
            TEST_FUNCTIONS.register("json_parse_failures", () -> ModGameTests::jsonParseFailures);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> JSON_REFERENCE_VALIDATION =
            TEST_FUNCTIONS.register("json_reference_validation", () -> ModGameTests::jsonReferenceValidation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KUBEJS_BRIDGE_ABSENCE =
            TEST_FUNCTIONS.register("kubejs_bridge_absence", () -> ModGameTests::kubejsBridgeAbsence);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHIVE_READ_STATE =
            TEST_FUNCTIONS.register("archive_read_state", () -> ModGameTests::archiveReadState);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REWARD_STORAGE_FLOW =
            TEST_FUNCTIONS.register("reward_storage_flow", () -> ModGameTests::rewardStorageFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVER_ACTION_VALIDATION =
            TEST_FUNCTIONS.register("server_action_validation", () -> ModGameTests::serverActionValidation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PLAYER_DATA_REWRITES =
            TEST_FUNCTIONS.register("player_data_rewrites", () -> ModGameTests::playerDataRewrites);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OWNED_TERMINAL_REWARD_FLOW =
            TEST_FUNCTIONS.register("owned_terminal_reward_flow", () -> ModGameTests::ownedTerminalRewardFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ECHO_CORE_SERVICE_GUARD =
            TEST_FUNCTIONS.register("echo_core_service_guard", () -> ModGameTests::echoCoreServiceGuard);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_STATE_SNAPSHOT =
            TEST_FUNCTIONS.register("terminal_state_snapshot", () -> ModGameTests::terminalStateSnapshot);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MENU_VALIDITY =
            TEST_FUNCTIONS.register("terminal_menu_validity", () -> ModGameTests::terminalMenuValidity);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> APP_REGISTRY_AND_DATA =
            TEST_FUNCTIONS.register("app_registry_and_data", () -> ModGameTests::appRegistryAndData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DATA_DRIVE_COMPONENT_FLOW =
            TEST_FUNCTIONS.register("data_drive_component_flow", () -> ModGameTests::dataDriveComponentFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COMPUTER_NETWORK_DISCOVERY =
            TEST_FUNCTIONS.register("computer_network_discovery", () -> ModGameTests::computerNetworkDiscovery);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NOTE_EDITING_FLOW =
            TEST_FUNCTIONS.register("note_editing_flow", () -> ModGameTests::noteEditingFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVER_RACK_MENU_ACTIONS =
            TEST_FUNCTIONS.register("server_rack_menu_actions", () -> ModGameTests::serverRackMenuActions);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CUSTOM_APP_RECORD_VIEW =
            TEST_FUNCTIONS.register("custom_app_record_view", () -> ModGameTests::customAppRecordView);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_CORE_CONTENT =
            TEST_FUNCTIONS.register("missioncore_content_registration", () -> ModGameTests::missionCoreContentRegistration);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("signalos_mvp"));
        register(event, environment, "registry_sorting", REGISTRY_SORTING.getId());
        register(event, environment, "duplicate_id_handling", DUPLICATE_ID_HANDLING.getId());
        register(event, environment, "json_parse_failures", JSON_PARSE_FAILURES.getId());
        register(event, environment, "json_reference_validation", JSON_REFERENCE_VALIDATION.getId());
        register(event, environment, "kubejs_bridge_absence", KUBEJS_BRIDGE_ABSENCE.getId());
        register(event, environment, "archive_read_state", ARCHIVE_READ_STATE.getId());
        register(event, environment, "reward_storage_flow", REWARD_STORAGE_FLOW.getId());
        register(event, environment, "server_action_validation", SERVER_ACTION_VALIDATION.getId());
        register(event, environment, "player_data_rewrites", PLAYER_DATA_REWRITES.getId());
        register(event, environment, "owned_terminal_reward_flow", OWNED_TERMINAL_REWARD_FLOW.getId());
        register(event, environment, "echo_core_service_guard", ECHO_CORE_SERVICE_GUARD.getId());
        register(event, environment, "terminal_state_snapshot", TERMINAL_STATE_SNAPSHOT.getId());
        register(event, environment, "terminal_menu_validity", TERMINAL_MENU_VALIDITY.getId());
        register(event, environment, "app_registry_and_data", APP_REGISTRY_AND_DATA.getId());
        register(event, environment, "data_drive_component_flow", DATA_DRIVE_COMPONENT_FLOW.getId());
        register(event, environment, "computer_network_discovery", COMPUTER_NETWORK_DISCOVERY.getId());
        register(event, environment, "note_editing_flow", NOTE_EDITING_FLOW.getId());
        register(event, environment, "server_rack_menu_actions", SERVER_RACK_MENU_ACTIONS.getId());
        register(event, environment, "custom_app_record_view", CUSTOM_APP_RECORD_VIEW.getId());
        register(event, environment, "missioncore_content_registration", MISSION_CORE_CONTENT.getId());
    }

    private static void registrySorting(GameTestHelper helper) {
        SignalOsContentRegistry.withClearedForTests(() -> {
            SignalOsContentRegistry.registerChapter(TerminalChapter.builder("signalos_test:zeta")
                    .title("Zeta")
                    .section("progress")
                    .order(40)
                    .build());
            SignalOsContentRegistry.registerChapter(TerminalChapter.builder("signalos_test:alpha")
                    .title("Alpha")
                    .section("command")
                    .order(100)
                    .build());
            SignalOsContentRegistry.registerChapter(TerminalChapter.builder("signalos_test:beta")
                    .title("Beta")
                    .section("progress")
                    .order(10)
                    .build());
            List<TerminalChapter> chapters = SignalOsContentRegistry.chapters();
            helper.assertTrue(chapters.get(0).id().equals(testId("alpha")),
                    "Command section chapters should sort before progress chapters.");
            helper.assertTrue(chapters.get(1).id().equals(testId("beta")),
                    "Progress chapters should sort by configured order.");
        });
        helper.succeed();
    }

    private static void duplicateIdHandling(GameTestHelper helper) {
        SignalOsContentRegistry.withClearedForTests(() -> {
            SignalOsContentRegistry.registerChapter(TerminalChapter.builder("signalos_test:dupe").title("One").build());
            try {
                SignalOsContentRegistry.registerChapter(TerminalChapter.builder("signalos_test:dupe").title("Two").build());
                helper.fail("Duplicate Java chapter ids should be rejected.");
            } catch (IllegalArgumentException expected) {
                helper.assertTrue(expected.getMessage().contains("Duplicate SignalOS chapter id"),
                        "Duplicate exception should name the conflicting surface.");
            }
        });
        helper.succeed();
    }

    private static void jsonParseFailures(GameTestHelper helper) {
        try {
            SignalOsJsonContentLoader.parseMissionForTests(testId("broken"), new JsonObject());
            helper.fail("Mission JSON without a chapter should fail parsing.");
        } catch (JsonParseException expected) {
            helper.assertTrue(expected.getMessage().contains("chapter"),
                    "Mission JSON failure should mention the missing chapter field.");
        }

        JsonObject badObjectives = new JsonObject();
        badObjectives.addProperty("chapter", testId("json_chapter").toString());
        badObjectives.addProperty("objectives", "not-an-array");
        try {
            SignalOsJsonContentLoader.parseMissionForTests(testId("bad_objectives"), badObjectives);
            helper.fail("Mission JSON with non-array objectives should fail parsing.");
        } catch (JsonParseException expected) {
            helper.assertTrue(expected.getMessage().contains("objectives"),
                    "Mission objective type failures should mention the objectives field.");
        }

        JsonObject badRewards = new JsonObject();
        badRewards.addProperty("chapter", testId("json_chapter").toString());
        JsonArray rewards = new JsonArray();
        rewards.add("minecraft:bread");
        badRewards.add("displayRewards", rewards);
        try {
            SignalOsJsonContentLoader.parseMissionForTests(testId("bad_rewards"), badRewards);
            helper.fail("Mission JSON with non-object rewards should fail parsing.");
        } catch (JsonParseException expected) {
            helper.assertTrue(expected.getMessage().contains("displayRewards[0]"),
                    "Reward type failures should mention the exact reward entry.");
        }

        JsonObject badArchiveLines = new JsonObject();
        badArchiveLines.addProperty("chapter", testId("json_chapter").toString());
        badArchiveLines.addProperty("lines", "not-an-array");
        try {
            SignalOsJsonContentLoader.parseArchiveForTests(testId("bad_archive"), badArchiveLines);
            helper.fail("Archive JSON with non-array lines should fail parsing.");
        } catch (JsonParseException expected) {
            helper.assertTrue(expected.getMessage().contains("lines"),
                    "Archive line type failures should mention the lines field.");
        }

        JsonObject chapter = new JsonObject();
        chapter.addProperty("title", "JSON Chapter");
        TerminalChapter parsed = SignalOsJsonContentLoader.parseChapterForTests(testId("json_chapter"), chapter);
        helper.assertTrue(parsed.id().equals(testId("json_chapter")),
                "Chapter JSON should use its datapack file id.");
        helper.succeed();
    }

    private static void jsonReferenceValidation(GameTestHelper helper) {
        SignalOsContentRegistry.withClearedForTests(() -> {
            Identifier jsonChapter = testId("json_chapter");
            Identifier javaChapter = testId("java_chapter");
            Identifier okMission = testId("ok_mission");
            Identifier javaMission = testId("java_mission");
            Identifier orphanMission = testId("orphan_mission");
            Identifier rewardMission = testId("reward_mission");
            Identifier missingRewardMission = testId("missing_reward_mission");
            Identifier okArchive = testId("ok_archive");
            Identifier orphanArchive = testId("orphan_archive");

            SignalOsContentRegistry.registerChapter(TerminalChapter.builder(javaChapter)
                    .title("Java Chapter")
                    .build());

            SignalOsContentRegistry.LoadedContent loaded = new SignalOsContentRegistry.LoadedContent(
                    Map.of(jsonChapter, TerminalChapter.builder(jsonChapter).title("JSON Chapter").build()),
                    Map.of(
                            okMission, TerminalMission.builder(okMission).chapter(jsonChapter.toString()).build(),
                            javaMission, TerminalMission.builder(javaMission).chapter(javaChapter.toString()).build(),
                            orphanMission, TerminalMission.builder(orphanMission).chapter(testId("missing").toString()).build(),
                            rewardMission, TerminalMission.builder(rewardMission).chapter(jsonChapter.toString()).reward("minecraft:bread", 1).build(),
                            missingRewardMission, TerminalMission.builder(missingRewardMission).chapter(jsonChapter.toString()).reward("signalos_test:missing_reward", 1).build()),
                    Map.of(
                            okArchive, TerminalArchiveRecord.builder(okArchive).chapter(jsonChapter.toString()).build(),
                            orphanArchive, TerminalArchiveRecord.builder(orphanArchive).chapter(testId("missing").toString()).build()),
                    new SignalOsContentRegistry.LoadReport(7, 7, 0, 0, 0));

            SignalOsContentRegistry.LoadedContent validated =
                    SignalOsJsonContentLoader.validateReferencesForTests(loaded);
            helper.assertTrue(validated.missions().containsKey(okMission),
                    "JSON missions should keep references to JSON chapters loaded in the same pass.");
            helper.assertTrue(validated.missions().containsKey(javaMission),
                    "JSON missions should keep references to already-registered Java chapters.");
            helper.assertTrue(validated.missions().containsKey(rewardMission),
                    "JSON missions should keep registered reward item references.");
            helper.assertFalse(validated.missions().containsKey(orphanMission),
                    "JSON missions with missing chapters should be skipped.");
            helper.assertFalse(validated.missions().containsKey(missingRewardMission),
                    "JSON missions with missing reward item ids should be skipped without creating reload-time stacks.");
            helper.assertTrue(validated.archives().containsKey(okArchive),
                    "JSON archives should keep references to JSON chapters loaded in the same pass.");
            helper.assertFalse(validated.archives().containsKey(orphanArchive),
                    "JSON archives with missing chapters should be skipped.");
            helper.assertTrue(validated.report().rejectedReferences() == 3,
                    "JSON load report should count skipped missing references.");
        });
        helper.succeed();
    }

    private static void kubejsBridgeAbsence(GameTestHelper helper) {
        SignalOsContentRegistry.withClearedForTests(() -> {
            SignalOSKubeBridge.clearScriptContent();
            SignalOSEvents.content(event -> {
                event.chapter("signalos_test:script")
                        .title("Script Chapter")
                        .section("intel")
                        .page("missions")
                        .register();
                event.mission("signalos_test:script_mission")
                        .chapter("signalos_test:script")
                        .title("Script Mission")
                        .objective("Reload safely")
                        .reward("minecraft:bread", 1)
                        .register();
            });
            helper.assertTrue(SignalOsContentRegistry.chapters().stream()
                            .anyMatch(chapter -> chapter.id().equals(testId("script"))),
                    "Soft script bridge should register content without KubeJS classes loaded.");
            helper.assertTrue(SignalOsContentRegistry.missionsFor(testId("script")).size() == 1,
                    "Script bridge missions should merge with registry content.");
            SignalOSKubeBridge.clearScriptContent();
            helper.assertTrue(SignalOsContentRegistry.chapters().isEmpty(),
                    "Script bridge clear should be reload-safe.");
        });
        helper.succeed();
    }

    private static void archiveReadState(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Identifier archiveId = testId("archive");
        helper.assertFalse(SignalOsPlayerData.isArchiveRead(player, archiveId),
                "Archives should start unread for a new player.");
        SignalOsPlayerData.markArchiveRead(player, archiveId);
        helper.assertTrue(SignalOsPlayerData.isArchiveRead(player, archiveId),
                "Archive read state should persist in player data.");
        helper.succeed();
    }

    private static void rewardStorageFlow(GameTestHelper helper) {
        SignalOsTerminalBlockEntity terminal = new SignalOsTerminalBlockEntity(
                BlockPos.ZERO,
                ModBlocks.TERMINAL.get().defaultBlockState());
        helper.assertFalse(terminal.storeRewards("signalos_test:empty", List.of()),
                "Terminal should reject empty reward batches.");
        helper.assertFalse(terminal.storeRewards("signalos_test:empty_stack", List.of(ItemStack.EMPTY)),
                "Terminal should reject empty reward stacks.");
        helper.assertTrue(terminal.storeRewards("signalos_test:reward",
                        List.of(new ItemStack(Items.BREAD, 4), new ItemStack(Items.APPLE, 2))),
                "Terminal should store simple reward stacks.");
        helper.assertTrue(terminal.storedRewardCount() == 6,
                "Stored reward count should include every cached item.");
        List<ItemStack> overflow = new ArrayList<>();
        for (int i = 0; i < terminal.rewardSlotCount() + 1; i++) {
            overflow.add(new ItemStack(Items.WOODEN_SWORD));
        }
        helper.assertFalse(terminal.storeRewards("signalos_test:overflow", overflow),
                "Overflow reward batches should fail before committing partial stacks.");
        helper.assertTrue(terminal.storedRewardCount() == 6,
                "Failed overflow storage should leave existing rewards unchanged.");
        helper.succeed();
    }

    private static void serverActionValidation(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SignalOsContentRegistry.withClearedForTests(() -> {
            Identifier lockedArchiveId = testId("locked_archive");
            Identifier openArchiveId = testId("open_archive");
            SignalOsContentRegistry.registerArchive(TerminalArchiveRecord.builder(lockedArchiveId)
                    .chapter(testId("chapter").toString())
                    .title("Locked Archive")
                    .locked(true)
                    .build());
            SignalOsContentRegistry.registerArchive(TerminalArchiveRecord.builder(openArchiveId)
                    .chapter(testId("chapter").toString())
                    .title("Open Archive")
                    .build());

            helper.assertFalse(SignalOsBuiltinActions.markArchiveRead(player, null),
                    "Null archive ids should not be marked read.");
            helper.assertFalse(SignalOsBuiltinActions.markArchiveRead(player, lockedArchiveId),
                    "Locked archive actions should be rejected.");
            helper.assertFalse(SignalOsPlayerData.isArchiveRead(player, lockedArchiveId),
                    "Locked archive records should not be marked read by server actions.");

            helper.assertTrue(SignalOsBuiltinActions.markArchiveRead(player, openArchiveId),
                    "Unlocked archive actions should be accepted.");
            helper.assertTrue(SignalOsPlayerData.isArchiveRead(player, openArchiveId),
                    "Unlocked archive records should be marked read by server actions.");

            Identifier missingMissionId = testId("missing_mission");
            helper.assertFalse(SignalOsPlayerData.isMissionClaimed(player, missingMissionId),
                    "Missing missions should not start marked claimed.");
        });
        helper.succeed();
    }

    private static void playerDataRewrites(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Identifier missionId = testId("mission_data");
        SignalOsPlayerData.markMissionClaimed(player, null);
        helper.assertFalse(SignalOsPlayerData.isMissionClaimed(player, null),
                "Null mission ids should be ignored by player data helpers.");

        CompoundTag signalOs = player.getPersistentData().getCompoundOrEmpty("signalos");
        signalOs.putString("claimed_mission_0", missionId.toString());
        signalOs.putString("claimed_mission_1", "");
        signalOs.putInt("claimed_mission_count", 2);
        player.getPersistentData().put("signalos", signalOs);
        SignalOsPlayerData.markMissionClaimed(player, missionId);
        CompoundTag rewritten = player.getPersistentData().getCompoundOrEmpty("signalos");
        helper.assertTrue(rewritten.getIntOr("claimed_mission_count", -1) == 1,
                "Persistent mission rewrites should de-duplicate values and drop blank stale entries.");

        Identifier archiveId = testId("archive_data");
        SignalOsPlayerData.markArchiveRead(player, archiveId);
        SignalOsPlayerData.markArchiveRead(player, archiveId);
        CompoundTag archiveData = player.getPersistentData().getCompoundOrEmpty("signalos");
        helper.assertTrue(archiveData.getIntOr("read_archive_count", -1) == 1,
                "Persistent archive rewrites should remain stable across repeated writes.");
        helper.succeed();
    }

    private static void ownedTerminalRewardFlow(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos terminalPos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlock(terminalPos, ModBlocks.TERMINAL.get().defaultBlockState(), 3);
        SignalOsTerminalBlockEntity terminal =
                (SignalOsTerminalBlockEntity) helper.getLevel().getBlockEntity(terminalPos);
        helper.assertTrue(terminal != null, "Placed SignalOS terminal should create a block entity.");
        terminal.setOwnerIfMissing(player);
        SignalOsTerminalServices.rememberTerminal(player, terminalPos);

        helper.assertTrue(SignalOsTerminalServices.findOwnedTerminal(player, false) == terminal,
                "Owned terminal cache should resolve the remembered terminal without a broad search.");
        helper.assertTrue(terminal.storeRewards("signalos_test:owned", List.of(new ItemStack(Items.BREAD, 3))),
                "Owned terminal block entity should store valid rewards.");
        helper.assertTrue(SignalOsTerminalServices.pendingRewardCount(player) == 3,
                "Owned terminal cache should report cached reward item counts.");
        helper.assertTrue(terminal.claimAllRewards(player),
                "Owned terminal block entity should claim stored rewards.");
        helper.assertTrue(SignalOsTerminalServices.pendingRewardCount(player) == 0,
                "Claimed reward inbox should be empty on the server.");
        helper.succeed();
    }

    private static void echoCoreServiceGuard(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            boolean registered = SignalOsTerminalServices.registerEchoCoreServices();
            if (ModList.get().isLoaded("echoterminal")) {
                helper.assertFalse(registered,
                        "SignalOS should not claim Echo Core terminal services while ECHO Terminal is loaded.");
                helper.assertTrue(EchoServiceRegistry.find(TerminalPlacementService.class).isEmpty(),
                        "SignalOS should leave placement ownership empty when it defers to ECHO Terminal.");
                helper.assertTrue(EchoServiceRegistry.find(TerminalRewardService.class).isEmpty(),
                        "SignalOS should leave reward ownership empty when it defers to ECHO Terminal.");
                return;
            }

            helper.assertTrue(registered,
                    "SignalOS should claim empty Echo Core terminal services when ECHO Terminal is absent.");
            TerminalPlacementService placement =
                    EchoServiceRegistry.find(TerminalPlacementService.class).orElse(null);
            TerminalRewardService reward =
                    EchoServiceRegistry.find(TerminalRewardService.class).orElse(null);
            helper.assertTrue(placement != null, "SignalOS should register a placement provider.");
            helper.assertTrue(reward != null, "SignalOS should register a reward provider.");
            helper.assertTrue(EchoCoreServices.terminalStructureBlockState().is(ModBlocks.TERMINAL.get()),
                    "Echo Core terminal structure state should resolve to the SignalOS terminal.");
            helper.assertTrue(EchoCoreServices.isTerminalBlock(ModBlocks.TERMINAL.get().defaultBlockState()),
                    "Echo Core terminal block checks should recognize the SignalOS terminal.");
            helper.assertTrue(SignalOsTerminalServices.registerEchoCoreServices(),
                    "Repeated SignalOS provider registration should be a no-op success.");
            helper.assertTrue(EchoServiceRegistry.find(TerminalPlacementService.class).orElse(null) == placement,
                    "Repeated registration should keep the same SignalOS placement provider instance.");
            helper.assertTrue(EchoServiceRegistry.find(TerminalRewardService.class).orElse(null) == reward,
                    "Repeated registration should keep the same SignalOS reward provider instance.");
        });

        EchoServiceRegistry.withClearedForTests(() -> {
            TerminalPlacementService foreignPlacement = new TerminalPlacementService() {
                @Override
                public boolean placeTerminal(Level level, BlockPos pos, Player owner) {
                    return false;
                }

                @Override
                public BlockState structureBlockState() {
                    return Blocks.BARRIER.defaultBlockState();
                }
            };
            TerminalRewardService foreignReward = new TerminalRewardService() {
                @Override
                public boolean storeRewards(net.minecraft.server.level.ServerPlayer player, String missionId,
                        List<ItemStack> rewards) {
                    return false;
                }

                @Override
                public boolean claimRewards(net.minecraft.server.level.ServerPlayer player) {
                    return false;
                }
            };
            EchoCoreServices.registerTerminalPlacementService(foreignPlacement);
            EchoCoreServices.registerTerminalRewardService(foreignReward);
            helper.assertFalse(SignalOsTerminalServices.registerEchoCoreServices(),
                    "SignalOS should not replace an existing Echo Core terminal provider.");
            helper.assertTrue(EchoServiceRegistry.find(TerminalPlacementService.class).orElse(null) == foreignPlacement,
                    "Existing placement provider should remain untouched.");
            helper.assertTrue(EchoServiceRegistry.find(TerminalRewardService.class).orElse(null) == foreignReward,
                    "Existing reward provider should remain untouched.");
        });
        helper.succeed();
    }

    private static void terminalStateSnapshot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SignalOsContentRegistry.withClearedForTests(() -> {
            Identifier chapterId = testId("state_chapter");
            Identifier completeMissionId = testId("complete_mission");
            Identifier claimedMissionId = testId("claimed_mission");
            Identifier readArchiveId = testId("read_archive");
            Identifier unreadArchiveId = testId("unread_archive");

            SignalOsContentRegistry.registerChapter(TerminalChapter.builder(chapterId)
                    .title("State Chapter")
                    .build());
            SignalOsContentRegistry.registerMission(TerminalMission.builder(completeMissionId)
                    .chapter(chapterId.toString())
                    .title("Complete Mission")
                    .build());
            SignalOsContentRegistry.registerMission(TerminalMission.builder(claimedMissionId)
                    .chapter(chapterId.toString())
                    .title("Claimed Mission")
                    .build());
            SignalOsContentRegistry.registerArchive(TerminalArchiveRecord.builder(readArchiveId)
                    .chapter(chapterId.toString())
                    .title("Read Archive")
                    .build());
            SignalOsContentRegistry.registerArchive(TerminalArchiveRecord.builder(unreadArchiveId)
                    .chapter(chapterId.toString())
                    .title("Unread Archive")
                    .build());

            SignalOsPlayerData.markMissionClaimed(player, claimedMissionId);
            SignalOsPlayerData.markArchiveRead(player, readArchiveId);

            SignalOsTerminalStatePacket state = SignalOsTerminalStatePacket.createForTests(
                    player,
                    mission -> mission.id().equals(completeMissionId),
                    7);

            helper.assertTrue(state.completedMissions().contains(completeMissionId),
                    "Terminal state should include server-resolved completed missions.");
            helper.assertFalse(state.completedMissions().contains(claimedMissionId),
                    "Terminal state should not mark unresolved missions complete just because they are claimed.");
            helper.assertTrue(state.claimedMissions().contains(claimedMissionId),
                    "Terminal state should include persisted claimed missions.");
            helper.assertTrue(state.readArchives().contains(readArchiveId),
                    "Terminal state should include persisted read archives.");
            helper.assertFalse(state.readArchives().contains(unreadArchiveId),
                    "Terminal state should omit unread archives.");
            helper.assertTrue(state.pendingRewardCount() == 7,
                    "Terminal state should carry the authoritative pending reward count.");
        });
        helper.succeed();
    }

    private static void terminalMenuValidity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SignalOsTerminalMenu remoteMenu = new SignalOsTerminalMenu(1, player.getInventory());
        helper.assertTrue(remoteMenu.stillValid(player),
                "Key-opened SignalOS terminal menus should use remote access.");

        BlockPos emptyPos = helper.absolutePos(new BlockPos(3, 1, 3));
        SignalOsTerminalMenu missingBlockMenu = new SignalOsTerminalMenu(2, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), emptyPos));
        helper.assertFalse(missingBlockMenu.stillValid(player),
                "Block-opened SignalOS terminal menus should require a valid terminal block.");

        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.TERMINAL.get());
        BlockPos absolute = helper.absolutePos(terminalPos);
        player.setPos(absolute.getX() + 0.5D, absolute.getY() + 0.5D, absolute.getZ() + 0.5D);
        SignalOsTerminalMenu blockMenu = new SignalOsTerminalMenu(3, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), absolute));
        helper.assertTrue(blockMenu.stillValid(player),
                "Block-opened SignalOS terminal menus should stay valid near their terminal block.");

        BlockPos workstationPos = new BlockPos(2, 1, 1);
        helper.setBlock(workstationPos, ModBlocks.WORKSTATION.get());
        BlockPos workstationAbsolute = helper.absolutePos(workstationPos);
        player.setPos(workstationAbsolute.getX() + 0.5D, workstationAbsolute.getY() + 0.5D, workstationAbsolute.getZ() + 0.5D);
        SignalOsTerminalMenu workstationMenu = new SignalOsTerminalMenu(4, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), workstationAbsolute));
        helper.assertTrue(workstationMenu.stillValid(player),
                "Block-opened SignalOS terminal menus should accept workstation access blocks.");
        helper.succeed();
    }

    private static void appRegistryAndData(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SignalOsContentRegistry.withClearedForTests(() -> {
            Identifier appA = testId("app_a");
            Identifier appB = testId("app_b");
            SignalOsContentRegistry.registerApp(SignalOsApp.builder(appB).title("B").order(20).build());
            SignalOsContentRegistry.registerApp(SignalOsApp.builder(appA).title("A").order(10).build());
            helper.assertTrue(SignalOsContentRegistry.apps().getFirst().id().equals(appA),
                    "SignalOS apps should sort by order before id.");
            try {
                SignalOsContentRegistry.registerApp(SignalOsApp.builder(appA).title("Duplicate").build());
                helper.fail("Duplicate SignalOS app ids should be rejected.");
            } catch (IllegalArgumentException expected) {
                helper.assertTrue(expected.getMessage().contains("Duplicate SignalOS app id"),
                        "Duplicate app exception should name the app surface.");
            }
            Identifier recordId = testId("record/provider");
            SignalOsContentRegistry.registerDataProvider(new com.knoxhack.signalos.api.SignalOsDataProvider() {
                @Override
                public Identifier id() {
                    return testId("provider");
                }

                @Override
                public List<SignalOsDataRecord> records(Player ignored) {
                    return List.of(new SignalOsDataRecord(recordId, "Provider Record", "record", "test", "ok", 0, false));
                }
            });
            helper.assertTrue(SignalOsContentRegistry.dataRecords(player).stream()
                            .anyMatch(record -> record.id().equals(recordId)),
                    "SignalOS data providers should feed desktop records.");
        });
        helper.succeed();
    }

    private static void dataDriveComponentFlow(GameTestHelper helper) {
        Identifier recordId = testId("drive/record");
        SignalOsDriveData driveData = new SignalOsDriveData("Test Drive", List.of(
                new SignalOsDataRecord(recordId, "Drive Record", "record", "test", "stored", 0, false)));
        ItemStack stack = new ItemStack(ModBlocks.DATA_DRIVE.get());
        stack.set(ModDataComponents.DRIVE_DATA.get(), driveData);
        helper.assertTrue(stack.get(ModDataComponents.DRIVE_DATA.get()).records().getFirst().id().equals(recordId),
                "SignalOS data drives should persist typed records through their data component.");
        helper.succeed();
    }

    private static void computerNetworkDiscovery(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos workstation = new BlockPos(1, 1, 1);
        BlockPos rackPos = new BlockPos(3, 1, 1);
        BlockPos relayPos = new BlockPos(4, 1, 1);
        helper.setBlock(workstation, ModBlocks.WORKSTATION.get());
        helper.setBlock(rackPos, ModBlocks.SERVER_RACK.get());
        helper.setBlock(relayPos, ModBlocks.NETWORK_RELAY.get());

        BlockPos workstationAbsolute = helper.absolutePos(workstation);
        player.setPos(workstationAbsolute.getX() + 0.5D, workstationAbsolute.getY() + 0.5D, workstationAbsolute.getZ() + 0.5D);
        if (helper.getLevel().getBlockEntity(workstationAbsolute) instanceof SignalOsTerminalBlockEntity terminal) {
            terminal.setOwnerIfMissing(player);
            SignalOsTerminalServices.rememberTerminal(player, workstationAbsolute);
        } else {
            helper.fail("Workstation should create a SignalOS terminal block entity.");
            return;
        }

        Identifier recordId = testId("network/drive_record");
        ItemStack drive = new ItemStack(ModBlocks.DATA_DRIVE.get());
        drive.set(ModDataComponents.DRIVE_DATA.get(), new SignalOsDriveData("Rack Drive", List.of(
                new SignalOsDataRecord(recordId, "Rack Record", "record", "test", "rack", 0, false))));
        if (helper.getLevel().getBlockEntity(helper.absolutePos(rackPos)) instanceof SignalOsServerRackBlockEntity rack) {
            helper.assertTrue(rack.insertDrive(drive), "Server rack should accept SignalOS data drives.");
        } else {
            helper.fail("Server rack should create a SignalOS rack block entity.");
            return;
        }

        SignalOsComputerNetworkService.NetworkSnapshot snapshot = SignalOsComputerNetworkService.snapshot(player);
        helper.assertTrue(snapshot.online(), "Owned workstation should produce an online SignalOS network.");
        helper.assertTrue(snapshot.workstations() >= 1, "Network scan should count workstation access blocks.");
        helper.assertTrue(snapshot.serverRacks() >= 1, "Network scan should count server racks.");
        helper.assertTrue(snapshot.relays() >= 1, "Network scan should count network relays.");
        helper.assertTrue(snapshot.records().stream().anyMatch(record -> record.id().equals(recordId)),
                "Network scan should expose records from installed rack drives.");
        helper.succeed();
    }

    private static void noteEditingFlow(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        SignalOsBuiltinActions.register();
        JsonObject payload = new JsonObject();
        payload.addProperty("title", "Field Note");
        payload.addProperty("body", "Initial body");
        helper.assertTrue(TerminalActionRegistry.handle(player, SignalOsBuiltinActions.PAGE_NOTES,
                        SignalOsBuiltinActions.SAVE_NOTE, payload.toString()),
                "JSON note save action should be registered and accepted.");
        List<SignalOsDataRecord> notes = SignalOsPlayerData.notes(player);
        helper.assertTrue(notes.size() == 1, "JSON note save should create a note.");
        Identifier noteId = notes.getFirst().id();

        JsonObject update = new JsonObject();
        update.addProperty("id", noteId.toString());
        update.addProperty("title", "Updated Field Note");
        update.addProperty("body", "Updated body");
        helper.assertTrue(TerminalActionRegistry.handle(player, SignalOsBuiltinActions.PAGE_NOTES,
                        SignalOsBuiltinActions.SAVE_NOTE, update.toString()),
                "JSON note save should update an existing note by id.");
        List<SignalOsDataRecord> updated = SignalOsPlayerData.notes(player);
        helper.assertTrue(updated.size() == 1 && "Updated Field Note".equals(updated.getFirst().title()),
                "Note update should preserve one note and replace title/body.");

        helper.assertTrue(TerminalActionRegistry.handle(player, SignalOsBuiltinActions.PAGE_NOTES,
                        SignalOsBuiltinActions.SAVE_NOTE, "Legacy Title\nLegacy body"),
                "Legacy newline note payload should remain accepted.");
        helper.assertTrue(SignalOsPlayerData.notes(player).size() == 2,
                "Legacy note payload should create a second note.");
        helper.assertTrue(TerminalActionRegistry.handle(player, SignalOsBuiltinActions.PAGE_NOTES,
                        SignalOsBuiltinActions.DELETE_NOTE, noteId.toString()),
                "Delete note action should be accepted.");
        helper.assertFalse(SignalOsPlayerData.notes(player).stream().anyMatch(note -> note.id().equals(noteId)),
                "Deleted note should be removed from player data.");
        TerminalActionRegistry.handle(player, SignalOsBuiltinActions.PAGE_NOTES,
                SignalOsBuiltinActions.CLEAR_NOTES, "");
        helper.assertTrue(SignalOsPlayerData.notes(player).isEmpty(), "Clear notes action should remove all notes.");
        helper.succeed();
    }

    private static void serverRackMenuActions(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos workstation = new BlockPos(1, 1, 1);
        BlockPos rackPos = new BlockPos(3, 1, 1);
        helper.setBlock(workstation, ModBlocks.WORKSTATION.get());
        helper.setBlock(rackPos, ModBlocks.SERVER_RACK.get());
        BlockPos workstationAbsolute = helper.absolutePos(workstation);
        if (helper.getLevel().getBlockEntity(workstationAbsolute) instanceof SignalOsTerminalBlockEntity terminal) {
            terminal.setOwnerIfMissing(player);
            SignalOsTerminalServices.rememberTerminal(player, workstationAbsolute);
        }
        SignalOsServerRackBlockEntity rack =
                (SignalOsServerRackBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(rackPos));
        helper.assertTrue(rack != null, "Server rack should create a rack block entity.");
        BlockPos rackAbsolute = helper.absolutePos(rackPos);
        player.setPos(rackAbsolute.getX() + 0.5D, rackAbsolute.getY() + 0.5D, rackAbsolute.getZ() + 0.5D);

        ItemStack playerDrive = new ItemStack(ModBlocks.DATA_DRIVE.get());
        player.getInventory().setItem(0, playerDrive);
        SignalOsServerRackMenu menu = new SignalOsServerRackMenu(1, player.getInventory(), rack);
        int playerDriveMenuSlot = -1;
        for (int i = SignalOsServerRackMenu.DRIVE_SLOT_COUNT; i < menu.slots.size(); i++) {
            if (menu.slots.get(i).getItem().is(ModBlocks.DATA_DRIVE.get())) {
                playerDriveMenuSlot = i;
                break;
            }
        }
        helper.assertTrue(playerDriveMenuSlot >= 0, "Rack menu should expose player inventory drive slots.");
        helper.assertTrue(!menu.quickMoveStack(player, playerDriveMenuSlot).isEmpty(),
                "Quick-moving a data drive from player inventory should succeed.");
        helper.assertTrue(rack.drives().getItem(0).is(ModBlocks.DATA_DRIVE.get()),
                "Quick-moved data drive should land in the rack drive bays.");
        player.getInventory().setItem(1, new ItemStack(Items.DIRT));
        int dirtMenuSlot = -1;
        for (int i = SignalOsServerRackMenu.DRIVE_SLOT_COUNT; i < menu.slots.size(); i++) {
            if (menu.slots.get(i).getItem().is(Items.DIRT)) {
                dirtMenuSlot = i;
                break;
            }
        }
        helper.assertTrue(menu.quickMoveStack(player, dirtMenuSlot).isEmpty(),
                "Quick-moving non-drive items into rack bays should be rejected.");

        Identifier recordId = testId("rack/source_record");
        Identifier templateId = testId("template/field");
        SignalOsContentRegistry.withClearedForTests(() -> {
            SignalOsContentRegistry.registerDataProvider(new SignalOsDataProvider() {
                @Override
                public Identifier id() {
                    return testId("rack_provider");
                }

                @Override
                public List<SignalOsDataRecord> records(Player ignored) {
                    return List.of(new SignalOsDataRecord(recordId, "Source Record", "record", "test",
                            "copy me", 0, false));
                }
            });
            SignalOsContentRegistry.replaceJsonContent(new SignalOsContentRegistry.LoadedContent(
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    Map.of(templateId, new SignalOsDriveData("Template Drive", List.of(
                            new SignalOsDataRecord(testId("template/record"), "Template Record", "record",
                                    "template", "templated", 0, false)))),
                    SignalOsContentRegistry.LoadReport.empty()));

            player.containerMenu = menu;
            helper.assertTrue(SignalOsRackActions.handle(player,
                            new SignalOsRackActionPacket(helper.absolutePos(rackPos), 0,
                                    SignalOsRackActions.COPY_RECORD, recordId.toString())),
                    "Rack copy action should write a selected network record to the selected drive.");
            helper.assertTrue(SignalOsDataDriveItem.data(rack.drives().getItem(0)).records().stream()
                            .anyMatch(record -> record.id().equals(recordId)),
                    "Copied network record should persist on the data drive component.");
            helper.assertTrue(SignalOsRackActions.handle(player,
                            new SignalOsRackActionPacket(helper.absolutePos(rackPos), 0,
                                    SignalOsRackActions.APPLY_TEMPLATE, templateId.toString())),
                    "Rack template action should merge loaded drive template records.");
            helper.assertTrue(SignalOsDataDriveItem.data(rack.drives().getItem(0)).records().stream()
                            .anyMatch(record -> record.id().equals(testId("template/record"))),
                    "Applied template record should persist on the data drive.");
            helper.assertTrue(SignalOsRackActions.handle(player,
                            new SignalOsRackActionPacket(helper.absolutePos(rackPos), 0,
                                    SignalOsRackActions.REMOVE_RECORD, recordId.toString())),
                    "Rack remove action should delete a selected drive record.");
            helper.assertFalse(SignalOsDataDriveItem.data(rack.drives().getItem(0)).records().stream()
                            .anyMatch(record -> record.id().equals(recordId)),
                    "Removed record should no longer be present on the drive.");
            helper.assertTrue(SignalOsRackActions.handle(player,
                            new SignalOsRackActionPacket(helper.absolutePos(rackPos), 0,
                                    SignalOsRackActions.RENAME_DRIVE, "Renamed Drive")),
                    "Rack rename action should update the drive label.");
            helper.assertTrue("Renamed Drive".equals(SignalOsDataDriveItem.data(rack.drives().getItem(0)).label()),
                    "Renamed drive label should persist on the component.");
            helper.assertTrue(SignalOsRackActions.handle(player,
                            new SignalOsRackActionPacket(helper.absolutePos(rackPos), 0,
                                    SignalOsRackActions.CLEAR_DRIVE, "")),
                    "Rack clear action should be accepted.");
            helper.assertTrue(SignalOsDataDriveItem.data(rack.drives().getItem(0)).records().isEmpty(),
                    "Clear action should remove drive records.");
        });
        helper.succeed();
    }

    private static void customAppRecordView(GameTestHelper helper) {
        JsonObject json = new JsonObject();
        json.addProperty("title", "Filtered Records");
        json.addProperty("type", "field_records");
        json.addProperty("view", "records");
        JsonArray types = new JsonArray();
        types.add("record");
        json.add("recordTypes", types);
        JsonArray sources = new JsonArray();
        sources.add("SignalOS Core");
        json.add("recordSources", sources);
        json.addProperty("includeArchived", true);
        json.addProperty("emptyText", "No filtered records");
        SignalOsApp app = SignalOsJsonContentLoader.parseAppForTests(testId("filtered_app"), json);
        helper.assertTrue("records".equals(app.view()), "Custom app JSON should parse record view mode.");
        helper.assertTrue(app.recordTypes().contains("record"), "Custom app JSON should parse record type filters.");
        helper.assertTrue(app.recordSources().contains("signalos core"),
                "Custom app JSON should parse source filters case-insensitively.");
        helper.assertTrue(app.includeArchived(), "Custom app JSON should parse includeArchived.");
        helper.assertTrue("No filtered records".equals(app.emptyText()),
                "Custom app JSON should parse empty view text.");
        helper.succeed();
    }

    private static void missionCoreContentRegistration(GameTestHelper helper) {
        InMemoryMissionRegistry registry = new InMemoryMissionRegistry();
        SignalOsMissionCoreIntegration.registerContent(registry);
        helper.assertTrue(registry.chapter(id("signalos")).isPresent(), "SignalOS MissionCore chapter should be owned by SignalOS.");
        assertMission(helper, registry, "boot_terminal", "boot", MissionObjectiveType.SCAN_BLOCK);
        assertMission(helper, registry, "rack_network_online", "rack", MissionObjectiveType.ESTABLISH_ROUTE);
        assertMission(helper, registry, "drive_record_flow", "record", MissionObjectiveType.UNLOCK_RESEARCH);
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
        helper.assertTrue(mission.kind() == MissionKind.SIDE_OP, "SignalOS MissionCore missions should be side ops.");
        helper.assertTrue(!mission.rewards().isEmpty(), "SignalOS MissionCore mission should have a claimable reward: " + missionId);
        helper.assertTrue(mission.objectives().size() == 1, "SignalOS MissionCore mission should have one direct objective: " + missionId);
        helper.assertTrue(mission.objectives().getFirst().type() == type, "SignalOS objective type should stay stable: " + missionId);
        String target = mission.objectives().getFirst().criteria().get("target");
        helper.assertTrue(MissionHookTargets.objectiveTarget(SignalOS.MODID, missionId, objectiveKey).toString().equals(target),
                "SignalOS MissionCore objective target should use MissionHookTargets: " + missionId);
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            String testName, Identifier functionId) {
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
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SignalOS.MODID, path);
    }

    private static Identifier testId(String path) {
        return Identifier.fromNamespaceAndPath("signalos_test", path);
    }
}
