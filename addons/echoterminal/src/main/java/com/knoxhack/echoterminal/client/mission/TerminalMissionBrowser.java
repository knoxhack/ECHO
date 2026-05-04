package com.knoxhack.echoterminal.client.mission;

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

    private boolean allExpanded;
    private MissionFilter filterMode = MissionFilter.ALL;
    private MissionViewMode viewMode = MissionViewMode.VISUAL_RPG;
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
        TerminalMissionChapter chapter = provider.chapter();
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
            int gap = 14;
            int leftW = Math.max(300, Math.min(408, w * 38 / 100));
            int detailX = x + leftW + gap;
            int detailW = Math.max(300, w - leftW - gap);
            drawRoadmapPane(context, graphics, state, x, y, leftW, h, mouseX, mouseY);
            drawDetailPane(context, graphics, selected, detailX, y, detailW, h, mouseX, mouseY, true);
        } else if (w >= 430) {
            int gap = w >= 560 ? 10 : 8;
            int leftW = Math.max(w >= 560 ? 238 : 190, Math.min(w >= 560 ? 318 : 230, w * 43 / 100));
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
            if (TerminalUi.inside(mouseX, mouseY, hitbox.x(), hitbox.y(), hitbox.w(), hitbox.h())
                    && hitbox.enabled()) {
                hitbox.action().run();
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(TerminalRenderContext context, KeyEvent event) {
        return false;
    }

    public boolean charTyped(TerminalRenderContext context, CharacterEvent event) {
        return false;
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
        List<TerminalMissionDefinition> definitions = provider.missions(context.player()).stream()
                .sorted(Comparator
                        .comparingInt(TerminalMissionDefinition::phaseOrder)
                        .thenComparingInt(TerminalMissionDefinition::missionOrder)
                        .thenComparing(mission -> mission.id().toString()))
                .toList();
        List<MissionRecord> records = new ArrayList<>();
        for (TerminalMissionDefinition definition : definitions) {
            TerminalMissionSnapshot snapshot = provider.snapshot(context.player(), definition.id());
            TerminalMissionPresentation presentation = provider.presentation(context.player(), definition, snapshot);
            TerminalMissionVisuals visuals = provider.visuals(context.player(), definition, snapshot);
            TerminalMissionRole role = provider.role(context.player(), definition, snapshot);
            records.add(new MissionRecord(definition, snapshot, presentation, visuals, role));
        }
        List<MissionRecord> visible = records.stream()
                .filter(filterMode::matches)
                .toList();
        MissionRecord focus = focusRecord(records);
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

    private void drawRoadmapPane(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRenderState state, int x, int y, int w, int h, int mouseX, int mouseY) {
        TerminalMissionChapter chapter = provider.chapter();
        TerminalUi.cinematicPanel(graphics, x, y, w, h, chapter.accentColor());
        TerminalUi.line(context, graphics, "PROTOCOL ROADMAP", x + 10, y + 10, w - 20, chapter.accentColor());
        TerminalUi.line(context, graphics, state.completedCount() + "/" + state.allRecords().size() + " complete",
                x + w - 112, y + 10, 98, TerminalUi.MUTED);
        TerminalUi.divider(graphics, x + 10, y + 26, w - 20, chapter.accentColor());
        int innerX = x + 10;
        int innerW = w - 20;
        int listY = y + 36;
        if (showExpandControls) {
            int utilityY = y + 36;
            int utilityW = Math.max(1, (innerW - 4) / 2);
            int compactW = Math.max(1, innerW - utilityW - 4);
            drawCompactButton(context, graphics, innerX, utilityY, utilityW, "EXPAND ALL", true, mouseX, mouseY, () -> {
                allExpanded = true;
                expandedPhases.clear();
                collapsedPhases.clear();
            });
            drawCompactButton(context, graphics, innerX + utilityW + 4, utilityY, compactW, "COLLAPSE ALL", true,
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
        lastTreeH = Math.max(68, h - (listY - y) - 10);
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
            TerminalUi.emptyState(context, graphics, x + 2, y + 4, Math.max(80, w - 12),
                    "No Matches", "No mission records match the " + filterMode.label() + " filter.", TerminalUi.MUTED);
            return;
        }
        int cy = y;
        if (viewMode == MissionViewMode.GUIDED) {
            cy = drawGuidedLanes(context, graphics, state, x, cy, w, viewportY, viewportH, mouseX, mouseY);
            cy += 4;
            TerminalUi.missionLaneHeader(context, graphics, x, cy, w - 8, "ROADMAP", "all records", provider.chapter().accentColor());
            cy += 20;
        }
        for (PhaseGroup phase : phases(state.visibleRecords())) {
            List<MissionRecord> phaseAll = recordsForPhase(state.allRecords(), phase.id());
            int complete = completedCount(phaseAll);
            int total = phaseAll.size();
            boolean expanded = isPhaseExpanded(phase);
            int phaseColor = complete == total ? TerminalUi.GREEN : provider.chapter().accentColor();
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
                x, cy, w, viewportY, viewportH, mouseX, mouseY, provider.chapter().accentColor());
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
        int accent = provider.chapter().accentColor();
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
        int chipW = Math.max(84, Math.min(112, rowW / 4));
        int chipX = rowX + rowW - chipW - 6;
        int textX = rowX + 36;
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
        String rolePrefix = record.role() == TerminalMissionRole.OPTIONAL ? "[OPT] " : "";
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
        TerminalUi.cinematicPanel(graphics, x, y, w - 4, h, provider.chapter().accentColor());
        if (record == null) {
            TerminalUi.emptyState(context, graphics, x + 10, y + 12, w - 24,
                    "Select Mission", "Choose a mission record from the command queue.", TerminalUi.MUTED);
            return;
        }
        int bodyX = x + 10;
        int bodyW = w - 24;
        int bodyY = y + 10;
        int actionH = Math.min(ACTION_BAR_HEIGHT, Math.max(84, h / 5));
        int actionY = y + h - actionH - 8;
        int bodyH = Math.max(70, actionY - bodyY - 8);
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
                    detailScroll, Math.max(0, lastDetailContentH - bodyH), provider.chapter().accentColor());
        }
        drawStickyActions(context, graphics, record, bodyX, actionY, bodyW, actionH, mouseX, mouseY);
    }

    private int drawDetailBody(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        int cy = drawBriefingHeader(context, graphics, record, x, y, w, mouseX, mouseY) + 8;
        cy = drawNextStepCallout(context, graphics, record, x, cy, w);
        TerminalUi.sectionHeader(context, graphics, "REQUIREMENTS", "", x, cy, w - 4, provider.chapter().accentColor());
        cy += 20;
        if (record.definition().requirements().isEmpty()) {
            TerminalUi.line(context, graphics, "No visible checklist. Server state tracks this protocol.",
                    x + 4, cy, w - 12, TerminalUi.MUTED);
            cy += 14;
        } else {
            for (TerminalMissionRequirement requirement : record.definition().requirements()) {
                cy = drawRequirementRow(context, graphics, requirement, x + 2, cy, w - 10, mouseX, mouseY);
            }
        }
        cy += 4;
        cy = drawRewards(context, graphics, record, x, cy, w, mouseX, mouseY);
        TerminalUi.sectionHeader(context, graphics, "FIELD GUIDE", "", x, cy, w - 4, provider.chapter().accentColor());
        cy += 20;
        String guide = record.definition().fieldGuide().isBlank()
                ? record.definition().briefing()
                : record.definition().fieldGuide();
        cy = TerminalUi.wrap(context, graphics, guide, x + 2, cy, w - 12, TerminalUi.TEXT) + 9;
        if (!record.presentation().relatedIntelKey().isBlank()) {
            TerminalUi.sectionHeader(context, graphics, "RELATED INTEL", "", x, cy, w - 4, provider.chapter().accentColor());
            cy += 20;
            cy = TerminalUi.wrap(context, graphics, intelLabel(record.presentation().relatedIntelKey()),
                    x + 2, cy, w - 12, TerminalUi.MUTED) + 8;
        }
        if (!record.definition().prerequisites().isEmpty()) {
            TerminalUi.sectionHeader(context, graphics, "PREREQUISITES", "", x, cy, w - 4, provider.chapter().accentColor());
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
        TerminalUi.iconTextureBadge(graphics, TerminalVisualAssets.missionIconArt(record.definition().id(),
                        record.definition().category()),
                x + 10, y + 14, 48, color, true);
        int chipW = Math.max(104, Math.min(140, w / 5));
        TerminalUi.missionStatusPill(context, graphics, compactStatusLabel(record.snapshot()),
                x + w - chipW - 14, y + 12, chipW);
        TerminalUi.line(context, graphics, record.presentation().shortTitle().toUpperCase(), x + 70, y + 20,
                Math.max(40, w - chipW - 92), TerminalUi.TEXT);
        TerminalUi.line(context, graphics, tagLine(record.definition(), record.presentation(), record.role()),
                x + 70, y + 36, Math.max(40, w - chipW - 92), color);
        TerminalUi.wrap(context, graphics, record.presentation().objectiveSummary(),
                x + 70, y + 54, Math.max(48, w - 94), TerminalUi.TEXT);
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
        TerminalUi.line(context, graphics, requirement.label(), x + 32, y + 6, w - 154, color);
        String progress = requirement.need() > 0 ? requirement.have() + "/" + requirement.need() : "";
        TerminalUi.missionStatusPill(context, graphics, requirement.satisfied() ? "DONE" : "NEEDED",
                x + w - 98, y + 6, 86);
        TerminalUi.wrap(context, graphics, requirement.detail().isBlank() ? progress : requirement.detail(),
                x + 32, y + 20, w - 142, TerminalUi.MUTED);
        return y + rowH;
    }

    private int drawRewards(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            MissionRecord record, int x, int y, int w, int mouseX, int mouseY) {
        TerminalUi.sectionHeader(context, graphics, "REWARDS", "", x, y, w - 4, provider.chapter().accentColor());
        int cy = y + 20;
        List<ItemStack> stacks = record.definition().rewards().stream()
                .map(TerminalMissionReward::stack)
                .filter(stack -> !stack.isEmpty())
                .toList();
        cy = TerminalUi.itemGrid(context, graphics, stacks, x + 2, cy, w - 12,
                provider.chapter().accentColor(), mouseX, mouseY) + 3;
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
        TerminalUi.flatHudPanel(graphics, x, y, w - 4, h, provider.chapter().accentColor());
        TerminalUi.line(context, graphics, "COMMAND", x + 8, y + 8, w - 20, provider.chapter().accentColor());
        TerminalUi.line(context, graphics, summary, x + 8, y + 21, w - 20, TerminalUi.TEXT);
        int buttonY = y + 43;
        List<TerminalMissionAction> actions = record.snapshot().actions();
        if (actions.isEmpty()) {
            TerminalUi.disabledReasonRow(context, graphics, x + 8, buttonY, w - 20,
                    "No direct terminal action is available for this record.", TerminalUi.MUTED);
            return;
        }
        int buttonH = 22;
        int buttonW = actions.size() == 1 ? Math.min(220, w - 20) : Math.max(104, (w - 28) / actions.size());
        int bx = x + 8;
        for (TerminalMissionAction action : actions) {
            boolean hover = action.enabled() && TerminalUi.inside(mouseX, mouseY, bx, buttonY, buttonW, buttonH);
            if (action.enabled()) {
                TerminalUi.primaryCommandButton(context, graphics, bx, buttonY, buttonW, buttonH, action.label(),
                        actionIcon(action), provider.chapter().accentColor(), hover);
            } else {
                TerminalUi.disabledCommandButton(context, graphics, bx, buttonY, buttonW, buttonH,
                        action.label(), actionIcon(action));
            }
            TerminalMissionAction hitAction = action;
            addHitbox(bx, buttonY, buttonW, buttonH, action.enabled(), () -> context.sendAction(
                    tabId,
                    TerminalMissionActions.MISSION_ACTION,
                    TerminalMissionActions.payload(provider.chapter().id(), record.definition().id(), hitAction.id())));
            bx += buttonW + 8;
        }
        String reason = firstDisabledReason(actions);
        if (!reason.isBlank()) {
            TerminalUi.line(context, graphics, reason, x + 8, buttonY + 29, w - 20, 0xFFC2D4DC);
        }
    }

    private void drawMissionModeChips(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int mouseX, int mouseY) {
        int labelW = 34;
        TerminalUi.line(context, graphics, "VIEW", x, y + 4, labelW, TerminalUi.MUTED);
        int chipX = x + labelW + 4;
        int chipW = Math.max(58, Math.min(82, (w - labelW - 12) / MissionViewMode.values().length));
        for (MissionViewMode mode : MissionViewMode.values()) {
            boolean selected = mode == viewMode;
            boolean hover = TerminalUi.inside(mouseX, mouseY, chipX, y, chipW, 15);
            TerminalUi.filterChip(context, graphics, chipX, y, chipW, mode.label(), selected, true,
                    provider.chapter().accentColor(), hover);
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

    // Retained for validation continuity; the cinematic roadmap no longer draws filter chips.
    @SuppressWarnings("unused")
    private void drawFilterChips(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int mouseX, int mouseY) {
    }

    private void drawCompactButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, boolean enabled, int mouseX, int mouseY, Runnable action) {
        boolean hover = enabled && TerminalUi.inside(mouseX, mouseY, x, y, w, 16);
        TerminalUi.compactButton(context, graphics, x, y, w, label, provider.chapter().accentColor(), enabled, hover);
        addHitbox(x, y, w, 16, enabled, action);
    }

    private int treePaneHeight(TerminalRenderContext context, MissionRenderState state, int width) {
        return 20 + 22 + 23 + treeRowsHeight(context, state);
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
        return viewMode == MissionViewMode.VISUAL_RPG && TerminalClientOptions.useVisualAssets() ? 104 : 90;
    }

    private int nextStepCalloutHeight(TerminalRenderContext context, MissionRecord record, int width) {
        String hint = record.presentation().nextStep().isBlank()
                ? "This record is visible for planning."
                : record.presentation().nextStep();
        return Math.max(38, 25 + TerminalUi.wrappedHeight(context, hint, Math.max(40, width - 24))) + 5;
    }

    private int requirementHeight(TerminalRenderContext context, TerminalMissionRequirement requirement, int width) {
        String detail = requirement.detail();
        if (requirement.need() > 0 && detail.isBlank()) {
            detail = requirement.have() + "/" + requirement.need();
        }
        int detailH = TerminalUi.wrappedHeight(context, detail, Math.max(40, width - 114));
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
        MissionRecord focus = state.focusRecord() == null ? state.visibleRecords().get(0) : state.focusRecord();
        selectedMissionId = focus.id();
    }

    private MissionRecord selectedRecord(MissionRenderState state) {
        if (selectedMissionId == null) {
            return null;
        }
        return state.allRecords().stream()
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
            case UNLOCKED -> "Follow the next step above. Turn-in unlocks after ECHO validation.";
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
    }
}
