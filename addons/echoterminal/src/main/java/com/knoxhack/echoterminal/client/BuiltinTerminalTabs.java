package com.knoxhack.echoterminal.client;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiagnosticBlocker;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echocore.api.EchoFactionProfile;
import com.knoxhack.echocore.api.EchoHazardTelemetry;
import com.knoxhack.echocore.api.EchoPackMode;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.TerminalIcon;
import com.knoxhack.echoterminal.api.TerminalNavigationProfile;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import com.knoxhack.echoterminal.mission.MainSurvivalQuestProvider;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class BuiltinTerminalTabs {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Identifier DIAGNOSTICS = id("diagnostics");
    private static final Identifier MISSION_GRAPH = id("mission_graph");
    private static final Identifier ROUTE_RECORDS = id("route_records");
    private static final Identifier VITALS = id("vitals");
    private static final Identifier REWARD_INBOX = id("reward_inbox");
    private static final Identifier ADDONS = id("addons");
    private static final Identifier ARCHIVES = id("archives");
    private static final Identifier CLAIM_REWARDS = id("claim_rewards");

    private BuiltinTerminalTabs() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        registerTab(new OverviewTab(), TerminalNavigationProfile.terminal(0));
        registerTab(new MissionGraphTab(), TerminalNavigationProfile.terminal(120));
        registerTab(new RouteRecordsTab(), TerminalNavigationProfile.core(125));
        registerTab(new FactionAtlasTab(), TerminalNavigationProfile.core(128));
        registerTab(new VitalsTab(), TerminalNavigationProfile.terminal(130));
        registerTab(new RewardInboxTab(), TerminalNavigationProfile.terminal(140));
        registerTab(new MainSurvivalRouteTab(), TerminalNavigationProfile.core(170));
        registerTab(new AddonsTab(), TerminalNavigationProfile.chaptersHub(0));
        TerminalActionRegistry.register(REWARD_INBOX, CLAIM_REWARDS,
                (player, payload) -> EchoCoreServices.claimTerminalRewards(player));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("field_manual"),
                "Protocol Flow",
                "Terminal Field Interface",
                "OPEN",
                List.of(
                        "Use this terminal as the command surface for installed ECHO chapters and survival routes.",
                        "Channels collect missions, field records, drone controls, route state, and chapter status when those systems are present.",
                        "Progression stays sealed until the field route proves it; the terminal shows the clearest safe command view without opening records early."),
                false));
        registerTab(new ArchivesTab(), TerminalNavigationProfile.terminal(950));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, path);
    }

    private static void registerTab(TerminalTab tab, TerminalNavigationProfile profile) {
        TerminalTabRegistry.register(tab);
        TerminalNavigationProfiles.register(tab.descriptor().id(), profile);
    }

    public static Identifier commandDeckPriorityTabForTests(
            boolean hasBlockers, int pendingRewards, boolean hasIncompleteRoute, int chapterCount) {
        return commandDeckPriorityTab(hasBlockers, pendingRewards, hasIncompleteRoute, chapterCount);
    }

    public static Identifier commandDeckRewardActionForTests() {
        return CLAIM_REWARDS;
    }

    private static Identifier commandDeckPriorityTab(
            boolean hasBlockers, int pendingRewards, boolean hasIncompleteRoute, int chapterCount) {
        if (hasBlockers) {
            return DIAGNOSTICS;
        }
        if (pendingRewards > 0) {
            return REWARD_INBOX;
        }
        if (hasIncompleteRoute) {
            return ROUTE_RECORDS;
        }
        if (chapterCount > 0) {
            return MainSurvivalQuestProvider.TAB_ID;
        }
        return ADDONS;
    }

    private static List<EchoAddonChapter> addonChapters() {
        try {
            return EchoAddonRegistry.chapters();
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal addon chapter list failed; rendering empty chapter list.", exception);
            return List.of();
        }
    }

    private static String chapterId(EchoAddonChapter chapter) {
        try {
            String id = chapter == null ? "" : chapter.id();
            return id == null ? "" : id;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter id failed; using fallback id.", exception);
            return "";
        }
    }

    private static String chapterDisplayName(EchoAddonChapter chapter) {
        try {
            String displayName = chapter == null ? "" : chapter.displayName();
            return displayName == null || displayName.isBlank() ? "ECHO Chapter" : displayName;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter display name failed; using fallback title.", exception);
            return "ECHO Chapter";
        }
    }

    private static String chapterModId(EchoAddonChapter chapter) {
        try {
            String modId = chapter == null ? "" : chapter.modId();
            return modId == null || modId.isBlank() ? "unknown" : modId;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter mod id failed; using fallback mod id.", exception);
            return "unknown";
        }
    }

    private static String chapterSummary(EchoAddonChapter chapter) {
        try {
            String summary = chapter == null ? "" : chapter.summary();
            return summary == null || summary.isBlank() ? "No chapter briefing available." : summary;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter summary failed; using fallback summary.", exception);
            return "No chapter briefing available.";
        }
    }

    private static String chapterStatusLine(EchoAddonChapter chapter, TerminalRenderContext context) {
        try {
            String status = chapter == null ? "" : chapter.statusLine(context == null ? null : context.player());
            return status == null || status.isBlank() ? "Status signal unavailable." : status;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter status failed; using fallback status.", exception);
            return "Status signal unavailable.";
        }
    }

    private static boolean chapterAvailable(EchoAddonChapter chapter, TerminalRenderContext context) {
        try {
            return chapter != null && chapter.isAvailable(context == null ? null : context.player());
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal chapter availability failed; treating chapter as locked.", exception);
            return false;
        }
    }

    private static TerminalMissionChapter missionChapter(TerminalMissionProvider provider) {
        try {
            TerminalMissionChapter chapter = provider == null ? null : provider.chapter();
            return chapter == null
                    ? new TerminalMissionChapter(id("mission_provider"), "Unlisted Chapter", "", Integer.MAX_VALUE,
                            0xFF92F7A6, true)
                    : chapter;
        } catch (RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Terminal mission provider chapter failed; using fallback chapter.", exception);
            return new TerminalMissionChapter(id("mission_provider"), "Unlisted Chapter", "", Integer.MAX_VALUE,
                    0xFF92F7A6, true);
        }
    }

    private static final class MainSurvivalRouteTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(MainSurvivalQuestProvider.TAB_ID, "SURVIVAL ROUTE", 170, 0xFF92F7A6);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Survival Route", TerminalTabChrome.GROUP_FIELD, "SR",
                        "Main survival quest line", 170);
        private final TerminalMissionBrowser browser =
                new TerminalMissionBrowser(MainSurvivalQuestProvider.INSTANCE, descriptor.id(), true);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            browser.onSelected(context);
            context.sendAction(descriptor.id(), TerminalMissionActions.MISSION_ACTION,
                    TerminalMissionActions.payload(
                            VanillaJourneyProvider.CHAPTER_ID,
                            id("vanilla_journey_refresh"),
                            "refresh"));
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            browser.render(context, graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            return browser.mouseClicked(context, mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
            return browser.mouseScrolled(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
            return browser.keyPressed(context, event);
        }

        @Override
        public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
            return browser.charTyped(context, event);
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return browser.contentHeight(context);
        }
    }

    private static final class OverviewTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("overview"), "COMMAND DECK", 0, 0xFF66D9FF);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Command Deck", TerminalTabChrome.GROUP_PROTOCOL, "CD",
                        "Active protocol dashboard", 0);
        private final List<DeckHitbox> hitboxes = new ArrayList<>();
        private List<DeckAction> lastActions = List.of();
        private int selectedActionIndex = -1;
        private int actionRenderIndex;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            hitboxes.clear();
            lastActions = List.of();
            selectedActionIndex = -1;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            hitboxes.clear();
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            EchoPackMode packMode = EchoCoreServices.packMode(context.player());
            List<EchoDiagnosticBlocker> diagnostics = EchoCoreServices.diagnostics(context.player());
            List<EchoRouteRecord> routes = EchoCoreServices.routeRecords(context.player());
            List<EchoAddonChapter> chapters = addonChapters();
            int pendingRewards = EchoCoreServices.pendingTerminalRewardCount(context.player());
            EchoRouteRecord route = firstIncompleteRoute(routes);
            DeckAction priority = priorityAction(diagnostics, route, pendingRewards, chapters.size());
            List<DeckAction> shortcuts = readyActions(diagnostics, route, pendingRewards, chapters.size());
            List<DeckAction> metrics = metricActions(packMode, diagnostics, routes, pendingRewards, chapters.size());
            lastActions = deckActions(priority, shortcuts, metrics);
            actionRenderIndex = 0;
            if (selectedActionIndex >= lastActions.size()) {
                selectedActionIndex = -1;
            }

            int heroH = Math.min(54, Math.max(40, h / 7));
            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.OVERVIEW_PROTOCOL_DASHBOARD,
                    x, y, w, heroH, descriptor.accentColor(), 0.62F, true, TerminalUi.ImageFit.COVER);
            y += heroH + 8;
            TerminalUi.sectionHeader(context, graphics, "COMMAND DECK", packMode.displayName(), x, y, w, descriptor.accentColor());
            int cy = y + 20;

            int topGap = w >= 620 ? 12 : 8;
            if (w >= 620) {
                int priorityW = Math.max(310, w * 52 / 100);
                int pulseX = x + priorityW + topGap;
                int pulseW = w - priorityW - topGap;
                drawPriorityPanel(context, graphics, priority, x, cy, priorityW, 104, mouseX, mouseY);
                drawPulsePanel(context, graphics, packMode, diagnostics, routes, pendingRewards, chapters.size(),
                        pulseX, cy, pulseW, 104);
                cy += 114;
            } else {
                drawPriorityPanel(context, graphics, priority, x, cy, w, 100, mouseX, mouseY);
                cy += 108;
                drawPulsePanel(context, graphics, packMode, diagnostics, routes, pendingRewards, chapters.size(),
                        x, cy, w, 96);
                cy += 106;
            }

            cy = drawReadyRow(context, graphics, shortcuts, x, cy, w, mouseX, mouseY) + 10;
            drawMetricGrid(context, graphics, metrics, x, cy, w, mouseX, mouseY);
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            int w = context.contentWidth();
            int readyColumns = w >= 640 ? 3 : w >= 410 ? 2 : 1;
            int readyRows = (3 + readyColumns - 1) / readyColumns;
            int metricRows = (int) Math.ceil(7.0D / dashboardColumns(w));
            int readyHeight = 18 + readyRows * 42 + 10;
            int base = w >= 620
                    ? 54 + 114 + readyHeight + 18 + metricRows * 52
                    : 54 + 108 + 106 + readyHeight + 18 + metricRows * 52;
            return Math.max(context.contentHeight(), base + 20);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            for (DeckHitbox hitbox : List.copyOf(hitboxes)) {
                if (TerminalUi.inside(mouseX, mouseY, hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                    selectedActionIndex = hitbox.index();
                    lastActions.get(hitbox.index()).execute(context);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
            int key = event.key();
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
                if (selectedActionIndex >= 0 && selectedActionIndex < lastActions.size()) {
                    lastActions.get(selectedActionIndex).execute(context);
                    return true;
                }
                if (!lastActions.isEmpty()) {
                    lastActions.get(0).execute(context);
                    return true;
                }
            }
            if (selectedActionIndex >= 0 && !lastActions.isEmpty()
                    && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_UP
                            || key == GLFW.GLFW_KEY_A || key == GLFW.GLFW_KEY_W)) {
                selectedActionIndex = Math.floorMod(selectedActionIndex - 1, lastActions.size());
                context.playCommandSound();
                return true;
            }
            if (selectedActionIndex >= 0 && !lastActions.isEmpty()
                    && (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_DOWN
                            || key == GLFW.GLFW_KEY_D || key == GLFW.GLFW_KEY_S)) {
                selectedActionIndex = Math.floorMod(selectedActionIndex + 1, lastActions.size());
                context.playCommandSound();
                return true;
            }
            return false;
        }

        private void drawPriorityPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                DeckAction action, int x, int y, int w, int h, int mouseX, int mouseY) {
            int actionIndex = registerHitbox(x, y, w, h, mouseX, mouseY);
            boolean hovered = TerminalUi.inside(mouseX, mouseY, x, y, w, h);
            boolean selected = actionIndex == selectedActionIndex;
            TerminalUi.densePanel(graphics, x, y, w, h, hovered || selected ? action.color() : descriptor.accentColor());
            TerminalUi.line(context, graphics, "PRIORITY ACTION", x + 12, y + 10, w - 24, action.color());
            TerminalUi.line(context, graphics, action.label(), x + 12, y + 30, Math.max(70, w - 118), TerminalUi.TEXT);
            int pillW = Math.min(104, Math.max(76, w / 4));
            TerminalUi.miniStatusPill(context, graphics, action.value(), x + w - pillW - 12,
                    y + 28, pillW, action.color(), hovered || selected);
            TerminalUi.wrap(context, graphics, action.detail(), x + 12, y + 52, w - 24,
                    action.enabled() ? TerminalUi.MUTED : TerminalUi.RED);
        }

        private static void drawPulsePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                EchoPackMode packMode, List<EchoDiagnosticBlocker> diagnostics, List<EchoRouteRecord> routes,
                int pendingRewards, int chapterCount, int x, int y, int w, int h) {
            TerminalUi.panel(graphics, x, y, w, h);
            TerminalUi.line(context, graphics, "SYSTEM PULSE", x + 12, y + 10, w - 24, TerminalUi.CYAN);
            int rows = Math.max(14, (h - 32) / 4);
            pulseLine(context, graphics, "Mode", modeChip(packMode), x + 12, y + 30, w - 24, TerminalUi.CYAN);
            pulseLine(context, graphics, "Diagnostics", diagnostics.isEmpty() ? "Clear" : diagnostics.size() + " blocker(s)",
                    x + 12, y + 30 + rows, w - 24,
                    diagnostics.isEmpty() ? TerminalUi.GREEN : DiagnosticsTab.severityColor(diagnostics.get(0).severity()));
            pulseLine(context, graphics, "Routes", completedRoutes(routes) + "/" + routes.size() + " complete",
                    x + 12, y + 30 + rows * 2, w - 24, routes.isEmpty() ? TerminalUi.MUTED : TerminalUi.GREEN);
            pulseLine(context, graphics, "Inbox / Chapters", pendingRewards + " item(s) / " + chapterCount,
                    x + 12, y + 30 + rows * 3, w - 24, pendingRewards > 0 ? TerminalUi.AMBER : TerminalUi.MUTED);
        }

        private static void pulseLine(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                String label, String value, int x, int y, int w, int color) {
            int labelW = Math.min(106, Math.max(62, w / 2));
            TerminalUi.line(context, graphics, label, x, y, labelW, TerminalUi.MUTED);
            TerminalUi.line(context, graphics, value, x + labelW, y, Math.max(40, w - labelW), color);
        }

        private int drawReadyRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<DeckAction> actions, int x, int y, int w, int mouseX, int mouseY) {
            TerminalUi.section(context, graphics, "READY NOW", x, y, descriptor.accentColor());
            int cy = y + 18;
            int columns = w >= 640 ? 3 : w >= 410 ? 2 : 1;
            int gap = 8;
            int cardW = Math.max(120, (w - gap * (columns - 1)) / columns);
            for (int i = 0; i < actions.size(); i++) {
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int actionY = cy + row * 42;
                int cw = col == columns - 1 ? Math.max(120, w - col * (cardW + gap)) : cardW;
                drawActionButton(context, graphics, actions.get(i), cx, actionY, cw, 34, mouseX, mouseY);
            }
            return cy + ((actions.size() + columns - 1) / columns) * 42;
        }

        private void drawMetricGrid(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                List<DeckAction> metrics, int x, int y, int width, int mouseX, int mouseY) {
            TerminalUi.section(context, graphics, "COMMAND SHORTCUTS", x, y, descriptor.accentColor());
            int columns = dashboardColumns(width);
            int gap = 8;
            int cardW = Math.max(84, (width - gap * (columns - 1)) / columns);
            int cy = y + 18;
            for (int i = 0; i < metrics.size(); i++) {
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cardY = cy + row * 52;
                int cw = col == columns - 1 ? Math.max(84, width - col * (cardW + gap)) : cardW;
                drawMetricCard(context, graphics, metrics.get(i), cx, cardY, cw, 44, mouseX, mouseY);
            }
        }

        private void drawActionButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                DeckAction action, int x, int y, int w, int h, int mouseX, int mouseY) {
            int actionIndex = registerHitbox(x, y, w, h, mouseX, mouseY);
            boolean hovered = TerminalUi.inside(mouseX, mouseY, x, y, w, h);
            boolean selected = actionIndex == selectedActionIndex;
            if (action.enabled()) {
                TerminalUi.primaryCommandButton(context, graphics, x, y, w, h, action.label(), null,
                        action.color(), hovered || selected);
            } else {
                TerminalUi.disabledCommandButton(context, graphics, x, y, w, h, action.label());
            }
        }

        private void drawMetricCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                DeckAction action, int x, int y, int w, int h, int mouseX, int mouseY) {
            int actionIndex = registerHitbox(x, y, w, h, mouseX, mouseY);
            boolean hovered = TerminalUi.inside(mouseX, mouseY, x, y, w, h);
            TerminalUi.densePanel(graphics, x, y, w, h,
                    hovered || actionIndex == selectedActionIndex ? action.color() : TerminalUi.CYAN_DIM);
            TerminalUi.line(context, graphics, action.label(), x + 8, y + 7, w - 16, TerminalUi.MUTED);
            TerminalUi.line(context, graphics, action.value(), x + 8, y + 22, w - 16, action.color());
        }

        private int registerHitbox(int x, int y, int w, int h, int mouseX, int mouseY) {
            int index = actionRenderIndex++;
            if (index < lastActions.size()) {
                hitboxes.add(new DeckHitbox(x, y, w, h, index));
                if (TerminalUi.inside(mouseX, mouseY, x, y, w, h)) {
                    selectedActionIndex = index;
                }
                return index;
            }
            return -1;
        }

        private static List<DeckAction> deckActions(DeckAction priority, List<DeckAction> shortcuts, List<DeckAction> metrics) {
            List<DeckAction> actions = new ArrayList<>();
            actions.add(priority);
            actions.addAll(shortcuts);
            actions.addAll(metrics);
            return List.copyOf(actions);
        }

        private static List<DeckAction> readyActions(List<EchoDiagnosticBlocker> diagnostics,
                EchoRouteRecord route, int pendingRewards, int chapterCount) {
            List<DeckAction> actions = new ArrayList<>();
            if (pendingRewards > 0) {
                actions.add(DeckAction.reward("Claim Rewards", pendingRewards + " item(s)",
                        "Move all stored support caches into inventory.", TerminalUi.AMBER, true));
            }
            if (!diagnostics.isEmpty()) {
                actions.add(blockerAction(diagnostics.get(0)));
            }
            if (route != null) {
                actions.add(routeAction(route));
            }
            actions.add(navigateAction("Open Survival Route", "Guide", "Open the aggregate survival route.",
                    TerminalUi.GREEN, MainSurvivalQuestProvider.TAB_ID));
            if (chapterCount > 0) {
                actions.add(navigateAction("Review Chapters", chapterCount + " linked",
                        "Inspect installed chapter status and availability.", TerminalUi.AMBER, ADDONS));
            }
            return actions.stream().limit(3).toList();
        }

        private static DeckAction priorityAction(List<EchoDiagnosticBlocker> diagnostics,
                EchoRouteRecord route, int pendingRewards, int chapterCount) {
            Identifier priorityTab = commandDeckPriorityTab(!diagnostics.isEmpty(), pendingRewards, route != null, chapterCount);
            if (DIAGNOSTICS.equals(priorityTab)) {
                return blockerAction(diagnostics.get(0));
            }
            if (REWARD_INBOX.equals(priorityTab)) {
                return DeckAction.reward("Claim Rewards", pendingRewards + " ready",
                        "Reward Inbox has support cache item(s) ready to claim.", TerminalUi.AMBER, true);
            }
            if (ROUTE_RECORDS.equals(priorityTab)) {
                return routeAction(route);
            }
            if (MainSurvivalQuestProvider.TAB_ID.equals(priorityTab)) {
                return navigateAction("Open Survival Route", "Route", "Continue through the aggregate survival guide.",
                        TerminalUi.GREEN, MainSurvivalQuestProvider.TAB_ID);
            }
            return navigateAction("Review Chapters", "No addons",
                    "Install or enable an ECHO chapter to populate missions and field systems.", TerminalUi.AMBER, ADDONS);
        }

        private static List<DeckAction> metricActions(EchoPackMode packMode, List<EchoDiagnosticBlocker> diagnostics,
                List<EchoRouteRecord> routes, int pendingRewards, int chapterCount) {
            int blockerColor = diagnostics.isEmpty() ? TerminalUi.GREEN : DiagnosticsTab.severityColor(diagnostics.get(0).severity());
            return List.of(
                    navigateAction("Blockers", String.valueOf(diagnostics.size()), "Open diagnostics.", blockerColor, DIAGNOSTICS),
                    navigateAction("Inbox", pendingRewards + " item(s)", "Open support cache inbox.",
                            pendingRewards > 0 ? TerminalUi.AMBER : TerminalUi.MUTED, REWARD_INBOX),
                    navigateAction("Routes", completedRoutes(routes) + "/" + routes.size(), "Open route records.",
                            routes.isEmpty() ? TerminalUi.MUTED : TerminalUi.GREEN, ROUTE_RECORDS),
                    navigateAction("Vitals", modeChip(packMode), "Open shared hazard telemetry.", TerminalUi.CYAN, VITALS),
                    navigateAction("Archives", "Records", "Open field archive records.", TerminalUi.CYAN, ARCHIVES),
                    navigateAction("Chapters", String.valueOf(chapterCount), "Open installed chapter status.",
                            chapterCount > 0 ? TerminalUi.AMBER : TerminalUi.MUTED, ADDONS),
                    navigateAction("Survival", "Route", "Open the aggregate survival route.",
                            TerminalUi.GREEN, MainSurvivalQuestProvider.TAB_ID));
        }

        private static DeckAction blockerAction(EchoDiagnosticBlocker blocker) {
            String action = blocker.nextAction().isBlank() ? blocker.detail() : blocker.nextAction();
            return navigateAction("Resolve Blocker", blocker.severity().name(),
                    blocker.title() + ". " + action, DiagnosticsTab.severityColor(blocker.severity()), DIAGNOSTICS);
        }

        private static DeckAction routeAction(EchoRouteRecord route) {
            return navigateAction("Continue Route", route.title(), route.status(), TerminalUi.GREEN, ROUTE_RECORDS);
        }

        private static DeckAction navigateAction(String label, String value, String detail, int color, Identifier tabId) {
            return new DeckAction(label, value, detail, color, tabId, false, true);
        }

        private static EchoRouteRecord firstIncompleteRoute(List<EchoRouteRecord> routes) {
            return routes.stream().filter(route -> !route.complete()).findFirst().orElse(null);
        }

        private static int completedRoutes(List<EchoRouteRecord> routes) {
            return (int) routes.stream().filter(EchoRouteRecord::complete).count();
        }

        private static int dashboardColumns(int width) {
            if (width >= 720) {
                return 4;
            }
            if (width >= 520) {
                return 3;
            }
            return 2;
        }

        private static String modeChip(EchoPackMode packMode) {
            return switch (packMode) {
                case ASHFALL_STANDALONE -> "ASHFALL";
                case ORBITAL_STANDALONE -> "ORBITAL";
                case FULL_SAGA -> "FULL SAGA";
                case UNKNOWN -> "UNKNOWN";
            };
        }

        private record DeckHitbox(int x, int y, int w, int h, int index) {
        }

        private record DeckAction(String label, String value, String detail, int color, Identifier tabId,
                boolean rewardClaim, boolean enabled) {
            static DeckAction reward(String label, String value, String detail, int color, boolean enabled) {
                return new DeckAction(label, value, detail, color, REWARD_INBOX, true, enabled);
            }

            void execute(TerminalRenderContext context) {
                if (!enabled) {
                    context.playRejectedSound();
                    return;
                }
                if (rewardClaim) {
                    context.sendAction(REWARD_INBOX, CLAIM_REWARDS, "");
                    return;
                }
                context.navigateToTab(tabId);
            }
        }
    }

    private static final class AddonsTab implements TerminalTab {
        private static final int ROW_HEIGHT = 62;
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("addons"), "ADDONS", 900, 0xFFFFD166);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Chapters", TerminalTabChrome.GROUP_ADDONS, "AD", "Installed chapter status", 900);
        private String selectedChapterId = "";
        private int lastListX;
        private int lastListY;
        private int lastListW;
        private int lastListH;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            normalizeSelection(addonChapters());
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            List<EchoAddonChapter> chapters = addonChapters();
            if (chapters.isEmpty()) {
                TerminalUi.flatDataPanel(context, graphics,
                        x, y, w, Math.min(140, Math.max(90, h / 4)), "ECHO ADDON ROADMAP", "",
                        descriptor.accentColor());
                TerminalUi.line(context, graphics, "No addon chapters detected.", x + 14, y + 44,
                        w - 28, TerminalUi.MUTED);
                return;
            }
            normalizeSelection(chapters);
            boolean wide = w >= 640;
            int listW = wide ? Math.max(260, Math.min(420, w * 42 / 100)) : w;
            int detailX = wide ? x + listW + 14 : x;
            int boardH = wide ? Math.max(280, Math.min(Math.max(280, h - 154), Math.max(280, chapters.size() * ROW_HEIGHT + 100)))
                    : Math.max(180, chapters.size() * ROW_HEIGHT + 46);
            int detailY = wide ? y : y + boardH + 12;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;

            int cy = TerminalUi.flatDataPanel(context, graphics,
                    x, y, listW - 8, boardH, "ECHO ADDON ROADMAP",
                    chapters.size() + " linked", descriptor.accentColor()) + 6;
            lastListX = x;
            lastListY = cy;
            lastListW = listW - 8;
            lastListH = Math.max(1, boardH - (cy - y) - 12);
            for (EchoAddonChapter chapter : chapters) {
                boolean available = chapterAvailable(chapter, context);
                boolean selected = chapterId(chapter).equals(selectedChapterId);
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x, cy, listW - 8, ROW_HEIGHT - 8);
                int color = available ? 0xFF92F7A6 : 0xFF8D96A3;
                TerminalUi.dataListRow(context, graphics, x + 10, cy, listW - 28, ROW_HEIGHT - 8,
                        chapterDisplayName(chapter), chapterStatusLine(chapter, context),
                        available ? "AVAILABLE" : "LOCKED", selected, hovered, descriptor.accentColor(), color);
                TerminalUi.line(context, graphics, chapterSummary(chapter), x + 20, cy + 38,
                        listW - 42, selected ? TerminalUi.TEXT : TerminalUi.MUTED);
                cy += ROW_HEIGHT;
            }

            EchoAddonChapter selected = selectedChapter(chapters);
            if (selected == null) {
                return;
            }
            boolean available = chapterAvailable(selected, context);
            int color = available ? TerminalUi.GREEN : TerminalUi.MUTED;
            int detailPanelH = wide ? boardH : addonDetailHeight(context, selected, detailW - 8);
            int dy = TerminalUi.flatDataPanel(context, graphics,
                    detailX, detailY, detailW - 8, detailPanelH, "CHAPTER DETAIL", "",
                    descriptor.accentColor()) + 2;
            TerminalUi.hybridIconBadge(graphics, TerminalVisualAssets.ICON_PAGE_CHAPTERS, TerminalIcon.ADDONS,
                    detailX + 14, dy + 2, 42, descriptor.accentColor(), true);
            TerminalUi.line(context, graphics, chapterDisplayName(selected), detailX + 66, dy + 8,
                    detailW - 178, available ? TerminalUi.GREEN : TerminalUi.MUTED);
            TerminalUi.miniStatusPill(context, graphics, available ? "AVAILABLE" : "LOCKED",
                    detailX + detailW - 102, dy + 7, 82, color, available);
            dy += 56;
            dy = TerminalUi.keyValue(context, graphics, detailX + 14, dy, detailW - 32,
                    "Signal", chapterModId(selected), TerminalUi.TEXT);
            dy = TerminalUi.keyValue(context, graphics, detailX + 14, dy, detailW - 32,
                    "Status", chapterStatusLine(selected, context), color);
            dy = TerminalUi.wrap(context, graphics, chapterSummary(selected), detailX + 14, dy + 4,
                    detailW - 32, TerminalUi.TEXT) + 8;
            TerminalUi.divider(graphics, detailX + 14, dy, detailW - 32, descriptor.accentColor());
            dy += 9;
            TerminalUi.wrap(context, graphics,
                    available
                            ? "Chapter systems are online in their terminal channels. ECHO will confirm the owning route before any field command is accepted."
                            : "Route preview only. Complete the listed requirement before commands wake up.",
                    detailX + 14, dy, detailW - 32, available ? TerminalUi.GREEN : TerminalUi.AMBER);

            int tileY = wide ? y + boardH + 12 : detailY + detailPanelH + 12;
            int tileW = Math.max(90, (w - 16) / 3);
            summaryCard(context, graphics, x, tileY, tileW, "TABS", String.valueOf(TerminalTabRegistry.tabs().size()),
                    "terminal pages", TerminalUi.CYAN);
            summaryCard(context, graphics, x + tileW + 8, tileY, tileW, "CHAPTERS",
                    String.valueOf(chapters.size()), "available chapters", TerminalUi.AMBER);
            summaryCard(context, graphics, x + (tileW + 8) * 2, tileY, Math.max(90, w - (tileW + 8) * 2),
                    "RECORDS", String.valueOf(TerminalArchiveRegistry.entries().size()), "shared archive entries", TerminalUi.GREEN);
            int calloutY = tileY + 82;
            if (wide && calloutY + 70 <= y + h) {
                TerminalUi.flatHudPanel(graphics, x, calloutY, w - 8, 64, descriptor.accentColor());
                TerminalUi.line(context, graphics, "ROUTE AUTHORITY", x + 12, calloutY + 10, w - 32, descriptor.accentColor());
                TerminalUi.wrap(context, graphics,
                        "Installed chapters keep their own mission state, rewards, and command checks. ECHO presents their linked routes here.",
                        x + 12, calloutY + 27, w - 32, TerminalUi.TEXT);
            }
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0 || !TerminalUi.inside(mouseX, mouseY, lastListX, lastListY, lastListW, lastListH)) {
                return false;
            }
            List<EchoAddonChapter> chapters = addonChapters();
            int index = (int) ((mouseY - lastListY) / ROW_HEIGHT);
            if (index >= 0 && index < chapters.size()) {
                selectedChapterId = chapterId(chapters.get(index));
                return true;
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            int w = context.contentWidth();
            boolean wide = w >= 640;
            int listW = wide ? Math.max(260, Math.min(420, w * 42 / 100)) : w;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;
            int boardH = wide ? Math.max(230, Math.min(Math.max(230, context.contentHeight() - 94),
                    Math.max(280, addonChapters().size() * ROW_HEIGHT + 100)))
                    : Math.max(180, addonChapters().size() * ROW_HEIGHT + 46);
            int rows = addonChapters().size() * ROW_HEIGHT;
            EchoAddonChapter selected = selectedChapter(addonChapters());
            int detailHeight = selected == null ? 40 : addonDetailHeight(context, selected, detailW - 8) + 28;
            if (wide) {
                return Math.max(context.contentHeight(), boardH + 158);
            }
            return Math.max(context.contentHeight(), boardH + detailHeight + 94);
        }

        private void normalizeSelection(List<EchoAddonChapter> chapters) {
            if (chapters.stream().anyMatch(chapter -> chapterId(chapter).equals(selectedChapterId))) {
                return;
            }
            selectedChapterId = chapters.isEmpty() ? "" : chapterId(chapters.get(0));
        }

        private EchoAddonChapter selectedChapter(List<EchoAddonChapter> chapters) {
            normalizeSelection(chapters);
            return chapters.stream()
                    .filter(chapter -> chapterId(chapter).equals(selectedChapterId))
                    .findFirst()
                    .orElse(null);
        }

        private static int addonDetailHeight(TerminalRenderContext context, EchoAddonChapter chapter, int width) {
            if (chapter == null) {
                return 80;
            }
            String summary = chapterSummary(chapter);
            String roadMap = chapterAvailable(chapter, context)
                    ? "Chapter systems are online in their terminal channels. ECHO will confirm the owning route before any field command is accepted."
                    : "Route preview only. Complete the listed requirement before commands wake up.";
            return Math.max(118,
                    72
                            + TerminalUi.wrappedHeight(context, summary, width - 16)
                            + TerminalUi.wrappedHeight(context, roadMap, width - 16));
        }

        private static void summaryCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int width, String label, String value, String detail, int color) {
            TerminalUi.flatHudPanel(graphics, x, y, width, 70, color);
            TerminalUi.line(context, graphics, label, x + 10, y + 10, width - 20, color);
            TerminalUi.line(context, graphics, value, x + 10, y + 30, width - 20, TerminalUi.TEXT);
            TerminalUi.line(context, graphics, detail, x + 10, y + 47, width - 20, TerminalUi.MUTED);
        }
    }

    private static final class DiagnosticsTab implements TerminalTab {
        private static final int ROW_HEIGHT = 54;
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("diagnostics"), "WHAT NOW", 80, 0xFFFFD166);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("What Now", TerminalTabChrome.GROUP_PROTOCOL, "WN",
                        "Progress diagnostics", 80);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            List<EchoDiagnosticBlocker> diagnostics = diagnostics(context);
            int panelH = Math.max(110, Math.min(context.contentHeight(), 76 + Math.max(1, diagnostics.size()) * ROW_HEIGHT));
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w, panelH,
                    "WHAT NOW", diagnostics.size() + " item(s)", descriptor.accentColor()) + 6;
            if (diagnostics.isEmpty()) {
                TerminalUi.wrap(context, graphics,
                        "No blockers reported. Continue the active chapter tab, claim pending rewards, or scan the next route.",
                        x + 14, cy, w - 28, TerminalUi.GREEN);
                return;
            }
            for (EchoDiagnosticBlocker blocker : diagnostics) {
                int color = severityColor(blocker.severity());
                TerminalUi.dataListRow(context, graphics, x + 10, cy, w - 20, ROW_HEIGHT - 6,
                        blocker.title(), blocker.detail(), blocker.severity().name(),
                        false, TerminalUi.inside(mouseX, mouseY, x + 10, cy, w - 20, ROW_HEIGHT - 6),
                        descriptor.accentColor(), color);
                if (!blocker.nextAction().isBlank()) {
                    TerminalUi.line(context, graphics, "Command: " + blocker.nextAction(),
                            x + 20, cy + 36, w - 40, TerminalUi.TEXT);
                }
                cy += ROW_HEIGHT;
            }
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), 90 + Math.max(1, diagnostics(context).size()) * ROW_HEIGHT);
        }

        private static List<EchoDiagnosticBlocker> diagnostics(TerminalRenderContext context) {
            Map<Identifier, EchoDiagnosticBlocker> diagnostics = new LinkedHashMap<>();
            for (EchoDiagnosticBlocker blocker : EchoCoreServices.diagnostics(context.player())) {
                diagnostics.putIfAbsent(blocker.id(), blocker);
            }
            for (TerminalMissionProvider provider : TerminalMissionRegistry.providers()) {
                for (TerminalMissionDefinition mission : safeMissions(provider, context)) {
                    TerminalMissionSnapshot snapshot = safeSnapshot(provider, context, mission);
                    if (snapshot.status() == TerminalMissionStatus.LOCKED && !snapshot.unlockReason().isBlank()) {
                        EchoDiagnosticBlocker blocker = new EchoDiagnosticBlocker(
                                mission.id(),
                                missionChapter(provider).id().toString(),
                                EchoDiagnosticBlocker.Severity.BLOCKED,
                                mission.title(),
                                snapshot.unlockReason(),
                                snapshot.actionHint());
                        diagnostics.putIfAbsent(blocker.id(), blocker);
                    }
                }
            }
            return diagnostics.values().stream()
                    .sorted(java.util.Comparator
                            .comparingInt((EchoDiagnosticBlocker blocker) -> severityRank(blocker.severity()))
                            .thenComparing(EchoDiagnosticBlocker::chapterId)
                            .thenComparing(EchoDiagnosticBlocker::title)
                            .thenComparing(blocker -> blocker.id().toString()))
                    .toList();
        }

        private static List<TerminalMissionDefinition> safeMissions(TerminalMissionProvider provider, TerminalRenderContext context) {
            try {
                List<TerminalMissionDefinition> missions = provider.missions(context == null ? null : context.player());
                return missions == null ? List.of() : missions.stream()
                        .filter(mission -> mission != null)
                        .toList();
            } catch (RuntimeException ignored) {
                return List.of();
            }
        }

        private static TerminalMissionSnapshot safeSnapshot(TerminalMissionProvider provider,
                TerminalRenderContext context, TerminalMissionDefinition mission) {
            try {
                TerminalMissionSnapshot snapshot = provider.snapshot(context == null ? null : context.player(), mission.id());
                return snapshot == null ? fallbackSnapshot(mission.id(),
                        "Mission signal did not return state.", "Open the chapter tab that owns this route.") : snapshot;
            } catch (RuntimeException ignored) {
                return fallbackSnapshot(mission.id(),
                        "Mission signal failed to report state.", "Open the chapter tab that owns this route or reload the world.");
            }
        }

        private static TerminalMissionSnapshot fallbackSnapshot(Identifier missionId, String reason, String actionHint) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "LOCKED", reason, actionHint, List.of());
        }

        private static int severityColor(EchoDiagnosticBlocker.Severity severity) {
            return switch (severity) {
                case CRITICAL -> TerminalUi.RED;
                case BLOCKED -> TerminalUi.AMBER;
                case WARNING -> 0xFFFFE08A;
                case INFO -> TerminalUi.CYAN;
            };
        }

        private static int severityRank(EchoDiagnosticBlocker.Severity severity) {
            return switch (severity == null ? EchoDiagnosticBlocker.Severity.INFO : severity) {
                case CRITICAL -> 0;
                case BLOCKED -> 1;
                case WARNING -> 2;
                case INFO -> 3;
            };
        }
    }

    private static final class MissionGraphTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("mission_graph"), "MISSION GRAPH", 120, 0xFF92F7A6);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Mission Graph", TerminalTabChrome.GROUP_PROTOCOL, "MG",
                        "Shared campaign route", 120);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            List<TerminalMissionProvider> providers = graphProviders();
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w,
                    Math.max(130, Math.min(context.contentHeight(), 78
                            + Math.max(1, providers.size()) * 66)), "MISSION GRAPH",
                    providers.size() + " source(s)", descriptor.accentColor()) + 8;
            if (providers.isEmpty()) {
                TerminalUi.wrap(context, graphics, "No mission routes are linked yet.",
                        x + 14, cy, w - 28, TerminalUi.MUTED);
                return;
            }
            for (TerminalMissionProvider provider : providers) {
                List<TerminalMissionDefinition> missions = safeMissions(provider, context);
                int locked = 0;
                int active = 0;
                int complete = 0;
                for (TerminalMissionDefinition mission : missions) {
                    TerminalMissionStatus status = safeStatus(provider, context, mission);
                    if (status == TerminalMissionStatus.LOCKED || status == TerminalMissionStatus.VIEW_ONLY) {
                        locked++;
                    } else if (status == TerminalMissionStatus.CLAIMED || status == TerminalMissionStatus.COMPLETED
                            || status == TerminalMissionStatus.CLAIMABLE) {
                        complete++;
                    } else {
                        active++;
                    }
                }
                TerminalUi.flatHudPanel(graphics, x + 10, cy, w - 20, 58, descriptor.accentColor());
                TerminalMissionChapter chapter = missionChapter(provider);
                TerminalUi.line(context, graphics, chapter.title(), x + 22, cy + 9, w - 44, TerminalUi.TEXT);
                TerminalUi.line(context, graphics, chapter.summary(), x + 22, cy + 25, w - 44, TerminalUi.MUTED);
                TerminalUi.line(context, graphics,
                        complete + " complete / " + active + " active / " + locked + " locked",
                        x + 22, cy + 41, w - 44, TerminalUi.GREEN);
                cy += 66;
            }
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), 70 + graphProviders().size() * 66);
        }

        private static List<TerminalMissionProvider> graphProviders() {
            return TerminalMissionRegistry.providers().stream()
                    .filter(provider -> provider != null && provider != MainSurvivalQuestProvider.INSTANCE)
                    .toList();
        }

        private static List<TerminalMissionDefinition> safeMissions(TerminalMissionProvider provider,
                TerminalRenderContext context) {
            try {
                List<TerminalMissionDefinition> missions = provider.missions(context == null ? null : context.player());
                return missions == null ? List.of() : missions.stream()
                        .filter(mission -> mission != null)
                        .toList();
            } catch (RuntimeException ignored) {
                return List.of();
            }
        }

        private static TerminalMissionStatus safeStatus(TerminalMissionProvider provider,
                TerminalRenderContext context, TerminalMissionDefinition mission) {
            try {
                TerminalMissionSnapshot snapshot = provider.snapshot(context == null ? null : context.player(), mission.id());
                return snapshot == null ? TerminalMissionStatus.LOCKED : snapshot.status();
            } catch (RuntimeException ignored) {
                return TerminalMissionStatus.LOCKED;
            }
        }
    }

    private static final class VitalsTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("vitals"), "VITALS", 130, 0xFF66E8FF);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Vitals", TerminalTabChrome.GROUP_SYSTEMS, "VT",
                        "Shared hazard telemetry", 130);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            EchoHazardTelemetry telemetry = EchoCoreServices.hazardTelemetry(context.player());
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w,
                    Math.max(190, Math.min(context.contentHeight(), 232)),
                    "VITALS TELEMETRY", telemetry.warning() ? "WARNING" : "NOMINAL", descriptor.accentColor()) + 8;
            cy = meter(context, graphics, x + 14, cy, w - 28, "HYDRATION", telemetry.hydration(), true, TerminalUi.CYAN);
            cy = meter(context, graphics, x + 14, cy, w - 28, "RADIATION", telemetry.radiation(), false, TerminalUi.AMBER);
            cy = meter(context, graphics, x + 14, cy, w - 28, "TOXIC AIR", telemetry.toxicAir(), false, TerminalUi.RED);
            cy = meter(context, graphics, x + 14, cy, w - 28, "OXYGEN", telemetry.oxygen(), true, TerminalUi.GREEN);
            cy = meter(context, graphics, x + 14, cy, w - 28, "PRESSURE", telemetry.pressure(), true, TerminalUi.CYAN);
            cy = meter(context, graphics, x + 14, cy, w - 28, "COLD", telemetry.cold(), false, 0xFF9FD1FF);
            cy = meter(context, graphics, x + 14, cy, w - 28, "HEAT", telemetry.heat(), false, TerminalUi.AMBER);
            cy = meter(context, graphics, x + 14, cy, w - 28, "EXPOSURE", telemetry.exposure(), false, TerminalUi.RED);
            TerminalUi.wrap(context, graphics, telemetry.statusLine(), x + 14, cy + 6, w - 28,
                    telemetry.warning() ? TerminalUi.AMBER : TerminalUi.GREEN);
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), 244);
        }

        private static int meter(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, String label, int value, boolean highGood, int color) {
            int danger = highGood ? 100 - value : value;
            int meterColor = danger >= 70 ? TerminalUi.RED : danger >= 40 ? TerminalUi.AMBER : color;
            TerminalUi.line(context, graphics, label, x, y, Math.max(90, w / 3), TerminalUi.MUTED);
            TerminalUi.progress(graphics, x + Math.max(96, w / 3), y + 3,
                    Math.max(70, w - Math.max(108, w / 3) - 48), 7, value / 100.0F, meterColor);
            TerminalUi.line(context, graphics, value + "%", x + w - 42, y, 42, meterColor);
            return y + 20;
        }
    }

    private static final class RouteRecordsTab implements TerminalTab {
        private static final int ROW_HEIGHT = 54;
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("route_records"), "ROUTE RECORDS", 125, 0xFF9FD1FF);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Route Records", TerminalTabChrome.GROUP_FIELD, "RR",
                        "Shared route records", 125);

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            List<EchoRouteRecord> records = EchoCoreServices.routeRecords(context.player());
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w,
                    Math.max(118, Math.min(context.contentHeight(), 68 + Math.max(1, records.size()) * ROW_HEIGHT)),
                    "ROUTE RECORDS", records.size() + " route(s)", descriptor.accentColor()) + 8;
            if (records.isEmpty()) {
                TerminalUi.wrap(context, graphics,
                        "No route records are online yet. Scanner routes, recovery sites, surveys, and orbital paths appear here when chapters publish them.",
                        x + 14, cy, w - 28, TerminalUi.MUTED);
                return;
            }
            for (EchoRouteRecord record : records) {
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x + 10, cy, w - 20, ROW_HEIGHT - 6);
                int color = record.complete() ? TerminalUi.GREEN : TerminalUi.AMBER;
                TerminalUi.dataListRow(context, graphics, x + 10, cy, w - 20, ROW_HEIGHT - 6,
                        record.title(), record.dimensionHint(), record.status(),
                        false, hovered, descriptor.accentColor(), color);
                int summaryW = w < 420 ? w - 40 : w - 210;
                TerminalUi.line(context, graphics, record.category() + " / " + record.summary(),
                        x + 20, cy + 32, Math.max(80, summaryW), TerminalUi.MUTED);
                cy += ROW_HEIGHT;
            }
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), 82 + Math.max(1, EchoCoreServices.routeRecords(context.player()).size()) * ROW_HEIGHT);
        }
    }

    private static final class FactionAtlasTab implements TerminalTab {
        private static final int ROW_HEIGHT = 66;
        private static final int FILTER_HEIGHT = 20;
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("faction_atlas"), "FACTIONS", 128, 0xFFB889F5);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Faction Atlas", TerminalTabChrome.GROUP_FIELD, "FX",
                        "Shared faction atlas", 128);
        private String selectedFactionId = "";
        private String filterNamespace = "all";
        private int lastListX;
        private int lastListY;
        private int lastListW;
        private int lastListH;
        private int lastFilterX;
        private int lastFilterY;
        private int lastFilterW;
        private int lastFilterH;
        private List<String> lastFilters = List.of();

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            normalizeSelection(filteredProfiles(context));
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            List<EchoFactionProfile> allProfiles = EchoCoreServices.factionProfiles(context.player());
            if (allProfiles.isEmpty()) {
                TerminalUi.flatDataPanel(context, graphics, x, y, w, Math.min(140, Math.max(90, h / 4)),
                        "FACTION ATLAS", "0 linked", descriptor.accentColor());
                TerminalUi.line(context, graphics, "No faction signals are online.",
                        x + 14, y + 44, w - 28, TerminalUi.MUTED);
                return;
            }

            lastFilters = filters(allProfiles);
            int fy = drawFilters(context, graphics, mouseX, mouseY, x, y, w, lastFilters);
            List<EchoFactionProfile> profiles = filteredProfiles(allProfiles);
            normalizeSelection(profiles);

            boolean wide = w >= 650;
            int listW = wide ? Math.max(260, Math.min(410, w * 42 / 100)) : w;
            int detailX = wide ? x + listW + 14 : x;
            int boardY = fy + 8;
            int boardH = wide ? Math.max(280, Math.min(Math.max(280, h - (boardY - y) - 12),
                    Math.max(280, profiles.size() * ROW_HEIGHT + 70)))
                    : Math.max(180, profiles.size() * ROW_HEIGHT + 58);
            int detailY = wide ? boardY : boardY + boardH + 12;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;

            int cy = TerminalUi.flatDataPanel(context, graphics, x, boardY, listW - 8, boardH,
                    "FACTION ATLAS", profiles.size() + " visible / " + allProfiles.size() + " total",
                    descriptor.accentColor()) + 6;
            lastListX = x;
            lastListY = cy;
            lastListW = listW - 8;
            lastListH = Math.max(1, boardH - (cy - boardY) - 10);
            for (EchoFactionProfile profile : profiles) {
                EchoFactionDefinition definition = profile.definition();
                boolean selected = definition.id().toString().equals(selectedFactionId);
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x + 10, cy, listW - 28, ROW_HEIGHT - 8);
                int color = profile.standing().accentColor();
                TerminalUi.dataListRow(context, graphics, x + 10, cy, listW - 28, ROW_HEIGHT - 8,
                        definition.displayName(), definition.route(), profile.standing().displayName(),
                        selected, hovered, descriptor.accentColor(), color);
                TerminalUi.line(context, graphics,
                        contactSummary(profile) + " / " + definition.serviceSummary(),
                        x + 20, cy + 38, listW - 42, selected ? TerminalUi.TEXT : TerminalUi.MUTED);
                cy += ROW_HEIGHT;
            }

            EchoFactionProfile selected = selectedProfile(profiles);
            if (selected != null) {
                renderDetail(context, graphics, detailX, detailY, detailW - 8,
                        wide ? boardH : detailHeight(context, selected, detailW - 8), selected);
            }
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }
            if (TerminalUi.inside(mouseX, mouseY, lastFilterX, lastFilterY, lastFilterW, lastFilterH)
                    && !lastFilters.isEmpty()) {
                int filterW = Math.max(72, lastFilterW / Math.max(1, lastFilters.size()));
                int index = (int) ((mouseX - lastFilterX) / filterW);
                if (index >= 0 && index < lastFilters.size()) {
                    filterNamespace = lastFilters.get(index);
                    selectedFactionId = "";
                    normalizeSelection(filteredProfiles(context));
                    return true;
                }
            }
            if (TerminalUi.inside(mouseX, mouseY, lastListX, lastListY, lastListW, lastListH)) {
                List<EchoFactionProfile> profiles = filteredProfiles(context);
                int index = (int) ((mouseY - lastListY) / ROW_HEIGHT);
                if (index >= 0 && index < profiles.size()) {
                    selectedFactionId = profiles.get(index).definition().id().toString();
                    return true;
                }
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            List<EchoFactionProfile> profiles = filteredProfiles(context);
            int w = context.contentWidth();
            boolean wide = w >= 650;
            int listW = wide ? Math.max(260, Math.min(410, w * 42 / 100)) : w;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;
            EchoFactionProfile selected = selectedProfile(profiles);
            int detail = selected == null ? 120 : detailHeight(context, selected, detailW - 8);
            int list = Math.max(190, profiles.size() * ROW_HEIGHT + 88);
            if (wide) {
                return Math.max(context.contentHeight(), FILTER_HEIGHT + 18 + Math.max(list, detail));
            }
            return Math.max(context.contentHeight(), FILTER_HEIGHT + 24 + list + detail + 16);
        }

        private int drawFilters(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, int x, int y, int w, List<String> filters) {
            lastFilterX = x;
            lastFilterY = y;
            lastFilterW = w - 8;
            lastFilterH = FILTER_HEIGHT;
            int filterW = Math.max(72, (w - 8) / Math.max(1, filters.size()));
            int fx = x;
            for (String filter : filters) {
                boolean selected = filter.equals(filterNamespace);
                boolean hovered = TerminalUi.inside(mouseX, mouseY, fx, y, filterW - 6, FILTER_HEIGHT);
                int color = selected ? descriptor.accentColor() : hovered ? TerminalUi.CYAN : TerminalUi.MUTED;
                TerminalUi.miniStatusPill(context, graphics, label(filter), fx, y + 3, filterW - 6, color, selected);
                fx += filterW;
            }
            return y + FILTER_HEIGHT;
        }

        private void renderDetail(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int w, int h, EchoFactionProfile profile) {
            EchoFactionDefinition definition = profile.definition();
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w, h,
                    "FACTION DETAIL", definition.modId(), descriptor.accentColor()) + 4;
            TerminalUi.hybridIconBadge(graphics, TerminalVisualAssets.ICON_PAGE_ROUTE_MAP, TerminalIcon.WORLD,
                    x + 14, cy + 2, 42, definition.accentColor(), true);
            TerminalUi.line(context, graphics, definition.displayName(), x + 66, cy + 8,
                    w - 170, TerminalUi.TEXT);
            TerminalUi.miniStatusPill(context, graphics, profile.standing().displayName(),
                    x + w - 104, cy + 7, 84, profile.standing().accentColor(),
                    profile.standing().ordinal() >= 2);
            cy += 56;
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Standing", profile.reputation() + " / " + profile.completedContracts() + " contract(s)",
                    profile.standing().accentColor());
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Contact", contactSummary(profile), profile.contacted() ? TerminalUi.GREEN : TerminalUi.MUTED);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Last Contact", lastContact(profile), profile.lastInteractionTick() > 0L ? TerminalUi.TEXT : TerminalUi.MUTED);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Route", blank(definition.route(), "Unassigned"), TerminalUi.TEXT);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Hazard", blank(definition.hazard(), "None listed"), TerminalUi.AMBER);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Prep", blank(definition.prepHint(), "Field kit"), TerminalUi.CYAN);
            cy = TerminalUi.wrap(context, graphics, definition.summary(), x + 14, cy + 4, w - 32,
                    TerminalUi.TEXT) + 8;
            TerminalUi.divider(graphics, x + 14, cy, w - 32, definition.accentColor());
            cy += 9;
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Services", blank(definition.serviceSummary(), "Dialogue only"), TerminalUi.GREEN);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Service State", serviceState(definition, profile), TerminalUi.AMBER);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "NPC Roles", definition.roles().isEmpty() ? "None"
                            : String.join(", ", definition.roles().stream().map(role -> role.displayName()).toList()),
                    TerminalUi.TEXT);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Commands", definition.actions().size() + " known"
                            + (definition.actions().stream().anyMatch(action -> action.service()) ? " / field service" : ""),
                    TerminalUi.MUTED);
            cy = TerminalUi.keyValue(context, graphics, x + 14, cy, w - 32,
                    "Active Contract", profile.activeContractId().map(Identifier::toString).orElse("none"),
                    profile.activeContractId().isPresent() ? TerminalUi.AMBER : TerminalUi.MUTED);
            if (!profile.npcMemory().isBlank()) {
                cy = TerminalUi.wrap(context, graphics, "Memory: " + profile.npcMemory(),
                        x + 14, cy + 2, w - 32, TerminalUi.MUTED) + 6;
            }
            if (!definition.contracts().isEmpty()) {
                TerminalUi.line(context, graphics, "Contracts", x + 14, cy + 4, w - 32, descriptor.accentColor());
                cy += 20;
                for (var contract : definition.contracts()) {
                    String status = profile.completedContractIds().contains(contract.id()) ? "DONE"
                            : profile.activeContractId().filter(contract.id()::equals).isPresent() ? "ACTIVE"
                            : profile.reputation() >= contract.requiredReputation() ? "AVAILABLE" : "LOCKED";
                    TerminalUi.line(context, graphics, status + " / " + contract.title() + " / " + contract.route(),
                            x + 22, cy, w - 44, TerminalUi.TEXT);
                    cy = TerminalUi.wrap(context, graphics, contract.objective(), x + 22, cy + 15,
                            w - 44, TerminalUi.MUTED) + 6;
                }
            }
            if (!definition.poiAffinities().isEmpty()) {
                TerminalUi.line(context, graphics,
                        "POI Affinity: " + String.join(", ", definition.poiAffinities().stream()
                                .map(affinity -> affinity.profileId()).limit(4).toList()),
                        x + 14, cy + 4, w - 32, TerminalUi.MUTED);
            }
        }

        private List<EchoFactionProfile> filteredProfiles(TerminalRenderContext context) {
            return filteredProfiles(EchoCoreServices.factionProfiles(context.player()));
        }

        private List<EchoFactionProfile> filteredProfiles(List<EchoFactionProfile> profiles) {
            if ("all".equals(filterNamespace)) {
                return profiles;
            }
            return profiles.stream()
                    .filter(profile -> profile.definition().modId().equals(filterNamespace))
                    .toList();
        }

        private List<String> filters(List<EchoFactionProfile> profiles) {
            List<String> namespaces = new ArrayList<>();
            namespaces.add("all");
            for (String namespace : profiles.stream()
                    .map(profile -> profile.definition().modId())
                    .distinct()
                    .sorted()
                    .toList()) {
                namespaces.add(namespace);
            }
            if (!namespaces.contains(filterNamespace)) {
                filterNamespace = "all";
            }
            return List.copyOf(namespaces);
        }

        private void normalizeSelection(List<EchoFactionProfile> profiles) {
            if (profiles.stream().anyMatch(profile -> profile.definition().id().toString().equals(selectedFactionId))) {
                return;
            }
            selectedFactionId = profiles.isEmpty() ? "" : profiles.get(0).definition().id().toString();
        }

        private EchoFactionProfile selectedProfile(List<EchoFactionProfile> profiles) {
            normalizeSelection(profiles);
            return profiles.stream()
                    .filter(profile -> profile.definition().id().toString().equals(selectedFactionId))
                    .findFirst()
                    .orElse(null);
        }

        private int detailHeight(TerminalRenderContext context, EchoFactionProfile profile, int width) {
            int wrapWidth = Math.max(80, width - 32);
            int contractHeight = profile.definition().contracts().stream()
                    .mapToInt(contract -> 26 + TerminalUi.wrappedHeight(context, contract.objective(), wrapWidth - 12))
                    .sum();
            return Math.max(260,
                    210
                            + TerminalUi.wrappedHeight(context, profile.definition().summary(), wrapWidth)
                            + (profile.npcMemory().isBlank() ? 0
                                    : TerminalUi.wrappedHeight(context, "Memory: " + profile.npcMemory(), wrapWidth))
                            + contractHeight
                            + profile.definition().poiAffinities().size() * 4);
        }

        private static String label(String namespace) {
            return "all".equals(namespace) ? "ALL" : namespace.replace("echo", "").replace("_", " ").toUpperCase(java.util.Locale.ROOT);
        }

        private static String blank(String value, String fallback) {
            return value == null || value.isBlank() ? fallback : value;
        }

        private static String contactSummary(EchoFactionProfile profile) {
            if (!profile.contacted()) {
                return "UNCONTACTED";
            }
            return "CONTACTED x" + profile.contactCount();
        }

        private static String lastContact(EchoFactionProfile profile) {
            if (profile.lastInteractionTick() <= 0L) {
                return "none";
            }
            String role = profile.lastRoleId().isBlank() ? "unidentified contact" : profile.lastRoleId();
            return role + " at tick " + profile.lastInteractionTick();
        }

        private static String serviceState(EchoFactionDefinition definition, EchoFactionProfile profile) {
            return definition.actions().stream()
                    .filter(action -> action.service())
                    .findFirst()
                    .map(action -> profile.reputation() >= action.requiredReputation()
                            ? "Unlocked: " + action.label()
                            : "Locked: " + action.label() + " requires standing " + action.requiredReputation())
                    .orElse("Dialogue and contracts only");
        }
    }

    private static final class RewardInboxTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(REWARD_INBOX, "REWARD INBOX", 140, 0xFFFFD166);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Reward Inbox", TerminalTabChrome.GROUP_SYSTEMS, "RI",
                        "Shared support caches", 140);
        private int claimX;
        private int claimY;
        private int claimW;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int pending = EchoCoreServices.pendingTerminalRewardCount(context.player());
            int panelH = Math.max(132, Math.min(190, context.contentHeight() - 12));
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w, panelH,
                    "REWARD INBOX", pending + " item(s)", descriptor.accentColor()) + 8;
            TerminalUi.wrap(context, graphics,
                    "Ashfall rewards, Orbital support caches, faction payouts, and future ECHO chapter caches can use this shared terminal inbox.",
                    x + 14, cy, w - 28, TerminalUi.TEXT);
            claimX = x + 14;
            claimY = cy + 54;
            claimW = Math.min(180, Math.max(130, w / 3));
            boolean enabled = pending > 0;
            boolean hovered = TerminalUi.inside(mouseX, mouseY, claimX, claimY, claimW, 24);
            if (enabled) {
                TerminalUi.primaryCommandButton(context, graphics, claimX, claimY, claimW, 24,
                        "CLAIM ALL", TerminalVisualAssets.ICON_ACTION_CLAIM, descriptor.accentColor(), hovered);
            } else {
                TerminalUi.miniStatusPill(context, graphics, "INBOX EMPTY", claimX, claimY + 4, claimW,
                        TerminalUi.MUTED, false);
            }
            String hint = enabled
                    ? "Support caches are stored in the nearest owned ECHO Terminal."
                    : "Complete chapter objectives to fill the inbox.";
            if (w < 420) {
                TerminalUi.wrap(context, graphics, hint, claimX, claimY + 32, w - 28, TerminalUi.MUTED);
            } else {
                TerminalUi.line(context, graphics, hint,
                        claimX + claimW + 12, claimY + 8, Math.max(80, w - claimW - 40), TerminalUi.MUTED);
            }
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button == 0 && EchoCoreServices.pendingTerminalRewardCount(context.player()) > 0
                    && TerminalUi.inside(mouseX, mouseY, claimX, claimY, claimW, 24)) {
                context.sendAction(REWARD_INBOX, CLAIM_REWARDS, "");
                return true;
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            return Math.max(context.contentHeight(), 180);
        }
    }

    private static final class ArchivesTab implements TerminalTab {
        private static final int ROW_HEIGHT = 30;
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(id("archives"), "FIELD ARCHIVE", 950, 0xFF9FD1FF);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Field Archive", TerminalTabChrome.GROUP_FIELD, "FA", "Shared archive records",
                        950);
        private String selectedEntryId = "";
        private int lastListX;
        private int lastListY;
        private int lastListW;
        private int lastListH;

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void onSelected(TerminalRenderContext context) {
            normalizeSelection(TerminalArchiveRegistry.entries());
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            int h = context.contentHeight();
            List<TerminalArchiveEntry> entries = TerminalArchiveRegistry.entries();
            TerminalUi.section(context, graphics, "SHARED ARCHIVES", x, y, descriptor.accentColor());
            if (entries.isEmpty()) {
                TerminalUi.line(context, graphics, "No shared archive records are available.", x, y + 18, w, TerminalUi.MUTED);
                return;
            }
            normalizeSelection(entries);
            boolean wide = w >= 640;
            int listW = wide ? Math.max(250, Math.min(390, w * 40 / 100)) : w;
            int detailX = wide ? x + listW + 14 : x;
            int detailY = wide ? y + 18 : y + 24 + entries.size() * ROW_HEIGHT;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;

            int cy = y + 18;
            lastListX = x;
            lastListY = cy;
            lastListW = listW - 8;
            lastListH = entries.size() * ROW_HEIGHT;
            for (TerminalArchiveEntry entry : entries) {
                boolean selected = entry.id().toString().equals(selectedEntryId);
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x, cy, listW - 8, ROW_HEIGHT - 4);
                boolean locked = locked(context, entry);
                int color = locked ? TerminalUi.MUTED : TerminalUi.TEXT;
                TerminalUi.selectableRow(graphics, x, cy, listW - 8, ROW_HEIGHT - 4,
                        selected, hovered, descriptor.accentColor());
                TerminalUi.line(context, graphics, entry.title(), x + 6, cy + 4, listW - 90, color);
                TerminalUi.miniStatusPill(context, graphics, entry.status(), x + listW - 78, cy + 3, 64, color, selected);
                TerminalUi.line(context, graphics, entry.group(), x + 6, cy + 17, listW - 16, TerminalUi.MUTED);
                cy += ROW_HEIGHT;
            }

            TerminalArchiveEntry selected = selectedEntry(entries);
            if (selected == null) {
                return;
            }
            int detailPanelH = archiveDetailHeight(context, selected, detailW - 8);
            if (wide) {
                detailPanelH = Math.max(detailPanelH, Math.max(150, h - (detailY - y) - 8));
            }
            boolean locked = locked(context, selected);
            int detailColor = locked ? TerminalUi.MUTED : TerminalUi.TEXT;
            int dy = TerminalUi.flatDataPanel(context, graphics,
                    detailX, detailY, detailW - 8, detailPanelH, "RECORD DETAIL", "",
                    descriptor.accentColor());
            TerminalUi.line(context, graphics, selected.title(), detailX + 8, dy,
                    detailW - 92, detailColor);
            TerminalUi.miniStatusPill(context, graphics, selected.status(), detailX + detailW - 92, dy - 2, 78,
                    selected.locked() ? TerminalUi.MUTED : TerminalUi.GREEN, false);
            dy = TerminalUi.keyValue(context, graphics, detailX + 8, dy + 22,
                    detailW - 24, "Group", selected.group(), TerminalUi.AMBER) + 4;
            TerminalUi.divider(graphics, detailX + 8, dy, detailW - 24, descriptor.accentColor());
            dy += 9;
            if (locked) {
                TerminalUi.wrap(context, graphics, "Record locked. Discover the route proof through its owning chapter.",
                        detailX + 8, dy, detailW - 24, TerminalUi.MUTED);
            } else {
                for (String line : selected.lines()) {
                    dy = TerminalUi.wrap(context, graphics, line, detailX + 8, dy, detailW - 24, TerminalUi.TEXT) + 6;
                }
            }
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0 || !TerminalUi.inside(mouseX, mouseY, lastListX, lastListY, lastListW, lastListH)) {
                return false;
            }
            List<TerminalArchiveEntry> entries = TerminalArchiveRegistry.entries();
            int index = (int) ((mouseY - lastListY) / ROW_HEIGHT);
            if (index >= 0 && index < entries.size()) {
                selectedEntryId = entries.get(index).id().toString();
                return true;
            }
            return false;
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            List<TerminalArchiveEntry> entries = TerminalArchiveRegistry.entries();
            int w = context.contentWidth();
            boolean wide = w >= 640;
            int listW = wide ? Math.max(250, Math.min(390, w * 40 / 100)) : w;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;
            TerminalArchiveEntry selected = selectedEntry(entries);
            int rows = entries.size() * ROW_HEIGHT;
            int detailHeight = selected == null ? 40 : archiveDetailHeight(context, selected, detailW - 8) + 28;
            if (wide) {
                return Math.max(context.contentHeight(), Math.max(42 + rows, detailHeight));
            }
            return Math.max(context.contentHeight(), rows + detailHeight + 24);
        }

        private void normalizeSelection(List<TerminalArchiveEntry> entries) {
            if (entries.stream().anyMatch(entry -> entry.id().toString().equals(selectedEntryId))) {
                return;
            }
            selectedEntryId = entries.isEmpty() ? "" : entries.get(0).id().toString();
        }

        private TerminalArchiveEntry selectedEntry(List<TerminalArchiveEntry> entries) {
            normalizeSelection(entries);
            return entries.stream()
                    .filter(entry -> entry.id().toString().equals(selectedEntryId))
                    .findFirst()
                    .orElse(null);
        }

        private static int archiveDetailHeight(TerminalRenderContext context, TerminalArchiveEntry entry, int width) {
            if (entry == null) {
                return 80;
            }
            int bodyHeight = 0;
            if (locked(context, entry)) {
                bodyHeight = TerminalUi.wrappedHeight(context,
                    "Record locked. Discover the route proof through its owning chapter.", width - 16);
            } else {
                for (String line : entry.lines()) {
                    bodyHeight += TerminalUi.wrappedHeight(context, line, width - 16) + 6;
                }
            }
            return Math.max(100, 58 + bodyHeight);
        }

        private static boolean locked(TerminalRenderContext context, TerminalArchiveEntry entry) {
            return entry.locked() && !EchoCoreServices.isArchiveUnlocked(context.player(), entry.id().toString());
        }
    }
}
