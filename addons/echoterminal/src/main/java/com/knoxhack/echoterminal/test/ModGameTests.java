package com.knoxhack.echoterminal.test;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonGuide;
import com.knoxhack.echoterminal.api.TerminalAddonInfo;
import com.knoxhack.echoterminal.api.TerminalAddonInfoProvider;
import com.knoxhack.echoterminal.api.TerminalAddonInfoRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonLink;
import com.knoxhack.echoterminal.api.TerminalAddonMetric;
import com.knoxhack.echoterminal.api.TerminalAddonSection;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalNavigationSection;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.theme.BuiltinTerminalThemes;
import com.knoxhack.echoterminal.api.theme.TerminalChapterStyle;
import com.knoxhack.echoterminal.api.theme.TerminalIconKey;
import com.knoxhack.echoterminal.api.theme.TerminalIconSet;
import com.knoxhack.echoterminal.api.theme.TerminalTheme;
import com.knoxhack.echoterminal.api.theme.TerminalThemeContext;
import com.knoxhack.echoterminal.api.theme.TerminalThemeRegistry;
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
import com.knoxhack.echoterminal.client.BuiltinTerminalTabs;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import com.knoxhack.echoterminal.mission.MainSurvivalQuestProvider;
import com.knoxhack.echoterminal.mission.VanillaJourneyData;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import com.knoxhack.echoterminal.network.TerminalActionPacket;
import com.knoxhack.echoterminal.registry.ModBlocks;
import com.knoxhack.echoterminal.service.EchoTerminalCoreServices;
import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoCoreServices;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_ADDON_INFO_REGISTRY =
            TEST_FUNCTIONS.register("terminal_addon_info_registry", () -> ModGameTests::terminalAddonInfoRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_ADDON_GUIDE_ORDERING =
            TEST_FUNCTIONS.register("terminal_addon_guide_ordering", () -> ModGameTests::terminalAddonGuideOrdering);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_NAVIGATION_PROFILES =
            TEST_FUNCTIONS.register("terminal_navigation_profiles", () -> ModGameTests::terminalNavigationProfiles);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_RENDER_CONTEXT_NAVIGATION =
            TEST_FUNCTIONS.register("terminal_render_context_navigation", () -> ModGameTests::terminalRenderContextNavigation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_THEME_REGISTRY =
            TEST_FUNCTIONS.register("terminal_theme_registry", () -> ModGameTests::terminalThemeRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_THEME_ICON_FALLBACK =
            TEST_FUNCTIONS.register("terminal_theme_icon_fallback", () -> ModGameTests::terminalThemeIconFallback);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_THEME_CHAPTER_STYLE =
            TEST_FUNCTIONS.register("terminal_theme_chapter_style", () -> ModGameTests::terminalThemeChapterStyle);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_THEME_RESOURCES =
            TEST_FUNCTIONS.register("terminal_theme_resources", () -> ModGameTests::terminalThemeResources);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_THEME_SELECTION =
            TEST_FUNCTIONS.register("terminal_theme_selection", () -> ModGameTests::terminalThemeSelection);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_COMMAND_DECK_PRIORITY =
            TEST_FUNCTIONS.register("terminal_command_deck_priority", () -> ModGameTests::terminalCommandDeckPriority);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_ACTION_ROUTING =
            TEST_FUNCTIONS.register("terminal_mission_action_routing", () -> ModGameTests::terminalMissionActionRouting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_LORE_TAXONOMY =
            TEST_FUNCTIONS.register("terminal_lore_taxonomy", () -> ModGameTests::terminalLoreTaxonomy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_EMPTY_PROVIDER_CONTRACTS =
            TEST_FUNCTIONS.register("terminal_empty_provider_contracts", () -> ModGameTests::terminalEmptyProviderContracts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MAIN_SURVIVAL_ROUTE =
            TEST_FUNCTIONS.register("terminal_main_survival_route", () -> ModGameTests::terminalMainSurvivalRoute);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MAIN_SURVIVAL_ROUTE_CACHE =
            TEST_FUNCTIONS.register("terminal_main_survival_route_cache", () -> ModGameTests::terminalMainSurvivalRouteCache);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MAIN_SURVIVAL_ROUTE_BOUNDS =
            TEST_FUNCTIONS.register("terminal_main_survival_route_bounds", () -> ModGameTests::terminalMainSurvivalRouteBounds);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REWARD_CACHE =
            TEST_FUNCTIONS.register("terminal_reward_cache", () -> ModGameTests::terminalRewardCache);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REWARD_TRANSACTIONAL =
            TEST_FUNCTIONS.register("terminal_reward_transactional", () -> ModGameTests::terminalRewardTransactional);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REWARD_CLAIM_FLOW =
            TEST_FUNCTIONS.register("terminal_reward_claim_flow", () -> ModGameTests::terminalRewardClaimFlow);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REWARD_EXPLICIT_OWNER =
            TEST_FUNCTIONS.register("terminal_reward_explicit_owner", () -> ModGameTests::terminalRewardExplicitOwner);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MENU_VALIDITY =
            TEST_FUNCTIONS.register("terminal_menu_validity", () -> ModGameTests::terminalMenuValidity);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_BASELINE_CACHE_CONTRACT =
            TEST_FUNCTIONS.register("terminal_baseline_cache_contract", () -> ModGameTests::terminalBaselineCacheContract);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_BROWSER_CACHE =
            TEST_FUNCTIONS.register("terminal_mission_browser_cache", () -> ModGameTests::terminalMissionBrowserCache);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_BROWSER_PHASE_GATING =
            TEST_FUNCTIONS.register("terminal_mission_browser_phase_gating", () -> ModGameTests::terminalMissionBrowserPhaseGating);

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
        register(event, environment, "terminal_addon_info_registry", TERMINAL_ADDON_INFO_REGISTRY.getId());
        register(event, environment, "terminal_addon_guide_ordering", TERMINAL_ADDON_GUIDE_ORDERING.getId());
        register(event, environment, "terminal_navigation_profiles", TERMINAL_NAVIGATION_PROFILES.getId());
        register(event, environment, "terminal_render_context_navigation", TERMINAL_RENDER_CONTEXT_NAVIGATION.getId());
        register(event, environment, "terminal_theme_registry", TERMINAL_THEME_REGISTRY.getId());
        register(event, environment, "terminal_theme_icon_fallback", TERMINAL_THEME_ICON_FALLBACK.getId());
        register(event, environment, "terminal_theme_chapter_style", TERMINAL_THEME_CHAPTER_STYLE.getId());
        register(event, environment, "terminal_command_deck_priority", TERMINAL_COMMAND_DECK_PRIORITY.getId());
        register(event, environment, "terminal_mission_action_routing", TERMINAL_MISSION_ACTION_ROUTING.getId());
        register(event, environment, "terminal_lore_taxonomy", TERMINAL_LORE_TAXONOMY.getId());
        register(event, environment, "terminal_empty_provider_contracts", TERMINAL_EMPTY_PROVIDER_CONTRACTS.getId());
        register(event, environment, "terminal_main_survival_route", TERMINAL_MAIN_SURVIVAL_ROUTE.getId());
        register(event, environment, "terminal_main_survival_route_cache", TERMINAL_MAIN_SURVIVAL_ROUTE_CACHE.getId());
        register(event, environment, "terminal_main_survival_route_bounds", TERMINAL_MAIN_SURVIVAL_ROUTE_BOUNDS.getId());
        register(event, environment, "terminal_reward_cache", TERMINAL_REWARD_CACHE.getId());
        register(event, environment, "terminal_reward_transactional", TERMINAL_REWARD_TRANSACTIONAL.getId());
        register(event, environment, "terminal_reward_claim_flow", TERMINAL_REWARD_CLAIM_FLOW.getId());
        register(event, environment, "terminal_reward_explicit_owner", TERMINAL_REWARD_EXPLICIT_OWNER.getId());
        register(event, environment, "terminal_menu_validity", TERMINAL_MENU_VALIDITY.getId());
        register(event, environment, "terminal_baseline_cache_contract", TERMINAL_BASELINE_CACHE_CONTRACT.getId());
        register(event, environment, "terminal_mission_browser_cache", TERMINAL_MISSION_BROWSER_CACHE.getId());
        register(event, environment, "terminal_mission_browser_phase_gating", TERMINAL_MISSION_BROWSER_PHASE_GATING.getId());
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

    private static void terminalAddonInfoRegistry(GameTestHelper helper) {
        TerminalAddonInfoRegistry.withClearedForTests(() -> {
            TerminalAddonInfo alpha = new TerminalAddonInfo(
                    "  Alpha summary  ",
                    Arrays.asList(
                            new TerminalAddonMetric("  State  ", "  ONLINE  ", "  Live details  ", TerminalUi.GREEN),
                            null),
                    Arrays.asList(new TerminalAddonSection("  Notes  ", Arrays.asList("  first  ", "", null, "second")), null),
                    Arrays.asList(new TerminalAddonLink(id("alpha_tab"), "  Open Alpha  ", "  Linked page  ", TerminalUi.CYAN), null));
            TerminalAddonInfoRegistry.register(new DummyAddonInfoProvider("zeta_chapter", TerminalAddonInfo.empty()));
            TerminalAddonInfoRegistry.register(new DummyAddonInfoProvider("alpha_chapter", alpha));

            helper.assertTrue(TerminalAddonInfoRegistry.providers().size() == 2,
                    "Addon info registry should expose registered providers");
            helper.assertTrue("alpha_chapter".equals(TerminalAddonInfoRegistry.providers().get(0).chapterId()),
                    "Addon info providers should sort by chapter id");

            TerminalAddonInfo resolved = TerminalAddonInfoRegistry.info(" alpha_chapter ", null);
            helper.assertTrue("Alpha summary".equals(resolved.summary()),
                    "Addon info should normalize summary text");
            helper.assertTrue(resolved.metrics().size() == 1
                            && "State".equals(resolved.metrics().get(0).label())
                            && "ONLINE".equals(resolved.metrics().get(0).value()),
                    "Addon info metrics should be immutable and null-safe");
            helper.assertTrue(resolved.sections().size() == 1
                            && resolved.sections().get(0).lines().equals(List.of("first", "second")),
                    "Addon info sections should drop blank lines");
            helper.assertTrue(resolved.links().size() == 1 && resolved.links().get(0).targetTabId().equals(id("alpha_tab")),
                    "Addon info links should preserve lowercase terminal tab targets");
            helper.assertTrue(resolved.guide() == TerminalAddonGuide.empty(),
                    "Legacy addon info constructors should use an empty guide snapshot");

            TerminalAddonInfo guided = new TerminalAddonInfo(
                    "Guided",
                    null,
                    null,
                    null,
                    new TerminalAddonGuide("  Chapter 9  ", 90, "  Test stage  ", "  Test hint  ",
                            Arrays.asList("  step one  ", "", null), true));
            helper.assertTrue("Chapter 9".equals(guided.guide().label())
                            && guided.guide().mainline()
                            && guided.guide().starterSteps().equals(List.of("step one")),
                    "Addon guides should normalize text and starter steps");
            TerminalAddonInfo nullGuide = new TerminalAddonInfo("Null guide", null, null, null, null);
            helper.assertTrue(nullGuide.guide() == TerminalAddonGuide.empty(),
                    "Null addon guides should fall back to the empty guide snapshot");

            TerminalAddonInfoRegistry.register(new NullAddonInfoProvider("null_output"));
            helper.assertTrue(TerminalAddonInfoRegistry.info("null_output", null) == TerminalAddonInfo.empty(),
                    "Null provider output should fall back to the empty snapshot");

            TerminalAddonInfoRegistry.register(new ThrowingAddonInfoProvider("throwing_output"));
            helper.assertTrue(TerminalAddonInfoRegistry.info("throwing_output", null) == TerminalAddonInfo.empty(),
                    "Failing provider output should fall back to the empty snapshot");

            TerminalAddonInfoRegistry.register(new ThrowingAddonChapterIdProvider());
            helper.assertTrue(TerminalAddonInfoRegistry.providers().size() == 4,
                    "Providers with failing chapter ids should be ignored");

            boolean duplicateRejected = false;
            try {
                TerminalAddonInfoRegistry.register(new DummyAddonInfoProvider("alpha_chapter", TerminalAddonInfo.empty()));
            } catch (IllegalArgumentException expected) {
                duplicateRejected = true;
            }
            helper.assertTrue(duplicateRejected, "Duplicate addon info provider ids must fail fast");

            boolean uppercaseRejected = false;
            try {
                TerminalAddonInfoRegistry.register(new DummyAddonInfoProvider("Bad_Chapter", TerminalAddonInfo.empty()));
            } catch (IllegalArgumentException expected) {
                uppercaseRejected = true;
            }
            helper.assertTrue(uppercaseRejected, "Addon info provider chapter ids must reject uppercase");
        });
        helper.succeed();
    }

    private static void terminalAddonGuideOrdering(GameTestHelper helper) {
        TerminalAddonInfoRegistry.withClearedForTests(() -> {
            List<String> order = BuiltinTerminalTabs.addonGuideOrderForTests(List.of(
                    new DummyAddonChapter("unknown_alpha", "ECHO: Unknown Alpha"),
                    new DummyAddonChapter("blackbox_protocol", "ECHO: Blackbox Protocol"),
                    new DummyAddonChapter("industrial_nexus", "ECHO: Industrial Nexus"),
                    new DummyAddonChapter("ashfall_protocol", "ECHO: Ashfall Protocol"),
                    new DummyAddonChapter("stationfall", "ECHO: Stationfall"),
                    new DummyAddonChapter("orbital_remnants", "ECHO: Orbital Remnants"),
                    new DummyAddonChapter("nexus_protocol", "ECHO: Nexus Protocol")));
            helper.assertTrue(order.equals(List.of(
                            "Chapter 1|ashfall_protocol",
                            "Chapter 2|orbital_remnants",
                            "Chapter 3|stationfall",
                            "Chapter 4|nexus_protocol",
                            "Chapter 5|blackbox_protocol",
                            "Optional|industrial_nexus",
                            "Optional|unknown_alpha")),
                    "Chapter guide should sort story chapters before optional and unknown addons");
            TerminalAddonGuide industrial = BuiltinTerminalTabs.addonGuideForTests("industrial_nexus");
            helper.assertTrue("Optional".equals(industrial.label()) && !industrial.mainline(),
                    "Industrial Nexus should remain an optional chapter guide entry");
        });
        helper.succeed();
    }

    private static void terminalNavigationProfiles(GameTestHelper helper) {
        TerminalNavigationProfiles.withClearedForTests(() -> {
            helper.assertTrue(TerminalNavigationSection.storyFirstOrder().equals(List.of(
                            TerminalNavigationSection.CHAPTERS,
                            TerminalNavigationSection.TERMINAL,
                            TerminalNavigationSection.CORE)),
                    "Terminal navigation should render story chapters before utility sections");

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

            TerminalNavigationProfiles.register(MainSurvivalQuestProvider.TAB_ID,
                    TerminalNavigationProfile.chaptersHub(0));
            TerminalNavigationProfile survivalRoute =
                    TerminalNavigationProfiles.profile(MainSurvivalQuestProvider.TAB_ID).orElse(null);
            helper.assertTrue(survivalRoute != null
                            && survivalRoute.section() == TerminalNavigationSection.CHAPTERS,
                    "Survival Route should be the main Chapters section destination");

            Identifier addons = id("addons");
            TerminalNavigationProfiles.register(addons, TerminalNavigationProfile.core(150));
            TerminalNavigationProfile chapterStatus = TerminalNavigationProfiles.profile(addons).orElse(null);
            helper.assertTrue(chapterStatus != null
                            && chapterStatus.section() == TerminalNavigationSection.CORE,
                    "Chapter Guide should live in the Core section while preserving the addons tab id");

            Identifier stationfall = id("stationfall");
            TerminalNavigationProfiles.register(stationfall,
                    TerminalNavigationProfile.chapter("stationfall", "Chapter 3: Stationfall", "C3", 330));
            TerminalNavigationProfile stationProfile = TerminalNavigationProfiles.profile(stationfall).orElse(null);
            helper.assertTrue(stationProfile != null, "Registered navigation profiles should be discoverable");
            helper.assertTrue(stationProfile.section() == TerminalNavigationSection.CHAPTERS,
                    "Addon profiles should live in the Chapters section");
            helper.assertTrue("stationfall".equals(stationProfile.chapterId()),
                    "Addon profiles should keep their chapter workspace id");
            helper.assertTrue("Chapter 3: Stationfall".equals(stationProfile.chapterTitle()),
                    "Addon profiles should expose numbered chapter titles");
        });
        helper.succeed();
    }

    private static void terminalRenderContextNavigation(GameTestHelper helper) {
        Identifier target = id("target_tab");
        List<Identifier> visited = new ArrayList<>();
        TerminalRenderContext inert = new TerminalRenderContext(null, null,
                0, 0, 0, 0, 0, 0, 0, null, null);
        helper.assertFalse(inert.canNavigateToTab(target),
                "Terminal render contexts without navigation callbacks should reject tab navigation safely");
        inert.navigateToTab(target);

        TerminalRenderContext navigable = new TerminalRenderContext(null, null,
                0, 0, 0, 0, 0, 0, 0, visited::add, target::equals);
        helper.assertTrue(navigable.canNavigateToTab(target),
                "Terminal render contexts should expose available local tab destinations");
        navigable.navigateToTab(target);
        helper.assertTrue(visited.size() == 1 && visited.get(0).equals(target),
                "Terminal render contexts should call the local tab navigation callback");
        helper.succeed();
    }

    private static void terminalThemeRegistry(GameTestHelper helper) {
        helper.assertTrue(TerminalThemeRegistry.byId(null).id().equals(BuiltinTerminalThemes.ECHO_CONSOLE),
                "Theme registry should fall back to the default ECHO console theme");
        helper.assertTrue(TerminalThemeRegistry.byId(BuiltinTerminalThemes.NEXUS_MODPACK).displayName()
                        .equals("Nexus Modpack"),
                "Built-in Nexus Modpack theme should be registered");
        boolean duplicateRejected = false;
        try {
            TerminalThemeRegistry.register(BuiltinTerminalThemes.echoConsole());
        } catch (IllegalArgumentException expected) {
            duplicateRejected = true;
        }
        helper.assertTrue(duplicateRejected, "Duplicate terminal theme ids should be rejected");
        helper.succeed();
    }

    private static void terminalThemeIconFallback(GameTestHelper helper) {
        Identifier fallback = id("fallback_icon");
        TerminalIconSet icons = TerminalIconSet.builder()
                .fallback(fallback)
                .icon(TerminalIconKey.action("claim"), id("claim_icon"))
                .build();
        helper.assertTrue(icons.resolve(TerminalIconKey.action("claim")).equals(id("claim_icon")),
                "Icon sets should resolve registered semantic icons");
        helper.assertTrue(icons.resolve(TerminalIconKey.action("missing")).equals(fallback),
                "Icon sets should fall back when a semantic icon is missing");
        boolean nullIconRejected = false;
        try {
            TerminalIconSet.builder().icon(TerminalIconKey.action("bad"), null);
        } catch (NullPointerException expected) {
            nullIconRejected = true;
        }
        helper.assertTrue(nullIconRejected, "Icon sets should reject null icon textures");
        TerminalTheme nexus = TerminalThemeRegistry.byId(BuiltinTerminalThemes.NEXUS_MODPACK);
        helper.assertTrue(nexus.icon(TerminalIconKey.action("claim"), TerminalThemeContext.empty(), null) != null,
                "Built-in themes should expose semantic action icons");
        helper.succeed();
    }

    private static void terminalThemeChapterStyle(GameTestHelper helper) {
        TerminalTheme nexus = TerminalThemeRegistry.byId(BuiltinTerminalThemes.NEXUS_MODPACK);
        TerminalThemeContext industrial = new TerminalThemeContext(
                id("industrial_tab"), "chapters", "echoindustrialnexus", "Industrial Nexus",
                "echoindustrialnexus", 0, true, false);
        TerminalChapterStyle style = nexus.chapterStyle(industrial);
        helper.assertTrue("echoindustrialnexus".equals(style.key()),
                "Theme chapter styles should resolve by active namespace");
        helper.assertTrue(style.banner() != null && style.icons().resolve(TerminalIconKey.chapter("echoindustrialnexus")) != null,
                "Chapter styles should provide banner and chapter icon assets");
        TerminalThemeContext unknown = new TerminalThemeContext(id("unknown_tab"), "", "", "", "unknownaddon", 0, true, false);
        helper.assertTrue(nexus.chapterStyle(unknown).equals(nexus.fallbackChapterStyle()),
                "Unknown namespaces should use the theme fallback chapter style");
        helper.succeed();
    }

    private static void terminalThemeResources(GameTestHelper helper) {
        TerminalTheme nexus = TerminalThemeRegistry.byId(BuiltinTerminalThemes.NEXUS_MODPACK);
        TerminalThemeContext context = new TerminalThemeContext(id("industrial_tab"), "chapters",
                "echoindustrialnexus", "Industrial Nexus", "echoindustrialnexus", 0, true, false);
        List<TerminalIconKey> keys = List.of(
                TerminalIconKey.theme("brand"),
                TerminalIconKey.theme("settings"),
                TerminalIconKey.theme("cycle"),
                TerminalIconKey.action("claim"),
                TerminalIconKey.action("theme_cycle"),
                TerminalIconKey.state("claimable"),
                TerminalIconKey.state("blocker"),
                TerminalIconKey.state("empty"),
                TerminalIconKey.reward("inbox"),
                TerminalIconKey.page("command_deck"),
                TerminalIconKey.page("reward_inbox"),
                TerminalIconKey.chapter("echoindustrialnexus"),
                TerminalIconKey.fallback("unknown"));
        for (TerminalIconKey key : keys) {
            Identifier texture = nexus.icon(key, context, null);
            helper.assertTrue(texture != null && classpathResourceExists(texture),
                    "Nexus Modpack theme icon should point at a packaged PNG: " + key + " -> " + texture);
        }
        helper.assertTrue(classpathResourceExists(nexus.tokens().assets().shellBackdrop()),
                "Nexus Modpack shell backdrop should be packaged");
        helper.assertFalse(classpathResourceExists(Identifier.fromNamespaceAndPath(EchoTerminal.MODID,
                        "textures/gui/themes/nexus_modpack/backgrounds/asset_sheet_source.png")),
                "Generated source sheet should not ship as a runtime theme asset");
        helper.succeed();
    }

    private static void terminalThemeSelection(GameTestHelper helper) {
        TerminalClientOptions.resetThemeForTests(BuiltinTerminalThemes.NEXUS_MODPACK);
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Client theme selection should accept registered theme ids");
        TerminalClientOptions.resetThemeForTests(id("missing_theme"));
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(TerminalThemeRegistry.defaultThemeId()),
                "Client theme selection should fall back when the stored theme id is missing");
        helper.succeed();
    }

    private static void terminalCommandDeckPriority(GameTestHelper helper) {
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(
                        true, true, 5, true, true, true, 2).equals(id("vitals")),
                "Command Deck priority should surface critical vitals before rewards or blockers");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(true, 5, true, 2).equals(id("reward_inbox")),
                "Command Deck priority should open rewards before blockers or routes");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(false, 5, true, 2).equals(id("reward_inbox")),
                "Command Deck priority should open reward inbox before routes");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(true, 0, true, 2).equals(id("route_records")),
                "Command Deck priority should continue routes before late-game diagnostics");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(true, 0, false, 2).equals(id("diagnostics")),
                "Command Deck priority should keep diagnostics visible when no route work is pending");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(
                        false, true, 0, true, false, true, 2).equals(MainSurvivalQuestProvider.TAB_ID),
                "Command Deck priority should send active survival objectives to the Survival Route before blockers");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(false, 0, true, 2).equals(id("route_records")),
                "Command Deck priority should continue incomplete routes before fallback guidance");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(false, 0, false, 2)
                        .equals(MainSurvivalQuestProvider.TAB_ID),
                "Command Deck priority should fall back to Survival Route before addon chapter review");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(false, 0, false, 0)
                        .equals(MainSurvivalQuestProvider.TAB_ID),
                "Command Deck priority should still prefer Survival Route when no addons are linked");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckPriorityTabForTests(false, 0, false, false, 2).equals(id("addons")),
                "Command Deck priority should open chapter review when Survival Route is unavailable");
        helper.assertTrue(BuiltinTerminalTabs.commandDeckRewardActionForTests().equals(id("claim_rewards")),
                "Command Deck reward shortcut should keep using the shared reward claim action");
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
            MainSurvivalQuestProvider.INSTANCE.clearCacheForTests();
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
            helper.assertFalse(missions.stream().anyMatch(definition -> "Stone Age".equals(definition.title())),
                    "Survival route should exclude vanilla Baseline records");
            helper.assertFalse(missions.stream()
                            .anyMatch(definition -> "minecraft".equals(definition.id().getNamespace())),
                    "Survival route should not contain vanilla namespaced missions");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Reclaim Power".equals(definition.title())),
                    "Survival route should include installed addon main records");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Phase 02".equals(definition.phaseTitle())
                            && "Reclaim Power".equals(definition.title())),
                    "Industrial salvage records should land on Phase 02");
            helper.assertTrue(missions.stream().anyMatch(definition -> "Phase 09".equals(definition.phaseTitle())
                            && "Field Reference".equals(definition.title())),
                    "Survival route should include remaining MAIN/REFERENCE records in Phase 09");
            helper.assertTrue(missions.stream().allMatch(definition ->
                            definition.phaseTitle().equals(String.format(java.util.Locale.ROOT,
                                    "Phase %02d", definition.phaseOrder()))),
                    "Survival route should expose numeric phase labels that match phase order");
            helper.assertTrue(missions.stream().allMatch(definition ->
                            definition.phaseId().equals(String.format(java.util.Locale.ROOT,
                                    "phase_%02d", definition.phaseOrder()))),
                    "Survival route should expose canonical numeric phase ids");
            long distinctMissionIds = missions.stream().map(TerminalMissionDefinition::id).distinct().count();
            helper.assertTrue(distinctMissionIds == missions.size(),
                    "Survival route should not duplicate authored records in Phase 09");
            helper.assertTrue(missions.stream()
                            .map(definition -> MainSurvivalQuestProvider.INSTANCE.snapshot(null, definition.id()))
                            .allMatch(snapshot -> snapshot.actions().isEmpty()),
                    "Survival route should expose passive source guidance instead of fake commands");

            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            List<TerminalMissionDefinition> vanilla = VanillaJourneyProvider.INSTANCE.missions(player);
            for (Identifier id : List.of(
                    Identifier.withDefaultNamespace("husbandry/breed_an_animal"),
                    Identifier.withDefaultNamespace("husbandry/tame_an_animal"),
                    Identifier.withDefaultNamespace("husbandry/safely_harvest_honey"),
                    Identifier.withDefaultNamespace("husbandry/balanced_diet"),
                    Identifier.withDefaultNamespace("adventure/hero_of_the_village"),
                    Identifier.withDefaultNamespace("adventure/kill_all_mobs"),
                    Identifier.withDefaultNamespace("nether/all_potions"),
                    Identifier.withDefaultNamespace("nether/all_effects"))) {
                TerminalMissionDefinition definition = vanilla.stream()
                        .filter(candidate -> candidate.id().equals(id))
                        .findFirst()
                        .orElseThrow();
                TerminalMissionSnapshot snapshot = VanillaJourneyProvider.INSTANCE.snapshot(player, id);
                helper.assertTrue(VanillaJourneyProvider.INSTANCE.role(player, definition, snapshot)
                                == TerminalMissionRole.OPTIONAL,
                        "Wasteland-unsafe vanilla ecology and rare-effect goals should be optional");
            }
        });
        helper.succeed();
    }

    private static void terminalMainSurvivalRouteCache(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            MainSurvivalQuestProvider.INSTANCE.clearCacheForTests();
            CountingRouteMissionProvider provider = new CountingRouteMissionProvider(
                    Identifier.fromNamespaceAndPath("echoindustrialnexus", "industrial_nexus"),
                    "Industrial Nexus",
                    List.of(new ConfiguredMission(
                            Identifier.fromNamespaceAndPath("echoindustrialnexus", "mission/cache_probe"),
                            "Cache Probe",
                            "Stage 1",
                            "Factory",
                            "Production",
                            TerminalMissionRole.MAIN,
                            TerminalMissionStatus.UNLOCKED,
                            List.of())));
            TerminalMissionRegistry.register(MainSurvivalQuestProvider.INSTANCE);
            TerminalMissionRegistry.register(provider);

            List<TerminalMissionDefinition> missions = MainSurvivalQuestProvider.INSTANCE.missions(null);
            helper.assertTrue(missions.size() == 1, "Survival route should include the test provider mission");
            for (int i = 0; i < 5; i++) {
                TerminalMissionDefinition definition = missions.get(0);
                MainSurvivalQuestProvider.INSTANCE.snapshot(null, definition.id());
                MainSurvivalQuestProvider.INSTANCE.presentation(null, definition,
                        MainSurvivalQuestProvider.INSTANCE.snapshot(null, definition.id()));
                MainSurvivalQuestProvider.INSTANCE.role(null, definition,
                        MainSurvivalQuestProvider.INSTANCE.snapshot(null, definition.id()));
            }
            helper.assertTrue(provider.missionCalls().get() == 1,
                    "Survival route should not rebuild provider mission lists for repeated record lookups");
        });
        helper.succeed();
    }

    private static void terminalMainSurvivalRouteBounds(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            MainSurvivalQuestProvider.INSTANCE.clearCacheForTests();
            TerminalMissionRegistry.register(MainSurvivalQuestProvider.INSTANCE);
            TerminalMissionRegistry.register(new ConfigurableMissionProvider(
                    Identifier.fromNamespaceAndPath("echoindustrialnexus", "industrial_nexus"),
                    "Industrial Nexus",
                    1,
                    generatedMissions(MainSurvivalQuestProvider.maxRouteRecordsForTests() + 25)));

            List<TerminalMissionDefinition> missions = MainSurvivalQuestProvider.INSTANCE.missions(null);
            helper.assertTrue(missions.size() == MainSurvivalQuestProvider.maxRouteRecordsForTests() + 1,
                    "Survival route should cap huge mission lists and append one overflow record");
            TerminalMissionDefinition overflow = missions.get(missions.size() - 1);
            helper.assertTrue("More Signals Available".equals(overflow.title()),
                    "Survival route overflow record should explain hidden records");
            helper.assertTrue(MainSurvivalQuestProvider.INSTANCE.snapshot(null, overflow.id()).status()
                            == TerminalMissionStatus.VIEW_ONLY,
                    "Survival route overflow record should be passive guidance");
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

    private static void terminalRewardTransactional(GameTestHelper helper) {
        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.ECHO_TERMINAL_BLOCK.get());
        EchoTerminalBlockEntity terminal = helper.getBlockEntity(terminalPos, EchoTerminalBlockEntity.class);
        helper.assertTrue(terminal.storeRewards("seed", List.of(new ItemStack(Items.BREAD, 63))),
                "Terminal should accept an initial partial stack");
        helper.assertTrue(terminal.storeRewards("fill", fullInboxStacks(26)),
                "Terminal should accept enough unique stacks to leave no empty reward slots");

        int before = terminal.getStoredRewardCount();
        helper.assertFalse(terminal.storeRewards("overflow", List.of(
                        new ItemStack(Items.BREAD, 2),
                        new ItemStack(Items.TORCH, 1))),
                "Terminal reward storage should reject mixed rewards when every stack cannot fit");
        helper.assertTrue(terminal.getStoredRewardCount() == before,
                "Rejected reward storage should not partially merge into existing inbox stacks");
        helper.succeed();
    }

    private static void terminalRewardClaimFlow(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.ECHO_TERMINAL_BLOCK.get());
        EchoTerminalBlockEntity terminal = helper.getBlockEntity(terminalPos, EchoTerminalBlockEntity.class);
        terminal.setOwnerIfMissing(player);

        helper.assertTrue(terminal.storeRewards("merge", List.of(
                        new ItemStack(Items.BREAD, 63),
                        new ItemStack(Items.BREAD, 1),
                        new ItemStack(Items.TORCH, 1))),
                "Terminal should merge partial stacks and store remaining successful rewards");
        helper.assertTrue(terminal.getStoredRewardCount() == 65,
                "Successful reward storage should preserve every inserted item");
        helper.assertTrue(terminal.claimAllRewards(player),
                "Terminal should claim stored rewards into the player inventory");
        helper.assertTrue(terminal.getStoredRewardCount() == 0,
                "Claiming rewards should clear the terminal inbox");
        helper.succeed();
    }

    private static void terminalRewardExplicitOwner(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos terminalPos = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPos, ModBlocks.ECHO_TERMINAL_BLOCK.get());
        EchoTerminalBlockEntity terminal = helper.getBlockEntity(terminalPos, EchoTerminalBlockEntity.class);
        terminal.storeRewards("unowned", List.of(new ItemStack(Items.BREAD, 1)));

        EchoTerminalCoreServices.rememberTerminal(player, helper.absolutePos(terminalPos));
        helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(player) == 0,
                "Reward service should not expose cached terminals without explicit ownership");
        terminal.setOwnerIfMissing(player);
        EchoTerminalCoreServices.rememberTerminal(player, helper.absolutePos(terminalPos));
        helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(player) == 1,
                "Reward service should expose cached terminals after ownership is assigned");
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

    private static void terminalMissionBrowserCache(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        CountingMissionProvider provider = new CountingMissionProvider();
        TerminalMissionBrowser browser = new TerminalMissionBrowser(provider, id("mission_cache_tab"), true);
        TerminalRenderContext context = new TerminalRenderContext(null, player,
                800, 600, 0, 0, 480, 240, 0, null, null);

        browser.onSelected(context);
        helper.assertTrue(provider.snapshotCalls.get() == 0,
                "Selecting mission browser should not eagerly build mission snapshots");
        helper.assertFalse(browser.hasCachedStateForTests(),
                "Selecting mission browser should leave route state lazy until first height or render query");

        int height = browser.contentHeight(context);
        helper.assertTrue(height >= context.contentHeight(), "Mission browser content height should remain valid");
        helper.assertTrue(provider.snapshotCalls.get() == provider.missionCount(),
                "First mission browser height query should build one snapshot per mission");

        browser.contentHeight(context);
        helper.assertTrue(provider.snapshotCalls.get() == provider.missionCount(),
                "Repeated contentHeight in the same refresh window should reuse cached mission state");

        TerminalRenderContext widerContext = new TerminalRenderContext(null, player,
                800, 600, 0, 0, 960, 240, 0, null, null);
        browser.contentHeight(widerContext);
        helper.assertTrue(provider.snapshotCalls.get() == provider.missionCount(),
                "A stale mission browser cache should be reusable for one frame after a width bucket change");
        browser.contentHeight(widerContext);
        helper.assertTrue(provider.snapshotCalls.get() == provider.missionCount() * 2,
                "Changing the width bucket should refresh the mission browser cache after the stale safety frame");
        helper.succeed();
    }

    private static void terminalMissionBrowserPhaseGating(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Identifier phase00Main = id("phase_00_main");
        Identifier phase00Optional = id("phase_00_optional");
        Identifier phase00Reference = id("phase_00_reference");
        Identifier phase01Main = id("phase_01_main");
        Identifier phase02Main = id("phase_02_main");
        PhaseGatingMissionProvider provider = new PhaseGatingMissionProvider(List.of(
                new PhaseGatingMission(phase00Main, "Awaken", "Provider Awakening", 0, 1,
                        TerminalMissionRole.MAIN, TerminalMissionStatus.CLAIMABLE,
                        List.of(TerminalMissionAction.enabled("claim_reward", "CLAIM"))),
                new PhaseGatingMission(phase00Optional, "Old Ecology", "Provider Awakening", 0, 2,
                        TerminalMissionRole.OPTIONAL, TerminalMissionStatus.UNLOCKED,
                        List.of(TerminalMissionAction.enabled("note", "NOTE"))),
                new PhaseGatingMission(phase00Reference, "Old World Note", "Provider Awakening", 0, 3,
                        TerminalMissionRole.REFERENCE, TerminalMissionStatus.VIEW_ONLY, List.of()),
                new PhaseGatingMission(phase01Main, "Hold Camp", "Provider Stability", 10, 1,
                        TerminalMissionRole.MAIN, TerminalMissionStatus.UNLOCKED,
                        List.of(TerminalMissionAction.enabled("start", "START"))),
                new PhaseGatingMission(phase02Main, "Build Relay", "Provider Machinery", 20, 1,
                        TerminalMissionRole.MAIN, TerminalMissionStatus.UNLOCKED,
                        List.of(TerminalMissionAction.enabled("power", "POWER")))));
        TerminalMissionBrowser browser = new TerminalMissionBrowser(provider, id("phase_gating_tab"), true);
        TerminalRenderContext context = new TerminalRenderContext(null, player,
                800, 600, 0, 0, 640, 260, 0, null, null);

        browser.onSelected(context);
        List<String> phases = browser.phaseDebugRowsForTests(context);
        helper.assertTrue(phases.size() == 3, "Browser should expose every numeric phase, including locked previews");
        helper.assertTrue(phases.get(0).startsWith("Phase 00|COMPLETE|Provider Awakening"),
                "Claimable MAIN objectives should complete Phase 00");
        helper.assertTrue(phases.get(1).startsWith("Phase 01|ACTIVE|Provider Stability"),
                "A completed Phase 00 should unlock Phase 01");
        helper.assertTrue(phases.get(2).startsWith("Phase 02|LOCKED|Provider Machinery"),
                "Incomplete Phase 01 MAIN objectives should lock Phase 02");
        helper.assertFalse(browser.missionReadOnlyForTests(context, phase01Main),
                "Incomplete OPTIONAL and REFERENCE records in Phase 00 should not block Phase 01");
        helper.assertTrue(browser.phaseExpandedForTests(context, "Phase 00"),
                "Claimable phases should expand by default");
        helper.assertTrue(browser.phaseExpandedForTests(context, "Phase 01"),
                "The current unlocked incomplete phase should expand by default");
        helper.assertFalse(browser.phaseExpandedForTests(context, "Phase 02"),
                "Locked future phases should stay collapsed by default");
        helper.assertTrue(phase00Main.equals(browser.focusMissionIdForTests(context))
                        && phase00Main.equals(browser.selectedMissionIdForTests(context)),
                "Mission browser should auto-focus the current ready mission by default");
        int headerHeight = browser.detailHeaderHeightForTests(context, phase02Main);
        helper.assertTrue(headerHeight >= 104 && headerHeight <= 128,
                "Mission browser detail hero should keep a stable V2 height");
        helper.assertTrue(browser.selectMissionForTests(context, phase02Main),
                "Locked future missions should remain selectable for preview");
        helper.assertTrue(browser.phaseExpandedForTests(context, "Phase 02"),
                "Selecting a locked preview should expand its phase");
        helper.assertTrue(browser.missionReadOnlyForTests(context, phase02Main),
                "Locked future missions should be read-only");
        helper.assertTrue(browser.enabledActionCountForTests(context, phase02Main) == 0,
                "Locked future missions should not expose enabled actions");
        helper.assertFalse(browser.activateMissionActionForTests(context, phase02Main),
                "Locked future mission actions should not be sent");
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

    private record DummyAddonChapter(String id, String displayName) implements EchoAddonChapter {
        @Override
        public String modId() {
            return "echotest";
        }

        @Override
        public String summary() {
            return "Test addon chapter.";
        }
    }

    private record DummyAddonInfoProvider(String chapterId, TerminalAddonInfo info) implements TerminalAddonInfoProvider {
        @Override
        public TerminalAddonInfo info(Player player) {
            return info;
        }
    }

    private record NullAddonInfoProvider(String chapterId) implements TerminalAddonInfoProvider {
        @Override
        public TerminalAddonInfo info(Player player) {
            return null;
        }
    }

    private record ThrowingAddonInfoProvider(String chapterId) implements TerminalAddonInfoProvider {
        @Override
        public TerminalAddonInfo info(Player player) {
            throw new IllegalStateException("test terminal addon info failure");
        }
    }

    private record ThrowingAddonChapterIdProvider() implements TerminalAddonInfoProvider {
        @Override
        public String chapterId() {
            throw new IllegalStateException("test terminal addon chapter id failure");
        }

        @Override
        public TerminalAddonInfo info(Player player) {
            return TerminalAddonInfo.empty();
        }
    }

    private record PhaseGatingMission(
            Identifier id,
            String title,
            String phaseTitle,
            int phaseOrder,
            int missionOrder,
            TerminalMissionRole role,
            TerminalMissionStatus status,
            List<TerminalMissionAction> actions) {
    }

    private record PhaseGatingMissionProvider(List<PhaseGatingMission> missions) implements TerminalMissionProvider {
        private static final Identifier CHAPTER_ID = Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "phase_gating");

        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(CHAPTER_ID, "Phase Gating", "Phase gating test provider",
                    1, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return missions.stream()
                    .map(mission -> new TerminalMissionDefinition(
                            mission.id(),
                            CHAPTER_ID,
                            mission.phaseTitle().toLowerCase(java.util.Locale.ROOT).replace(' ', '_'),
                            mission.phaseTitle(),
                            mission.phaseOrder(),
                            mission.missionOrder(),
                            mission.title(),
                            mission.title() + " briefing",
                            mission.title() + " field guide",
                            "Test",
                            "Test",
                            ItemStack.EMPTY,
                            List.of(),
                            List.of(),
                            List.of()))
                    .toList();
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            PhaseGatingMission mission = missions.stream()
                    .filter(candidate -> candidate.id().equals(missionId))
                    .findFirst()
                    .orElse(null);
            return mission == null
                    ? new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                            "LOCKED", "Missing phase test mission.", "Missing phase test mission.", List.of())
                    : new TerminalMissionSnapshot(mission.id(), mission.status(),
                            mission.status() == TerminalMissionStatus.CLAIMABLE ? 1.0F : 0.25F,
                            mission.status().name(), "", mission.title() + " next step", mission.actions());
        }

        @Override
        public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
            return missions.stream()
                    .filter(candidate -> candidate.id().equals(definition.id()))
                    .map(PhaseGatingMission::role)
                    .findFirst()
                    .orElse(TerminalMissionRole.MAIN);
        }
    }

    private static final class CountingMissionProvider implements TerminalMissionProvider {
        private final AtomicInteger snapshotCalls = new AtomicInteger();
        private final Identifier chapterId = id("cache_chapter");
        private final List<TerminalMissionDefinition> definitions = List.of(
                definition(id("cache_mission_a"), 0, 1, "Cache Mission A"),
                definition(id("cache_mission_b"), 0, 2, "Cache Mission B"));

        int missionCount() {
            return definitions.size();
        }

        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, "Cache Chapter", "Cache test provider",
                    1, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return definitions;
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            snapshotCalls.incrementAndGet();
            return new TerminalMissionSnapshot(
                    missionId,
                    TerminalMissionStatus.UNLOCKED,
                    0.25F,
                    "UNLOCKED",
                    "",
                    "Cache test",
                    List.of(TerminalMissionAction.enabled("claim_reward", "CLAIM")));
        }

        private TerminalMissionDefinition definition(Identifier missionId, int phaseOrder, int missionOrder, String title) {
            return new TerminalMissionDefinition(
                    missionId,
                    chapterId,
                    "cache",
                    "Cache",
                    phaseOrder,
                    missionOrder,
                    title,
                    "Cache test briefing",
                    "Cache test field guide",
                    "Test",
                    "Test",
                    ItemStack.EMPTY,
                    List.of(),
                    List.of(),
                    List.of());
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

    private static final class CountingRouteMissionProvider implements TerminalMissionProvider {
        private final ConfigurableMissionProvider delegate;
        private final AtomicInteger missionCalls = new AtomicInteger();

        private CountingRouteMissionProvider(
                Identifier chapterId, String title, List<ConfiguredMission> configuredMissions) {
            this.delegate = new ConfigurableMissionProvider(chapterId, title, 1, configuredMissions);
        }

        @Override
        public TerminalMissionChapter chapter() {
            return delegate.chapter();
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            missionCalls.incrementAndGet();
            return delegate.missions(player);
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return delegate.snapshot(player, missionId);
        }

        @Override
        public TerminalMissionRole role(
                Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
            return delegate.role(player, definition, snapshot);
        }

        AtomicInteger missionCalls() {
            return missionCalls;
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

    private static boolean classpathResourceExists(Identifier id) {
        if (id == null) {
            return false;
        }
        String path = "assets/" + id.getNamespace() + "/" + id.getPath();
        return ModGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static List<ConfiguredMission> generatedMissions(int count) {
        List<ConfiguredMission> missions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            missions.add(new ConfiguredMission(
                    Identifier.fromNamespaceAndPath("echoindustrialnexus", "mission/generated_" + i),
                    "Generated " + i,
                    i < MainSurvivalQuestProvider.maxRouteRecordsForTests() ? "Stage 1" : "Stage 2",
                    "Factory",
                    "Production",
                    TerminalMissionRole.MAIN,
                    TerminalMissionStatus.UNLOCKED,
                    List.of()));
        }
        return List.copyOf(missions);
    }

    private static List<ItemStack> fullInboxStacks(int count) {
        List<net.minecraft.world.item.Item> items = List.of(
                Items.COBBLESTONE,
                Items.DIRT,
                Items.OAK_LOG,
                Items.SPRUCE_LOG,
                Items.BIRCH_LOG,
                Items.JUNGLE_LOG,
                Items.ACACIA_LOG,
                Items.DARK_OAK_LOG,
                Items.MANGROVE_LOG,
                Items.CHERRY_LOG,
                Items.SAND,
                Items.GRAVEL,
                Items.COAL,
                Items.RAW_IRON,
                Items.RAW_COPPER,
                Items.RAW_GOLD,
                Items.REDSTONE,
                Items.LAPIS_LAZULI,
                Items.EMERALD,
                Items.DIAMOND,
                Items.QUARTZ,
                Items.NETHERRACK,
                Items.BASALT,
                Items.BLACKSTONE,
                Items.END_STONE,
                Items.CLAY_BALL,
                Items.FLINT);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            net.minecraft.world.item.Item item = items.get(i);
            stacks.add(new ItemStack(item, item.getDefaultMaxStackSize()));
        }
        return stacks;
    }
}
