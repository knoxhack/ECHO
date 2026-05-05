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

public final class BuiltinTerminalTabs {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Identifier REWARD_INBOX = id("reward_inbox");
    private static final Identifier CLAIM_REWARDS = id("claim_rewards");

    private BuiltinTerminalTabs() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalTabRegistry.register(new OverviewTab());
        TerminalTabRegistry.register(new DiagnosticsTab());
        TerminalTabRegistry.register(new MissionGraphTab());
        TerminalTabRegistry.register(new RouteRecordsTab());
        TerminalTabRegistry.register(new FactionAtlasTab());
        TerminalTabRegistry.register(new VitalsTab());
        TerminalTabRegistry.register(new RewardInboxTab());
        TerminalTabRegistry.register(new VanillaJourneyTab());
        TerminalTabRegistry.register(new AddonsTab());
        TerminalActionRegistry.register(REWARD_INBOX, CLAIM_REWARDS,
                (player, payload) -> EchoCoreServices.claimTerminalRewards(player));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("field_manual"),
                "Protocol Flow",
                "Terminal Field Interface",
                "OPEN",
                List.of(
                        "Use this terminal as the command surface for installed ECHO chapters and survival routes.",
                        "Tabs collect missions, field records, drone controls, route state, and chapter status when those systems are present.",
                        "Progression stays sealed until the field route proves it; the terminal shows the clearest safe command view without opening records early."),
                false));
        TerminalTabRegistry.register(new ArchivesTab());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, path);
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

    private static final class VanillaJourneyTab implements TerminalTab {
        private final TerminalTabDescriptor descriptor =
                new TerminalTabDescriptor(VanillaJourneyProvider.TAB_ID, "BASELINE", 170, 0xFF92F7A6);
        private final TerminalTabChrome chrome =
                TerminalTabChrome.of("Baseline", TerminalTabChrome.GROUP_FIELD, "BL", "Recovered Minecraft tasks",
                        170);
        private final TerminalMissionBrowser browser =
                new TerminalMissionBrowser(VanillaJourneyProvider.INSTANCE, descriptor.id(), true);

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

        @Override
        public TerminalTabDescriptor descriptor() {
            return descriptor;
        }

        @Override
        public TerminalTabChrome chrome() {
            return chrome;
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            EchoPackMode packMode = EchoCoreServices.packMode(context.player());
            List<EchoDiagnosticBlocker> diagnostics = EchoCoreServices.diagnostics(context.player());
            List<EchoRouteRecord> routes = EchoCoreServices.routeRecords(context.player());
            int pendingRewards = EchoCoreServices.pendingTerminalRewardCount(context.player());
            int heroH = Math.min(58, Math.max(44, context.contentHeight() / 6));
            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.OVERVIEW_PROTOCOL_DASHBOARD,
                    x, y, w, heroH, descriptor.accentColor(), 0.62F, true, TerminalUi.ImageFit.COVER);
            y += heroH + 8;
            TerminalUi.sectionHeader(context, graphics, "ECHO STACK DASHBOARD", packMode.displayName(), x, y, w, descriptor.accentColor());
            int cy = TerminalUi.wrap(context, graphics,
                    packMode.statusLine() + " Terminal deck collects chapters, missions, diagnostics, rewards, routes, and vitals through the shared stack.",
                    x, y + 18, w - 8, TerminalUi.TEXT) + 12;

            cy = summaryGrid(context, graphics, x, cy, w, new String[][] {
                    {"MODE", modeChip(packMode)},
                    {"CHAPTERS", String.valueOf(addonChapters().size())},
                    {"ROUTES", String.valueOf(routes.size())},
                    {"BLOCKERS", String.valueOf(diagnostics.size())},
                    {"INBOX", String.valueOf(pendingRewards)}
            }, new int[] {
                    TerminalUi.CYAN,
                    TerminalUi.AMBER,
                    TerminalUi.GREEN,
                    diagnostics.isEmpty() ? TerminalUi.MUTED : DiagnosticsTab.severityColor(diagnostics.get(0).severity()),
                    pendingRewards > 0 ? TerminalUi.AMBER : TerminalUi.MUTED
            });

            TerminalUi.section(context, graphics, "NEXT", x, cy, descriptor.accentColor());
            TerminalUi.wrap(context, graphics, nextAction(diagnostics, routes, pendingRewards),
                    x, cy + 16, w, diagnostics.isEmpty() ? TerminalUi.MUTED : DiagnosticsTab.severityColor(diagnostics.get(0).severity()));
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            int w = context.contentWidth();
            int introH = TerminalUi.wrappedHeight(context,
                    "Terminal deck collects chapters, missions, diagnostics, rewards, routes, and vitals through the shared stack.",
                    w - 8);
            List<EchoDiagnosticBlocker> diagnostics = EchoCoreServices.diagnostics(context.player());
            String next = nextAction(diagnostics, EchoCoreServices.routeRecords(context.player()),
                    EchoCoreServices.pendingTerminalRewardCount(context.player()));
            int columns = dashboardColumns(w);
            int rows = (int) Math.ceil(5.0D / columns);
            return Math.max(context.contentHeight(),
                    66 + introH + 12 + rows * 50 + 17 + TerminalUi.wrappedHeight(context, next, w));
        }

        private static int summaryGrid(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int width, String[][] cards, int[] colors) {
            int columns = dashboardColumns(width);
            int gap = 8;
            int cardW = Math.max(64, (width - gap * (columns - 1)) / columns);
            for (int i = 0; i < cards.length; i++) {
                int col = i % columns;
                int row = i / columns;
                int cx = x + col * (cardW + gap);
                int cy = y + row * 50;
                int cw = col == columns - 1 ? Math.max(64, width - col * (cardW + gap)) : cardW;
                summaryCard(context, graphics, cx, cy, cw, cards[i][0], cards[i][1], colors[i]);
            }
            int rows = (cards.length + columns - 1) / columns;
            return y + rows * 50 + 8;
        }

        private static int dashboardColumns(int width) {
            if (width >= 620) {
                return 5;
            }
            if (width >= 420) {
                return 3;
            }
            return 2;
        }

        private static void summaryCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int width, String label, String value, int color) {
            TerminalUi.panel(graphics, x, y, width, 42);
            TerminalUi.line(context, graphics, label, x + 8, y + 8, width - 16, TerminalUi.MUTED);
            TerminalUi.line(context, graphics, value, x + 8, y + 23, width - 16, color);
        }

        private static String nextAction(List<EchoDiagnosticBlocker> diagnostics,
                List<EchoRouteRecord> routes, int pendingRewards) {
            if (!diagnostics.isEmpty()) {
                EchoDiagnosticBlocker blocker = diagnostics.get(0);
                String action = blocker.nextAction().isBlank() ? blocker.detail() : blocker.nextAction();
                return blocker.severity().name() + ": " + blocker.title() + ". " + action;
            }
            if (pendingRewards > 0) {
                return "Reward Inbox has " + pendingRewards + " support cache item(s) ready to claim.";
            }
            if (addonChapters().isEmpty()) {
                return "Install or enable an ECHO chapter to populate missions and field systems.";
            }
            for (EchoRouteRecord route : routes) {
                if (!route.complete()) {
                    return "Continue route: " + route.title() + " / " + route.status() + ".";
                }
            }
            return "Use the chapter tabs for current objectives, archive records, and available commands.";
        }

        private static String modeChip(EchoPackMode packMode) {
            return switch (packMode) {
                case ASHFALL_STANDALONE -> "ASHFALL";
                case ORBITAL_STANDALONE -> "ORBITAL";
                case FULL_SAGA -> "FULL SAGA";
                case UNKNOWN -> "UNKNOWN";
            };
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
                    chapters.size() + " registered", descriptor.accentColor()) + 6;
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
                            ? "Chapter systems are online in their registered terminal tabs. ECHO will confirm the owning route before any field action is accepted."
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
                        "Installed chapters keep their own mission state, rewards, and command checks. ECHO presents their registered routes here.",
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
                    ? "Chapter systems are online in their registered terminal tabs. ECHO will confirm the owning route before any field action is accepted."
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
        private static final int ROW_HEIGHT = 58;
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
                        blocker.title(), "Reason: " + blocker.detail(), blocker.severity().name(),
                        false, TerminalUi.inside(mouseX, mouseY, x + 10, cy, w - 20, ROW_HEIGHT - 6),
                        descriptor.accentColor(), color);
                if (!blocker.nextAction().isBlank()) {
                    TerminalUi.line(context, graphics, "Next: " + blocker.nextAction(), x + 20, cy + 39, w - 40, TerminalUi.TEXT);
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
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w,
                    Math.max(130, context.contentHeight()), "MISSION GRAPH",
                    TerminalMissionRegistry.providers().size() + " source(s)", descriptor.accentColor()) + 8;
            if (TerminalMissionRegistry.providers().isEmpty()) {
                TerminalUi.wrap(context, graphics, "No mission routes are registered yet.",
                        x + 14, cy, w - 28, TerminalUi.MUTED);
                return;
            }
            for (TerminalMissionProvider provider : TerminalMissionRegistry.providers()) {
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
            return Math.max(context.contentHeight(), 70 + TerminalMissionRegistry.providers().size() * 66);
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
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w, Math.max(190, context.contentHeight()),
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
            return Math.max(context.contentHeight(), 270);
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
        private static final int ROW_HEIGHT = 58;
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
                    Math.max(130, Math.min(context.contentHeight(), 72 + Math.max(1, records.size()) * ROW_HEIGHT)),
                    "ROUTE RECORDS", records.size() + " route(s)", descriptor.accentColor()) + 8;
            if (records.isEmpty()) {
                TerminalUi.wrap(context, graphics,
                        "No route records are registered yet. Scanner routes, recovery sites, surveys, and orbital paths appear here when chapter providers publish them.",
                        x + 14, cy, w - 28, TerminalUi.MUTED);
                return;
            }
            for (EchoRouteRecord record : records) {
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x + 10, cy, w - 20, ROW_HEIGHT - 6);
                int color = record.complete() ? TerminalUi.GREEN : TerminalUi.AMBER;
                TerminalUi.dataListRow(context, graphics, x + 10, cy, w - 20, ROW_HEIGHT - 6,
                        record.title(), record.dimensionHint(), record.status(),
                        false, hovered, descriptor.accentColor(), color);
                TerminalUi.line(context, graphics, record.category() + " / " + record.summary(),
                        x + 20, cy + 34, w - 40, TerminalUi.MUTED);
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
                        "FACTION ATLAS", "0 registered", descriptor.accentColor());
                TerminalUi.line(context, graphics, "No faction signals are registered.",
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
                    "Contact Roles", definition.roles().isEmpty() ? "None"
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
            int cy = TerminalUi.flatDataPanel(context, graphics, x, y, w, Math.max(140, context.contentHeight() / 2),
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
