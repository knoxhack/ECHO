package com.knoxhack.echoterminal.client.mission;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalRenderCache;
import com.knoxhack.echoterminal.api.TerminalRenderContext;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import com.knoxhack.echoterminal.api.mission.TerminalMissionVisuals;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class TerminalMissionBrowser {
    private static final int MODE_ROW_HEIGHT = 18;
    private static final int PHASE_ROW_HEIGHT = 22;
    private static final int MISSION_ROW_HEIGHT = 30;
    private static final int ACTION_BAR_HEIGHT = 92;
    private static final int TREE_FOCUS_EXTRA = 8;

    private final TerminalMissionProvider provider;
    private final Identifier tabId;
    private final boolean showExpandControls;
    private final List<Hitbox> hitboxes = new ArrayList<>();
    private final Set<String> expandedPhases = new LinkedHashSet<>();
    private final Set<String> collapsedPhases = new LinkedHashSet<>();
    private final Set<String> warnedProviderSurfaces = new LinkedHashSet<>();

    private boolean allExpanded;
    private MissionFilter filterMode = MissionFilter.ALL;
    private MissionViewMode viewMode = MissionViewMode.VISUAL_RPG;
    private String searchQuery = "";
    private boolean searchFocused;
    private Identifier selectedMissionId;
    private Identifier lastDetailMissionId;
    private boolean pendingTreeFocus;
    private int treeScroll;
    private int detailScroll;
    private int lastTreeX;
    private int lastTreeY;
    private int lastTreeW;
    private int lastTreeH;
    private int lastTreeContentH;
    private int lastDetailX;
    private int lastDetailY;
    private int lastDetailW;
    private int lastDetailH;
    private int lastDetailContentH;
    private long cachedStateFrame = -1L;
    private MissionRenderState cachedState;

    public TerminalMissionBrowser(TerminalMissionProvider provider, Identifier tabId, boolean showExpandControls) {
        this.provider = provider;
        this.tabId = tabId;
        this.showExpandControls = showExpandControls;
    }

    public void onSelected(TerminalRenderContext context) {
        allExpanded = false;
        expandedPhases.clear();
        collapsedPhases.clear();
        filterMode = MissionFilter.ALL;
        viewMode = MissionViewMode.fromClientDefault();
        searchQuery = "";
        searchFocused = false;
        treeScroll = 0;
        detailScroll = 0;
        invalidateStateCache();
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        lastDetailMissionId = selectedMissionId;
        pendingTreeFocus = true;
    }

    public void render(TerminalRenderContext context, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        hitboxes.clear();
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        syncDetailScrollWithSelection();
        TerminalMissionChapter chapter = chapter();
        int x = context.contentX();
        int y = context.contentY();
        int w = context.contentWidth();
        int h = context.contentHeight();
        if (state.allRecords().isEmpty()) {
            y = TerminalUi.imageHero(context, graphics, TerminalVisualAssets.MISSIONS_VISUAL_HERO,
                    x, y, w, Math.min(60, Math.max(44, h / 5)), chapter.accentColor());
            TerminalUi.emptyState(context, graphics, x, y, w,
                    chapter.title(), "No mission records are available from this chapter yet.", chapter.accentColor());
            return;
        }

        MissionRecord selected = selectedRecord(state);
        // Validator token retained for the previous wide mission readability gate: w >= 820.
        boolean wide = w >= 720;
        if (wide) {
            int gap = 12;
            int leftW = Math.max(292, Math.min(388, w * 36 / 100));
            int detailX = x + leftW + gap;
            int detailW = Math.max(300, w - leftW - gap);
            drawRoadmapPane(context, graphics, state, x, y, leftW, h, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, detailX, y, detailW, h, mouseX, mouseY, true);
        } else if (w >= 430) {
            int gap = w >= 560 ? 10 : 8;
            int leftW = Math.max(w >= 560 ? 224 : 176, Math.min(w >= 560 ? 300 : 220, w * 40 / 100));
            int detailX = x + leftW + gap;
            drawRoadmapPane(context, graphics, state, x, y, leftW, h, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, detailX, y, Math.max(220, w - leftW - gap), h,
                    mouseX, mouseY, true);
        } else {
            int treeH = Math.min(treePaneHeight(context, state, w), Math.max(168, h * 44 / 100));
            drawRoadmapPane(context, graphics, state, x, y, w, treeH, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, x, y + treeH + 10, w,
                    Math.max(180, detailBodyHeight(context, selected, w) + ACTION_BAR_HEIGHT + 18),
                    mouseX, mouseY, false);
        }
    }

    public boolean mouseClicked(TerminalRenderContext context, double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        for (Hitbox hitbox : List.copyOf(hitboxes)) {
            if (TerminalUi.inside(mouseX, mouseY, hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())) {
                if (hitbox.enabled()) {
                    hitbox.action().run();
                } else {
                    context.playRejectedSound();
                }
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
        int key = event.key();
        boolean control = (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0
                || (event.modifiers() & GLFW.GLFW_MOD_SUPER) != 0;
        if (control && key == GLFW.GLFW_KEY_F) {
            searchFocused = true;
            return true;
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                if (!searchQuery.isBlank()) {
                    searchQuery = "";
                    invalidateStateCache();
                    normalizeSelection(buildState(context));
                    pendingTreeFocus = true;
                    return true;
                }
                searchFocused = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                invalidateStateCache();
                normalizeSelection(buildState(context));
                pendingTreeFocus = true;
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_W) {
            return selectRelative(state, -1);
        }
        if (key == GLFW.GLFW_KEY_DOWN || key == GLFW.GLFW_KEY_S) {
            return selectRelative(state, 1);
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) {
            filterMode = MissionFilter.previous(filterMode);
            invalidateStateCache();
            normalizeSelection(buildState(context));
            pendingTreeFocus = true;
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) {
            filterMode = MissionFilter.next(filterMode);
            invalidateStateCache();
            normalizeSelection(buildState(context));
            pendingTreeFocus = true;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
            return activateSelectedAction(context, state);
        }
        if (key == GLFW.GLFW_KEY_ESCAPE && (!searchQuery.isBlank() || filterMode != MissionFilter.ALL)) {
            searchQuery = "";
            searchFocused = false;
            filterMode = MissionFilter.ALL;
            invalidateStateCache();
            normalizeSelection(buildState(context));
            pendingTreeFocus = true;
            return true;
        }
        return false;
    }

    public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
        if (!event.isAllowedChatCharacter()) {
            return false;
        }
        String value = event.codepointAsString();
        if (value == null || value.isBlank()) {
            return false;
        }
        searchFocused = true;
        searchQuery = (searchQuery + value).stripLeading();
        invalidateStateCache();
        normalizeSelection(buildState(context));
        pendingTreeFocus = true;
        return true;
    }

    public boolean mouseScrolled(TerminalRenderContext context, double mouseX, double mouseY, double delta) {
        int amount = (int) Math.round(delta * 18.0D);
        if (TerminalUi.inside(mouseX, mouseY, lastTreeX, lastTreeY, lastTreeW, lastTreeH)) {
            treeScroll = TerminalUi.clampScroll(treeScroll - amount, lastTreeContentH, lastTreeH);
            return true;
        }
        if (TerminalUi.inside(mouseX, mouseY, lastDetailX, lastDetailY, lastDetailW, lastDetailH)) {
            detailScroll = TerminalUi.clampScroll(detailScroll - amount, lastDetailContentH, lastDetailH);
            return true;
        }
        return false;
    }

    public int contentHeight(TerminalRenderContext context) {
        int w = context.contentWidth();
        MissionRenderState state = buildState(context);
        normalizeSelection(state);
        if (w >= 430) {
            return context.contentHeight();
        }
        MissionRecord selected = selectedRecord(state);
        return Math.max(context.contentHeight(), treePaneHeight(context, state, w)
                + detailBodyHeight(context, selected, w) + ACTION_BAR_HEIGHT + 38);
    }

    private MissionRenderState buildState(TerminalRenderContext context) {
        long frame = TerminalRenderCache.current().frameId();
        if (cachedState != null && cachedStateFrame == frame) {
            return cachedState;
        }
        List<TerminalMissionDefinition> definitions = safeMissions(context).stream()
                .filter(definition -> definition != null)
                .sorted(Comparator
                        .comparingInt(TerminalMissionDefinition::phaseOrder)
                        .thenComparingInt(TerminalMissionDefinition::missionOrder)
                        .thenComparing(mission -> mission.id().toString()))
                .toList();
        List<MissionRecord> records = new ArrayList<>();
        for (TerminalMissionDefinition definition : definitions) {
            TerminalMissionSnapshot snapshot = safeSnapshot(context, definition);
            TerminalMissionPresentation presentation = safePresentation(context, definition, snapshot);
            TerminalMissionVisuals visuals = safeVisuals(context, definition, snapshot);
            TerminalMissionRole role = safeRole(context, definition, snapshot);
            records.add(new MissionRecord(definition, snapshot, presentation, visuals, role));
        }
        String query = normalizedSearchQuery();
        List<MissionRecord> visible = records.stream()
                .filter(filterMode::matches)
                .filter(record -> matchesSearch(record, query))
                .toList();
        MissionRecord focus = focusRecord(visible);
        int completed = 0;
        for (MissionRecord record : records) {
            if (isDone(record.snapshot().status())) {
                completed++;
            }
        }
        cachedState = new MissionRenderState(records, visible, focus, completed);
        cachedStateFrame = frame;
        return cachedState;
    }

    private TerminalMissionChapter chapter() {
        try {
            TerminalMissionChapter chapter = provider == null ? null : provider.chapter();
            return chapter == null ? fallbackChapter() : chapter;
        } catch (RuntimeException exception) {
            warnProviderFailure("chapter", exception);
            return fallbackChapter();
        }
    }

    private TerminalMissionChapter fallbackChapter() {
        Identifier id = tabId == null ? Identifier.fromNamespaceAndPath("echoterminal", "unknown_missions") : tabId;
        return new TerminalMissionChapter(id, "Mission Records", "Mission provider unavailable.", Integer.MAX_VALUE,
                0xFF66D9FF, true);
    }

    private List<TerminalMissionDefinition> safeMissions(TerminalRenderContext context) {
        try {
            List<TerminalMissionDefinition> missions = provider == null
                    ? List.of()
                    : provider.missions(context == null ? null : context.player());
            return missions == null ? List.of() : missions;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission list", exception);
            return List.of();
        }
    }

    private TerminalMissionSnapshot safeSnapshot(TerminalRenderContext context, TerminalMissionDefinition definition) {
        try {
            TerminalMissionSnapshot snapshot = provider.snapshot(context == null ? null : context.player(), definition.id());
            return snapshot == null ? fallbackSnapshot(definition) : snapshot;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission snapshot", exception);
            return fallbackSnapshot(definition);
        }
    }

    private TerminalMissionSnapshot fallbackSnapshot(TerminalMissionDefinition definition) {
        return new TerminalMissionSnapshot(
                definition.id(),
                TerminalMissionStatus.LOCKED,
                0.0F,
                "LOCKED",
                "Mission provider unavailable.",
                "Open What Now? diagnostics or reload this chapter later.",
                List.of());
    }

    private TerminalMissionPresentation safePresentation(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionPresentation presentation = provider.presentation(
                    context == null ? null : context.player(), definition, snapshot);
            return presentation == null
                    ? TerminalMissionPresentation.fallback(definition, snapshot)
                    : presentation;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission presentation", exception);
            return TerminalMissionPresentation.fallback(definition, snapshot);
        }
    }

    private TerminalMissionVisuals safeVisuals(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionVisuals visuals = provider.visuals(context == null ? null : context.player(), definition, snapshot);
            return visuals == null ? TerminalMissionVisuals.fallback(definition, snapshot) : visuals;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission visuals", exception);
            return TerminalMissionVisuals.fallback(definition, snapshot);
        }
    }

    private TerminalMissionRole safeRole(TerminalRenderContext context,
            TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        try {
            TerminalMissionRole role = provider.role(context == null ? null : context.player(), definition, snapshot);
            return role == null ? TerminalMissionRole.fallback(definition, snapshot) : role;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission role", exception);
            return TerminalMissionRole.fallback(definition, snapshot);
        }
    }

    private void warnProviderFailure(String surface, RuntimeException exception) {
        if (warnedProviderSurfaces.add(surface)) {
            EchoTerminal.LOGGER.warn("Terminal mission provider {} failed while building {}; rendering fallback.",
                    providerName(), surface, exception);
        }
    }

    private String providerName() {
        return provider == null ? "<null>" : provider.getClass().getName();
    }

    private void drawRoadmapPane(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int h, int mouseX, int mouseY) {
        TerminalMissionChapter chapter = chapter();
        TerminalUi.cinematicPanel(graphics, x, y, w, h, chapter.accentColor());
        TerminalUi.line(context, graphics, "PROTOCOL ROADMAP", x + 12, y + 10, w - 24, chapter.accentColor());
        TerminalUi.line(context, graphics, state.completedCount() + "/" + state.allRecords().size() + " complete",
                x + w - 112, y + 10, 98, TerminalUi.MUTED);
        TerminalUi.divider(graphics, x + 12, y + 27, w - 24, chapter.accentColor());
        int innerX = x + 12;
        int innerW = w - 24;
        int listY = y + 38;
        TerminalUi.searchBox(graphics, context.minecraft().font,
                innerX, listY, innerW - 4, searchLabel(), searchFocused ? chapter.accentColor() : TerminalUi.MUTED);
        listY += 22;
        listY = drawFilterChips(context, graphics, innerX, listY, innerW - 4, mouseX, mouseY) + 4;
        if (showExpandControls) {
            int utilityY = listY;
            int utilityW = Math.max(1, (innerW - 4) / 2);
            int compactW = Math.max(1, innerW - utilityW - 4);
            drawCompactButton(context, graphics, innerX, utilityY, utilityW, "EXPAND", true, mouseX, mouseY, () -> {
                allExpanded = true;
                expandedPhases.clear();
                collapsedPhases.clear();
            });
            drawCompactButton(context, graphics, innerX + utilityW + 4, utilityY, compactW, "COMPACT", true,
                    mouseX, mouseY, () -> {
                        allExpanded = false;
                        expandedPhases.clear();
                        collapsedPhases.clear();
                        pendingTreeFocus = true;
                    });
            listY = utilityY + 20;
        }

        lastTreeX = innerX;
        lastTreeY = listY;
        lastTreeW = innerW - 4;
        lastTreeH = Math.max(68, h - (listY - y) - 12);
        lastTreeContentH = treeRowsHeight(context, state);
        focusTreeOnSelection(state);
        treeScroll = TerminalUi.clampScroll(treeScroll, lastTreeContentH, lastTreeH);
        boolean scissor = lastTreeContentH > lastTreeH;
        if (scissor) {
            graphics.enableScissor(innerX, listY, innerX + innerW - 4, listY + lastTreeH);
        }
        drawTreeRows(context, graphics, state, innerX, listY - (scissor ? treeScroll : 0),
                innerW, listY, lastTreeH, mouseX, mouseY);
        if (scissor) {
            graphics.disableScissor();
            TerminalUi.scrollbar(graphics, innerX + innerW - 5, listY, lastTreeH,
                    treeScroll, Math.max(0, lastTreeContentH - lastTreeH), chapter.accentColor());
        }
    }

    private void drawTreeRows(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int viewportY, int viewportH, int mouseX, int mouseY) {
        if (state.visibleRecords().isEmpty()) {
            String detail = searchQuery.isBlank()
                    ? "No mission records match the " + filterMode.label() + " filter."
                    : "No mission records match \"" + searchQuery + "\" in the " + filterMode.label() + " filter.";
            TerminalUi.emptyState(context, graphics, x + 2, y + 4, Math.max(80, w - 12),
                    "No Matches", detail, TerminalUi.MUTED);
            return;
        }
        int cy = y;
        if (viewMode == MissionViewMode.GUIDED) {
            cy = drawGuidedLanes(context, graphics, state, x, cy, w, viewportY, viewportH, mouseX, mouseY);
            cy += 4;
            TerminalUi.missionLaneHeader(context, graphics, x, cy, w - 8, "ROADMAP", "all records", chapter().accentColor());
            cy += 20;
        }
        for (PhaseGroup phase : phases(state.visibleRecords())) {
            List<MissionRecord> phaseAll = recordsForPhase(state.allRecords(), phase.id());
            int complete = completedCount(phaseAll);
            int total = phaseAll.size();
            boolean expanded = isPhaseExpanded(phase);
            int phaseColor = complete == total ? TerminalUi.GREEN : chapter().accentColor();
            boolean hover = TerminalUi.inside(mouseX, mouseY, x, cy, w - 8, PHASE_ROW_HEIGHT - 2);
            graphics.fill(x, cy, x + w - 8, cy + PHASE_ROW_HEIGHT - 2, hover ? 0xFF102630 : 0xAA0A151C);
            graphics.fill(x, cy, x + 3, cy + PHASE_ROW_HEIGHT - 2, phaseColor);
            TerminalUi.line(context, graphics, (expanded ? "- " : "+ ") + phase.title(),
                    x + 8, cy + 4, w - 92, phaseColor);
            TerminalUi.line(context, graphics, complete + "/" + total + " done", x + w - 82, cy + 4, 76,
                    complete == total ? TerminalUi.GREEN : TerminalUi.MUTED);
            if (visible(cy, PHASE_ROW_HEIGHT, viewportY, viewportH)) {
                PhaseGroup hitPhase = phase;
                addHitbox(x, cy, w - 8, PHASE_ROW_HEIGHT - 2, true, () -> togglePhase(hitPhase));
            }
            cy += PHASE_ROW_HEIGHT;
            if (!expanded) {
                continue;
            }
            for (MissionRecord record : phase.records()) {
                drawMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
                cy += MISSION_ROW_HEIGHT;
            }
        }
    }

    private int drawGuidedLanes(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int viewportY, int viewportH, int mouseX, int mouseY) {
        int cy = y;
        List<MissionRecord> main = state.visibleRecords().stream()
                .filter(record -> record.role() == TerminalMissionRole.MAIN)
                .filter(record -> !isDone(record.snapshot().status()))
                .limit(3)
                .toList();
        List<MissionRecord> ready = state.visibleRecords().stream()
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .limit(3)
                .toList();
        List<MissionRecord> optional = state.visibleRecords().stream()
                .filter(record -> record.role() == TerminalMissionRole.OPTIONAL)
                .limit(3)
                .toList();
        cy = drawLane(context, graphics, "ECHO-7 PROTOCOL", main, state.focusRecord() == null ? List.of() : List.of(state.focusRecord()),
                x, cy, w, viewportY, viewportH, mouseX, mouseY, chapter().accentColor());
        cy = drawLane(context, graphics, "READY TO CLAIM", ready, List.of(),
                x, cy, w, viewportY, viewportH, mouseX, mouseY, TerminalUi.GREEN);
        cy = drawLane(context, graphics, "SIGNAL LEADS", optional, List.of(),
                x, cy, w, viewportY, viewportH, mouseX, mouseY, TerminalUi.AMBER);
        if (optional.isEmpty()) {
            TerminalUi.line(context, graphics, "Optional missions are nonblocking and appear here when available.",
                    x + 8, cy - 4, w - 18, TerminalUi.MUTED);
            cy += 12;
        }
        return cy;
    }

    private int drawLane(TerminalRenderContext context, GuiGraphicsExtractor graphics, String title,
            List<MissionRecord> records, List<MissionRecord> fallback, int x, int y, int w,
            int viewportY, int viewportH, int mouseX, int mouseY, int color) {
        List<MissionRecord> rows = records.isEmpty() ? fallback : records;
        int cy = TerminalUi.missionLaneHeader(context, graphics, x, y, w - 8, title,
                rows.isEmpty() ? "0" : String.valueOf(rows.size()), color);
        for (MissionRecord record : rows) {
            drawMissionRow(context, graphics, record, x + 8, cy, w - 18, viewportY, viewportH, mouseX, mouseY);
            cy += MISSION_ROW_HEIGHT;
        }
        return cy + 2;
    }

    private void drawMissionSideRail(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int h, int mouseX, int mouseY) {
        int accent = chapter().accentColor();
        int gap = 10;
        int optionalH = Math.max(138, Math.min(218, h * 35 / 100));
        int readyH = Math.max(90, Math.min(132, h * 20 / 100));
        int tipH = Math.max(92, h - optionalH - readyH - gap * 2);
        int cy = y;

        TerminalUi.densePanel(graphics, x, cy, w, optionalH, TerminalUi.AMBER);
        TerminalUi.line(context, graphics, "SIGNAL LEADS", x + 8, cy + 7, w - 16, TerminalUi.AMBER);
        int bodyY = cy + 22;
        List<MissionRecord> optional = state.allRecords().stream()
                .filter(record -> record.role() == TerminalMissionRole.OPTIONAL)
                .filter(record -> !isDone(record.snapshot().status()))
                .limit(4)
                .toList();
        if (optional.isEmpty()) {
            TerminalUi.wrap(context, graphics, "Signal leads are nonblocking. They appear here as the protocol expands.",
                    x + 8, bodyY + 4, w - 16, TerminalUi.MUTED);
        } else {
            int rowY = bodyY + 2;
            for (MissionRecord record : optional) {
                drawSideMissionRow(context, graphics, record, x + 8, rowY, w - 16, mouseX, mouseY);
                rowY += 30;
            }
        }
        drawRailButton(context, graphics, x + 8, cy + optionalH - 22, w - 16, "Show Optional", TerminalUi.AMBER,
                mouseX, mouseY, () -> {
                    optional.stream().findFirst().ifPresent(record -> {
                        expandedPhases.add(record.definition().phaseId());
                        selectMission(record.id(), true);
                    });
                });
        cy += optionalH + gap;

        bodyY = TerminalUi.dashboardCard(context, graphics, x, cy, w, readyH, "READY TO CLAIM", TerminalUi.GREEN);
        List<MissionRecord> ready = state.allRecords().stream()
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .limit(3)
                .toList();
        if (ready.isEmpty()) {
            TerminalUi.wrap(context, graphics, "No reward caches are ready. Finish objectives, then return here.",
                    x + 8, bodyY + 4, w - 16, TerminalUi.MUTED);
        } else {
            int rowY = bodyY + 2;
            for (MissionRecord record : ready) {
                drawSideMissionRow(context, graphics, record, x + 8, rowY, w - 16, mouseX, mouseY);
                rowY += 30;
            }
        }
        cy += readyH + gap;

        bodyY = TerminalUi.dashboardCard(context, graphics, x, cy, w, tipH, "GUIDE SIGNAL", accent);
        MissionRecord focus = state.focusRecord();
        String next = focus == null ? "Open a mission to pick the next protocol step." : focus.presentation().nextStep();
        TerminalUi.wrap(context, graphics, next.isBlank() ? "Current objective is ready for field execution." : next,
                x + 8, bodyY + 4, w - 16, TerminalUi.TEXT);
        int lineY = bodyY + Math.min(54, Math.max(34, TerminalUi.wrappedHeight(context, next, w - 16) + 14));
        TerminalUi.line(context, graphics, "MAIN missions drive progression.", x + 8, lineY, w - 16, TerminalUi.GREEN);
        TerminalUi.line(context, graphics, "OPTIONAL missions never block the main route.", x + 8, lineY + 13,
                w - 16, TerminalUi.AMBER);
    }

    private void drawSideMissionRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        int color = statusColor(record.snapshot().status());
        boolean selected = record.id().equals(selectedMissionId);
        boolean hovered = TerminalUi.inside(mouseX, mouseY, x, y, w, 26);
        if (viewMode == MissionViewMode.VISUAL_RPG && TerminalClientOptions.useVisualAssets()) {
            TerminalUi.questArtCard(context, graphics, record.visuals().categoryArt(), x, y, w, 26,
                    color, selected, hovered);
            TerminalUi.iconTextureBadge(graphics, TerminalVisualAssets.missionIconArt(record.definition().id(),
                            record.definition().category()),
                    x + 5, y + 3, 20, color, selected);
            TerminalUi.line(context, graphics, record.presentation().shortTitle(), x + 31, y + 7, w - 134,
                    missionTitleColor(record.snapshot().status(), selected, color));
            TerminalUi.missionStatusPill(context, graphics, compactStatusLabel(record.snapshot()),
                    x + w - 94, y + 6, 84);
        } else {
            TerminalUi.missionCard(context, graphics, null,
                    x, y, w, 26, record.presentation().shortTitle(), roleLabel(record.role()),
                    compactStatusLabel(record.snapshot()), color, selected, hovered);
        }
        addHitbox(x, y, w, 26, true, () -> selectMission(record.id(), false));
    }

    private void drawRailButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, int mouseX, int mouseY, Runnable action) {
        boolean hover = TerminalUi.inside(mouseX, mouseY, x, y, w, 16);
        TerminalUi.actionButton(context, graphics, x, y, w, label, color, true, hover);
        addHitbox(x, y, w, 16, true, action);
    }

    private void drawMissionRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int rowX, int y, int rowW, int viewportY, int viewportH, int mouseX, int mouseY) {
        TerminalMissionSnapshot snapshot = record.snapshot();
        boolean selected = record.id().equals(selectedMissionId);
        int color = statusColor(snapshot.status());
        int chipW = Math.max(72, Math.min(98, rowW / 4));
        int chipX = rowX + rowW - chipW - 6;
        int textX = rowX + 34;
        int titleW = Math.max(42, chipX - textX - 8);
        int progressW = Math.max(48, chipX - textX);
        boolean hovered = TerminalUi.inside(mouseX, mouseY, rowX, y, rowW, MISSION_ROW_HEIGHT - 4);
        TerminalUi.selectableRow(graphics, rowX, y, rowW, MISSION_ROW_HEIGHT - 4,
                selected, hovered, color);
        if (selected) {
            graphics.fill(rowX, y, rowX + 3, y + MISSION_ROW_HEIGHT - 4, color);
            graphics.outline(rowX, y, rowW, MISSION_ROW_HEIGHT - 4, color);
        } else if (snapshot.status() == TerminalMissionStatus.UNLOCKED || snapshot.status() == TerminalMissionStatus.CLAIMABLE) {
            graphics.fill(rowX, y, rowX + 2, y + MISSION_ROW_HEIGHT - 4, color);
        }
        TerminalUi.iconTextureBadge(graphics,
                TerminalVisualAssets.missionIconArt(record.definition().id(), record.definition().category()),
                rowX + 5, y + 2, 22, color,
                selected || TerminalUi.inside(mouseX, mouseY, rowX + 5, y + 2, 22, 22));
        String rolePrefix = record.role() == TerminalMissionRole.OPTIONAL ? "OPT " : "";
        TerminalUi.line(context, graphics, rolePrefix + record.definition().missionOrder() + ". " + record.presentation().shortTitle(),
                textX, y + 3, titleW, missionTitleColor(snapshot.status(), selected, color));
        TerminalUi.missionStatusPill(context, graphics, compactStatusLabel(snapshot), chipX, y + 3, chipW);
        TerminalUi.progress(graphics, textX, y + 20, progressW, 4, snapshot.progress(), color);
        if (visible(y, MISSION_ROW_HEIGHT, viewportY, viewportH)) {
            addHitbox(rowX, y, rowW, MISSION_ROW_HEIGHT - 4, true, () -> selectMission(record.id(), false));
        }
    }

    private void drawDetailPane(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int h, int mouseX, int mouseY, boolean scrollable) {
        TerminalUi.cinematicPanel(graphics, x, y, w - 4, h, chapter().accentColor());
        if (record == null) {
            TerminalUi.emptyState(context, graphics, x + 10, y + 12, w - 24,
                    "Select Mission", "Choose a mission record from the command queue.", TerminalUi.MUTED);
            return;
        }
        int bodyX = x + 12;
        int bodyW = w - 30;
        int bodyY = y + 12;
        int actionH = Math.min(ACTION_BAR_HEIGHT, Math.max(84, h / 5));
        int actionY = y + h - actionH - 10;
        int bodyH = Math.max(70, actionY - bodyY - 10);
        lastDetailX = bodyX;
        lastDetailY = bodyY;
        lastDetailW = bodyW;
        lastDetailH = bodyH;
        lastDetailContentH = detailBodyHeight(context, record, bodyW);
        detailScroll = TerminalUi.clampScroll(detailScroll, lastDetailContentH, lastDetailH);
        boolean scissor = scrollable && lastDetailContentH > lastDetailH;
        if (scissor) {
            graphics.enableScissor(bodyX, bodyY, bodyX + bodyW - 4, bodyY + bodyH);
        }
        int cy = drawDetailBody(context, graphics, record, bodyX, bodyY - (scissor ? detailScroll : 0), bodyW, mouseX, mouseY);
        lastDetailContentH = Math.max(lastDetailContentH, cy - (bodyY - (scissor ? detailScroll : 0)));
        if (scissor) {
            graphics.disableScissor();
            TerminalUi.scrollbar(graphics, x + w - 9, bodyY, bodyH,
                    detailScroll, Math.max(0, lastDetailContentH - bodyH), chapter().accentColor());
        }
        drawStickyActions(context, graphics, record, bodyX, actionY, bodyW, actionH, mouseX, mouseY);
    }

    private int drawDetailBody(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        int cy = drawBriefingHeader(context, graphics, record, x, y, w, mouseX, mouseY) + 8;
        cy = drawNextStepCallout(context, graphics, record, x, cy, w) + 2;
        TerminalUi.sectionHeader(context, graphics, "REQUIREMENTS", "", x, cy, w - 4, chapter().accentColor());
        cy += 20;
        if (record.definition().requirements().isEmpty()) {
            TerminalUi.line(context, graphics, "No visible checklist. ECHO tracks this protocol in the field.",
                    x + 4, cy, w - 12, TerminalUi.MUTED);
            cy += 14;
        } else {
            for (TerminalMissionRequirement requirement : record.definition().requirements()) {
                cy = drawRequirementRow(context, graphics, requirement, x + 2, cy, w - 10, mouseX, mouseY);
            }
        }
        cy += 4;
        cy = drawRewards(context, graphics, record, x, cy, w, mouseX, mouseY);
        TerminalUi.sectionHeader(context, graphics, "FIELD GUIDE", "", x, cy, w - 4, chapter().accentColor());
        cy += 20;
        String guide = record.definition().fieldGuide().isBlank()
                ? record.definition().briefing()
                : record.definition().fieldGuide();
        cy = TerminalUi.wrap(context, graphics, guide, x + 2, cy, w - 12, TerminalUi.TEXT) + 9;
        if (!record.presentation().relatedIntelKey().isBlank()) {
            TerminalUi.sectionHeader(context, graphics, "RELATED INTEL", "", x, cy, w - 4, chapter().accentColor());
            cy += 20;
            cy = TerminalUi.wrap(context, graphics, intelLabel(record.presentation().relatedIntelKey()),
                    x + 2, cy, w - 12, TerminalUi.MUTED) + 8;
        }
        if (!record.definition().prerequisites().isEmpty()) {
            TerminalUi.sectionHeader(context, graphics, "PREREQUISITES", "", x, cy, w - 4, chapter().accentColor());
            cy += 20;
            for (String prerequisite : record.definition().prerequisites()) {
                cy = TerminalUi.wrap(context, graphics, "- " + prerequisite, x + 2, cy, w - 12, TerminalUi.MUTED) + 2;
            }
        }
        return cy + 8;
    }

    private int drawBriefingHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        int color = statusColor(record.snapshot().status());
        int height = briefingHeaderHeight(record);
        TerminalUi.flatHudPanel(graphics, x, y, w - 4, height, color);
        int iconSize = w < 360 ? 38 : 46;
        TerminalUi.iconTextureBadge(graphics, TerminalVisualAssets.missionIconArt(record.definition().id(),
                        record.definition().category()),
                x + 10, y + 14, iconSize, color, true);
        int chipW = Math.max(84, Math.min(124, w / 4));
        int chipX = x + w - chipW - 14;
        int textX = x + iconSize + 24;
        int titleW = Math.max(40, chipX - textX - 10);
        TerminalUi.missionStatusPill(context, graphics, compactStatusLabel(record.snapshot()),
                chipX, y + 12, chipW);
        TerminalUi.miniStatusPill(context, graphics, roleChipLabel(record.role()),
                chipX, y + 30, chipW, color, false);
        TerminalUi.line(context, graphics, record.presentation().shortTitle().toUpperCase(), textX, y + 20,
                titleW, TerminalUi.TEXT);
        TerminalUi.line(context, graphics, tagLine(record.definition(), record.presentation(), record.role()),
                textX, y + 36, titleW, color);
        TerminalUi.wrap(context, graphics, record.presentation().objectiveSummary(),
                textX, y + 54, Math.max(48, w - (textX - x) - 14), TerminalUi.TEXT);
        TerminalUi.progress(graphics, x + 10, y + height - 13, w - 24, 6, record.snapshot().progress(), color);
        return y + height;
    }

    private int drawNextStepCallout(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w) {
        String hint = record.presentation().nextStep().isBlank()
                ? "This record is visible for planning."
                : record.presentation().nextStep();
        return TerminalUi.callout(context, graphics, x, y, w - 4, "NEXT STEP", hint,
                actionHintColor(record.snapshot()));
    }

    private int drawRequirementRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            TerminalMissionRequirement requirement, int x, int y, int w, int mouseX, int mouseY) {
        int color = requirement.satisfied() ? TerminalUi.GREEN : TerminalUi.AMBER;
        int rowH = requirementHeight(context, requirement, w);
        TerminalUi.flatHudPanel(graphics, x, y, w, rowH - 4, color);
        TerminalUi.itemSlot(context, graphics, requirement.icon(), x + 6, y + 6, color,
                TerminalUi.inside(mouseX, mouseY, x + 6, y + 6, 20, 20));
        int chipW = Math.max(74, Math.min(92, w / 4));
        int chipX = x + w - chipW - 10;
        int textW = Math.max(38, chipX - (x + 32) - 8);
        TerminalUi.line(context, graphics, requirement.label(), x + 32, y + 6, textW, color);
        String progress = requirement.need() > 0 ? requirement.have() + "/" + requirement.need() : "";
        TerminalUi.missionStatusPill(context, graphics, requirement.satisfied() ? "DONE" : "NEEDED",
                chipX, y + 6, chipW);
        TerminalUi.wrap(context, graphics, requirement.detail().isBlank() ? progress : requirement.detail(),
                x + 32, y + 20, Math.max(40, textW), TerminalUi.MUTED);
        return y + rowH;
    }

    private int drawRewards(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        TerminalUi.sectionHeader(context, graphics, "REWARDS", "", x, y, w - 4, chapter().accentColor());
        int cy = y + 20;
        List<ItemStack> stacks = record.definition().rewards().stream()
                .map(TerminalMissionReward::stack)
                .filter(stack -> !stack.isEmpty())
                .toList();
        cy = TerminalUi.itemGrid(context, graphics, stacks, x + 2, cy, w - 12,
                chapter().accentColor(), mouseX, mouseY) + 3;
        for (TerminalMissionReward reward : record.definition().rewards()) {
            if (!reward.stack().isEmpty()) {
                continue;
            }
            cy = TerminalUi.wrap(context, graphics, reward.label() + ": " + reward.detail(),
                    x + 2, cy, w - 12, TerminalUi.MUTED) + 2;
        }
        return cy + 8;
    }

    private void drawStickyActions(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int h, int mouseX, int mouseY) {
        String summary = commandSummary(record.snapshot(), record.presentation());
        TerminalUi.flatHudPanel(graphics, x, y, w - 4, h, chapter().accentColor());
        TerminalUi.line(context, graphics, "COMMAND", x + 8, y + 8, w - 20, chapter().accentColor());
        TerminalUi.line(context, graphics, summary, x + 8, y + 21, w - 20, TerminalUi.TEXT);
        int buttonY = y + 43;
        List<TerminalMissionAction> actions = record.snapshot().actions();
        if (actions.isEmpty()) {
            TerminalUi.disabledReasonRow(context, graphics, x + 8, buttonY, w - 20,
                    "No command is available for this record.", TerminalUi.MUTED);
            return;
        }
        int buttonH = 22;
        int gaps = Math.max(0, actions.size() - 1) * 8;
        int buttonW = actions.size() == 1
                ? Math.min(220, w - 20)
                : Math.min(150, Math.max(72, (w - 16 - gaps) / actions.size()));
        int bx = x + 8;
        for (TerminalMissionAction action : actions) {
            boolean hover = action.enabled() && TerminalUi.inside(mouseX, mouseY, bx, buttonY, buttonW, buttonH);
            if (action.enabled()) {
                TerminalUi.primaryCommandButton(context, graphics, bx, buttonY, buttonW, buttonH, action.label(),
                        actionIcon(action), chapter().accentColor(), hover);
            } else {
                TerminalUi.disabledCommandButton(context, graphics, bx, buttonY, buttonW, buttonH,
                        action.label(), actionIcon(action));
            }
            TerminalMissionAction hitAction = action;
            addHitbox(bx, buttonY, buttonW, buttonH, action.enabled(), () -> context.sendAction(
                    tabId,
                    TerminalMissionActions.MISSION_ACTION,
                        TerminalMissionActions.payload(chapter().id(), record.definition().id(), hitAction.id())));
            bx += buttonW + 8;
        }
        String reason = firstDisabledReason(actions);
        if (!reason.isBlank()) {
            TerminalUi.line(context, graphics, reason, x + 8, buttonY + 29, w - 20, 0xFFC2D4DC);
        }
    }

    private void drawMissionModeChips(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int mouseX, int mouseY) {
        int labelW = 38;
        TerminalUi.line(context, graphics, "MODE", x, y + 4, labelW, TerminalUi.MUTED);
        int chipX = x + labelW + 4;
        int chipW = Math.max(54, Math.min(78, (w - labelW - 12) / MissionViewMode.values().length));
        for (MissionViewMode mode : MissionViewMode.values()) {
            boolean selected = mode == viewMode;
            boolean hover = TerminalUi.inside(mouseX, mouseY, chipX, y, chipW, 15);
            TerminalUi.filterChip(context, graphics, chipX, y, chipW, mode.label(), selected, true,
                    chapter().accentColor(), hover);
            addHitbox(chipX, y, chipW, 15, true, () -> {
                viewMode = mode;
                detailScroll = 0;
                treeScroll = 0;
                pendingTreeFocus = true;
                invalidateStateCache();
            });
            chipX += chipW + 4;
        }
    }

    private int drawFilterChips(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int mouseX, int mouseY) {
        int labelW = 38;
        TerminalUi.line(context, graphics, "SHOW", x, y + 4, labelW, TerminalUi.MUTED);
        int chipX = x + labelW + 4;
        int chipY = y;
        int chipW = Math.max(52, Math.min(74, (w - labelW - 8) / MissionFilter.values().length - 3));
        for (MissionFilter filter : MissionFilter.values()) {
            boolean selected = filter == filterMode;
            boolean hover = TerminalUi.inside(mouseX, mouseY, chipX, chipY, chipW, 15);
            TerminalUi.filterChip(context, graphics, chipX, chipY, chipW, filter.label(), selected, true,
                    chapter().accentColor(), hover);
            addHitbox(chipX, chipY, chipW, 15, true, () -> {
                filterMode = filter;
                detailScroll = 0;
                treeScroll = 0;
                pendingTreeFocus = true;
                invalidateStateCache();
            });
            chipX += chipW + 3;
            if (chipX + chipW > x + w && filter.ordinal() + 1 < MissionFilter.values().length) {
                chipX = x + labelW + 4;
                chipY += 18;
            }
        }
        int modeY = chipY + 19;
        if (w >= 230) {
            drawMissionModeChips(context, graphics, x, modeY, w, mouseX, mouseY);
            return modeY + 18;
        }
        return modeY;
    }

    private void drawCompactButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, boolean enabled, int mouseX, int mouseY, Runnable action) {
        boolean hover = enabled && TerminalUi.inside(mouseX, mouseY, x, y, w, 16);
        TerminalUi.compactButton(context, graphics, x, y, w, label, chapter().accentColor(), enabled, hover);
        addHitbox(x, y, w, 16, enabled, action);
    }

    private int treePaneHeight(TerminalRenderContext context, MissionRenderState state, int width) {
        int filterRows = Math.max(1, (MissionFilter.values().length + Math.max(1, width / 70) - 1)
                / Math.max(1, width / 70));
        return 20 + 22 + 23 + filterRows * 18 + 22 + treeRowsHeight(context, state);
    }

    private int treeRowsHeight(TerminalRenderContext context, MissionRenderState state) {
        int height = 0;
        if (viewMode == MissionViewMode.GUIDED) {
            height += 20 + Math.max(1, Math.min(3, laneMain(state).size())) * MISSION_ROW_HEIGHT + 2;
            height += 20 + Math.max(0, Math.min(3, laneReady(state).size())) * MISSION_ROW_HEIGHT + 2;
            height += 20 + Math.max(0, Math.min(3, laneOptional(state).size())) * MISSION_ROW_HEIGHT + 16;
            height += 20;
        }
        for (PhaseGroup phase : phases(state.visibleRecords())) {
            height += PHASE_ROW_HEIGHT;
            if (isPhaseExpanded(phase)) {
                height += phase.records().size() * MISSION_ROW_HEIGHT;
            }
        }
        return height + 6;
    }

    private int detailBodyHeight(TerminalRenderContext context, MissionRecord record, int width) {
        if (record == null) {
            return 60;
        }
        int height = briefingHeaderHeight(record) + 8;
        height += nextStepCalloutHeight(context, record, width);
        height += 20;
        if (record.definition().requirements().isEmpty()) {
            height += 14;
        } else {
            for (TerminalMissionRequirement requirement : record.definition().requirements()) {
                height += requirementHeight(context, requirement, width);
            }
        }
        String guide = record.definition().fieldGuide().isBlank()
                ? record.definition().briefing()
                : record.definition().fieldGuide();
        height += 20 + TerminalUi.wrappedHeight(context, guide, width - 12) + 18;
        height += 20 + TerminalUi.itemGridHeight((int) record.definition().rewards().stream()
                .filter(reward -> !reward.stack().isEmpty()).count(), width - 12) + 12;
        height += (int) record.definition().rewards().stream().filter(reward -> reward.stack().isEmpty()).count() * 14;
        if (!record.presentation().relatedIntelKey().isBlank()) {
            height += 20 + TerminalUi.wrappedHeight(context, record.presentation().relatedIntelKey(), width - 12) + 8;
        }
        if (!record.definition().prerequisites().isEmpty()) {
            height += 20;
            for (String prerequisite : record.definition().prerequisites()) {
                height += TerminalUi.wrappedHeight(context, "- " + prerequisite, width - 12) + 2;
            }
        }
        return height + 8;
    }

    private int briefingHeaderHeight(MissionRecord record) {
        return viewMode == MissionViewMode.VISUAL_RPG && TerminalClientOptions.useVisualAssets() ? 108 : 92;
    }

    private int nextStepCalloutHeight(TerminalRenderContext context, MissionRecord record, int width) {
        String hint = record.presentation().nextStep().isBlank()
                ? "This record is visible for planning."
                : record.presentation().nextStep();
        return Math.max(40, 27 + TerminalUi.wrappedHeight(context, hint, Math.max(40, width - 24))) + 5;
    }

    private int requirementHeight(TerminalRenderContext context, TerminalMissionRequirement requirement, int width) {
        String detail = requirement.detail();
        if (requirement.need() > 0 && detail.isBlank()) {
            detail = requirement.have() + "/" + requirement.need();
        }
        int chipW = Math.max(74, Math.min(92, width / 4));
        int detailH = TerminalUi.wrappedHeight(context, detail, Math.max(40, width - chipW - 54));
        return Math.max(38, 24 + detailH) + 5;
    }

    private List<MissionRecord> laneMain(MissionRenderState state) {
        List<MissionRecord> rows = state.visibleRecords().stream()
                .filter(record -> record.role() == TerminalMissionRole.MAIN)
                .filter(record -> !isDone(record.snapshot().status()))
                .limit(3)
                .toList();
        return rows.isEmpty() && state.focusRecord() != null ? List.of(state.focusRecord()) : rows;
    }

    private List<MissionRecord> laneReady(MissionRenderState state) {
        return state.visibleRecords().stream()
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .limit(3)
                .toList();
    }

    private List<MissionRecord> laneOptional(MissionRenderState state) {
        return state.visibleRecords().stream()
                .filter(record -> record.role() == TerminalMissionRole.OPTIONAL)
                .limit(3)
                .toList();
    }

    private void normalizeSelection(MissionRenderState state) {
        if (state.visibleRecords().isEmpty()) {
            selectedMissionId = null;
            return;
        }
        if (selectedMissionId != null && state.visibleRecords().stream().anyMatch(record -> record.id().equals(selectedMissionId))) {
            return;
        }
        MissionRecord focus = state.focusRecord() == null
                || state.visibleRecords().stream().noneMatch(record -> record.id().equals(state.focusRecord().id()))
                        ? state.visibleRecords().get(0)
                        : state.focusRecord();
        selectedMissionId = focus.id();
    }

    private boolean selectRelative(MissionRenderState state, int offset) {
        List<MissionRecord> rows = navigationRecords(state);
        if (rows.isEmpty()) {
            return false;
        }
        int index = 0;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id().equals(selectedMissionId)) {
                index = i;
                break;
            }
        }
        selectMission(rows.get(Math.floorMod(index + offset, rows.size())).id(), true);
        return true;
    }

    private boolean activateSelectedAction(TerminalRenderContext context, MissionRenderState state) {
        MissionRecord selected = selectedRecord(state);
        if (selected == null) {
            return false;
        }
        for (TerminalMissionAction action : selected.snapshot().actions()) {
            if (action.enabled()) {
                context.sendAction(tabId, TerminalMissionActions.MISSION_ACTION,
                        TerminalMissionActions.payload(chapter().id(), selected.definition().id(), action.id()));
                return true;
            }
        }
        return false;
    }

    private List<MissionRecord> navigationRecords(MissionRenderState state) {
        List<MissionRecord> rows = new ArrayList<>();
        for (PhaseGroup phase : phases(state.visibleRecords())) {
            if (isPhaseExpanded(phase)) {
                rows.addAll(phase.records());
            }
        }
        return rows.isEmpty() ? state.visibleRecords() : rows;
    }

    private MissionRecord selectedRecord(MissionRenderState state) {
        if (selectedMissionId == null) {
            return null;
        }
        return state.allRecords().stream()
                .filter(record -> state.visibleRecords().stream().anyMatch(visible -> visible.id().equals(record.id())))
                .filter(record -> record.id().equals(selectedMissionId))
                .findFirst()
                .orElse(null);
    }

    private MissionRecord focusRecord(List<MissionRecord> records) {
        return records.stream()
                .filter(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE)
                .findFirst()
                .or(() -> records.stream()
                        .filter(record -> record.role() == TerminalMissionRole.MAIN)
                        .filter(record -> record.snapshot().status() == TerminalMissionStatus.UNLOCKED)
                        .findFirst())
                .or(() -> records.stream()
                        .filter(record -> record.role() == TerminalMissionRole.MAIN)
                        .filter(record -> !isDone(record.snapshot().status()))
                        .findFirst())
                .or(() -> records.stream()
                        .filter(record -> !isDone(record.snapshot().status()))
                        .findFirst())
                .orElse(records.isEmpty() ? null : records.get(0));
    }

    private void selectMission(Identifier missionId, boolean focusTree) {
        if (missionId == null) {
            return;
        }
        if (!missionId.equals(selectedMissionId)) {
            selectedMissionId = missionId;
            detailScroll = 0;
            lastDetailMissionId = missionId;
        }
        pendingTreeFocus = pendingTreeFocus || focusTree;
    }

    private void syncDetailScrollWithSelection() {
        if (selectedMissionId == null) {
            lastDetailMissionId = null;
            detailScroll = 0;
            return;
        }
        if (!selectedMissionId.equals(lastDetailMissionId)) {
            detailScroll = 0;
            lastDetailMissionId = selectedMissionId;
        }
    }

    private void focusTreeOnSelection(MissionRenderState state) {
        if (!pendingTreeFocus || selectedMissionId == null || state.visibleRecords().isEmpty()) {
            return;
        }
        int rowY = selectedRowOffset(state);
        if (rowY < 0) {
            pendingTreeFocus = false;
            return;
        }
        if (rowY < treeScroll) {
            treeScroll = Math.max(0, rowY - TREE_FOCUS_EXTRA);
        } else if (rowY + MISSION_ROW_HEIGHT > treeScroll + lastTreeH) {
            treeScroll = Math.max(0, rowY + MISSION_ROW_HEIGHT - lastTreeH + TREE_FOCUS_EXTRA);
        }
        pendingTreeFocus = false;
    }

    private int selectedRowOffset(MissionRenderState state) {
        int cy = 0;
        if (viewMode == MissionViewMode.GUIDED) {
            cy += treeRowsHeightForGuidedLanes(state) + 20;
        }
        for (PhaseGroup phase : phases(state.visibleRecords())) {
            cy += PHASE_ROW_HEIGHT;
            if (!isPhaseExpanded(phase)) {
                continue;
            }
            for (MissionRecord record : phase.records()) {
                if (record.id().equals(selectedMissionId)) {
                    return cy;
                }
                cy += MISSION_ROW_HEIGHT;
            }
        }
        return -1;
    }

    private int treeRowsHeightForGuidedLanes(MissionRenderState state) {
        return 20 + Math.max(1, laneMain(state).size()) * MISSION_ROW_HEIGHT + 2
                + 20 + laneReady(state).size() * MISSION_ROW_HEIGHT + 2
                + 20 + laneOptional(state).size() * MISSION_ROW_HEIGHT + 16;
    }

    private boolean isPhaseExpanded(PhaseGroup phase) {
        if (allExpanded) {
            return true;
        }
        if (collapsedPhases.contains(phase.id())) {
            return false;
        }
        if (expandedPhases.contains(phase.id())) {
            return true;
        }
        if (filterMode != MissionFilter.ALL) {
            return true;
        }
        if (phase.records().stream().anyMatch(record -> record.id().equals(selectedMissionId))) {
            return true;
        }
        return phase.records().stream().anyMatch(record -> record.snapshot().status() == TerminalMissionStatus.CLAIMABLE);
    }

    private void togglePhase(PhaseGroup phase) {
        if (isPhaseExpanded(phase)) {
            expandedPhases.remove(phase.id());
            collapsedPhases.add(phase.id());
        } else {
            collapsedPhases.remove(phase.id());
            expandedPhases.add(phase.id());
        }
    }

    private List<PhaseGroup> phases(List<MissionRecord> records) {
        Map<String, List<MissionRecord>> grouped = new LinkedHashMap<>();
        for (MissionRecord record : records) {
            grouped.computeIfAbsent(record.definition().phaseId(), ignored -> new ArrayList<>()).add(record);
        }
        List<PhaseGroup> groups = new ArrayList<>();
        for (List<MissionRecord> group : grouped.values()) {
            MissionRecord first = group.get(0);
            groups.add(new PhaseGroup(first.definition().phaseId(), first.definition().phaseTitle(),
                    first.definition().phaseOrder(), group));
        }
        groups.sort(Comparator.comparingInt(PhaseGroup::order).thenComparing(PhaseGroup::id));
        return groups;
    }

    private List<MissionRecord> recordsForPhase(List<MissionRecord> records, String phaseId) {
        return records.stream()
                .filter(record -> record.definition().phaseId().equals(phaseId))
                .toList();
    }

    private int completedCount(List<MissionRecord> records) {
        int count = 0;
        for (MissionRecord record : records) {
            if (isDone(record.snapshot().status())) {
                count++;
            }
        }
        return count;
    }

    private static int statusColor(TerminalMissionStatus status) {
        return switch (status) {
            case COMPLETED, CLAIMED, CLAIMABLE -> TerminalUi.GREEN;
            case UNLOCKED -> TerminalUi.AMBER;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static int missionTitleColor(TerminalMissionStatus status, boolean selected, int statusColor) {
        if (selected) {
            return TerminalUi.TEXT;
        }
        return switch (status) {
            case CLAIMABLE, UNLOCKED -> statusColor;
            case COMPLETED, CLAIMED -> TerminalUi.GREEN;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static String compactStatusLabel(TerminalMissionSnapshot snapshot) {
        return switch (snapshot.status()) {
            case UNLOCKED -> "ACTIVE";
            case CLAIMABLE -> "READY";
            case COMPLETED, CLAIMED -> "DONE";
            case VIEW_ONLY -> "VIEW";
            case LOCKED -> "LOCKED";
        };
    }

    private static int actionHintColor(TerminalMissionSnapshot snapshot) {
        return switch (snapshot.status()) {
            case COMPLETED, CLAIMABLE, CLAIMED -> TerminalUi.GREEN;
            case UNLOCKED -> TerminalUi.AMBER;
            case LOCKED, VIEW_ONLY -> TerminalUi.MUTED;
        };
    }

    private static String commandSummary(TerminalMissionSnapshot snapshot, TerminalMissionPresentation presentation) {
        return switch (snapshot.status()) {
            case CLAIMABLE -> "Reward cache is ready. Claim it here before moving on.";
            case COMPLETED, CLAIMED -> "Protocol complete. Any pending cache remains available here.";
            case UNLOCKED -> "Command unlocks after ECHO confirms the route.";
            case VIEW_ONLY -> "View-only record. Actions are disabled for this path.";
            case LOCKED -> presentation.nextStep();
        };
    }

    private static String tagLine(TerminalMissionDefinition mission,
            TerminalMissionPresentation presentation, TerminalMissionRole role) {
        List<String> tags = new ArrayList<>();
        if (role == TerminalMissionRole.OPTIONAL) {
            tags.add("Optional");
        } else if (role == TerminalMissionRole.REFERENCE) {
            tags.add("Reference");
        }
        if (!presentation.routeHint().isBlank()) {
            tags.add(presentation.routeHint());
        }
        tags.addAll(presentation.tags());
        if (tags.isEmpty()) {
            tags.add(emptyFallback(mission.category(), "Mission"));
            tags.add(emptyFallback(mission.difficulty(), "Standard"));
        }
        return String.join(" / ", tags);
    }

    private static String roleLabel(TerminalMissionRole role) {
        return switch (role) {
            case MAIN -> "Main progression";
            case OPTIONAL -> "Optional / nonblocking";
            case REFERENCE -> "Reference";
        };
    }

    private static String roleChipLabel(TerminalMissionRole role) {
        return switch (role) {
            case MAIN -> "MAIN";
            case OPTIONAL -> "OPTIONAL";
            case REFERENCE -> "REFERENCE";
        };
    }

    private static String firstDisabledReason(List<TerminalMissionAction> actions) {
        for (TerminalMissionAction action : actions) {
            if (!action.enabled() && !action.disabledReason().isBlank()) {
                return action.label() + ": " + action.disabledReason();
            }
        }
        return "";
    }

    private static Identifier actionIcon(TerminalMissionAction action) {
        String value = ((action == null ? "" : action.id()) + " " + (action == null ? "" : action.label()))
                .toLowerCase(Locale.ROOT);
        if (value.contains("claim") || value.contains("reward")) {
            return TerminalVisualAssets.ICON_ACTION_CLAIM;
        }
        if (value.contains("turn") || value.contains("submit") || value.contains("finish")) {
            return TerminalVisualAssets.ICON_ACTION_TURN_IN;
        }
        return TerminalVisualAssets.ICON_ACTION_VIEW;
    }

    private static String intelLabel(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String cleaned = key;
        int colon = cleaned.indexOf(':');
        if (colon >= 0 && colon + 1 < cleaned.length()) {
            cleaned = cleaned.substring(colon + 1);
        }
        if (cleaned.startsWith("ashfall_")) {
            cleaned = cleaned.substring("ashfall_".length());
        }
        String[] words = cleaned.replace('_', ' ').split(" ");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                label.append(word.substring(1));
            }
        }
        return label.length() == 0 ? key : label.toString();
    }

    private static boolean isDone(TerminalMissionStatus status) {
        return status == TerminalMissionStatus.COMPLETED || status == TerminalMissionStatus.CLAIMED;
    }

    private static boolean visible(int y, int h, int viewportY, int viewportH) {
        return y + h >= viewportY && y <= viewportY + viewportH;
    }

    private static String emptyFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String searchLabel() {
        if (searchQuery.isBlank()) {
            return searchFocused ? "TYPE TO SEARCH" : "SEARCH MISSIONS";
        }
        return "SEARCH: " + searchQuery;
    }

    private String normalizedSearchQuery() {
        return searchQuery == null ? "" : searchQuery.strip().toLowerCase(Locale.ROOT);
    }

    private static boolean matchesSearch(MissionRecord record, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String haystack = String.join(" ",
                record.definition().id().toString(),
                record.definition().title(),
                record.definition().phaseTitle(),
                record.definition().category(),
                record.definition().difficulty(),
                record.presentation().shortTitle(),
                record.presentation().objectiveSummary(),
                record.presentation().nextStep(),
                record.presentation().tags().toString(),
                record.snapshot().statusLabel()).toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private void addHitbox(int x, int y, int w, int h, boolean enabled, Runnable action) {
        hitboxes.add(new Hitbox(x, y, w, h, enabled, action));
    }

    private void invalidateStateCache() {
        cachedState = null;
        cachedStateFrame = -1L;
    }

    private record MissionRenderState(
            List<MissionRecord> allRecords,
            List<MissionRecord> visibleRecords,
            MissionRecord focusRecord,
            int completedCount) {
    }

    private record MissionRecord(
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot,
            TerminalMissionPresentation presentation,
            TerminalMissionVisuals visuals,
            TerminalMissionRole role) {
        Identifier id() {
            return definition.id();
        }
    }

    private record PhaseGroup(String id, String title, int order, List<MissionRecord> records) {
    }

    private record Hitbox(int x, int y, int w, int h, boolean enabled, Runnable action) {
    }

    private enum MissionViewMode {
        GUIDED("GUIDED"),
        VISUAL_RPG("VISUAL"),
        MINIMAL_FUTURE("MINIMAL");

        private final String label;

        MissionViewMode(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        static MissionViewMode fromClientDefault() {
            return switch (TerminalClientOptions.missionView) {
                case VISUAL_QUEST_HUB -> VISUAL_RPG;
                case GUIDED -> GUIDED;
                case VISUAL_RPG -> VISUAL_RPG;
                case MINIMAL -> MINIMAL_FUTURE;
            };
        }
    }

    private enum MissionFilter {
        ALL("ALL"),
        ACTIVE("ACTIVE"),
        READY("READY"),
        OPTIONAL("OPTIONAL"),
        LOCKED("LOCKED"),
        COMPLETED("DONE");

        private final String label;

        MissionFilter(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        boolean matches(MissionRecord record) {
            TerminalMissionStatus status = record.snapshot().status();
            return switch (this) {
                case ALL -> true;
                case ACTIVE -> status == TerminalMissionStatus.UNLOCKED;
                case READY -> status == TerminalMissionStatus.CLAIMABLE;
                case OPTIONAL -> record.role() == TerminalMissionRole.OPTIONAL;
                case LOCKED -> status == TerminalMissionStatus.LOCKED || status == TerminalMissionStatus.VIEW_ONLY;
                case COMPLETED -> status == TerminalMissionStatus.COMPLETED || status == TerminalMissionStatus.CLAIMED;
            };
        }

        static MissionFilter next(MissionFilter current) {
            MissionFilter[] values = values();
            return values[Math.floorMod((current == null ? ALL.ordinal() : current.ordinal()) + 1, values.length)];
        }

        static MissionFilter previous(MissionFilter current) {
            MissionFilter[] values = values();
            return values[Math.floorMod((current == null ? ALL.ordinal() : current.ordinal()) - 1, values.length)];
        }
    }
}
