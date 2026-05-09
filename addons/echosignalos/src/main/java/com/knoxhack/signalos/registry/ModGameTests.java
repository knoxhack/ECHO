package com.knoxhack.signalos.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.TerminalPlacementService;
import com.knoxhack.echocore.api.TerminalRewardService;
import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalChapter;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import com.knoxhack.signalos.content.SignalOsJsonContentLoader;
import com.knoxhack.signalos.kubejs.SignalOSKubeBridge;
import com.knoxhack.signalos.kubejs.SignalOSEvents;
import com.knoxhack.signalos.menu.SignalOsTerminalMenu;
import com.knoxhack.signalos.network.SignalOsTerminalStatePacket;
import com.knoxhack.signalos.service.SignalOsBuiltinActions;
import com.knoxhack.signalos.service.SignalOsPlayerData;
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
                            orphanMission, TerminalMission.builder(orphanMission).chapter(testId("missing").toString()).build()),
                    Map.of(
                            okArchive, TerminalArchiveRecord.builder(okArchive).chapter(jsonChapter.toString()).build(),
                            orphanArchive, TerminalArchiveRecord.builder(orphanArchive).chapter(testId("missing").toString()).build()),
                    new SignalOsContentRegistry.LoadReport(5, 5, 0, 0, 0));

            SignalOsContentRegistry.LoadedContent validated =
                    SignalOsJsonContentLoader.validateReferencesForTests(loaded);
            helper.assertTrue(validated.missions().containsKey(okMission),
                    "JSON missions should keep references to JSON chapters loaded in the same pass.");
            helper.assertTrue(validated.missions().containsKey(javaMission),
                    "JSON missions should keep references to already-registered Java chapters.");
            helper.assertFalse(validated.missions().containsKey(orphanMission),
                    "JSON missions with missing chapters should be skipped.");
            helper.assertTrue(validated.archives().containsKey(okArchive),
                    "JSON archives should keep references to JSON chapters loaded in the same pass.");
            helper.assertFalse(validated.archives().containsKey(orphanArchive),
                    "JSON archives with missing chapters should be skipped.");
            helper.assertTrue(validated.report().rejectedReferences() == 2,
                    "JSON load report should count skipped missing-chapter references.");
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
