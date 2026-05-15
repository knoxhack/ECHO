package com.knoxhack.echoterminal.test;

import com.knoxhack.echorendercore.client.RenderCoreScreenChromeStyle;
import com.knoxhack.echorendercore.client.RenderCoreScreenFrameOptions;
import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.BuiltinTerminalCommonIntegration;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonGuide;
import com.knoxhack.echoterminal.api.TerminalAddonInfo;
import com.knoxhack.echoterminal.api.TerminalAddonInfoProvider;
import com.knoxhack.echoterminal.api.TerminalAddonInfoRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonLink;
import com.knoxhack.echoterminal.api.TerminalAddonMetric;
import com.knoxhack.echoterminal.api.TerminalAddonSection;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalLayoutProfile;
import com.knoxhack.echoterminal.api.TerminalPageLayout;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalNavigationSection;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
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
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRoutePlacement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeCategory;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeEntry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeNote;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeProvider;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeRegistry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeSnapshot;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeSlot;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import com.knoxhack.echoterminal.client.BuiltinTerminalTabs;
import com.knoxhack.echoterminal.client.discovery.DiscoveryGridTab;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import com.knoxhack.echoterminal.client.mission.TerminalMissionHudController;
import com.knoxhack.echoterminal.client.mission.TerminalMissionNotice;
import com.knoxhack.echoterminal.client.mission.TerminalMissionNoticeType;
import com.knoxhack.echoterminal.client.recipe.TerminalRecipeIndexTab;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreen;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.client.screen.TerminalScreenTheme;
import com.knoxhack.echoterminal.integration.TerminalRenderCoreClientIntegration;
import com.knoxhack.echoterminal.discovery.TerminalDiscoveryProvider;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import com.knoxhack.echoterminal.mission.MainSurvivalQuestProvider;
import com.knoxhack.echoterminal.mission.VanillaJourneyData;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import com.knoxhack.echoterminal.network.TerminalActionPacket;
import com.knoxhack.echoterminal.registry.ModBlocks;
import com.knoxhack.echoterminal.service.EchoTerminalCoreServices;
import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiscoveryCategory;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import com.knoxhack.echocore.api.EchoDiscoveryState;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echocore.api.config.EchoConfigApplyResult;
import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigEntrySnapshot;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigModuleSnapshot;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echocore.api.config.EchoConfigValueKind;
import com.knoxhack.echocore.discovery.EchoDiscoveryData;
import com.knoxhack.echoterminal.network.TerminalConfigActionPacket;
import com.knoxhack.echoterminal.network.TerminalConfigClientState;
import com.knoxhack.echoterminal.network.TerminalConfigSyncPacket;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.lwjgl.glfw.GLFW;

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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_DISCOVERY_GRID_FILTERS =
            TEST_FUNCTIONS.register("terminal_discovery_grid_filters", () -> ModGameTests::terminalDiscoveryGridFilters);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_DISCOVERY_GRID_ROUTE_STATE =
            TEST_FUNCTIONS.register("terminal_discovery_grid_route_state", () -> ModGameTests::terminalDiscoveryGridRouteState);
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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_ZOOM_OPTIONS =
            TEST_FUNCTIONS.register("terminal_zoom_options", () -> ModGameTests::terminalZoomOptions);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_VISUAL_POLISH_LAYOUT =
            TEST_FUNCTIONS.register("terminal_visual_polish_layout", () -> ModGameTests::terminalVisualPolishLayout);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_RESOURCE_NAME_CONTRACTS =
            TEST_FUNCTIONS.register("terminal_resource_name_contracts", () -> ModGameTests::terminalResourceNameContracts);
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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_BASELINE_AUTO_REFRESH =
            TEST_FUNCTIONS.register("terminal_baseline_auto_refresh", () -> ModGameTests::terminalBaselineAutoRefresh);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_BASELINE_DATA_DEFINITIONS =
            TEST_FUNCTIONS.register("terminal_baseline_data_definitions", () -> ModGameTests::terminalBaselineDataDefinitions);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_BROWSER_CACHE =
            TEST_FUNCTIONS.register("terminal_mission_browser_cache", () -> ModGameTests::terminalMissionBrowserCache);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_BROWSER_PHASE_GATING =
            TEST_FUNCTIONS.register("terminal_mission_browser_phase_gating", () -> ModGameTests::terminalMissionBrowserPhaseGating);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_HUD_NOTIFICATIONS =
            TEST_FUNCTIONS.register("terminal_mission_hud_notifications", () -> ModGameTests::terminalMissionHudNotifications);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_RECIPE_REGISTRY =
            TEST_FUNCTIONS.register("terminal_recipe_registry", () -> ModGameTests::terminalRecipeRegistry);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_RECIPE_LOOKUPS =
            TEST_FUNCTIONS.register("terminal_recipe_lookups", () -> ModGameTests::terminalRecipeLookups);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_CONFIG_WORKFLOW =
            TEST_FUNCTIONS.register("terminal_config_workflow", () -> ModGameTests::terminalConfigWorkflow);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("terminal_release"));
        register(event, environment, "terminal_api_ids", TERMINAL_API_IDS.getId());
        register(event, environment, "terminal_tab_registry", TERMINAL_TAB_REGISTRY.getId());
        register(event, environment, "terminal_mission_registry", TERMINAL_MISSION_REGISTRY.getId());
        register(event, environment, "terminal_addon_info_registry", TERMINAL_ADDON_INFO_REGISTRY.getId());
        register(event, environment, "terminal_addon_guide_ordering", TERMINAL_ADDON_GUIDE_ORDERING.getId());
        register(event, environment, "terminal_navigation_profiles", TERMINAL_NAVIGATION_PROFILES.getId());
        register(event, environment, "terminal_discovery_grid_filters", TERMINAL_DISCOVERY_GRID_FILTERS.getId());
        register(event, environment, "terminal_discovery_grid_route_state", TERMINAL_DISCOVERY_GRID_ROUTE_STATE.getId());
        register(event, environment, "terminal_render_context_navigation", TERMINAL_RENDER_CONTEXT_NAVIGATION.getId());
        register(event, environment, "terminal_theme_registry", TERMINAL_THEME_REGISTRY.getId());
        register(event, environment, "terminal_theme_icon_fallback", TERMINAL_THEME_ICON_FALLBACK.getId());
        register(event, environment, "terminal_theme_chapter_style", TERMINAL_THEME_CHAPTER_STYLE.getId());
        register(event, environment, "terminal_theme_resources", TERMINAL_THEME_RESOURCES.getId());
        register(event, environment, "terminal_theme_selection", TERMINAL_THEME_SELECTION.getId());
        register(event, environment, "terminal_zoom_options", TERMINAL_ZOOM_OPTIONS.getId());
        register(event, environment, "terminal_visual_polish_layout", TERMINAL_VISUAL_POLISH_LAYOUT.getId());
        register(event, environment, "terminal_resource_name_contracts", TERMINAL_RESOURCE_NAME_CONTRACTS.getId());
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
        register(event, environment, "terminal_baseline_auto_refresh", TERMINAL_BASELINE_AUTO_REFRESH.getId());
        register(event, environment, "terminal_baseline_data_definitions", TERMINAL_BASELINE_DATA_DEFINITIONS.getId());
        register(event, environment, "terminal_mission_browser_cache", TERMINAL_MISSION_BROWSER_CACHE.getId());
        register(event, environment, "terminal_mission_browser_phase_gating", TERMINAL_MISSION_BROWSER_PHASE_GATING.getId());
        register(event, environment, "terminal_mission_hud_notifications", TERMINAL_MISSION_HUD_NOTIFICATIONS.getId());
        register(event, environment, "terminal_recipe_registry", TERMINAL_RECIPE_REGISTRY.getId());
        register(event, environment, "terminal_recipe_lookups", TERMINAL_RECIPE_LOOKUPS.getId());
        register(event, environment, "terminal_config_workflow", TERMINAL_CONFIG_WORKFLOW.getId());
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
            AtomicBoolean denied = new AtomicBoolean(false);
            TerminalActionRegistry.register(id("test_tab"), id("denied_action"),
                    (player, payload) -> denied.set(true), context -> false);
            helper.assertTrue(TerminalActionRegistry.handle(null, id("test_tab"), id("denied_action"), ""),
                    "Known terminal actions rejected by validators should be consumed without reporting unknown");
            helper.assertFalse(denied.get(), "Rejected terminal action handlers should not run");
        });
        helper.succeed();
    }

    private static void terminalConfigWorkflow(GameTestHelper helper) {
        EchoConfigRegistry.withClearedForTests(() -> {
            AtomicInteger serverCount = new AtomicInteger(2);
            AtomicInteger clientCount = new AtomicInteger(3);
            EchoConfigRegistry.register(EchoConfigProvider.of(EchoTerminal.MODID, () -> new EchoConfigModule(
                    EchoTerminal.MODID,
                    "ECHO Terminal",
                    List.of(
                            new EchoConfigCategory("server", "Server", List.of(
                                    EchoConfigEntry.intEntry("server_count", "Server Count", "",
                                            EchoConfigSide.COMMON, 2, 0, 10, serverCount::get, serverCount::set,
                                            null, true, false, false))),
                            new EchoConfigCategory("client", "Client", List.of(
                                    EchoConfigEntry.intEntry("client_count", "Client Count", "",
                                            EchoConfigSide.CLIENT, 3, 0, 10, clientCount::get, clientCount::set,
                                            null, true, false, false)))))));

            TerminalConfigActionPacket packet = new TerminalConfigActionPacket(
                    TerminalConfigActionPacket.Action.SET,
                    EchoConfigSide.COMMON,
                    "ECHOterminal",
                    "Server_Count",
                    "4");
            helper.assertTrue(packet.moduleId().equals(EchoTerminal.MODID)
                            && packet.entryId().equals("server_count")
                            && packet.action() == TerminalConfigActionPacket.Action.SET,
                    "Config action packets should normalize ids and preserve action intent");

            List<EchoConfigModuleSnapshot> commonSnapshot = EchoConfigRegistry.snapshots(EchoConfigSide.COMMON);
            TerminalConfigClientState.apply(new TerminalConfigSyncPacket(commonSnapshot, "Snapshot ready."));
            helper.assertTrue(TerminalConfigClientState.commonModule("ECHOterminal").isPresent(),
                    "Client config state should apply common snapshots from the server");
            helper.assertTrue(TerminalConfigClientState.status().equals("Snapshot ready."),
                    "Client config state should expose visible server status");

            EchoConfigEntrySnapshot frozen = TerminalConfigClientState.commonModule(EchoTerminal.MODID).orElseThrow()
                    .categories().get(0).entries().get(0);
            helper.assertTrue(frozen.value().equals("2"), "Server snapshot should contain the synced value");
            serverCount.set(8);
            EchoConfigEntrySnapshot stillFrozen = TerminalConfigClientState.commonModule(EchoTerminal.MODID).orElseThrow()
                    .categories().get(0).entries().get(0);
            helper.assertTrue(stillFrozen.value().equals("2"),
                    "Terminal common config should render from server snapshots, not local common values");

            EchoConfigApplyResult applied = EchoConfigRegistry.apply(
                    EchoConfigSide.COMMON, EchoTerminal.MODID, "server_count", "6");
            helper.assertTrue(applied.success() && serverCount.get() == 6,
                    "Server config edits should validate and update common entries");
            helper.assertFalse(EchoConfigRegistry.apply(
                    EchoConfigSide.COMMON, EchoTerminal.MODID, "client_count", "5").success(),
                    "Server config actions should reject client-local entries");
            helper.assertFalse(EchoConfigRegistry.apply(
                    EchoConfigSide.COMMON, EchoTerminal.MODID, "missing", "5").success(),
                    "Unknown config entries should be rejected");
            helper.assertTrue(EchoConfigRegistry.apply(
                    EchoConfigSide.CLIENT, EchoTerminal.MODID, "client_count", "5").success()
                            && clientCount.get() == 5,
                    "Client-local config edits should apply through the client registry side");

            EchoAddonChapter aliasChapter = new EchoAddonChapter() {
                @Override
                public String id() {
                    return "terminal_alias";
                }

                @Override
                public String modId() {
                    return EchoTerminal.MODID;
                }

                @Override
                public String displayName() {
                    return "Terminal Alias";
                }

                @Override
                public String summary() {
                    return "Alias chapter for config matching.";
                }
            };
            List<String> matched = BuiltinTerminalTabs.addonConfigAwareGuideOrderForTests(List.of(aliasChapter));
            helper.assertTrue(matched.stream().filter(entry -> entry.endsWith("|" + EchoTerminal.MODID)).count() == 1,
                    "Addon config matching should use modId/chapterId keys and avoid duplicates");
            List<String> synthetic = BuiltinTerminalTabs.addonConfigAwareGuideOrderForTests(List.of());
            helper.assertTrue(synthetic.stream().anyMatch(entry -> entry.endsWith("|" + EchoTerminal.MODID)),
                    "Loaded config-capable modules without chapters should be added to the addon guide");
        });
        TerminalConfigClientState.apply(null);
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

    private static void terminalRecipeRegistry(GameTestHelper helper) {
        TerminalRecipeRegistry.withClearedForTests(() -> {
            TerminalRecipeRegistry.register(new DummyRecipeProvider(id("alpha_provider"), 10));
            TerminalRecipeRegistry.register(new DummyRecipeProvider(id("zeta_provider"), 20));
            TerminalRecipeRegistry.register(new DuplicateRecipeProvider());
            TerminalRecipeRegistry.register(new ThrowingRecipeProvider());
            helper.assertTrue(TerminalRecipeRegistry.providers().size() == 4,
                    "Recipe provider registry should expose registered providers that pass id validation");
            helper.assertTrue(TerminalRecipeRegistry.providers().get(0).id().equals(id("alpha_provider")),
                    "Recipe providers should sort by id");
            TerminalRecipeSnapshot snapshot = TerminalRecipeRegistry.snapshot(null);
            helper.assertTrue(snapshot.categories().get(0).id().equals(id("alpha_category")),
                    "Recipe categories should sort by order then id");
            helper.assertTrue(snapshot.categories().stream().filter(category -> category.id().equals(id("alpha_category"))).count() == 1,
                    "Duplicate recipe categories should be de-duped in snapshots");
            helper.assertTrue(snapshot.recipes().stream().filter(recipe -> recipe.id().equals(id("alpha_provider/recipe"))).count() == 1,
                    "Duplicate recipe ids should be de-duped in snapshots");
            helper.assertTrue(snapshot.recipesFor(Items.APPLE).size() == 2,
                    "Snapshot output index should include deterministic provider recipes");
            helper.assertTrue(snapshot.usesFor(Items.STICK).size() == 2,
                    "Snapshot use index should include deterministic provider recipes");

            boolean duplicateRejected = false;
            try {
                TerminalRecipeRegistry.register(new DummyRecipeProvider(id("alpha_provider"), 99));
            } catch (IllegalArgumentException expected) {
                duplicateRejected = true;
            }
            helper.assertTrue(duplicateRejected, "Duplicate recipe provider ids must fail fast");
        });
        helper.succeed();
    }

    private static void terminalRecipeLookups(GameTestHelper helper) {
        TerminalRecipeEntry lockedRecipe = new TerminalRecipeEntry(
                id("recipe/test_locked"),
                id("recipe_category"),
                "Locked Apple",
                new ItemStack(Items.CRAFTING_TABLE),
                List.of(
                        TerminalRecipeSlot.input(new ItemStack(Items.STICK)),
                        TerminalRecipeSlot.catalyst(new ItemStack(Items.REDSTONE)),
                        TerminalRecipeSlot.info(new ItemStack(Items.BOOK), "Info"),
                        TerminalRecipeSlot.output(new ItemStack(Items.APPLE))),
                List.of(TerminalRecipeNote.warning("Requires TEST schematic unlock.")),
                40,
                true);
        helper.assertTrue(lockedRecipe.outputs(Items.APPLE), "Recipe lookup should match outputs");
        helper.assertTrue(lockedRecipe.uses(Items.STICK), "Recipe lookup should match inputs");
        helper.assertTrue(lockedRecipe.uses(Items.REDSTONE), "Recipe lookup should match catalysts");
        helper.assertTrue(lockedRecipe.uses(Items.CRAFTING_TABLE), "Recipe lookup should match machine slots");
        helper.assertTrue(lockedRecipe.mentions(Items.BOOK), "Recipe lookup should match info slots");
        helper.assertTrue(lockedRecipe.locked() && lockedRecipe.notes().stream().anyMatch(TerminalRecipeNote::warning),
                "Locked recipes should keep warning notes visible");
        helper.assertTrue(TerminalRecipeIndexTab.matchingRecipesForTests(
                        List.of(lockedRecipe), new ItemStack(Items.APPLE), false).size() == 1,
                "Recipe index should expose recipes for selected outputs");
        helper.assertTrue(TerminalRecipeIndexTab.matchingRecipesForTests(
                        List.of(lockedRecipe), new ItemStack(Items.STICK), true).size() == 1,
                "Recipe index should expose uses for selected inputs");
        helper.assertTrue(TerminalRecipeIndexTab.echoItemsForTests().stream()
                        .allMatch(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().startsWith("echo")),
                "Recipe index item grid should only expose ECHO namespaces");
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
                            TerminalNavigationSection.COMMAND,
                            TerminalNavigationSection.CHAPTERS,
                            TerminalNavigationSection.INTEL,
                            TerminalNavigationSection.SYSTEM)),
                    "Terminal navigation should render task-first sections in player-facing order");

            TerminalNavigationProfile command = TerminalNavigationProfiles.profileFor(
                    new DummyTab(id("overview"), "OVERVIEW", 0));
            helper.assertTrue(command.section() == TerminalNavigationSection.COMMAND,
                    "Legacy protocol tabs should fall back into the Command section");

            helper.assertTrue(TerminalNavigationProfile.terminal(10).section() == TerminalNavigationSection.COMMAND,
                    "Legacy Terminal profiles should canonicalize to Command");
            helper.assertTrue(TerminalNavigationProfile.core(10).section() == TerminalNavigationSection.INTEL,
                    "Legacy Core profiles should canonicalize to Intel");
            helper.assertTrue(TerminalNavigationSection.fromKey("TERMINAL") == TerminalNavigationSection.COMMAND,
                    "Legacy Terminal section keys should resolve to Command");
            helper.assertTrue(TerminalNavigationSection.fromKey("CORE") == TerminalNavigationSection.INTEL,
                    "Legacy Core section keys should resolve to Intel");

            TerminalNavigationProfile endgame = TerminalNavigationProfiles.profileFor(new DummyChromeTab(
                    new TerminalTabDescriptor(id("legacy_endgame"), "ENDGAME", 220, 0xFFC77DFF),
                    TerminalTabChrome.of("Legacy Endgame", TerminalTabChrome.GROUP_ENDGAME, "EG",
                            "Legacy finale", 220)));
            helper.assertTrue(endgame.section() == TerminalNavigationSection.INTEL,
                    "Legacy endgame tabs should not create a standalone Endgame section");

            TerminalNavigationProfile nexus = TerminalNavigationProfiles.profileFor(new DummyChromeTab(
                    new TerminalTabDescriptor(id("legacy_nexus"), "NEXUS", 230, 0xFFC77DFF),
                    TerminalTabChrome.of("Legacy Nexus", TerminalTabChrome.GROUP_NEXUS, "NX",
                            "Legacy finale", 230)));
            helper.assertTrue(nexus.section() == TerminalNavigationSection.INTEL,
                    "Nexus tabs require an explicit addon profile before they appear as beta chapter navigation");

            TerminalNavigationProfiles.register(MainSurvivalQuestProvider.TAB_ID,
                    TerminalNavigationProfile.progress(0));
            TerminalNavigationProfile survivalRoute =
                    TerminalNavigationProfiles.profile(MainSurvivalQuestProvider.TAB_ID).orElse(null);
            helper.assertTrue(survivalRoute != null
                            && survivalRoute.section() == TerminalNavigationSection.CHAPTERS,
                    "Survival Route should be the main Progress section destination");

            Identifier addons = id("addons");
            TerminalNavigationProfiles.register(addons, TerminalNavigationProfile.progress(150));
            TerminalNavigationProfile chapterStatus = TerminalNavigationProfiles.profile(addons).orElse(null);
            helper.assertTrue(chapterStatus != null
                            && chapterStatus.section() == TerminalNavigationSection.CHAPTERS,
                    "Mods should live in the Progress section while preserving the addons tab id");

            Map<Identifier, TerminalNavigationProfile> builtinProfiles =
                    BuiltinTerminalTabs.builtinNavigationProfilesForTests();
            helper.assertTrue(builtinProfiles.get(id("overview")).section() == TerminalNavigationSection.COMMAND,
                    "Command Deck should live in Command");
            helper.assertTrue(builtinProfiles.get(BuiltinTerminalTabs.commandDeckDiagnosticsTabForTests())
                            .section() == TerminalNavigationSection.COMMAND,
                    "What Now diagnostics should be a registered Command page");
            helper.assertTrue(builtinProfiles.get(MainSurvivalQuestProvider.TAB_ID)
                            .section() == TerminalNavigationSection.CHAPTERS,
                    "Survival Route should live in Progress");
            helper.assertTrue(builtinProfiles.get(VanillaJourneyProvider.TAB_ID)
                            .section() == TerminalNavigationSection.CHAPTERS,
                    "Baseline should expose the standalone vanilla route in Progress");
            helper.assertTrue(builtinProfiles.get(id("mission_graph")).section() == TerminalNavigationSection.INTEL,
                    "Mission Graph should be demoted to Intel as a route source diagnostic");
            helper.assertTrue(builtinProfiles.get(MainSurvivalQuestProvider.TAB_ID).order()
                            < builtinProfiles.get(VanillaJourneyProvider.TAB_ID).order()
                            && builtinProfiles.get(VanillaJourneyProvider.TAB_ID).order()
                            < builtinProfiles.get(id("addons")).order(),
                    "Baseline should sit between the aggregate Survival Route and Mods");
            helper.assertTrue(builtinProfiles.get(id("addons")).section() == TerminalNavigationSection.CHAPTERS,
                    "Mods should live in Progress");
            helper.assertTrue(builtinProfiles.get(id("route_records")).section() == TerminalNavigationSection.INTEL,
                    "Route Records should live in Intel");
            helper.assertTrue(builtinProfiles.get(DiscoveryGridTab.TAB_ID).section() == TerminalNavigationSection.INTEL,
                    "Discovery Grid should live in Intel");
            helper.assertTrue(builtinProfiles.get(id("route_records")).order()
                            < builtinProfiles.get(DiscoveryGridTab.TAB_ID).order()
                            && builtinProfiles.get(DiscoveryGridTab.TAB_ID).order()
                            < builtinProfiles.get(id("faction_atlas")).order(),
                    "Discovery Grid should sit between Route Records and Faction Atlas");
            helper.assertTrue(builtinProfiles.get(id("faction_atlas")).section() == TerminalNavigationSection.INTEL,
                    "Faction Atlas should live in Intel");
            helper.assertTrue(builtinProfiles.get(id("archives")).section() == TerminalNavigationSection.INTEL,
                    "Field Archive should live in Intel");
            helper.assertTrue(builtinProfiles.get(id("vitals")).section() == TerminalNavigationSection.SYSTEM,
                    "Vitals should live in System");
            helper.assertTrue(builtinProfiles.get(id("reward_inbox")).section() == TerminalNavigationSection.SYSTEM,
                    "Reward Inbox should live in System");
            helper.assertTrue(builtinProfiles.get(id("settings")).section() == TerminalNavigationSection.SYSTEM,
                    "Interface Settings should live in System");

            Identifier stationfall = id("stationfall");
            TerminalNavigationProfiles.register(stationfall,
                    TerminalNavigationProfile.chapter("stationfall", "Chapter 3: Stationfall", "C3", 330));
            TerminalNavigationProfile stationProfile = TerminalNavigationProfiles.profile(stationfall).orElse(null);
            helper.assertTrue(stationProfile != null, "Registered navigation profiles should be discoverable");
            helper.assertTrue(stationProfile.section() == TerminalNavigationSection.CHAPTERS,
                    "Addon profiles should live in the Progress mod section");
            helper.assertTrue("stationfall".equals(stationProfile.chapterId()),
                    "Addon profiles should keep their chapter workspace id");
            helper.assertTrue("Chapter 3: Stationfall".equals(stationProfile.chapterTitle()),
                    "Addon profiles should expose numbered chapter titles");
        });
        helper.succeed();
    }

    private static void terminalDiscoveryGridFilters(GameTestHelper helper) {
        EchoCoreServices.clearPlatformServicesForTests();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoDiscoveryEntry locked = discoveryEntry("locked_structure", EchoDiscoveryCategory.STRUCTURE,
                "Locked Structure", 10);
        EchoDiscoveryEntry discovered = discoveryEntry("discovered_biome", EchoDiscoveryCategory.BIOME,
                "Discovered Biome", 20);
        EchoDiscoveryEntry checked = discoveryEntry("checked_guardian", EchoDiscoveryCategory.GUARDIAN,
                "Checked Guardian", 30);

        EchoCoreServices.registerDiscoveryProvider(new com.knoxhack.echocore.api.EchoDiscoveryProvider() {
            @Override
            public List<EchoDiscoveryEntry> entries(Player player) {
                return List.of(locked, discovered, checked);
            }

            @Override
            public EchoDiscoveryState state(Player player, EchoDiscoveryEntry entry) {
                return checked.id().equals(entry.id()) ? EchoDiscoveryState.CHECKED : EchoDiscoveryState.LOCKED;
            }
        });

        helper.assertTrue(recordDiscoveredForTest(player, discovered.id()),
                "Stored discovery should record the discovered card once");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, null).size() == 3,
                "Discovery Grid should include all registered entries with no filters");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, EchoDiscoveryCategory.STRUCTURE, null)
                        .equals(List.of(locked)),
                "Discovery Grid category filter should isolate structures");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.LOCKED)
                        .equals(List.of(locked)),
                "Discovery Grid locked filter should keep provider-locked hint cards");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.DISCOVERED)
                        .equals(List.of(discovered)),
                "Discovery Grid discovered filter should include stored discoveries");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.CHECKED)
                        .equals(List.of(checked)),
                "Discovery Grid checked filter should include live completed entries");
        EchoCoreServices.clearPlatformServicesForTests();
        helper.succeed();
    }

    private static void terminalDiscoveryGridRouteState(GameTestHelper helper) {
        EchoCoreServices.clearPlatformServicesForTests();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoRouteRecord readyRecord = new EchoRouteRecord(
                id("route/ready_route"),
                "terminal_tests",
                "Ready Route",
                "Route",
                "Field",
                "READY",
                "Ready route summary",
                false);
        EchoRouteRecord completeRecord = new EchoRouteRecord(
                id("route/complete_route"),
                "terminal_tests",
                "Complete Route",
                "Route",
                "Field",
                "COMPLETE",
                "Complete route summary",
                true);
        Identifier readyDiscoveryId = EchoCoreServices.routeDiscoveryId(readyRecord.id());
        Identifier completeDiscoveryId = EchoCoreServices.routeDiscoveryId(completeRecord.id());

        EchoCoreServices.registerRouteRecordService(ignored -> List.of(readyRecord, completeRecord));
        EchoCoreServices.registerDiscoveryProvider(new TerminalDiscoveryProvider());

        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.LOCKED).stream()
                        .anyMatch(entry -> entry.id().equals(readyDiscoveryId)),
                "READY route records should stay locked until a persisted discovery exists");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.DISCOVERED).stream()
                        .noneMatch(entry -> entry.id().equals(readyDiscoveryId)),
                "READY route records should not reveal from route status alone");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.CHECKED).stream()
                        .anyMatch(entry -> entry.id().equals(completeDiscoveryId)),
                "Complete route records should resolve as checked from live progression");
        EchoDiscoveryEntry lockedEntry = EchoCoreServices.discoveryEntries(player).stream()
                .filter(entry -> entry.id().equals(readyDiscoveryId))
                .findFirst()
                .orElseThrow();
        helper.assertTrue(!lockedEntry.lockedHintTitle().equals(lockedEntry.revealedTitle())
                        && !lockedEntry.hintText().isBlank(),
                "Locked route cards should keep hint-only metadata");

        helper.assertTrue(recordDiscoveredForTest(player, readyDiscoveryId),
                "Test discovery seed should persist the READY route id once");
        helper.assertTrue(recordDiscoveredForTest(player, completeDiscoveryId),
                "Test discovery seed should persist the COMPLETE route id once");
        helper.assertTrue(EchoCoreServices.hasDiscoveredFeature(player, readyDiscoveryId),
                "Visible route discovery should persist the READY route id");
        helper.assertTrue(DiscoveryGridTab.visibleEntriesForTests(player, null, EchoDiscoveryState.DISCOVERED).stream()
                        .anyMatch(entry -> entry.id().equals(readyDiscoveryId)),
                "Persisted route discovery should reveal non-complete route cards");
        helper.assertFalse(recordDiscoveredForTest(player, readyDiscoveryId),
                "Duplicate route discovery should remain silent");

        EchoCoreServices.clearPlatformServicesForTests();
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
        TerminalThemeRegistry.setDefaultTheme(BuiltinTerminalThemes.ECHO_CONSOLE);
        helper.assertTrue(TerminalThemeRegistry.byId(null).id().equals(BuiltinTerminalThemes.ECHO_CONSOLE),
                "Theme registry should fall back to the default ECHO console theme");
        helper.assertTrue(TerminalThemeRegistry.byId(BuiltinTerminalThemes.NEXUS_MODPACK).displayName()
                        .equals("Nexus Modpack"),
                "Built-in Nexus Modpack theme should be registered");
        helper.assertFalse(TerminalThemeRegistry.setDefaultTheme(id("missing_theme")),
                "Theme registry should reject unregistered default theme ids");
        helper.assertTrue(TerminalThemeRegistry.setDefaultTheme(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Theme registry should accept a registered default theme id");
        helper.assertTrue(TerminalThemeRegistry.defaultThemeId().equals(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Theme registry should expose the active default theme id");
        helper.assertTrue(TerminalThemeRegistry.byId(null).id().equals(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Theme registry should resolve null theme ids to the active default theme");
        helper.assertTrue(TerminalThemeRegistry.setDefaultTheme(BuiltinTerminalThemes.ECHO_CONSOLE),
                "Theme registry should allow restoring the built-in ECHO console default");
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
        TerminalTheme echo = TerminalThemeRegistry.byId(BuiltinTerminalThemes.ECHO_CONSOLE);
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
        Identifier mission = Identifier.fromNamespaceAndPath("echoashfallprotocol", "acquire_mutagen");
        List<Identifier> visuals = List.of(
                TerminalVisualAssets.TERMINAL_FRAME_BACKDROP,
                TerminalVisualAssets.MISSIONS_VISUAL_HERO,
                TerminalVisualAssets.CARD_PANEL_DETAIL_STANDARD,
                TerminalVisualAssets.CARD_METRIC_TILE_PLATE,
                TerminalVisualAssets.ICON_ACTION_CLAIM,
                TerminalVisualAssets.MISSION_ICON_SURVIVAL,
                TerminalVisualAssets.missionIconArt(mission, "story"),
                TerminalVisualAssets.missionHeroArt(mission, "story"));
        for (TerminalTheme theme : List.of(echo, nexus)) {
            for (TerminalIconKey key : keys) {
                Identifier texture = theme.icon(key, context, null);
                helper.assertTrue(texture != null && classpathResourceExists(texture),
                        theme.displayName() + " semantic icon should point at a packaged PNG: " + key + " -> " + texture);
                helper.assertTrue(pngHasTransparentCorners(texture),
                        theme.displayName() + " semantic icon should preserve transparent corners: " + texture);
            }
            for (Identifier visual : visuals) {
                Identifier themed = theme.visual(visual);
                helper.assertTrue(themed != null && classpathResourceExists(themed),
                        theme.displayName() + " visual override should point at a packaged PNG: " + visual + " -> " + themed);
            }
            helper.assertTrue(classpathResourceExists(theme.visual(theme.tokens().assets().shellBackdrop())),
                    theme.displayName() + " shell backdrop should be packaged");
            helper.assertTrue(classpathResourceExists(theme.chapterStyle(context).banner()),
                    theme.displayName() + " chapter banner should be packaged");
            helper.assertTrue(classpathResourceExists(theme.chapterStyle(context).panel()),
                    theme.displayName() + " chapter panel should be packaged");
        }
        helper.assertFalse(classpathResourceExists(Identifier.fromNamespaceAndPath(EchoTerminal.MODID,
                        "textures/gui/themes/nexus_modpack/backgrounds/asset_sheet_source.png")),
                "Generated source sheet should not ship as a runtime theme asset");
        helper.assertFalse(classpathResourceExists(Identifier.fromNamespaceAndPath(EchoTerminal.MODID,
                        "textures/gui/themes/echo_console/backgrounds/asset_sheet_source.png")),
                "Generated source sheet should not ship as a runtime theme asset");
        helper.succeed();
    }

    private static void terminalThemeSelection(GameTestHelper helper) {
        helper.assertTrue(TerminalThemeRegistry.setDefaultTheme(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Theme registry should allow tests to switch the active default theme");
        TerminalClientOptions.resetThemeForTests(null);
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Missing client theme selections should resolve dynamically to the active registry default");
        TerminalClientOptions.resetThemeForTests(BuiltinTerminalThemes.ECHO_CONSOLE);
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(BuiltinTerminalThemes.ECHO_CONSOLE),
                "Valid saved client theme selections should be preserved");
        TerminalClientOptions.resetThemeForTests(BuiltinTerminalThemes.NEXUS_MODPACK);
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(BuiltinTerminalThemes.NEXUS_MODPACK),
                "Client theme selection should accept registered theme ids");
        TerminalClientOptions.resetThemeForTests(id("missing_theme"));
        helper.assertTrue(TerminalClientOptions.selectedThemeId().equals(TerminalThemeRegistry.defaultThemeId()),
                "Client theme selection should fall back when the stored theme id is missing");
        helper.assertTrue(TerminalThemeRegistry.setDefaultTheme(BuiltinTerminalThemes.ECHO_CONSOLE),
                "Theme registry should allow tests to restore the built-in default theme");
        helper.succeed();
    }

    private static void terminalZoomOptions(GameTestHelper helper) {
        List<String> labels = Arrays.stream(TerminalClientOptions.TerminalZoom.values())
                .map(TerminalClientOptions.TerminalZoom::label)
                .toList();
        helper.assertTrue(labels.equals(List.of("50%", "75%", "85%", "90%", "100%", "110%", "125%", "150%")),
                "Terminal zoom options should preserve legacy presets and add 50%, 75%, and 150%");
        helper.assertTrue(TerminalClientOptions.TerminalZoom.ZOOM_50.scale() == 0.5D,
                "50% terminal zoom should scale to 0.5");
        helper.assertTrue(TerminalClientOptions.TerminalZoom.ZOOM_150.scale() == 1.5D,
                "150% terminal zoom should scale to 1.5");
        TerminalScreenTheme theme = TerminalScreenTheme.modular();
        EchoTerminalScreen.LayoutMetrics zoom50 = EchoTerminalScreen.layoutMetricsForTests(
                2048, 1152, theme,
                TerminalClientOptions.InterfaceDensity.BALANCED,
                TerminalClientOptions.TerminalZoom.ZOOM_50,
                false);
        EchoTerminalScreen.LayoutMetrics zoom100 = EchoTerminalScreen.layoutMetricsForTests(
                2048, 1152, theme,
                TerminalClientOptions.InterfaceDensity.BALANCED,
                TerminalClientOptions.TerminalZoom.ZOOM_100,
                false);
        EchoTerminalScreen.LayoutMetrics zoom150 = EchoTerminalScreen.layoutMetricsForTests(
                2048, 1152, theme,
                TerminalClientOptions.InterfaceDensity.BALANCED,
                TerminalClientOptions.TerminalZoom.ZOOM_150,
                false);
        helper.assertTrue(zoom50.panelX() == zoom100.panelX() && zoom100.panelX() == zoom150.panelX()
                        && zoom50.panelY() == zoom100.panelY() && zoom100.panelY() == zoom150.panelY()
                        && zoom50.panelW() == zoom100.panelW() && zoom100.panelW() == zoom150.panelW()
                        && zoom50.panelH() == zoom100.panelH() && zoom100.panelH() == zoom150.panelH(),
                "Terminal zoom should not resize or move the outer shell");
        helper.assertTrue(zoom50.contentX() == zoom100.contentX() && zoom100.contentX() == zoom150.contentX()
                        && zoom50.contentY() == zoom100.contentY() && zoom100.contentY() == zoom150.contentY()
                        && zoom50.contentW() == zoom100.contentW() && zoom100.contentW() == zoom150.contentW()
                        && zoom50.contentH() == zoom100.contentH() && zoom100.contentH() == zoom150.contentH(),
                "Terminal zoom should keep the outer content frame fixed");
        helper.assertTrue(zoom50.renderContentX() < zoom100.renderContentX()
                        && zoom100.renderContentX() < zoom150.renderContentX()
                        && zoom50.renderContentW() > zoom100.renderContentW()
                        && zoom100.renderContentW() > zoom150.renderContentW(),
                "Terminal zoom should affect the padded tab-rendered content viewport");
        for (EchoTerminalScreen.LayoutMetrics metrics : List.of(zoom50, zoom100, zoom150)) {
            helper.assertTrue(metrics.panelW() > 0 && metrics.panelH() > 0,
                    "Terminal shell dimensions should stay positive");
            helper.assertTrue(metrics.contentW() > 0 && metrics.contentH() > 0,
                    "Terminal content frame dimensions should stay positive");
            helper.assertTrue(metrics.renderContentW() > 0 && metrics.renderContentH() > 0,
                    "Terminal rendered content dimensions should stay positive");
        }
        helper.succeed();
    }

    private static void terminalVisualPolishLayout(GameTestHelper helper) {
        helper.assertTrue(Arrays.asList(TerminalLayoutProfile.values()).equals(List.of(
                        TerminalLayoutProfile.COMPACT_STACK,
                        TerminalLayoutProfile.MEDIUM_CAROUSEL,
                        TerminalLayoutProfile.APP_HUB)),
                "Terminal layout profiles should preserve compact, medium, and app hub breakpoints");
        helper.assertTrue(Arrays.asList(TerminalPageLayout.values()).containsAll(List.of(
                        TerminalPageLayout.DASHBOARD_GRID,
                        TerminalPageLayout.LIST_DETAIL,
                        TerminalPageLayout.HERO_DASHBOARD,
                        TerminalPageLayout.COMMAND_PANEL,
                        TerminalPageLayout.COMPACT_STACK)),
                "Terminal page layouts should expose all visual polish templates");
        helper.assertTrue(BuiltinTerminalTabs.addonConfigControlsStackForTests(260, EchoConfigValueKind.INTEGER),
                "Narrow config rows should stack numeric controls below copy");
        helper.assertFalse(BuiltinTerminalTabs.addonConfigControlsStackForTests(560, EchoConfigValueKind.ENUM),
                "Wide enum config rows should keep controls right-aligned");
        int narrowText = BuiltinTerminalTabs.addonConfigRowHeightForTests(260, EchoConfigValueKind.STRING, true);
        int wideToggle = BuiltinTerminalTabs.addonConfigRowHeightForTests(560, EchoConfigValueKind.BOOLEAN, false);
        helper.assertTrue(narrowText > wideToggle,
                "Stacked text config rows with badges should reserve more height than simple wide toggles");
        for (int width : List.of(160, 260, 420, 720)) {
            helper.assertTrue(TerminalUi.responsiveControlWidth(width, true) > 0,
                    "Responsive text controls should always reserve positive width");
            helper.assertTrue(TerminalUi.responsiveControlRowHeight(width, true, true) > 0,
                    "Responsive text rows should always reserve positive height");
            helper.assertTrue(TerminalUi.responsiveControlRowHeight(width, false, false) > 0,
                    "Responsive toggle rows should always reserve positive height");
        }

        EchoConfigRegistry.withClearedForTests(() -> {
            AtomicInteger serverCount = new AtomicInteger(2);
            AtomicInteger clientCount = new AtomicInteger(3);
            EchoConfigRegistry.register(EchoConfigProvider.of(EchoTerminal.MODID, () -> new EchoConfigModule(
                    EchoTerminal.MODID,
                    "ECHO Terminal",
                    List.of(
                            new EchoConfigCategory("server", "Server", List.of(
                                    EchoConfigEntry.intEntry("server_count", "Server Count", "",
                                            EchoConfigSide.COMMON, 2, 0, 10, serverCount::get, serverCount::set,
                                            null, false, true, false))),
                            new EchoConfigCategory("client", "Client", List.of(
                                    EchoConfigEntry.intEntry("client_count", "Client Count", "",
                                            EchoConfigSide.CLIENT, 3, 0, 10, clientCount::get, clientCount::set,
                                            null, true, false, false)))))));
            TerminalConfigClientState.apply(new TerminalConfigSyncPacket(
                    EchoConfigRegistry.snapshots(EchoConfigSide.COMMON), "Layout snapshot ready."));
        helper.assertTrue(BuiltinTerminalTabs.addonConfigSideTitlesForTests(EchoTerminal.MODID)
                            .equals(List.of("Server/Common", "Client Local")),
                    "Addon config polish should keep server/common before client-local sections");
        });
        TerminalConfigClientState.apply(null);
        TerminalClientOptions.VisualLevel previousVisualLevel = TerminalClientOptions.visualLevel;
        boolean previousReducedMotion = TerminalClientOptions.reducedMotion;
        try {
            helper.assertTrue(TerminalRenderCoreClientIntegration.screenProfileForTests()
                            .equals(id("screen/terminal_hud")),
                    "RenderCore terminal screen compat should keep the terminal HUD profile id");
            TerminalClientOptions.visualLevel = TerminalClientOptions.VisualLevel.MINIMAL;
            TerminalClientOptions.reducedMotion = false;
            helper.assertFalse(TerminalRenderCoreClientIntegration.shouldRenderScreenAccentForTests(),
                    "Minimal terminal visuals should skip the RenderCore screen accent");
            TerminalClientOptions.visualLevel = TerminalClientOptions.VisualLevel.BALANCED;
            helper.assertTrue(TerminalRenderCoreClientIntegration.shouldRenderScreenAccentForTests(),
                    "Balanced terminal visuals should allow the subtle RenderCore screen accent");
            RenderCoreScreenFrameOptions normalFrame =
                    TerminalRenderCoreClientIntegration.screenFrameOptionsForTests(false);
            helper.assertTrue(normalFrame.style() == RenderCoreScreenChromeStyle.TERMINAL
                            && !normalFrame.drawScanlines()
                            && normalFrame.chromaticEdge()
                            && !normalFrame.glassGlints(),
                    "Balanced terminal RenderCore chrome should use the terminal cyberglass preset with clean glass and no scanlines");
            RenderCoreScreenFrameOptions reducedFrame =
                    TerminalRenderCoreClientIntegration.screenFrameOptionsForTests(true);
            helper.assertTrue(reducedFrame.style() == RenderCoreScreenChromeStyle.TERMINAL
                            && !reducedFrame.drawScanlines()
                            && !reducedFrame.edgeGlow()
                            && !reducedFrame.glassGlints()
                            && !reducedFrame.chromaticEdge(),
                    "Reduced-motion terminal RenderCore chrome should disable animated glass accents");
        } finally {
            TerminalClientOptions.visualLevel = previousVisualLevel;
            TerminalClientOptions.reducedMotion = previousReducedMotion;
        }
        helper.succeed();
    }

    private static void terminalResourceNameContracts(GameTestHelper helper) {
        Path root = Path.of("addons", "echoterminal", "src", "main", "resources",
                "assets", EchoTerminal.MODID, "textures", "gui");
        if (Files.isDirectory(root)) {
            try (var paths = Files.walk(root)) {
                List<String> unsafe = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".png"))
                        .map(root::relativize)
                        .map(Path::toString)
                        .map(path -> path.replace('\\', '/'))
                        .filter(path -> !path.matches("[a-z0-9_./-]+"))
                        .toList();
                helper.assertTrue(unsafe.isEmpty(),
                        "Terminal GUI texture names must be lowercase identifier-safe: " + unsafe);
            } catch (IOException exception) {
                helper.assertTrue(false, "Terminal resource name scan failed: " + exception.getMessage());
            }
        }
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
            BuiltinTerminalCommonIntegration.registerActionsForTests();
            helper.assertTrue(TerminalActionRegistry.handle(null,
                            BuiltinTerminalCommonIntegration.REWARD_INBOX,
                            BuiltinTerminalCommonIntegration.CLAIM_REWARDS,
                            ""),
                    "Built-in reward inbox action should be registered from common setup");
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
            Identifier reclaimPowerId = Identifier.fromNamespaceAndPath("echoindustrialnexus", "mission/reclaim_power");
            Identifier lockedFutureId = Identifier.fromNamespaceAndPath("echoindustrialnexus", "mission/locked_future");
            Identifier agricultureRouteId = Identifier.fromNamespaceAndPath("echoagriculturereclamation", "mission/recover_seed");
            Identifier hiddenRouteId = Identifier.fromNamespaceAndPath("echoagriculturereclamation", "mission/internal_hidden");
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
                            reclaimPowerId,
                            "Reclaim Power",
                            "Stage 1",
                            "Factory",
                            "Production",
                            TerminalMissionRole.MAIN,
                            TerminalMissionStatus.UNLOCKED,
                            List.of(TerminalMissionAction.enabled("scan_factory", "SCAN FACTORY"))),
                            new ConfiguredMission(
                                    lockedFutureId,
                                    "Locked Future",
                                    "Stage 2",
                                    "Factory",
                                    "Production",
                                    TerminalMissionRole.MAIN,
                                    TerminalMissionStatus.LOCKED,
                                    List.of(TerminalMissionAction.disabled("scan_factory", "SCAN FACTORY",
                                            "Factory uplink offline."))))));
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
            TerminalMissionRegistry.register(new PlacedMissionProvider(
                    Identifier.fromNamespaceAndPath("echoagriculturereclamation", "field_reclamation"),
                    "FIELD > Reclamation",
                    4,
                    List.of(new ConfiguredMission(
                            agricultureRouteId,
                            "Recover Seed",
                            "Unsorted Local Phase",
                            "Field",
                            "Seed",
                            TerminalMissionRole.MAIN,
                            TerminalMissionStatus.UNLOCKED,
                            List.of(TerminalMissionAction.enabled("field_report", "FIELD REPORT"))),
                            new ConfiguredMission(
                                    hiddenRouteId,
                                    "Internal Hidden",
                                    "Unsorted Local Phase",
                                    "Field",
                                    "Hidden",
                                    TerminalMissionRole.MAIN,
                                    TerminalMissionStatus.UNLOCKED,
                                    List.of())),
                    Map.of(
                            agricultureRouteId, TerminalMissionRoutePlacement.optional(2, 42),
                            hiddenRouteId, TerminalMissionRoutePlacement.hidden())));
            TerminalMissionRegistry.register(new ThrowingMissionsProvider(id("throwing_missions"), 5));

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
            TerminalMissionDefinition agricultureRoute = missions.stream()
                    .filter(definition -> definition.id().equals(agricultureRouteId))
                    .findFirst()
                    .orElseThrow();
            helper.assertTrue(agricultureRoute.phaseOrder() == 2
                            && "Phase 02".equals(agricultureRoute.phaseTitle())
                            && agricultureRoute.missionOrder() == 42,
                    "Explicit route placement should override local phase/order metadata");
            TerminalMissionSnapshot agricultureSnapshot =
                    MainSurvivalQuestProvider.INSTANCE.snapshot(null, agricultureRouteId);
            helper.assertTrue(MainSurvivalQuestProvider.INSTANCE.role(null, agricultureRoute, agricultureSnapshot)
                            == TerminalMissionRole.OPTIONAL,
                    "Explicit route placement should override the source role for aggregate gating");
            TerminalMissionPresentation agriculturePresentation =
                    MainSurvivalQuestProvider.INSTANCE.presentation(null, agricultureRoute, agricultureSnapshot);
            String aggregateCopy = String.join(" ",
                    agricultureSnapshot.actionHint(),
                    agricultureSnapshot.unlockReason(),
                    agriculturePresentation.nextStep(),
                    agriculturePresentation.objectiveSummary(),
                    agriculturePresentation.routeHint(),
                    String.join(" ", agriculturePresentation.tags()));
            String noisySourceLabel = "Source:";
            String legacyCommandHint = "Command unlocks " + "after";
            helper.assertFalse(aggregateCopy.contains(noisySourceLabel) || aggregateCopy.contains(legacyCommandHint),
                    "Aggregate Survival Route player copy should hide provider/source noise");
            helper.assertFalse(missions.stream().anyMatch(definition -> definition.id().equals(hiddenRouteId)),
                    "Hidden explicit route placement should omit internal records from the aggregate Survival Route");
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
            TerminalMissionSnapshot reclaimSnapshot = MainSurvivalQuestProvider.INSTANCE.snapshot(null, reclaimPowerId);
            helper.assertTrue(reclaimSnapshot.actions().stream()
                            .anyMatch(action -> action.enabled() && "scan_factory".equals(action.id())),
                    "Survival route should preserve enabled child mission actions");
            helper.assertTrue(MainSurvivalQuestProvider.INSTANCE.handleAction(null, reclaimPowerId, "scan_factory"),
                    "Survival route should delegate child actions back to the source provider");
            TerminalMissionSnapshot lockedFutureSnapshot = MainSurvivalQuestProvider.INSTANCE.snapshot(null, lockedFutureId);
            helper.assertTrue(lockedFutureSnapshot.actions().stream().noneMatch(TerminalMissionAction::enabled),
                    "Locked Survival Route records should not expose enabled actions");
            helper.assertFalse(MainSurvivalQuestProvider.INSTANCE.handleAction(null, lockedFutureId, "scan_factory"),
                    "Survival route should not delegate disabled future actions");

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
        TerminalMissionSnapshot open = VanillaJourneyProvider.INSTANCE.snapshot(player, missionId);
        helper.assertTrue(open.actions().stream().anyMatch(action -> action.enabled()
                        && "refresh".equals(action.id()) && "SYNC ADVANCEMENTS".equals(action.label())),
                "Open Baseline records should expose advancement sync before cache claims");

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

    @SuppressWarnings("removal")
    private static void terminalBaselineAutoRefresh(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Identifier missionId = Identifier.withDefaultNamespace("story/mine_stone");
        VanillaJourneyData data = VanillaJourneyData.get(player);
        data.setCompleted(List.of());
        helper.assertFalse(data.isCompleted(missionId),
                "Baseline test should start with unsynced advancement data");

        AdvancementHolder holder = player.level().getServer().getAdvancements().get(missionId);
        helper.assertTrue(holder != null, "Tracked vanilla advancement should exist on the test server");
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        List<String> remainingCriteria = new ArrayList<>();
        for (String criterion : progress.getRemainingCriteria()) {
            remainingCriteria.add(criterion);
        }
        helper.assertFalse(remainingCriteria.isEmpty(),
                "Tracked vanilla advancement should have criteria to award");
        for (String criterion : remainingCriteria) {
            player.getAdvancements().award(holder, criterion);
        }

        helper.assertTrue(VanillaJourneyData.get(player).isCompleted(missionId),
                "Awarding a tracked vanilla advancement should automatically sync Baseline progress");
        helper.assertTrue(VanillaJourneyProvider.INSTANCE.snapshot(player, missionId).status()
                        == TerminalMissionStatus.CLAIMABLE,
                "Automatically synced Baseline progress should make the cache claimable");
        helper.succeed();
    }

    private static void terminalBaselineDataDefinitions(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        List<TerminalMissionDefinition> definitions = VanillaJourneyProvider.INSTANCE.missions(player);
        helper.assertTrue(definitions.size() == 39,
                "Baseline should load the full vanilla advancement route from bundled data definitions");

        TerminalMissionDefinition root = definitions.stream()
                .filter(definition -> definition.id().equals(Identifier.withDefaultNamespace("story/root")))
                .findFirst()
                .orElseThrow();
        TerminalMissionSnapshot rootSnapshot = VanillaJourneyProvider.INSTANCE.snapshot(player, root.id());
        helper.assertTrue(root.rewards().isEmpty() && root.requirements().isEmpty(),
                "Data-defined Baseline roots should remain guide records without rewards");
        helper.assertTrue(VanillaJourneyProvider.INSTANCE.role(player, root, rootSnapshot) == TerminalMissionRole.REFERENCE,
                "Data-defined Baseline roots should expose reference roles");

        Identifier stoneId = Identifier.withDefaultNamespace("story/mine_stone");
        TerminalMissionDefinition stone = definitions.stream()
                .filter(definition -> definition.id().equals(stoneId))
                .findFirst()
                .orElseThrow();
        helper.assertTrue("Stone Age".equals(stone.title())
                        && "story".equals(stone.phaseId())
                        && stone.phaseOrder() == 0
                        && stone.missionOrder() == 1,
                "Data-defined Baseline missions should preserve title and ordering metadata");
        helper.assertTrue("Task Cache".equals(stone.difficulty())
                        && stone.icon().getItem() == Items.COBBLESTONE,
                "Data-defined Baseline missions should preserve tier labels and icons");
        helper.assertTrue(stone.rewards().stream().anyMatch(reward ->
                        reward.stack().getItem() == Items.BREAD && reward.stack().getCount() == 4),
                "Data-defined task cache should include bread rewards");
        helper.assertTrue(stone.rewards().stream().anyMatch(reward ->
                        reward.stack().getItem() == Items.TORCH && reward.stack().getCount() == 12),
                "Data-defined task cache should include torch rewards");
        helper.assertTrue(stone.rewards().stream().anyMatch(reward ->
                        reward.stack().getItem() == Items.EXPERIENCE_BOTTLE && reward.stack().getCount() == 2),
                "Data-defined task cache should include experience bottle rewards");
        helper.assertTrue(VanillaJourneyProvider.INSTANCE.tracksAdvancement(stoneId),
                "Data-defined Baseline missions should be tracked for server refresh");
        helper.assertFalse(VanillaJourneyProvider.INSTANCE.tracksAdvancement(id("not_a_vanilla_advancement")),
                "Unknown advancement ids should not be tracked by the Baseline provider");

        Identifier optionalId = Identifier.withDefaultNamespace("nether/all_effects");
        TerminalMissionDefinition optional = definitions.stream()
                .filter(definition -> definition.id().equals(optionalId))
                .findFirst()
                .orElseThrow();
        TerminalMissionSnapshot optionalSnapshot = VanillaJourneyProvider.INSTANCE.snapshot(player, optionalId);
        helper.assertTrue(VanillaJourneyProvider.INSTANCE.role(player, optional, optionalSnapshot)
                        == TerminalMissionRole.OPTIONAL,
                "Data-defined Baseline roles should preserve optional high-risk records");
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
        for (TerminalClientOptions.MissionView legacyView : TerminalClientOptions.MissionView.values()) {
            TerminalClientOptions.resetMissionViewForTests(legacyView);
            helper.assertTrue(TerminalClientOptions.missionView == TerminalClientOptions.MissionView.GUIDED,
                    "Legacy mission view config values should normalize to GUIDED");
            browser.contentHeight(context);
            helper.assertTrue(provider.snapshotCalls.get() == provider.missionCount(),
                    "Legacy mission view aliases should not invalidate the guided-only browser cache");
        }

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

        TerminalClientOptions.resetMissionViewForTests(TerminalClientOptions.MissionView.VISUAL_RPG);
        browser.onSelected(context);
        List<String> phases = browser.phaseDebugRowsForTests(context);
        helper.assertTrue(browser.visibleMissionCountForTests(context) == browser.allMissionCountForTests(context),
                "Mission browser should keep every record visible now that roadmap filters are removed");
        helper.assertTrue("ACTIONS".equals(browser.stickyActionsTitleForTests()),
                "Mission details should label the sticky footer as ACTIONS");
        helper.assertFalse(browser.emptyRequirementsCopyForTests().contains("COMMAND")
                        || browser.metRequirementsCopyForTests().contains("COMMAND"),
                "Requirement helper copy should not point players back to a Command footer");
        int compactTreeHeight = browser.treePaneHeightForTests(context, 180);
        int wideTreeHeight = browser.treePaneHeightForTests(context, 640);
        helper.assertTrue(compactTreeHeight == wideTreeHeight,
                "Mission browser tree height should not reserve responsive filter or expand-control rows");
        Identifier initialSelection = browser.selectedMissionIdForTests(context);
        helper.assertFalse(browser.keyCodeForTests(context, GLFW.GLFW_KEY_LEFT),
                "Left arrow should no longer cycle hidden mission filters");
        helper.assertFalse(browser.keyCodeForTests(context, GLFW.GLFW_KEY_RIGHT),
                "Right arrow should no longer cycle hidden mission filters");
        helper.assertTrue(initialSelection.equals(browser.selectedMissionIdForTests(context)),
                "Removing hidden mission filters should leave arrow keys from changing the selected record");
        helper.assertFalse(browser.charTyped(context, null),
                "Typing should no longer feed a hidden mission search box");
        helper.assertTrue(browser.visibleMissionCountForTests(context) == 5,
                "Typing with hidden search removed should not hide mixed-status mission records");
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
        int focusViewportHeight = 80;
        int focusRowOffset = browser.selectedRowOffsetForTests(context);
        int maxTreeScroll = browser.treeMaxScrollForTests(context, focusViewportHeight);
        helper.assertTrue(maxTreeScroll > focusRowOffset,
                "Mission browser test setup should allow the selected full-roadmap row to top-align");
        helper.assertTrue(browser.applyTreeFocusForTests(context, focusViewportHeight) == focusRowOffset,
                "Opening the mission browser should top-align the current ready mission in the full roadmap");
        helper.assertTrue(browser.keyCodeForTests(context, GLFW.GLFW_KEY_DOWN),
                "Down arrow should move from the ready mission to the next visible mission");
        helper.assertTrue(phase00Optional.equals(browser.selectedMissionIdForTests(context)),
                "Down arrow should select the next mission in the expanded roadmap");
        int scrollBeforeNavigationFocus = browser.treeScrollForTests();
        int navigatedRowOffset = browser.selectedRowOffsetForTests(context);
        int scrollAfterNavigationFocus = browser.applyTreeFocusForTests(context, focusViewportHeight);
        helper.assertTrue(scrollAfterNavigationFocus == scrollBeforeNavigationFocus,
                "Keyboard navigation should keep an already visible selected mission in place");
        helper.assertFalse(scrollAfterNavigationFocus == navigatedRowOffset,
                "Keyboard navigation should not top-align every newly selected mission");
        int headerHeight = browser.detailHeaderHeightForTests(context, phase02Main);
        helper.assertTrue(headerHeight >= 92 && headerHeight <= 104,
                "Guided mission browser detail header should stay compact for next-step-first scanning");
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

    private static void terminalMissionHudNotifications(GameTestHelper helper) {
        TerminalMissionRegistry.withClearedForTests(() -> {
            TerminalMissionHudController controller = new TerminalMissionHudController();
            MutableHudMissionProvider provider = new MutableHudMissionProvider(id("hud_chapter"), "HUD Chapter", 25);
            Identifier relay = id("hud_relay");
            Identifier camp = id("hud_camp");
            Identifier burstA = id("hud_burst_a");
            Identifier burstB = id("hud_burst_b");
            Identifier burstC = id("hud_burst_c");
            Identifier burstD = id("hud_burst_d");
            provider.add(relay, "Repair Relay", "Relay Phase", 10, 1,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.LOCKED, 0.0F);
            provider.add(camp, "Stabilize Camp", "Camp Phase", 0, 1,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.UNLOCKED, 0.45F);
            provider.add(burstA, "Burst A", "Factory Phase", 20, 1,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.LOCKED, 0.0F);
            provider.add(burstB, "Burst B", "Factory Phase", 20, 2,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.LOCKED, 0.0F);
            provider.add(burstC, "Burst C", "Factory Phase", 20, 3,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.LOCKED, 0.0F);
            provider.add(burstD, "Burst D", "Factory Phase", 20, 4,
                    TerminalMissionRole.MAIN, TerminalMissionStatus.LOCKED, 0.0F);

            TerminalMissionRegistry.register(MainSurvivalQuestProvider.INSTANCE);
            TerminalMissionRegistry.register(provider);
            TerminalMissionRegistry.register(new ThrowingMissionsProvider(id("hud_throwing"), 50));

            controller.scanForTests(null, 100L);
            helper.assertTrue(controller.drainQueuedNoticesForTests().isEmpty(),
                    "First mission HUD scan should baseline without startup notices");

            provider.set(relay, TerminalMissionStatus.UNLOCKED, 0.25F);
            controller.scanForTests(null, 120L);
            List<TerminalMissionNotice> relayNotices = controller.drainQueuedNoticesForTests();
            helper.assertTrue(hasNotice(relayNotices, TerminalMissionNoticeType.MISSION_AVAILABLE),
                    "Locked-to-unlocked missions should raise a mission available notice");
            helper.assertTrue(hasNotice(relayNotices, TerminalMissionNoticeType.PHASE_ONLINE),
                    "The first active mission in a phase should raise a phase online notice");
            helper.assertFalse(relayNotices.stream()
                            .anyMatch(notice -> MainSurvivalQuestProvider.CHAPTER_ID.equals(notice.chapterId())),
                    "Mission HUD should skip the aggregate Survival Route provider");

            provider.set(relay, TerminalMissionStatus.LOCKED, 0.0F);
            controller.scanForTests(null, 125L);
            provider.set(relay, TerminalMissionStatus.UNLOCKED, 0.25F);
            controller.scanForTests(null, 130L);
            helper.assertTrue(controller.drainQueuedNoticesForTests().isEmpty(),
                    "Repeated mission available signals should respect the notice cooldown");

            provider.set(camp, TerminalMissionStatus.COMPLETED, 1.0F);
            controller.scanForTests(null, 160L);
            helper.assertTrue(hasNotice(controller.drainQueuedNoticesForTests(), TerminalMissionNoticeType.OBJECTIVE_READY),
                    "Unlocked-to-completed missions should raise an objective ready notice");

            provider.set(relay, TerminalMissionStatus.CLAIMABLE, 1.0F);
            controller.scanForTests(null, 220L);
            helper.assertTrue(hasNotice(controller.drainQueuedNoticesForTests(), TerminalMissionNoticeType.CACHE_READY),
                    "Claimable missions should raise a cache ready notice");

            provider.set(relay, TerminalMissionStatus.CLAIMED, 1.0F);
            controller.scanForTests(null, 300L);
            helper.assertTrue(hasNotice(controller.drainQueuedNoticesForTests(), TerminalMissionNoticeType.CACHE_CLAIMED),
                    "Claimed rewards should raise a cache claimed notice");

            provider.set(burstA, TerminalMissionStatus.UNLOCKED, 0.1F);
            provider.set(burstB, TerminalMissionStatus.UNLOCKED, 0.1F);
            provider.set(burstC, TerminalMissionStatus.UNLOCKED, 0.1F);
            provider.set(burstD, TerminalMissionStatus.UNLOCKED, 0.1F);
            controller.scanForTests(null, 400L);
            List<TerminalMissionNotice> burstNotices = controller.drainQueuedNoticesForTests();
            helper.assertTrue(burstNotices.size() == 1
                            && burstNotices.get(0).type() == TerminalMissionNoticeType.SUMMARY,
                    "Large mission update bursts should collapse into a single summary card");
        });
        helper.succeed();
    }

    private static boolean hasNotice(List<TerminalMissionNotice> notices, TerminalMissionNoticeType type) {
        return notices.stream().anyMatch(notice -> notice.type() == type);
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

    private record DummyRecipeProvider(Identifier providerId, int order) implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return providerId;
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            return List.of(new TerminalRecipeCategory(
                    ModGameTests.id(providerId.getPath().replace("_provider", "_category")),
                    providerId.getPath(),
                    new ItemStack(Items.CRAFTING_TABLE),
                    0xFFFFD166,
                    order));
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            Identifier category = ModGameTests.id(providerId.getPath().replace("_provider", "_category"));
            return List.of(new TerminalRecipeEntry(
                    ModGameTests.id(providerId.getPath() + "/recipe"),
                    category,
                    "Recipe " + providerId.getPath(),
                    new ItemStack(Items.CRAFTING_TABLE),
                    List.of(TerminalRecipeSlot.input(new ItemStack(Items.STICK)),
                            TerminalRecipeSlot.output(new ItemStack(Items.APPLE))),
                    List.of(TerminalRecipeNote.info("Test recipe")),
                    20,
                    false));
        }
    }

    private record DuplicateRecipeProvider() implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return ModGameTests.id("duplicate_provider");
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            return List.of(new TerminalRecipeCategory(
                    ModGameTests.id("alpha_category"),
                    "duplicate category",
                    new ItemStack(Items.CRAFTING_TABLE),
                    0xFFFFD166,
                    5));
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            return List.of(new TerminalRecipeEntry(
                    ModGameTests.id("alpha_provider/recipe"),
                    ModGameTests.id("alpha_category"),
                    "Duplicate Recipe",
                    new ItemStack(Items.CRAFTING_TABLE),
                    List.of(TerminalRecipeSlot.input(new ItemStack(Items.STICK)),
                            TerminalRecipeSlot.output(new ItemStack(Items.APPLE))),
                    List.of(),
                    20,
                    false));
        }
    }

    private record ThrowingRecipeProvider() implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return ModGameTests.id("throwing_provider");
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            throw new IllegalStateException("test terminal recipe category failure");
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            throw new IllegalStateException("test terminal recipe list failure");
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

    private static final class MutableHudMissionProvider implements TerminalMissionProvider {
        private final Identifier chapterId;
        private final String title;
        private final int order;
        private final Map<Identifier, MutableHudMission> missions = new LinkedHashMap<>();

        private MutableHudMissionProvider(Identifier chapterId, String title, int order) {
            this.chapterId = chapterId;
            this.title = title;
            this.order = order;
        }

        void add(Identifier missionId, String missionTitle, String phaseTitle, int phaseOrder, int missionOrder,
                TerminalMissionRole role, TerminalMissionStatus status, float progress) {
            missions.put(missionId, new MutableHudMission(
                    missionId, missionTitle, phaseTitle, phaseOrder, missionOrder, role, status, progress));
        }

        void set(Identifier missionId, TerminalMissionStatus status, float progress) {
            MutableHudMission mission = missions.get(missionId);
            if (mission != null) {
                mission.status = status;
                mission.progress = progress;
            }
        }

        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(chapterId, title, "HUD notice test provider", order, 0xFF66D9FF, true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            return missions.values().stream()
                    .map(mission -> new TerminalMissionDefinition(
                            mission.id,
                            chapterId,
                            mission.phaseTitle.toLowerCase(java.util.Locale.ROOT).replace(' ', '_'),
                            mission.phaseTitle,
                            mission.phaseOrder,
                            mission.missionOrder,
                            mission.title,
                            mission.title + " briefing",
                            mission.title + " guide",
                            "HUD Test",
                            "Test",
                            new ItemStack(Items.COMPASS),
                            List.of(),
                            List.of(),
                            List.of()))
                    .toList();
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            MutableHudMission mission = missions.get(missionId);
            return mission == null
                    ? new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                            "LOCKED", "Missing HUD test mission.", "Missing HUD test mission.", List.of())
                    : new TerminalMissionSnapshot(mission.id, mission.status, mission.progress,
                            mission.status.name(), "", mission.title + " next step",
                            mission.status == TerminalMissionStatus.CLAIMABLE
                                    ? List.of(TerminalMissionAction.enabled("claim_reward", "CLAIM CACHE"))
                                    : List.of());
        }

        @Override
        public TerminalMissionPresentation presentation(
                Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
            return new TerminalMissionPresentation(
                    definition.title(),
                    definition.briefing(),
                    snapshot.actionHint(),
                    definition.phaseTitle(),
                    snapshot.status().name().toLowerCase(java.util.Locale.ROOT),
                    List.of("HUD Test"),
                    "");
        }

        @Override
        public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
            MutableHudMission mission = missions.get(definition.id());
            return mission == null ? TerminalMissionRole.MAIN : mission.role;
        }
    }

    private static final class MutableHudMission {
        private final Identifier id;
        private final String title;
        private final String phaseTitle;
        private final int phaseOrder;
        private final int missionOrder;
        private final TerminalMissionRole role;
        private TerminalMissionStatus status;
        private float progress;

        private MutableHudMission(Identifier id, String title, String phaseTitle, int phaseOrder, int missionOrder,
                TerminalMissionRole role, TerminalMissionStatus status, float progress) {
            this.id = id;
            this.title = title;
            this.phaseTitle = phaseTitle;
            this.phaseOrder = phaseOrder;
            this.missionOrder = missionOrder;
            this.role = role;
            this.status = status;
            this.progress = progress;
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

        @Override
        public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
            return configuredMissions.stream()
                    .filter(candidate -> candidate.id().equals(missionId))
                    .flatMap(candidate -> candidate.actions().stream())
                    .anyMatch(action -> action.enabled() && action.id().equals(actionId));
        }
    }

    private static final class PlacedMissionProvider implements TerminalMissionProvider {
        private final ConfigurableMissionProvider delegate;
        private final Map<Identifier, TerminalMissionRoutePlacement> placements;

        private PlacedMissionProvider(
                Identifier chapterId,
                String title,
                int order,
                List<ConfiguredMission> configuredMissions,
                Map<Identifier, TerminalMissionRoutePlacement> placements) {
            this.delegate = new ConfigurableMissionProvider(chapterId, title, order, configuredMissions);
            this.placements = Map.copyOf(placements);
        }

        @Override
        public TerminalMissionChapter chapter() {
            return delegate.chapter();
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
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

        @Override
        public Optional<TerminalMissionRoutePlacement> routePlacement(
                Player player,
                TerminalMissionDefinition definition,
                TerminalMissionSnapshot snapshot,
                TerminalMissionRole role) {
            return definition == null ? Optional.empty() : Optional.ofNullable(placements.get(definition.id()));
        }

        @Override
        public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
            return delegate.handleAction(player, missionId, actionId);
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

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoTerminal.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, path);
    }

    private static EchoDiscoveryEntry discoveryEntry(
            String path, EchoDiscoveryCategory category, String title, int sortOrder) {
        return new EchoDiscoveryEntry(
                id(path),
                id("test_chapter"),
                category,
                title,
                "Unknown Signal",
                "Find this signal in the field.",
                title + " summary.",
                null,
                null,
                0xFF66E8FF,
                null,
                sortOrder);
    }

    private static boolean recordDiscoveredForTest(Player player, Identifier id) {
        return EchoDiscoveryData.get(player).discover(id);
    }

    private static boolean classpathResourceExists(Identifier id) {
        if (id == null) {
            return false;
        }
        String path = "assets/" + id.getNamespace() + "/" + id.getPath();
        return ModGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static boolean pngHasTransparentCorners(Identifier id) {
        if (id == null) {
            return false;
        }
        String path = "assets/" + id.getNamespace() + "/" + id.getPath();
        try (InputStream stream = ModGameTests.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return false;
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null || !image.getColorModel().hasAlpha()) {
                return false;
            }
            return alphaAt(image, 0, 0) == 0
                    && alphaAt(image, image.getWidth() - 1, 0) == 0
                    && alphaAt(image, 0, image.getHeight() - 1) == 0
                    && alphaAt(image, image.getWidth() - 1, image.getHeight() - 1) == 0;
        } catch (IOException exception) {
            return false;
        }
    }

    private static int alphaAt(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
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
