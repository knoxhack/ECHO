package com.knoxhack.echoterminal.client;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalTabDescriptor;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.client.mission.TerminalMissionBrowser;
import com.knoxhack.echoterminal.mission.VanillaJourneyProvider;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class BuiltinTerminalTabs {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private BuiltinTerminalTabs() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalTabRegistry.register(new OverviewTab());
        TerminalTabRegistry.register(new VanillaJourneyTab());
        TerminalTabRegistry.register(new AddonsTab());
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("field_manual"),
                "Protocol Flow",
                "Terminal Field Interface",
                "OPEN",
                List.of(
                        "Use this terminal as the command surface for installed ECHO chapters and survival routes.",
                        "Tabs collect missions, field records, drone controls, route state, and addon status when those systems are present.",
                        "Progression remains server-authoritative; the terminal shows the clearest safe command view without unlocking content early."),
                false));
        TerminalTabRegistry.register(new ArchivesTab());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID, path);
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
            y = TerminalUi.imageHero(context, graphics, TerminalVisualAssets.OVERVIEW_PROTOCOL_DASHBOARD,
                    x, y, w, Math.min(58, Math.max(44, context.contentHeight() / 6)), descriptor.accentColor());
            TerminalUi.sectionHeader(context, graphics, "ECHO COMMAND SURFACE", "", x, y, w, descriptor.accentColor());
            int cy = TerminalUi.wrap(context, graphics,
                    "Installed ECHO chapters add mission control, field manuals, route state, drone commands, and addon status here.",
                    x, y + 18, w - 8, TerminalUi.TEXT) + 12;

            int cardW = Math.max(88, (w - 16) / 3);
            summaryCard(context, graphics, x, cy, cardW, "TABS", String.valueOf(TerminalTabRegistry.tabs().size()), TerminalUi.CYAN);
            summaryCard(context, graphics, x + cardW + 8, cy, cardW, "CHAPTERS",
                    String.valueOf(EchoAddonRegistry.chapters().size()), TerminalUi.AMBER);
            summaryCard(context, graphics, x + (cardW + 8) * 2, cy, cardW, "RECORDS",
                    String.valueOf(TerminalArchiveRegistry.entries().size()), TerminalUi.GREEN);
            cy += 58;

            TerminalUi.section(context, graphics, "NEXT", x, cy, descriptor.accentColor());
            if (EchoAddonRegistry.chapters().isEmpty()) {
                TerminalUi.wrap(context, graphics, "Install an ECHO gameplay chapter to populate missions and field systems.",
                        x, cy + 16, w, TerminalUi.MUTED);
            } else {
                TerminalUi.wrap(context, graphics, "Use the chapter tabs for current objectives, archive records, and available commands.",
                        x, cy + 16, w, TerminalUi.MUTED);
            }
        }

        @Override
        public int contentHeight(TerminalRenderContext context) {
            int w = context.contentWidth();
            int introH = TerminalUi.wrappedHeight(context,
                    "Installed ECHO chapters add mission control, field manuals, route state, drone commands, and addon status here.",
                    w - 8);
            String next = EchoAddonRegistry.chapters().isEmpty()
                    ? "Install an ECHO gameplay chapter to populate missions and field systems."
                    : "Use the chapter tabs for current objectives, archive records, and available commands.";
            return Math.max(context.contentHeight(),
                    66 + introH + 12 + 58 + 17 + TerminalUi.wrappedHeight(context, next, w));
        }

        private static void summaryCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
                int x, int y, int width, String label, String value, int color) {
            TerminalUi.panel(graphics, x, y, width, 42);
            TerminalUi.line(context, graphics, label, x + 8, y + 8, width - 16, TerminalUi.MUTED);
            TerminalUi.line(context, graphics, value, x + 8, y + 23, width - 16, color);
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
            normalizeSelection(EchoAddonRegistry.chapters());
        }

        @Override
        public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            int x = context.contentX();
            int y = context.contentY();
            int w = context.contentWidth();
            List<EchoAddonChapter> chapters = EchoAddonRegistry.chapters();
            TerminalUi.section(context, graphics, "ECHO ADDON ROADMAP", x, y, descriptor.accentColor());
            if (chapters.isEmpty()) {
                TerminalUi.line(context, graphics, "No addon chapters detected.", x, y + 18, w, TerminalUi.MUTED);
                return;
            }
            normalizeSelection(chapters);
            boolean wide = w >= 640;
            int listW = wide ? Math.max(260, Math.min(420, w * 42 / 100)) : w;
            int detailX = wide ? x + listW + 14 : x;
            int detailY = wide ? y + 18 : y + 24 + chapters.size() * ROW_HEIGHT;
            int detailW = wide ? Math.max(220, w - listW - 18) : w;

            int cy = y + 18;
            lastListX = x;
            lastListY = cy;
            lastListW = listW - 8;
            lastListH = chapters.size() * ROW_HEIGHT;
            TerminalUi.cinematicPanel(graphics, x, cy, lastListW, lastListH, descriptor.accentColor());
            for (EchoAddonChapter chapter : chapters) {
                boolean available = chapter.isAvailable(context.player());
                boolean selected = chapter.id().equals(selectedChapterId);
                boolean hovered = TerminalUi.inside(mouseX, mouseY, x, cy, listW - 8, ROW_HEIGHT - 8);
                int color = available ? 0xFF92F7A6 : 0xFF8D96A3;
                TerminalUi.selectableRow(graphics, x, cy, listW - 8, ROW_HEIGHT - 8,
                        selected, hovered, descriptor.accentColor());
                TerminalUi.line(context, graphics, chapter.displayName(), x + 8, cy + 7, listW - 106, color);
                TerminalUi.miniStatusPill(context, graphics, available ? "AVAILABLE" : "LOCKED",
                        x + listW - 96, cy + 6, 82, color, selected);
                TerminalUi.line(context, graphics, chapter.statusLine(context.player()), x + 8, cy + 25,
                        listW - 16, color);
                TerminalUi.line(context, graphics, chapter.summary(), x + 8, cy + 40,
                        listW - 20, TerminalUi.TEXT);
                cy += ROW_HEIGHT;
            }

            EchoAddonChapter selected = selectedChapter(chapters);
            if (selected == null) {
                return;
            }
            boolean available = selected.isAvailable(context.player());
            int color = available ? TerminalUi.GREEN : TerminalUi.MUTED;
            int detailPanelH = addonDetailHeight(context, selected, detailW - 8);
            TerminalUi.section(context, graphics, "CHAPTER DETAIL", detailX, detailY, descriptor.accentColor());
            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.ADDONS_MODULE_GRID,
                    detailX, detailY + 18, detailW - 8, detailPanelH, descriptor.accentColor(), 0.75F, true);
            TerminalUi.cinematicPanel(graphics, detailX, detailY + 18, detailW - 8, detailPanelH, descriptor.accentColor());
            TerminalUi.line(context, graphics, selected.displayName(), detailX + 8, detailY + 27,
                    detailW - 110, available ? TerminalUi.GREEN : TerminalUi.MUTED);
            TerminalUi.miniStatusPill(context, graphics, available ? "AVAILABLE" : "LOCKED",
                    detailX + detailW - 96, detailY + 25, 82, color, false);
            int dy = detailY + 48;
            dy = TerminalUi.keyValue(context, graphics, detailX + 8, dy, detailW - 24,
                    "Module", selected.modId(), TerminalUi.TEXT);
            dy = TerminalUi.keyValue(context, graphics, detailX + 8, dy, detailW - 24,
                    "Status", selected.statusLine(context.player()), color);
            dy = TerminalUi.wrap(context, graphics, selected.summary(), detailX + 8, dy + 4,
                    detailW - 24, TerminalUi.TEXT) + 6;
            TerminalUi.divider(graphics, detailX + 8, dy, detailW - 24, descriptor.accentColor());
            dy += 9;
            TerminalUi.wrap(context, graphics,
                    available
                            ? "Chapter systems are available in their registered terminal tabs. Server actions remain validated by the owning module."
                            : "Roadmap preview only. Complete the listed requirement before terminal actions or progression hooks become available.",
                    detailX + 8, dy, detailW - 24, available ? TerminalUi.GREEN : TerminalUi.AMBER);
        }

        @Override
        public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
            if (button != 0 || !TerminalUi.inside(mouseX, mouseY, lastListX, lastListY, lastListW, lastListH)) {
                return false;
            }
            List<EchoAddonChapter> chapters = EchoAddonRegistry.chapters();
            int index = (int) ((mouseY - lastListY) / ROW_HEIGHT);
            if (index >= 0 && index < chapters.size()) {
                selectedChapterId = chapters.get(index).id();
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
            int rows = EchoAddonRegistry.chapters().size() * ROW_HEIGHT;
            EchoAddonChapter selected = selectedChapter(EchoAddonRegistry.chapters());
            int detailHeight = selected == null ? 40 : addonDetailHeight(context, selected, detailW - 8) + 28;
            if (wide) {
                return Math.max(context.contentHeight(), Math.max(detailHeight, 42 + rows));
            }
            return Math.max(context.contentHeight(), rows + detailHeight + 24);
        }

        private void normalizeSelection(List<EchoAddonChapter> chapters) {
            if (chapters.stream().anyMatch(chapter -> chapter.id().equals(selectedChapterId))) {
                return;
            }
            selectedChapterId = chapters.isEmpty() ? "" : chapters.get(0).id();
        }

        private EchoAddonChapter selectedChapter(List<EchoAddonChapter> chapters) {
            normalizeSelection(chapters);
            return chapters.stream()
                    .filter(chapter -> chapter.id().equals(selectedChapterId))
                    .findFirst()
                    .orElse(null);
        }

        private static int addonDetailHeight(TerminalRenderContext context, EchoAddonChapter chapter, int width) {
            if (chapter == null) {
                return 80;
            }
            String summary = chapter.summary();
            String roadMap = chapter.isAvailable(context.player())
                    ? "Chapter systems are available in their registered terminal tabs. Server actions remain validated by the owning module."
                    : "Roadmap preview only. Complete the listed requirement before terminal actions or progression hooks become available.";
            return Math.max(118,
                    72
                            + TerminalUi.wrappedHeight(context, summary, width - 16)
                            + TerminalUi.wrappedHeight(context, roadMap, width - 16));
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
            List<TerminalArchiveEntry> entries = TerminalArchiveRegistry.entries();
            TerminalUi.section(context, graphics, "SHARED ARCHIVES", x, y, descriptor.accentColor());
            if (entries.isEmpty()) {
                TerminalUi.line(context, graphics, "No shared archive records registered.", x, y + 18, w, TerminalUi.MUTED);
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
                int color = entry.locked() ? TerminalUi.MUTED : TerminalUi.TEXT;
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
            int detailColor = selected.locked() ? TerminalUi.MUTED : TerminalUi.TEXT;
            TerminalUi.section(context, graphics, "RECORD DETAIL", detailX, detailY, descriptor.accentColor());
            TerminalUi.imagePanel(context, graphics, TerminalVisualAssets.ARCHIVES_DOSSIER_WALL,
                    detailX, detailY + 18, detailW - 8, detailPanelH, descriptor.accentColor(), 0.78F, true);
            TerminalUi.line(context, graphics, selected.title(), detailX + 8, detailY + 27,
                    detailW - 92, detailColor);
            TerminalUi.miniStatusPill(context, graphics, selected.status(), detailX + detailW - 78, detailY + 25, 64,
                    selected.locked() ? TerminalUi.MUTED : TerminalUi.GREEN, false);
            int dy = TerminalUi.keyValue(context, graphics, detailX + 8, detailY + 49,
                    detailW - 24, "Group", selected.group(), TerminalUi.AMBER) + 4;
            TerminalUi.divider(graphics, detailX + 8, dy, detailW - 24, descriptor.accentColor());
            dy += 9;
            if (selected.locked()) {
                TerminalUi.wrap(context, graphics, "Content locked. Discover this record through its owning chapter.",
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
            if (entry.locked()) {
                bodyHeight = TerminalUi.wrappedHeight(context,
                        "Content locked. Discover this record through its owning chapter.", width - 16);
            } else {
                for (String line : entry.lines()) {
                    bodyHeight += TerminalUi.wrappedHeight(context, line, width - 16) + 6;
                }
            }
            return Math.max(100, 58 + bodyHeight);
        }
    }
}
