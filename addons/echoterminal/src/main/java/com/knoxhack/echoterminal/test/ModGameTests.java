package com.knoxhack.echoterminal.test;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalNavigationSection;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import com.knoxhack.echoterminal.mission.MainSurvivalQuestProvider;
import com.knoxhack.echoterminal.mission.VanillaJourneyData;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import com.knoxhack.echoterminal.network.TerminalActionPacket;
import com.knoxhack.echoterminal.registry.ModBlocks;
import com.knoxhack.echoterminal.service.EchoTerminalCoreServices;
import com.knoxhack.echocore.api.EchoCoreServices;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoTerminal.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_API_IDS =
            TEST_FUNCTIONS.register("terminal_api_ids", () -> ModGameTests::terminalApiIds);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_TAB_REGISTRY =
            TEST_FUNCTIONS.register("terminal_tab_registry", () -> ModGameTests::terminalTabRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_REGISTRY =
            TEST_FUNCTIONS.register("terminal_mission_registry", () -> ModGameTests::terminalMissionRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_NAVIGATION_PROFILES =
            TEST_FUNCTIONS.register("terminal_navigation_profiles", () -> ModGameTests::terminalNavigationProfiles);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_ACTION_ROUTING =
            TEST_FUNCTIONS.register("terminal_mission_action_routing", () -> ModGameTests::terminalMissionActionRouting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_LORE_TAXONOMY =
            TEST_FUNCTIONS.register("terminal_lore_taxonomy", () -> ModGameTests::terminalLoreTaxonomy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_EMPTY_PROVIDER_CONTRACTS =
            TEST_FUNCTIONS.register("terminal_empty_provider_contracts", () -> ModGameTests::terminalEmptyProviderContracts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MAIN_SURVIVAL_ROUTE =
            TEST_FUNCTIONS.register("terminal_main_survival_route", () -> ModGameTests::terminalMainSurvivalRoute);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REWARD_CACHE =
            TEST_FUNCTIONS.register("terminal_reward_cache", () -> ModGameTests::terminalRewardCache);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MENU_VALIDITY =
            TEST_FUNCTIONS.register("terminal_menu_validity", () -> ModGameTests::terminalMenuValidity);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_BASELINE_CACHE_CONTRACT =
            TEST_FUNCTIONS.register("terminal_baseline_cache_contract", () -> ModGameTests::terminalBaselineCacheContract);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("terminal_release"));
        register(event, environment, "terminal_api_ids", TERMINAL_API_IDS.getId());
        register(event, environment, "terminal_tab_registry", TERMINAL_TAB_REGISTRY.getId());
        register(event, environment, "terminal_mission_registry", TERMINAL_MISSION_REGISTRY.getId());
        register(event, environment, "terminal_navigation_profiles", TERMINAL_NAVIGATION_PROFILES.getId());
        register(event, environment, "terminal_mission_action_routing", TERMINAL_MISSION_ACTION_ROUTING.getId());
        register(event, environment, "terminal_lore_taxonomy", TERMINAL_LORE_TAXONOMY.getId());
        register(event, environment, "terminal_empty_provider_contracts", TERMINAL_EMPTY_PROVIDER_CONTRACTS.getId());
        register(event, environment, "terminal_main_survival_route", TERMINAL_MAIN_SURVIVAL_ROUTE.getId());
        register(event, environment, "terminal_reward_cache", TERMINAL_REWARD_CACHE.getId());
        register(event, environment, "terminal_menu_validity", TERMINAL_MENU_VALIDITY.getId());
        register(event, environment, "terminal_baseline_cache_contract", TERMINAL_BASELINE_CACHE_CONTRACT.getId());
    }

    private static void terminalApiIds(GameTestHelper helper) {
        helper.assertTrue(TerminalActionPacket.ID.equals(id("terminal_action")),
                "Terminal action packet id must be echoterminal:terminal_action");
        TerminalActionRegistry.withClearedForTests(() -> {
            AtomicBoolean handled = new AtomicBoolean(false);
            TerminalActionRegistry.register(id("test_tab"), id("test_action"), (player, payload) -> handled.set(true));
            helper.assertTrue(TerminalActionRegistry.handle(null, id("test_tab"), id("test_action"), ""),
                    "Registered terminal action should be routed");
            helper.assertTrue(handled.get(), "Registered terminal action handler should run");
            TerminalActionRegistry.register(id("test_tab"), id("failing_action"), (player, payload) -> {
                throw new IllegalStateException("test terminal action failure");
            });
            helper.assertFalse(TerminalActionRegistry.handle(null, id("test_tab"), id("failing_action"), ""),
                    "Failing terminal action handlers should be logged and ignored");
        });
        helper.succeed();
    }

    private static void terminalTabRegistry(GameTestHelper helper) {
        TerminalTabRegistry.withClearedForTests(() -> {
            TerminalTabRegistry.register(new DummyTab(id("zeta"), "ZETA", 20));
            TerminalTabRegistry.register(new DummyTab(id("alpha"), "ALPHA", 10));
            TerminalTabRegistry.register(new DummyTab(id("beta"), "BETA", 10));

            helper.assertTrue(TerminalTabRegistry.tabs().size() == 3,
                    "Dynamic terminal registry should expose registered tabs");
            helper.assertTrue(TerminalTabRegistry.tabs().get(0).descriptor().id().equals(id("alpha")),
                    "Tabs with lower order should sort first by id");
            helper.assertTrue(TerminalTabRegistry.tabs().get(1).descriptor().id().equals(id("beta")),
                    "Tabs with equal order should sort by id");
            helper.assertTrue(TerminalTabRegistry.tabs().get(2).descriptor().id().equals(id("zeta")),
                    "Tabs with higher order should sort last");
        });
        helper.succeed();
    }

    private static void terminalMissionRegistry(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            TerminalMissionRegistry.register(new DummyMissionProvider(id("zeta_chapter"), 20, new AtomicBoolean(false)));
            TerminalMissionRegistry.register(new DummyMissionProvider(id("alpha_chapter"), 10, new AtomicBoolean(false)));

            helper.assertTrue(TerminalMissionRegistry.providers().size() == 2,
                    "Mission provider registry should expose registered providers");
            helper.assertTrue(TerminalMissionRegistry.providers().get(0).chapter().id().equals(id("alpha_chapter")),
                    "Mission providers should sort by order and id");
            TerminalMissionRegistry.register(new ThrowingChapterProvider());
            helper.assertTrue(TerminalMissionRegistry.providers().size() == 2,
                    "Mission providers with failing chapter metadata should be ignored");

            boolean duplicateRejected = false;
            try {
                TerminalMissionRegistry.register(new DummyMissionProvider(id("alpha_chapter"), 99, new AtomicBoolean(false)));
            } catch (IllegalArgumentException expected) {
                duplicateRejected = true;
            }
            helper.assertTrue(duplicateRejected, "Duplicate mission provider ids must fail fast");

            boolean uppercaseRejected = false;
            try {
                String badNamespace = "Echo" + "Terminal";
                new TerminalMissionChapter(Identifier.fromNamespaceAndPath(badNamespace, "bad"), "Bad", "", 0, 0xFFFFFFFF, true);
            } catch (RuntimeException expected) {
                uppercaseRejected = true;
            }
            helper.assertTrue(uppercaseRejected, "Mission chapter ids must reject uppercase namespaces");
        });
        helper.succeed();
    }

    private static void terminalNavigationProfiles(GameTestHelper helper) {
        TerminalNavigationProfiles.withClearedForTests(() -> {
            TerminalNavigationProfile command = TerminalNavigationProfiles.profileFor(
                    new DummyTab(id("overview"), "OVERVIEW", 0));
            helper.assertTrue(command.section() == TerminalNavigationSection.TERMINAL,
                    "Legacy protocol tabs should fall back into the Terminal section");

            TerminalNavigationProfile endgame = TerminalNavigationProfiles.profileFor(new DummyChromeTab(
                    new TerminalTabDescriptor(id("legacy_endgame"), "ENDGAME", 220, 0xFFC77DFF),
                    TerminalTabChrome.of("Legacy Endgame", TerminalTabChrome.GROUP_ENDGAME, "EG",
                            "Legacy finale", 220)));
            helper.assertTrue(endgame.section() == TerminalNavigationSection.CORE,
                    "Legacy endgame tabs should not create a standalone Endgame section");

            TerminalNavigationProfile nexus = TerminalNavigationProfiles.profileFor(new DummyChromeTab(
                    new TerminalTabDescriptor(id("legacy_nexus"), "NEXUS", 230, 0xFFC77DFF),
                    TerminalTabChrome.of("Legacy Nexus", TerminalTabChrome.GROUP_NEXUS, "NX",
                            "Legacy finale", 230)));
            helper.assertTrue(nexus.section() == TerminalNavigationSection.CORE,
                    "Nexus tabs require an explicit addon profile before they appear as beta chapter navigation");

            Identifier stationfall = id("stationfall");
            TerminalNavigationProfiles.register(stationfall,
                    TerminalNavigationProfile.chapter("stationfall", "Stationfall", "SF", 330));
            TerminalNavigationProfile stationProfile = TerminalNavigationProfiles.profile(stationfall).orElse(null);
            helper.assertTrue(stationProfile != null, "Registered navigation profiles should be discoverable");
            helper.assertTrue(stationProfile.section() == TerminalNavigationSection.CHAPTERS,
                    "Addon profiles should live in the Chapters section");
            helper.assertTrue("stationfall".equals(stationProfile.chapterId()),
                    "Addon profiles should keep their chapter workspace id");
        });
        helper.succeed();
    }

    private static void terminalMissionActionRouting(GameTestHelper helper) {
        TerminalActionRegistry.withClearedForTests(() -> TerminalMissionRegistry.withClearedForTests(() -> {
            AtomicBoolean handled = new AtomicBoolean(false);
            Identifier chapter = id("test_chapter");
            Identifier mission = id("test_mission");
            Identifier tab = id("mission_tab");
            TerminalMissionRegistry.register(new DummyMissionProvider(chapter, 1, handled));
            TerminalMissionActions.registerForTab(tab);

            boolean routed = TerminalActionRegistry.handle(null, tab, TerminalMissionActions.MISSION_ACTION,
                    TerminalMissionActions.payload(chapter, mission, "claim_reward"));
            helper.assertTrue(routed, "Generic mission action should route through TerminalActionRegistry");
            helper.assertTrue(handled.get(), "Mission provider should receive generic mission action payload");
        }));
        helper.succeed();
    }

    private static void terminalLoreTaxonomy(GameTestHelper helper) {
        TerminalTabChrome command = TerminalTabChrome.fromDescriptor(
                new TerminalTabDescriptor(id("overview"), "OVERVIEW", 0, 0xFF66D9FF));
        helper.assertTrue("Command Deck".equals(command.shortTitle()),
                "Overview descriptor should render as Command Deck");
        helper.assertTrue(TerminalTabChrome.GROUP_PROTOCOL.equals(command.group()),
                "Command Deck should live in PROTOCOL");

        TerminalTabChrome roadmap = TerminalTabChrome.fromDescriptor(
                new TerminalTabDescriptor(id("missions"), "MISSIONS", 100, 0xFF66D9FF));
        helper.assertTrue("Protocol Roadmap".equals(roadmap.shortTitle()),
                "Missions descriptor should render as Protocol Roadmap");
        helper.assertTrue(TerminalTabChrome.GROUP_PROTOCOL.equals(roadmap.group()),
                "Protocol Roadmap should live in PROTOCOL");

        TerminalTabChrome nexus = TerminalTabChrome.fromDescriptor(
                new TerminalTabDescriptor(id("nexus"), "NEXUS", 220, 0xFFC77DFF));
        helper.assertTrue("Nexus Core".equals(nexus.shortTitle()),
                "Nexus descriptor should render as Nexus Core");
        helper.assertTrue(TerminalTabChrome.GROUP_NEXUS.equals(nexus.group()),
                "Nexus Core should live in NEXUS");

        TerminalTabChrome orbital = TerminalTabChrome.fromDescriptor(
                new TerminalTabDescriptor(id("orbital"), "ORBITAL", 300, 0xFF66D9FF));
        helper.assertTrue("Orbital Command".equals(orbital.shortTitle()),
                "Orbital descriptor should render as Orbital Command");
        helper.assertTrue(TerminalTabChrome.GROUP_ORBITAL.equals(orbital.group()),
                "Owned orbital tabs should live in ORBITAL");
        helper.assertTrue(TerminalIcon.fromGroup(TerminalTabChrome.GROUP_ORBITAL) == TerminalIcon.ORBITAL,
                "ORBITAL group should use the orbital icon");
        helper.assertTrue(TerminalIcon.fromTitle("ECHO-0 Records") == TerminalIcon.ORBITAL,
                "ECHO-0 records should use the orbital icon");
        helper.succeed();
    }

    private static void terminalEmptyProviderContracts(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            TerminalMissionRegistry.register(new EmptyMissionProvider(id("empty_chapter"), 1));
            helper.assertTrue(TerminalMissionRegistry.providers().size() == 1,
                    "Terminal mission registry should keep empty providers registered");
            helper.assertTrue(TerminalMissionRegistry.providers().get(0).missions(null).isEmpty(),
                    "Empty mission providers should be valid for standalone installs");
            TerminalMissionSnapshot snapshot = TerminalMissionRegistry.providers().get(0)
                    .snapshot(null, id("missing_mission"));
            helper.assertTrue(snapshot.status() == TerminalMissionStatus.LOCKED,
                    "Empty provider snapshots should return stable locked state");
        });
        helper.succeed();
    }

    private static void terminalMainSurvivalRoute(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            TerminalMissionRegistry.register(MainSurvivalQuestProvider.INSTANCE);
            TerminalMissionRegistry.register(new ConfigurableMissionProvider(
                    VanillaJourneyProvider.CHAPTER_ID,
                    "Baseline",
                    1,
                    List.of(new ConfiguredMission(
                            Identifier.withDefaultNamespace("story/mine_stone"),
                            "Stone Age",
                            "Story",
                            "Story",
                            "Task",
                            TerminalMissionRole.MAIN,
                            TerminalMissionStatus.CLAIMABLE,
                            List.of(TerminalMissionAction.enabled("claim_reward", "CLAIM CACHE"))))));
            TerminalMissionRegistry.register(new ConfigurableMissionProvider(
                    Identifier.fromNamespaceAndPath("echoindustrialnexus", "industrial_nexus"),
                    "Industrial Nexus",
                    2,
                    List.of(new ConfiguredMission(
                            Identifier.fromNamespaceAndPath("echoindustrialnexus", "mission/reclaim_power"),
                            "Reclaim Power",
                            "Stage 1",
                            "Factory",
                            "Production",
                            TerminalMissionRole.MAIN,
                            TerminalMissionStatus.UNLOCKED,
                            List.of(TerminalMissionAction.enabled("scan_factory", "SCAN FACTORY"))))));
            TerminalMissionRegistry.register(new ConfigurableMissionProvider(
                    id("reference_chapter"),
                    "Reference Chapter",
                    3,
                    List.of(new ConfiguredMission(
                            id("field_reference"),
                            "Field Reference",
                            "Reference",
                            "Reference",
                            "View",
                            TerminalMissionRole.REFERENCE,
                            TerminalMissionStatus.VIEW_ONLY,
                            List.of()))));
            TerminalMissionRegistry.register(new ThrowingMissionsProvider(id("throwing_missions"), 4));

            List<TerminalMissionDefinition> missions = MainSurvivalQuestProvider.INSTANCE.missions(null);
            helper.assertTrue(missions.stream()
                            .allMatch(definition -> MainSurvivalQuestProvider.CHAPTER_ID.equals(definition.chapterId())),
                    "Survival route definitions should render through the aggregate chapter");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Stone Age".equals(definition.title())),
                    "Survival route should include authored vanilla records");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Reclaim Power".equals(definition.title())),
                    "Survival route should include installed addon main records");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Other Signals".equals(definition.phaseTitle())
                            && "Field Reference".equals(definition.title())),
                    "Survival route should include remaining MAIN/REFERENCE records in Other Signals");
            long distinctMissionIds = missions.stream().map(TerminalMissionDefinition::id).distinct().count();
            helper.assertTrue(distinctMissionIds == missions.size(),
                    "Survival route should not duplicate authored records in Other Signals");
            helper.assertTrue(missions.stream()
                            .map(definition -> MainSurvivalQuestProvider.INSTANCE.snapshot(null, definition.id()))
                            .allMatch(snapshot -> snapshot.actions().isEmpty()),
                    "Survival route should expose passive source guidance instead of fake commands");
        });
        helper.succeed();
    }

    private static void terminalRewardCache(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.ECHO_TERMINAL_BLOCK.get());
        EchoTerminalBlockEntity terminal = helper.getBlockEntity(terminalPos, EchoTerminalBlockEntity.class);
        terminal.setOwnerIfMissing(player);
        terminal.storeRewards("test_reward", List.of(new ItemStack(Items.BREAD, 3)));

        helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(player) == 0,
                "Render-facing reward lookup should not scan when no terminal is cached");
        EchoTerminalCoreServices.rememberTerminal(player, helper.absolutePos(terminalPos));
        helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(player) == 3,
                "Render-facing reward lookup should read a remembered owned terminal");
        helper.succeed();
    }

    private static void terminalMenuValidity(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalMenu remoteMenu = new EchoTerminalMenu(1, player.getInventory());
        helper.assertTrue(remoteMenu.stillValid(player), "Key-opened terminal menus should use virtual access");

        BlockPos emptyPos = helper.absolutePos(new BlockPos(3, 1, 3));
        EchoTerminalMenu missingBlockMenu = new EchoTerminalMenu(2, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), emptyPos));
        helper.assertFalse(missingBlockMenu.stillValid(player), "Block-opened terminal menus should require a valid block");

        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.ECHO_TERMINAL_BLOCK.get());
        BlockPos absolute = helper.absolutePos(terminalPos);
        player.setPos(absolute.getX() + 0.5D, absolute.getY() + 0.5D, absolute.getZ() + 0.5D);
        EchoTerminalMenu blockMenu = new EchoTerminalMenu(3, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), absolute));
        helper.assertTrue(blockMenu.stillValid(player), "Block-opened terminal menus should stay valid near their block");
        helper.succeed();
    }

    private static void terminalBaselineCacheContract(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Identifier missionId = Identifier.withDefaultNamespace("story/mine_stone");
        VanillaJourneyData data = VanillaJourneyData.get(player);
        data.setCompleted(List.of(missionId));
        TerminalMissionSnapshot claimable = VanillaJourneyProvider.INSTANCE.snapshot(player, missionId);
        helper.assertTrue(claimable.status() == TerminalMissionStatus.CLAIMABLE,
                "Completed Baseline records should expose a claimable cache state");
        helper.assertTrue(claimable.actions().stream().anyMatch(action -> action.enabled()
                        && "claim_reward".equals(action.id()) && "CLAIM CACHE".equals(action.label())),
                "Completed Baseline records should expose a cache claim action");

        data.markClaimed(missionId);
        TerminalMissionSnapshot claimed = VanillaJourneyProvider.INSTANCE.snapshot(player, missionId);
        helper.assertTrue(claimed.status() == TerminalMissionStatus.CLAIMED,
                "Claimed Baseline records should stay claimed");
        helper.assertTrue(claimed.actions().stream().anyMatch(action -> !action.enabled()
                        && action.disabledReason().contains("already claimed")),
                "Claimed Baseline records should explain that the cache is already claimed");
        helper.succeed();
    }

    private record DummyTab(TerminalTabDescriptor descriptor) implements TerminalTab {
        DummyTab(Identifier id, String title, int order) {
            this(new TerminalTabDescriptor(id, title, order, 0xFF66D9FF));
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        }
    }

    private record DummyChromeTab(TerminalTabDescriptor descriptor, TerminalTabChrome chrome) implements TerminalTab {
        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        }
    }

    private record DummyMissionProvider(Identifier chapterId, int order, AtomicBoolean handled) implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, chapterId.getPath(), "Test provider", order, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return List.of(new TerminalMissionDefinition(
                    id("test_mission"),
                    chapterId,
                    "test",
                    "Test",
                    0,
                    1,
                    "Test Mission",
                    "Test briefing",
                    "Test field guide",
                    "Test",
                    "Test",
                    ItemStack.EMPTY,
                    List.of(),
                    List.of(),
                    List.of()));
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return new TerminalMissionSnapshot(
                    missionId,
                    TerminalMissionStatus.UNLOCKED,
                    0.0F,
                    "UNLOCKED",
                    "",
                    "Test",
                    List.of(TerminalMissionAction.enabled("claim_reward", "CLAIM")));
        }

        @Override
        public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
            handled.set(true);
            return true;
        }
    }

    private record EmptyMissionProvider(Identifier chapterId, int order) implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, chapterId.getPath(), "Empty provider", order, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return List.of();
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return new TerminalMissionSnapshot(
                    missionId,
                    TerminalMissionStatus.LOCKED,
                    0.0F,
                    "LOCKED",
                    "No mission records registered.",
                    "Install or enable a chapter provider.",
                    List.of());
        }
    }

    private record ConfiguredMission(
            Identifier id,
            String title,
            String phase,
            String category,
            String difficulty,
            TerminalMissionRole role,
            TerminalMissionStatus status,
            List<TerminalMissionAction> actions) {
    }

    private record ConfigurableMissionProvider(
            Identifier chapterId,
            String title,
            int order,
            List<ConfiguredMission> configuredMissions) implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, title, "Configurable test provider", order, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return configuredMissions.stream()
                    .map(mission -> new TerminalMissionDefinition(
                            mission.id(),
                            chapterId,
                            mission.phase().toLowerCase(java.util.Locale.ROOT).replace(' ', '_'),
                            mission.phase(),
                            0,
                            configuredMissions.indexOf(mission),
                            mission.title(),
                            mission.title() + " briefing",
                            mission.title() + " guide",
                            mission.category(),
                            mission.difficulty(),
                            ItemStack.EMPTY,
                            List.of(),
                            List.of(),
                            List.of()))
                    .toList();
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            ConfiguredMission mission = configuredMissions.stream()
                    .filter(candidate -> candidate.id().equals(missionId))
                    .findFirst()
                    .orElse(null);
            return mission == null
                    ? new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                            "LOCKED", "Missing test mission.", "Missing test mission.", List.of())
                    : new TerminalMissionSnapshot(mission.id(), mission.status(),
                            mission.status() == TerminalMissionStatus.CLAIMABLE ? 1.0F : 0.0F,
                            mission.status().name(), "", mission.title() + " next step", mission.actions());
        }

        @Override
        public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
            return configuredMissions.stream()
                    .filter(candidate -> candidate.id().equals(definition.id()))
                    .map(ConfiguredMission::role)
                    .findFirst()
                    .orElse(TerminalMissionRole.MAIN);
        }
    }

    private record ThrowingMissionsProvider(Identifier chapterId, int order) implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, "Throwing Missions", "Throws during missions", order,
                    0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            throw new IllegalStateException("test terminal mission list failure");
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "LOCKED", "Provider failed.", "Retry later.", List.of());
        }
    }

    private record ThrowingChapterProvider() implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            throw new IllegalStateException("test terminal mission chapter failure");
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return List.of();
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return new TerminalMissionSnapshot(
                    missionId,
                    TerminalMissionStatus.LOCKED,
                    0.0F,
                    "LOCKED",
                    "Provider failed.",
                    "Retry later.",
                    List.of());
        }
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
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, path);
    }
}
